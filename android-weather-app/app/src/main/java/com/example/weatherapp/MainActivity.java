package com.example.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.weatherapp.adapter.ForecastAdapter;
import com.example.weatherapp.location.LocationHelper;
import com.example.weatherapp.model.City;
import com.example.weatherapp.model.Forecast;
import com.example.weatherapp.model.Weather;
import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.network.ApiCallback;
import com.example.weatherapp.network.RetrofitClient;
import com.example.weatherapp.network.WeatherApiService;
import com.example.weatherapp.storage.CityRepository;
import com.example.weatherapp.util.WeatherIconUtil;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 主页 Activity。
 * 展示当前城市天气信息和未来天气预报列表。
 * 支持左右滑动切换城市、下拉刷新、定位请求。
 */
public class MainActivity extends AppCompatActivity {

    // UI 组件
    private TextView tvCityName;
    private TextView tvTemperature;
    private TextView tvCondition;
    private TextView tvHumidity;
    private TextView tvWindSpeed;
    private TextView tvUpdateTime;
    private ImageView ivWeatherIcon;
    private ProgressBar progressBar;
    private LinearLayout layoutWeatherInfo;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvForecast;
    private TextView tvEmptyState;
    private TextView tvPageIndicator;

    // 按钮
    private ImageButton btnAddCity;
    private ImageButton btnManageCity;
    private ImageButton btnLocation;

    // 数据
    private List<City> cityList = new ArrayList<>();
    private int currentCityIndex = 0;
    private ForecastAdapter forecastAdapter;
    private CityRepository cityRepository;
    private WeatherApiService apiService;
    private LocationHelper locationHelper;
    private GestureDetector gestureDetector;

    // API Key - 存储在 strings.xml 中，不硬编码
    private String apiKey;
    private static final String UNITS = "metric";
    private static final int FORECAST_CNT = 40; // 5天 / 3小时 = 40条

    // 定位权限请求码
    private static final int LOCATION_PERMISSION_CODE = 1001;

    // 城市管理页返回结果
    private final ActivityResultLauncher<Intent> cityManageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // 城市列表有变化，重新加载
                    loadCitiesFromStorage();
                    currentCityIndex = 0;
                    if (!cityList.isEmpty()) {
                        loadWeatherData(cityList.get(currentCityIndex));
                    } else {
                        showEmptyState();
                    }
                }
            });

    // 城市搜索页返回结果
    private final ActivityResultLauncher<Intent> citySearchLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadCitiesFromStorage();
                    currentCityIndex = 0;
                    if (!cityList.isEmpty()) {
                        loadWeatherData(cityList.get(currentCityIndex));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiKey = getString(R.string.openweather_api_key);
        cityRepository = CityRepository.getInstance(this);
        apiService = RetrofitClient.getApiService();
        locationHelper = new LocationHelper(this);

        initViews();
        setupForecastRecyclerView();
        setupGestureDetector();
        setupListeners();
        loadCitiesFromStorage();

        if (cityList.isEmpty()) {
            showEmptyState();
            Toast.makeText(this, "请添加城市或使用定位", Toast.LENGTH_LONG).show();
        } else {
            currentCityIndex = 0;
            loadWeatherData(cityList.get(currentCityIndex));
        }
    }

    /** 初始化视图 */
    private void initViews() {
        tvCityName = findViewById(R.id.tv_city_name);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvCondition = findViewById(R.id.tv_condition);
        tvHumidity = findViewById(R.id.tv_humidity);
        tvWindSpeed = findViewById(R.id.tv_wind_speed);
        tvUpdateTime = findViewById(R.id.tv_update_time);
        ivWeatherIcon = findViewById(R.id.iv_weather_icon);
        progressBar = findViewById(R.id.progress_bar);
        layoutWeatherInfo = findViewById(R.id.layout_weather_info);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvForecast = findViewById(R.id.rv_forecast);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        tvPageIndicator = findViewById(R.id.tv_page_indicator);
        btnAddCity = findViewById(R.id.btn_add_city);
        btnManageCity = findViewById(R.id.btn_manage_city);
        btnLocation = findViewById(R.id.btn_location);
    }

    /** 设置天气预报RecyclerView */
    private void setupForecastRecyclerView() {
        forecastAdapter = new ForecastAdapter();
        rvForecast.setLayoutManager(new LinearLayoutManager(this));
        rvForecast.setAdapter(forecastAdapter);
    }

    /** 设置左右滑动手势检测 */
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // 向右滑 -> 上一个城市
                        switchCity(currentCityIndex - 1);
                    } else {
                        // 向左滑 -> 下一个城市
                        switchCity(currentCityIndex + 1);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    /** 设置事件监听 */
    private void setupListeners() {
        btnAddCity.setOnClickListener(v -> openCitySearch());
        btnManageCity.setOnClickListener(v -> openCityManage());
        btnLocation.setOnClickListener(v -> initLocationRequest());

        swipeRefresh.setOnRefreshListener(() -> {
            if (!cityList.isEmpty() && currentCityIndex < cityList.size()) {
                loadWeatherData(cityList.get(currentCityIndex));
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    /** 处理触摸事件以检测滑动手势 */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    /** 从本地存储加载城市列表 */
    private void loadCitiesFromStorage() {
        cityList = cityRepository.getCities();
        updatePageIndicator();
    }

    /** 更新页面指示器 */
    private void updatePageIndicator() {
        if (cityList.isEmpty()) {
            tvPageIndicator.setText("");
        } else {
            tvPageIndicator.setText((currentCityIndex + 1) + " / " + cityList.size());
        }
    }

    /** 加载指定城市的天气数据 */
    private void loadWeatherData(City city) {
        if (city == null) {
            showEmptyState();
            return;
        }

        showLoading(true);
        loadCurrentWeather(city);
        loadForecast(city);
    }

    /** 加载当前天气 */
    private void loadCurrentWeather(City city) {
        // 先检查缓存
        String cachedJson = cityRepository.getCachedWeather(city.getCityId());
        if (cachedJson != null) {
            try {
                Weather cachedWeather = new Gson().fromJson(cachedJson, Weather.class);
                if (cachedWeather != null) {
                    updateWeatherUI(cachedWeather);
                }
            } catch (Exception ignored) {}
        }

        Call<WeatherResponse> call = apiService.getCurrentWeather(
                city.getLatitude(), city.getLongitude(), apiKey, UNITS);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call,
                                   @NonNull Response<WeatherResponse> response) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    Weather weather = response.body().toWeather();
                    weather.setCityName(city.getCityName());
                    updateWeatherUI(weather);
                    // 缓存天气数据
                    cityRepository.cacheWeather(city.getCityId(),
                            new Gson().toJson(weather));
                } else {
                    Toast.makeText(MainActivity.this,
                            "获取天气数据失败: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(MainActivity.this,
                        "网络错误: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 加载天气预报 */
    private void loadForecast(City city) {
        Call<WeatherResponse> call = apiService.getForecast(
                city.getLatitude(), city.getLongitude(), apiKey, UNITS, FORECAST_CNT);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call,
                                   @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isForecastResponse()) {
                    List<Forecast> forecasts = new ArrayList<>();
                    // 按天聚合：取每天的第一条数据作为代表
                    java.util.Map<String, Forecast> dailyMap = new java.util.LinkedHashMap<>();
                    SimpleDateFormat dayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

                    for (WeatherResponse item : response.body().getList()) {
                        Forecast f = item.toForecast();
                        String key = dayKey.format(new Date(f.getDateTime() * 1000));
                        if (!dailyMap.containsKey(key)) {
                            dailyMap.put(key, f);
                        } else {
                            // 取最高温和最低温
                            Forecast existing = dailyMap.get(key);
                            if (f.getMaxTemp() > existing.getMaxTemp()) {
                                existing.setMaxTemp(f.getMaxTemp());
                            }
                            if (f.getMinTemp() < existing.getMinTemp()) {
                                existing.setMinTemp(f.getMinTemp());
                            }
                        }
                    }
                    forecasts.addAll(dailyMap.values());
                    // 最多显示7天
                    if (forecasts.size() > 7) {
                        forecasts = forecasts.subList(0, 7);
                    }
                    forecastAdapter.updateData(forecasts);
                    rvForecast.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call,
                                  @NonNull Throwable t) {
                // 预报加载失败不影响主天气展示
            }
        });
    }

    /** 更新天气UI */
    private void updateWeatherUI(Weather weather) {
        layoutWeatherInfo.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        tvCityName.setText(weather.getCityName());
        tvTemperature.setText(weather.getFormattedTemperature());
        tvCondition.setText(WeatherIconUtil.getConditionText(weather.getCondition()));
        tvHumidity.setText("湿度: " + weather.getHumidity() + "%");
        tvWindSpeed.setText("风速: " + String.format("%.1f", weather.getWindSpeed()) + " m/s");

        // 更新时间
        if (weather.getUpdateTime() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm 更新", Locale.CHINA);
            tvUpdateTime.setText(sdf.format(new Date(weather.getUpdateTime() * 1000)));
        }

        // 天气图标
        ivWeatherIcon.setImageResource(WeatherIconUtil.getIconResource(weather.getIconCode()));

        updatePageIndicator();
    }

    /** 切换城市 */
    private void switchCity(int newIndex) {
        if (cityList.isEmpty()) return;
        if (newIndex < 0) {
            newIndex = cityList.size() - 1; // 循环到最后一个
        } else if (newIndex >= cityList.size()) {
            newIndex = 0; // 循环到第一个
        }
        currentCityIndex = newIndex;
        loadWeatherData(cityList.get(currentCityIndex));
    }

    /** 初始化定位请求 */
    private void initLocationRequest() {
        if (!locationHelper.hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_CODE);
            return;
        }

        showLoading(true);
        locationHelper.requestLocation(this, new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double lat, double lon) {
                showLoading(false);
                // 用定位坐标创建临时城市并加载天气
                City locationCity = new City(
                        "location_" + lat + "_" + lon,
                        "当前位置", lat, lon, "CN"
                );
                loadWeatherData(locationCity);
            }

            @Override
            public void onLocationError(String msg) {
                showLoading(false);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 打开城市搜索页 */
    private void openCitySearch() {
        citySearchLauncher.launch(new Intent(this, CitySearchActivity.class));
    }

    /** 打开城市管理页 */
    private void openCityManage() {
        cityManageLauncher.launch(new Intent(this, CityManageActivity.class));
    }

    /** 显示/隐藏加载进度 */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            layoutWeatherInfo.setVisibility(View.GONE);
        }
    }

    /** 显示空状态 */
    private void showEmptyState() {
        layoutWeatherInfo.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
        rvForecast.setVisibility(View.GONE);
        tvPageIndicator.setText("");
    }

    /** 处理权限请求结果 */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            boolean granted = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (granted) {
                initLocationRequest();
            } else {
                Toast.makeText(this, "需要定位权限才能获取当前位置天气",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 如果城市列表为空，重新从存储加载
        if (cityList.isEmpty()) {
            loadCitiesFromStorage();
            if (!cityList.isEmpty()) {
                currentCityIndex = 0;
                loadWeatherData(cityList.get(currentCityIndex));
            }
        }
    }
}

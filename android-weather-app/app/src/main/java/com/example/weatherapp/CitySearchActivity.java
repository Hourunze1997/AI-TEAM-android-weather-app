package com.example.weatherapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.adapter.CityListAdapter;
import com.example.weatherapp.model.City;
import com.example.weatherapp.network.RetrofitClient;
import com.example.weatherapp.network.WeatherApiService;
import com.example.weatherapp.storage.CityRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 城市搜索页 Activity。
 * 提供搜索输入框，调用API搜索城市，展示结果列表，点击添加到本地城市列表。
 */
public class CitySearchActivity extends AppCompatActivity
        implements CityListAdapter.OnCityActionListener {

    private EditText etSearch;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private TextView tvHint;
    private CityListAdapter adapter;
    private CityRepository cityRepository;
    private String apiKey;

    // geocode 接口路径不同，使用独立 Retrofit
    private WeatherApiService geoApiService;

    // 搜索防抖
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);

        apiKey = getString(R.string.openweather_api_key);
        cityRepository = CityRepository.getInstance(this);

        // geocode 接口路径不同，使用独立 Retrofit 实例
        geoApiService = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService.class);

        initViews();
        setupSearchObserver();
    }

    /** 初始化视图 */
    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        rvResults = findViewById(R.id.rv_search_results);
        progressBar = findViewById(R.id.progress_bar);
        tvNoResults = findViewById(R.id.tv_no_results);
        tvHint = findViewById(R.id.tv_hint);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        adapter = new CityListAdapter(this, false);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);
    }

    /** 设置搜索输入监听（带防抖） */
    private void setupSearchObserver() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    etSearch.removeCallbacks(searchRunnable);
                }
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    tvHint.setVisibility(View.VISIBLE);
                    tvNoResults.setVisibility(View.GONE);
                    adapter.updateCities(null);
                    return;
                }
                searchRunnable = () -> performCitySearch(query);
                etSearch.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /** 执行城市搜索 */
    private void performCitySearch(String query) {
        showLoading(true);
        tvHint.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);

        // 使用 geocode 接口搜索城市
        Call<JsonElement> call = geoApiService.geocodeCityRaw(query, 10, apiKey);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call,
                                   @NonNull Response<JsonElement> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<City> results = parseGeoResponse(response.body());
                    displaySearchResults(results);
                } else {
                    tvNoResults.setVisibility(View.VISIBLE);
                    tvNoResults.setText("搜索失败: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call,
                                  @NonNull Throwable t) {
                showLoading(false);
                tvNoResults.setVisibility(View.VISIBLE);
                tvNoResults.setText("网络错误: " + t.getMessage());
            }
        });
    }

    /** 解析 geocode 响应 */
    private List<City> parseGeoResponse(JsonElement response) {
        List<City> cities = new ArrayList<>();
        if (response == null || !response.isJsonArray()) {
            return cities;
        }
        JsonArray array = response.getAsJsonArray();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();
            String name = obj.has("name") ? obj.get("name").getAsString() : "";
            double lat = obj.has("lat") ? obj.get("lat").getAsDouble() : 0;
            double lon = obj.has("lon") ? obj.get("lon").getAsDouble() : 0;
            String country = obj.has("country") ? obj.get("country").getAsString() : "";
            String id = name + "_" + lat + "_" + lon;

            City city = new City(id, name, lat, lon, country);
            cities.add(city);
        }
        return cities;
    }

    /** 展示搜索结果 */
    private void displaySearchResults(List<City> results) {
        if (results == null || results.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            tvNoResults.setText("未找到匹配的城市");
            adapter.updateCities(null);
        } else {
            tvNoResults.setVisibility(View.GONE);
            adapter.updateCities(results);
        }
    }

    /** 添加城市到本地存储 */
    private void addCityToLocal(City city) {
        if (cityRepository.containsCity(city.getCityId())) {
            Toast.makeText(this, "该城市已添加", Toast.LENGTH_SHORT).show();
            return;
        }
        cityRepository.saveCity(city);
        Toast.makeText(this, "已添加: " + city.getCityName(), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    /** 显示/隐藏加载进度 */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvResults.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ---- CityListAdapter.OnCityActionListener ----

    @Override
    public void onCityClick(City city) {
        addCityToLocal(city);
    }

    @Override
    public void onCityDelete(City city) {
        // 搜索页不处理删除
    }
}

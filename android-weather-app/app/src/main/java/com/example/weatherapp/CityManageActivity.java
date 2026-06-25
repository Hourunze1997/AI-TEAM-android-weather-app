package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.adapter.CityListAdapter;
import com.example.weatherapp.model.City;
import com.example.weatherapp.storage.CityRepository;
import com.example.weatherapp.util.ItemMoveCallback;

import java.util.List;

/**
 * 城市管理页 Activity。
 * 展示已添加城市列表，支持删除城市，支持拖拽排序。
 */
public class CityManageActivity extends AppCompatActivity
        implements CityListAdapter.OnCityActionListener,
        ItemMoveCallback.ItemMoveListener {

    private RecyclerView rvCities;
    private CityListAdapter adapter;
    private CityRepository cityRepository;
    private TextView tvEmptyState;
    private List<City> cityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manage);

        cityRepository = CityRepository.getInstance(this);

        initViews();
        loadSavedCities();
        setupDragDrop();
    }

    /** 初始化视图 */
    private void initViews() {
        rvCities = findViewById(R.id.rv_cities);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        adapter = new CityListAdapter(this, true);
        rvCities.setLayoutManager(new LinearLayoutManager(this));
        rvCities.setAdapter(adapter);
    }

    /** 加载已保存的城市 */
    private void loadSavedCities() {
        cityList = cityRepository.getCities();
        adapter.updateCities(cityList);
        tvEmptyState.setVisibility(cityList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** 设置拖拽排序 */
    private void setupDragDrop() {
        ItemTouchHelper.Callback callback = new ItemMoveCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rvCities);
    }

    /** 删除城市 */
    private void deleteCity(City city) {
        if (city == null || city.getCityId() == null) return;
        cityRepository.deleteCity(city.getCityId());
        cityList = cityRepository.getCities();
        adapter.updateCities(cityList);
        tvEmptyState.setVisibility(cityList.isEmpty() ? View.VISIBLE : View.GONE);
        Toast.makeText(this, "已删除: " + city.getCityName(), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    /** 保存城市排序 */
    private void saveCityOrder() {
        List<City> currentOrder = adapter.getCities();
        cityRepository.updateCityOrder(currentOrder);
        cityList = currentOrder;
        setResult(RESULT_OK);
    }

    // ---- CityListAdapter.OnCityActionListener ----

    @Override
    public void onCityClick(City city) {
        // 在管理页点击城市：设为默认城市
        cityRepository.setAsDefault(city.getCityId());
        cityList = cityRepository.getCities();
        adapter.updateCities(cityList);
        Toast.makeText(this, "已设为默认: " + city.getCityName(), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    @Override
    public void onCityDelete(City city) {
        deleteCity(city);
    }

    // ---- ItemMoveCallback.ItemMoveListener ----

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // 交给 adapter 处理
    }

    @Override
    public void onItemDismiss(int position) {
        // 由 adapter 的 onCityDelete 处理
    }

    @Override
    public void onItemMoveFinished() {
        saveCityOrder();
    }
}

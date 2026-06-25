package com.example.weatherapp.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weatherapp.model.City;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 本地城市存储管理。
 * 使用 SharedPreferences + Gson 序列化存储城市列表。
 */
public class CityRepository {

    private static final String PREF_NAME = "weather_cities";
    private static final String KEY_CITIES = "city_list";
    private static final String KEY_DEFAULT_CITY_ID = "default_city_id";
    private static final String KEY_WEATHER_CACHE = "weather_cache_";

    private static CityRepository instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    private CityRepository(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /** 获取单例 */
    public static synchronized CityRepository getInstance(Context context) {
        if (instance == null) {
            instance = new CityRepository(context);
        }
        return instance;
    }

    /** 获取所有已保存的城市列表 */
    public List<City> getCities() {
        String json = prefs.getString(KEY_CITIES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<List<City>>() {}.getType();
            List<City> cities = gson.fromJson(json, type);
            return cities != null ? cities : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /** 添加城市到本地存储（去重） */
    public void saveCity(City city) {
        List<City> cities = getCities();
        // 去重：如果已存在相同ID则不重复添加
        for (City c : cities) {
            if (c.getCityId() != null && c.getCityId().equals(city.getCityId())) {
                return;
            }
        }
        // 第一个城市自动设为默认
        if (cities.isEmpty()) {
            city.setDefault(true);
        }
        cities.add(city);
        saveCitiesToPrefs(cities);
    }

    /** 根据城市ID删除城市 */
    public void deleteCity(String cityId) {
        List<City> cities = getCities();
        Iterator<City> iterator = cities.iterator();
        boolean wasDefault = false;
        while (iterator.hasNext()) {
            City c = iterator.next();
            if (c.getCityId() != null && c.getCityId().equals(cityId)) {
                wasDefault = c.isDefault();
                iterator.remove();
                break;
            }
        }
        // 如果删除的是默认城市，将第一个设为默认
        if (wasDefault && !cities.isEmpty()) {
            cities.get(0).setDefault(true);
        }
        saveCitiesToPrefs(cities);
        // 清理该城市天气缓存
        prefs.edit().remove(KEY_WEATHER_CACHE + cityId).apply();
    }

    /** 更新城市列表排序 */
    public void updateCityOrder(List<City> orderedCities) {
        saveCitiesToPrefs(orderedCities);
    }

    /** 获取默认城市 */
    public City getDefaultCity() {
        List<City> cities = getCities();
        for (City c : cities) {
            if (c.isDefault()) {
                return c;
            }
        }
        // 没有默认城市则返回第一个
        return cities.isEmpty() ? null : cities.get(0);
    }

    /** 设为默认城市 */
    public void setAsDefault(String cityId) {
        List<City> cities = getCities();
        for (City c : cities) {
            c.setDefault(c.getCityId() != null && c.getCityId().equals(cityId));
        }
        saveCitiesToPrefs(cities);
    }

    /** 检查城市是否已存在 */
    public boolean containsCity(String cityId) {
        List<City> cities = getCities();
        for (City c : cities) {
            if (c.getCityId() != null && c.getCityId().equals(cityId)) {
                return true;
            }
        }
        return false;
    }

    /** 缓存天气数据JSON */
    public void cacheWeather(String cityId, String weatherJson) {
        prefs.edit().putString(KEY_WEATHER_CACHE + cityId, weatherJson).apply();
    }

    /** 获取缓存的天气数据 */
    public String getCachedWeather(String cityId) {
        return prefs.getString(KEY_WEATHER_CACHE + cityId, null);
    }

    /** 将城市列表序列化存入SharedPreferences */
    private void saveCitiesToPrefs(List<City> cities) {
        String json = gson.toJson(cities);
        prefs.edit().putString(KEY_CITIES, json).apply();
    }
}

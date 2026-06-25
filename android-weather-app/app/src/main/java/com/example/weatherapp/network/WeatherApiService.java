package com.example.weatherapp.network;

import com.example.weatherapp.model.WeatherResponse;
import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * OpenWeatherMap API 接口定义。
 * 文档参考：https://openweathermap.org/api
 * 注意：RetrofitClient 的 baseUrl 已包含 data/2.5/ 前缀，
 * 此处路径是相对于该前缀的。
 */
public interface WeatherApiService {

    /**
     * 根据经纬度获取当前天气数据
     * 完整URL: {baseUrl}weather?lat=...&lon=...
     */
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    /**
     * 获取天气预报数据
     * 完整URL: {baseUrl}forecast?lat=...&lon=...
     */
    @GET("forecast")
    Call<WeatherResponse> getForecast(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("cnt") int cnt
    );

    /**
     * 通过城市名称搜索城市（geocode 接口）
     * 注意：此方法需配合 baseUrl="https://api.openweathermap.org/" 使用
     * 完整URL: https://api.openweathermap.org/geo/1.0/direct?q=...&limit=...
     */
    @GET("geo/1.0/direct")
    Call<JsonElement> geocodeCityRaw(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}

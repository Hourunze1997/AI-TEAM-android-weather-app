package com.example.weatherapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 单例客户端。
 * 配置基础URL和Gson转换器。
 */
public class RetrofitClient {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private static Retrofit retrofit;
    private static WeatherApiService apiService;

    /** 获取Retrofit单例 */
    public static synchronized Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /** 获取API Service单例 */
    public static synchronized WeatherApiService getApiService() {
        if (apiService == null) {
            apiService = getInstance().create(WeatherApiService.class);
        }
        return apiService;
    }

    /** 设置自定义BaseURL（用于测试或切换环境） */
    public static void setBaseUrl(String baseUrl) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(WeatherApiService.class);
    }
}

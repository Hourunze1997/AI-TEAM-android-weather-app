package com.example.weatherapp.network;

/** API回调接口：统一处理网络请求结果 */
public interface ApiCallback<T> {
    /** 请求成功 */
    void onSuccess(T result);
    /** 请求失败 */
    void onError(String message);
}

package com.example.weatherapp.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * GPS定位辅助类。
 * 使用系统原生 LocationManager，兼容无 Google Play Services 的国内设备。
 * 优先使用 GPS，无信号时降级到网络定位。
 */
public class LocationHelper {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final Context context;
    private final LocationManager locationManager;
    private LocationListener activeListener;

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    /** 定位结果回调接口 */
    public interface LocationCallback {
        void onLocationResult(double lat, double lon);
        void onLocationError(String msg);
    }

    /** 请求定位 */
    public void requestLocation(Activity activity, LocationCallback callback) {
        if (!hasLocationPermission()) {
            requestLocationPermission(activity);
            callback.onLocationError("缺少定位权限，请授权后重试");
            return;
        }

        // 先返回上次已知位置（速度快）
        Location last = getLastKnownLocation();
        if (last != null) {
            callback.onLocationResult(last.getLatitude(), last.getLongitude());
            return;
        }

        // 没有已知位置则主动请求一次
        requestSingleUpdate(callback);
    }

    /** 获取上次已知位置 */
    private Location getLastKnownLocation() {
        try {
            Location gps = null;
            Location network = null;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                network = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            // 返回更精确或更新的那个
            if (gps != null && network != null) {
                return gps.getAccuracy() <= network.getAccuracy() ? gps : network;
            }
            return gps != null ? gps : network;
        } catch (SecurityException e) {
            return null;
        }
    }

    /** 主动请求一次定位更新 */
    private void requestSingleUpdate(LocationCallback callback) {
        String provider = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        }

        if (provider == null) {
            callback.onLocationError("请在系统设置中开启定位服务");
            return;
        }

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                callback.onLocationResult(location.getLatitude(), location.getLongitude());
                stopLocationUpdates(null);
            }

            @Override
            public void onProviderDisabled(@NonNull String p) {
                callback.onLocationError("定位服务已关闭");
            }

            @Override
            public void onStatusChanged(String p, int status, Bundle extras) {}
        };

        activeListener = listener;
        try {
            locationManager.requestLocationUpdates(provider, 0, 0, listener, Looper.getMainLooper());
        } catch (SecurityException e) {
            callback.onLocationError("定位权限被拒绝: " + e.getMessage());
        }
    }

    /** 检查是否有定位权限 */
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** 请求定位权限 */
    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    /** 停止定位更新 */
    public void stopLocationUpdates(LocationCallback callback) {
        if (activeListener != null) {
            locationManager.removeUpdates(activeListener);
            activeListener = null;
        }
    }
}

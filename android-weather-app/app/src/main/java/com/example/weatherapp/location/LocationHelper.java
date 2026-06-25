package com.example.weatherapp.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;

/**
 * GPS定位辅助类。
 * 封装 Google Play Services Location 获取当前位置。
 */
public class LocationHelper {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /** 定位结果回调接口 */
    public interface LocationCallback {
        void onLocationResult(double lat, double lon);
        void onLocationError(String msg);
    }

    /**
     * 请求GPS定位。
     * 先检查权限，若无权限则请求；有权限则获取最近位置或主动请求定位。
     */
    public void requestLocation(Activity activity, LocationCallback callback) {
        if (!hasLocationPermission()) {
            requestLocationPermission(activity);
            callback.onLocationError("缺少定位权限，请授权后重试");
            return;
        }
        getLastLocation(callback);
    }

    /** 获取最近一次已知位置 */
    public void getLastLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationError("缺少定位权限");
            return;
        }
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocationResult(location.getLatitude(), location.getLongitude());
                        } else {
                            // 最近位置为空，主动请求一次定位
                            requestSingleLocationUpdate(callback);
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onLocationError("获取位置失败: " + e.getMessage());
                    });
        } catch (SecurityException e) {
            callback.onLocationError("定位权限被拒绝: " + e.getMessage());
        }
    }

    /** 主动请求一次定位更新 */
    private void requestSingleLocationUpdate(LocationCallback callback) {
        LocationRequest locationRequest = buildLocationRequest();
        LocationCallbackImpl locationCallback = new LocationCallbackImpl(callback);

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
            // 5秒后停止定位更新
            fusedLocationClient.getAvailable()
                    .addOnSuccessListener(aBoolean -> {
                        // 延迟移除
                    });
        } catch (SecurityException e) {
            callback.onLocationError("定位权限被拒绝: " + e.getMessage());
        }
    }

    /** 构建LocationRequest */
    private LocationRequest buildLocationRequest() {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(2000L)
                .setWaitForAccurateLocation(true)
                .build();
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
        if (callback instanceof LocationCallbackImpl) {
            fusedLocationClient.removeLocationUpdates((LocationCallbackImpl) callback);
        }
    }

    /** LocationCallback 实现类，将结果转发给自定义接口 */
    private class LocationCallbackImpl extends LocationCallback {
        private final LocationHelper.LocationCallback userCallback;

        LocationCallbackImpl(LocationHelper.LocationCallback userCallback) {
            this.userCallback = userCallback;
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null && locationResult.getLastLocation() != null) {
                android.location.Location location = locationResult.getLastLocation();
                userCallback.onLocationResult(location.getLatitude(), location.getLongitude());
                // 获取到结果后停止更新
                fusedLocationClient.removeLocationUpdates(this);
            } else {
                userCallback.onLocationError("无法获取当前位置");
            }
        }
    }
}

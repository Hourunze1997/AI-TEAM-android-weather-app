# WeatherApp - Android 天气应用

## 简介
基于 OpenWeatherMap API 的 Android 天气应用，支持城市搜索、城市管理、天气预报、GPS定位等功能。

## 配置
1. 在 `app/src/main/res/values/strings.xml` 中将 `YOUR_API_KEY_HERE` 替换为你的 OpenWeatherMap API Key
2. API Key 申请：https://openweathermap.org/api

## 构建
```bash
cd android-weather-app
./gradlew assembleDebug
```

## 功能
- 当前天气展示（温度、状况、湿度、风速）
- 未来7天天气预报
- 城市搜索与添加
- 城市管理（删除、拖拽排序）
- 左右滑动切换城市
- GPS定位获取当前位置天气
- 网络失败时显示缓存数据

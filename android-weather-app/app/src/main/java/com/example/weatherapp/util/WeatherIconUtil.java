package com.example.weatherapp.util;

import com.example.weatherapp.R;

/**
 * 天气图标工具类。
 * 根据 OpenWeatherMap 返回的 iconCode 映射到本地 drawable 资源。
 * iconCode 格式如 "01d"（白天晴）、"02n"（夜间少云）等。
 */
public class WeatherIconUtil {

    /** 根据 iconCode 返回对应的 drawable 资源ID */
    public static int getIconResource(String iconCode) {
        if (iconCode == null || iconCode.isEmpty()) {
            return R.drawable.ic_weather_unknown;
        }

        // 提取数字部分判断天气类型
        String code = iconCode.substring(0, Math.min(2, iconCode.length()));

        switch (code) {
            case "01": // 晴
                return iconCode.endsWith("d")
                        ? R.drawable.ic_weather_clear_day
                        : R.drawable.ic_weather_clear_night;
            case "02": // 少云
                return iconCode.endsWith("d")
                        ? R.drawable.ic_weather_few_clouds_day
                        : R.drawable.ic_weather_few_clouds_night;
            case "03": // 多云
                return R.drawable.ic_weather_clouds;
            case "04": // 阴
                return R.drawable.ic_weather_overcast;
            case "09": // 阵雨
                return R.drawable.ic_weather_shower_rain;
            case "10": // 雨
                return iconCode.endsWith("d")
                        ? R.drawable.ic_weather_rain_day
                        : R.drawable.ic_weather_rain_night;
            case "11": // 雷雨
                return R.drawable.ic_weather_thunderstorm;
            case "13": // 雪
                return R.drawable.ic_weather_snow;
            case "50": // 雾
                return R.drawable.ic_weather_mist;
            default:
                return R.drawable.ic_weather_unknown;
        }
    }

    /** 根据天气状况文字返回图标 */
    public static int getIconByCondition(String condition) {
        if (condition == null) {
            return R.drawable.ic_weather_unknown;
        }
        String c = condition.toLowerCase();
        if (c.contains("clear")) {
            return R.drawable.ic_weather_clear_day;
        } else if (c.contains("cloud")) {
            return R.drawable.ic_weather_clouds;
        } else if (c.contains("rain") || c.contains("drizzle")) {
            return R.drawable.ic_weather_rain_day;
        } else if (c.contains("thunder")) {
            return R.drawable.ic_weather_thunderstorm;
        } else if (c.contains("snow")) {
            return R.drawable.ic_weather_snow;
        } else if (c.contains("mist") || c.contains("fog") || c.contains("haze")) {
            return R.drawable.ic_weather_mist;
        } else {
            return R.drawable.ic_weather_unknown;
        }
    }

    /** 返回天气状况的中文描述 */
    public static String getConditionText(String condition) {
        if (condition == null) return "未知";
        String c = condition.toLowerCase();
        if (c.contains("clear")) return "晴";
        if (c.contains("cloud")) return "多云";
        if (c.contains("rain") || c.contains("drizzle")) return "雨";
        if (c.contains("thunder")) return "雷雨";
        if (c.contains("snow")) return "雪";
        if (c.contains("mist") || c.contains("fog") || c.contains("haze")) return "雾";
        return condition;
    }
}

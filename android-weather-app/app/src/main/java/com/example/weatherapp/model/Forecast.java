package com.example.weatherapp.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 预报数据模型：封装未来天气预报 */
public class Forecast {
    private long dateTime;       // 时间戳（秒）
    private double maxTemp;
    private double minTemp;
    private String condition;
    private String conditionDescription;
    private String iconCode;
    private int humidity;
    private double windSpeed;

    public Forecast() {}

    public Forecast(long dateTime, double maxTemp, double minTemp, String condition,
                    String conditionDescription, String iconCode, int humidity, double windSpeed) {
        this.dateTime = dateTime;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.condition = condition;
        this.conditionDescription = conditionDescription;
        this.iconCode = iconCode;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    public long getDateTime() { return dateTime; }
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }

    public double getMaxTemp() { return maxTemp; }
    public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }

    public double getMinTemp() { return minTemp; }
    public void setMinTemp(double minTemp) { this.minTemp = minTemp; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getConditionDescription() { return conditionDescription; }
    public void setConditionDescription(String conditionDescription) {
        this.conditionDescription = conditionDescription;
    }

    public String getIconCode() { return iconCode; }
    public void setIconCode(String iconCode) { this.iconCode = iconCode; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    /** 返回格式化日期，如 "周一 12月25日" */
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE MM月dd日", Locale.CHINA);
        return sdf.format(new Date(dateTime * 1000));
    }

    /** 返回简短日期，如 "周一" */
    public String getShortDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EE", Locale.CHINA);
        return sdf.format(new Date(dateTime * 1000));
    }

    /** 返回格式化最高温，如 "25°" */
    public String getFormattedMaxTemp() {
        return Math.round(maxTemp) + "°";
    }

    /** 返回格式化最低温，如 "15°" */
    public String getFormattedMinTemp() {
        return Math.round(minTemp) + "°";
    }
}

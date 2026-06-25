package com.example.weatherapp.model;

/** 天气数据模型：封装当前天气信息 */
public class Weather {
    private String cityName;
    private double temperature;
    private String condition;
    private String conditionDescription;
    private int humidity;
    private double windSpeed;
    private String iconCode;
    private long updateTime;

    public Weather() {}

    public Weather(String cityName, double temperature, String condition,
                   String conditionDescription, int humidity, double windSpeed,
                   String iconCode, long updateTime) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.condition = condition;
        this.conditionDescription = conditionDescription;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.iconCode = iconCode;
        this.updateTime = updateTime;
    }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getConditionDescription() { return conditionDescription; }
    public void setConditionDescription(String conditionDescription) {
        this.conditionDescription = conditionDescription;
    }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public String getIconCode() { return iconCode; }
    public void setIconCode(String iconCode) { this.iconCode = iconCode; }

    public long getUpdateTime() { return updateTime; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }

    /** 返回格式化温度，如 "25°C" */
    public String getFormattedTemperature() {
        return Math.round(temperature) + "°C";
    }
}

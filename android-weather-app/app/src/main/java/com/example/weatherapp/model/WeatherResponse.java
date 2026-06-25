package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenWeatherMap API 响应映射模型。
 * 同时适配当前天气接口和预报接口的JSON结构。
 */
public class WeatherResponse {

    // ---- 当前天气接口字段 ----
    private MainInfo main;
    private List<WeatherInfo> weather;
    private WindInfo wind;
    private String name;
    private SysInfo sys;
    private long dt;

    // ---- 预报接口字段 ----
    private List<WeatherResponse> list;
    private CityInfo city;

    /** 主信息：温度、湿度、气压等 */
    public static class MainInfo {
        private double temp;
        @SerializedName("feels_like")
        private double feelsLike;
        private int humidity;
        @SerializedName("temp_min")
        private double tempMin;
        @SerializedName("temp_max")
        private double tempMax;
        private int pressure;

        public double getTemp() { return temp; }
        public double getFeelsLike() { return feelsLike; }
        public int getHumidity() { return humidity; }
        public double getTempMin() { return tempMin; }
        public double getTempMax() { return tempMax; }
        public int getPressure() { return pressure; }
    }

    /** 天气状况信息 */
    public static class WeatherInfo {
        private int id;
        private String main;
        private String description;
        private String icon;

        public int getId() { return id; }
        public String getMain() { return main; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    /** 风信息 */
    public static class WindInfo {
        private double speed;
        private int deg;

        public double getSpeed() { return speed; }
        public int getDeg() { return deg; }
    }

    /** 系统信息：国家、日出日落等 */
    public static class SysInfo {
        private String country;
        private long sunrise;
        private long sunset;

        public String getCountry() { return country; }
        public long getSunrise() { return sunrise; }
        public long getSunset() { return sunset; }
    }

    /** 预报接口的城市信息 */
    public static class CityInfo {
        private int id;
        private String name;
        private CoordInfo coord;
        private String country;

        public int getId() { return id; }
        public String getName() { return name; }
        public CoordInfo getCoord() { return coord; }
        public String getCountry() { return country; }
    }

    /** 坐标信息 */
    public static class CoordInfo {
        private double lat;
        private double lon;

        public double getLat() { return lat; }
        public double getLon() { return lon; }
    }

    public MainInfo getMain() { return main; }
    public void setMain(MainInfo main) { this.main = main; }

    public List<WeatherInfo> getWeather() {
        return weather != null ? weather : new ArrayList<>();
    }
    public void setWeather(List<WeatherInfo> weather) { this.weather = weather; }

    public WindInfo getWind() { return wind; }
    public void setWind(WindInfo wind) { this.wind = wind; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SysInfo getSys() { return sys; }
    public void setSys(SysInfo sys) { this.sys = sys; }

    public long getDt() { return dt; }
    public void setDt(long dt) { this.dt = dt; }

    public List<WeatherResponse> getList() {
        return list != null ? list : new ArrayList<>();
    }
    public void setList(List<WeatherResponse> list) { this.list = list; }

    public CityInfo getCity() { return city; }
    public void setCity(CityInfo city) { this.city = city; }

    /** 是否为预报响应 */
    public boolean isForecastResponse() {
        return list != null && !list.isEmpty();
    }

    /** 转换为 Weather 模型 */
    public Weather toWeather() {
        Weather w = new Weather();
        w.setCityName(this.name != null ? this.name : "");
        if (main != null) {
            w.setTemperature(main.getTemp());
            w.setHumidity(main.getHumidity());
        }
        if (this.weather != null && !this.weather.isEmpty()) {
            WeatherInfo info = this.weather.get(0);
            w.setCondition(info.getMain());
            w.setConditionDescription(info.getDescription());
            w.setIconCode(info.getIcon());
        }
        if (wind != null) {
            w.setWindSpeed(wind.getSpeed());
        }
        w.setUpdateTime(dt);
        return w;
    }

    /** 从预报列表项转换为 Forecast 模型 */
    public Forecast toForecast() {
        Forecast f = new Forecast();
        f.setDateTime(dt);
        if (main != null) {
            f.setMaxTemp(main.getTempMax());
            f.setMinTemp(main.getTempMin());
            f.setHumidity(main.getHumidity());
        }
        if (weather != null && !weather.isEmpty()) {
            WeatherInfo info = weather.get(0);
            f.setCondition(info.getMain());
            f.setConditionDescription(info.getDescription());
            f.setIconCode(info.getIcon());
        }
        if (wind != null) {
            f.setWindSpeed(wind.getSpeed());
        }
        return f;
    }
}

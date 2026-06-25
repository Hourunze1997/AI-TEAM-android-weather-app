package com.example.weatherapp.model;

/** 城市数据模型：封装城市信息 */
public class City {
    private String cityId;
    private String cityName;
    private double latitude;
    private double longitude;
    private boolean isDefault;
    private String country;

    public City() {}

    public City(String cityId, String cityName, double latitude, double longitude, String country) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.isDefault = false;
    }

    public City(String cityId, String cityName, double latitude, double longitude,
                boolean isDefault, String country) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isDefault = isDefault;
        this.country = country;
    }

    public String getCityId() { return cityId; }
    public void setCityId(String cityId) { this.cityId = cityId; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    /** 返回 "城市名, 国家" 格式 */
    public String getDisplayName() {
        if (country != null && !country.isEmpty()) {
            return cityName + ", " + country;
        }
        return cityName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        City city = (City) obj;
        return cityId != null && cityId.equals(city.cityId);
    }

    @Override
    public int hashCode() {
        return cityId != null ? cityId.hashCode() : 0;
    }
}

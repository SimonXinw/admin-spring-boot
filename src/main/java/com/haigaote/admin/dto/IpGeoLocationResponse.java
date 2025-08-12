package com.haigaote.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * IP地理位置响应数据传输对象
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
public class IpGeoLocationResponse {

    /**
     * 查询的IP地址
     */
    @JsonProperty("ip")
    private String ip;

    /**
     * 查询状态 (success/fail)
     */
    @JsonProperty("status")
    private String status;

    /**
     * 国家名称
     */
    @JsonProperty("country")
    private String country;

    /**
     * 国家代码
     */
    @JsonProperty("countryCode")
    private String countryCode;

    /**
     * 地区/省份
     */
    @JsonProperty("region")
    private String region;

    /**
     * 地区代码
     */
    @JsonProperty("regionName")
    private String regionName;

    /**
     * 城市
     */
    @JsonProperty("city")
    private String city;

    /**
     * 邮政编码
     */
    @JsonProperty("zip")
    private String zip;

    /**
     * 纬度
     */
    @JsonProperty("lat")
    private Double lat;

    /**
     * 经度
     */
    @JsonProperty("lon")
    private Double lon;

    /**
     * 时区
     */
    @JsonProperty("timezone")
    private String timezone;

    /**
     * ISP提供商
     */
    @JsonProperty("isp")
    private String isp;

    /**
     * 组织机构
     */
    @JsonProperty("org")
    private String org;

    /**
     * AS号码和名称
     */
    @JsonProperty("as")
    private String asInfo;

    /**
     * 错误消息（当status为fail时）
     */
    @JsonProperty("message")
    private String message;

    /**
     * 查询时间戳
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 默认构造函数
     */
    public IpGeoLocationResponse() {
    }

    // Getter和Setter方法

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getAsInfo() {
        return asInfo;
    }

    public void setAsInfo(String asInfo) {
        this.asInfo = asInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "IpGeoLocationResponse{" +
                "ip='" + ip + '\'' +
                ", status='" + status + '\'' +
                ", country='" + country + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", region='" + region + '\'' +
                ", regionName='" + regionName + '\'' +
                ", city='" + city + '\'' +
                ", zip='" + zip + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", timezone='" + timezone + '\'' +
                ", isp='" + isp + '\'' +
                ", org='" + org + '\'' +
                ", asInfo='" + asInfo + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 
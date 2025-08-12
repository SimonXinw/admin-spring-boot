package com.haigaote.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * IP信息响应数据传输对象
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
public class IpInfoResponse {

    /**
     * 客户端IP地址
     */
    @JsonProperty("clientIp")
    private String clientIp;

    /**
     * 响应消息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 时间戳
     */
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * 默认构造函数
     */
    public IpInfoResponse() {
    }

    /**
     * 全参构造函数
     * 
     * @param clientIp 客户端IP地址
     * @param message 响应消息
     * @param timestamp 时间戳
     */
    public IpInfoResponse(String clientIp, String message, Long timestamp) {
        this.clientIp = clientIp;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getter和Setter方法

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
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
        return "IpInfoResponse{" +
                "clientIp='" + clientIp + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 
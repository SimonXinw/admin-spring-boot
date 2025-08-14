package com.haigaote.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 客户端IP实体类
 * 对应数据库表 client_ip
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
@TableName("client_ip")
public class ClientIp {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 用户代理信息
     */
    private String userAgent;

    /**
     * 远程主机
     */
    private String remoteHost;

    /**
     * 远程端口
     */
    private Integer remotePort;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 服务器端口
     */
    private Integer serverPort;

    /**
     * IP类型（内网IP/公网IP）
     */
    private String ipType;

    /**
     * 是否为有效格式
     */
    private Boolean validFormat;

    /**
     * 是否为内网IP
     */
    private Boolean privateIp;

    /**
     * 请求时间戳（毫秒）
     */
    private Long requestTimestamp;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 默认构造函数
     */
    public ClientIp() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 构造函数
     */
    public ClientIp(String clientIp, String userAgent, String remoteHost, 
                    Integer remotePort, String serverName, Integer serverPort,
                    String ipType, Boolean validFormat, Boolean privateIp, Long requestTimestamp) {
        this();
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.ipType = ipType;
        this.validFormat = validFormat;
        this.privateIp = privateIp;
        this.requestTimestamp = requestTimestamp;
    }

    // Getter和Setter方法

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getIpType() {
        return ipType;
    }

    public void setIpType(String ipType) {
        this.ipType = ipType;
    }

    public Boolean getValidFormat() {
        return validFormat;
    }

    public void setValidFormat(Boolean validFormat) {
        this.validFormat = validFormat;
    }

    public Boolean getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(Boolean privateIp) {
        this.privateIp = privateIp;
    }

    public Long getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(Long requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "ClientIp{" +
                "id=" + id +
                ", clientIp='" + clientIp + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                ", remotePort=" + remotePort +
                ", serverName='" + serverName + '\'' +
                ", serverPort=" + serverPort +
                ", ipType='" + ipType + '\'' +
                ", validFormat=" + validFormat +
                ", privateIp=" + privateIp +
                ", requestTimestamp=" + requestTimestamp +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
} 
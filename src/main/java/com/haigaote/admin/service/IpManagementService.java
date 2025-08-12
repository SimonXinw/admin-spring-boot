package com.haigaote.admin.service;

import com.haigaote.admin.dto.IpInfoResponse;
import com.haigaote.admin.dto.IpGeoLocationResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * IP管理服务类
 * 提供IP地址获取、验证、分析等相关功能
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
@Service
public class IpManagementService {

    private final RestTemplate restTemplate;

    /**
     * 构造函数注入RestTemplate
     */
    @Autowired
    public IpManagementService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 获取客户端IP信息
     * 
     * @param request HTTP请求对象
     * @return IP信息响应对象
     */
    public IpInfoResponse getClientIpInfo(HttpServletRequest request) {
        String clientIp = extractClientIpAddress(request);
        
        return new IpInfoResponse(
            clientIp,
            "客户端IP地址获取成功",
            System.currentTimeMillis()
        );
    }

    /**
     * 提取客户端真实IP地址
     * 支持多级代理服务器环境下的IP获取
     * 
     * @param request HTTP请求对象
     * @return 客户端真实IP地址
     */
    public String extractClientIpAddress(HttpServletRequest request) {
        // 代理服务器传递的原始客户端IP列表（优先级最高）
        String ipAddress = getHeaderValue(request, "X-Forwarded-For");
        
        // Apache代理传递的客户端IP
        if (!isValidIp(ipAddress)) {
            ipAddress = getHeaderValue(request, "Proxy-Client-IP");
        }
        
        // WebLogic代理传递的客户端IP
        if (!isValidIp(ipAddress)) {
            ipAddress = getHeaderValue(request, "WL-Proxy-Client-IP");
        }
        
        // HTTP协议中的客户端IP
        if (!isValidIp(ipAddress)) {
            ipAddress = getHeaderValue(request, "HTTP_CLIENT_IP");
        }
        
        // HTTP协议中的转发IP
        if (!isValidIp(ipAddress)) {
            ipAddress = getHeaderValue(request, "HTTP_X_FORWARDED_FOR");
        }
        
        // 直连情况下的远程IP地址
        if (!isValidIp(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // 处理多级代理情况，取第一个非unknown的IP
        return processMultiProxyIp(ipAddress);
    }

    /**
     * 获取请求头的值
     * 
     * @param request HTTP请求对象
     * @param headerName 请求头名称
     * @return 请求头值
     */
    private String getHeaderValue(HttpServletRequest request, String headerName) {
        return request.getHeader(headerName);
    }

    /**
     * 验证IP地址是否有效
     * 
     * @param ipAddress IP地址字符串
     * @return 是否为有效IP
     */
    private boolean isValidIp(String ipAddress) {
        return StringUtils.hasText(ipAddress) && !"unknown".equalsIgnoreCase(ipAddress);
    }

    /**
     * 处理多级代理情况下的IP地址
     * X-Forwarded-For可能包含多个IP地址，用逗号分隔，第一个为真实客户端IP
     * 
     * @param ipAddress 原始IP地址字符串
     * @return 处理后的客户端IP地址
     */
    private String processMultiProxyIp(String ipAddress) {
        if (StringUtils.hasText(ipAddress) && ipAddress.contains(",")) {
            // 取第一个IP地址作为真实客户端IP
            return ipAddress.substring(0, ipAddress.indexOf(",")).trim();
        }
        return ipAddress;
    }

    /**
     * 验证IP地址格式是否正确
     * 
     * @param ipAddress IP地址字符串
     * @return 是否为有效的IP格式
     */
    public boolean isValidIpFormat(String ipAddress) {
        if (!StringUtils.hasText(ipAddress)) {
            return false;
        }
        
        // 简单的IPv4格式验证（可以根据需要扩展为更严格的验证）
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        try {
            for (String part : parts) {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 检查IP是否为内网IP
     * 
     * @param ipAddress IP地址
     * @return 是否为内网IP
     */
    public boolean isPrivateIp(String ipAddress) {
        if (!isValidIpFormat(ipAddress)) {
            return false;
        }
        
        // 常见的内网IP段
        return ipAddress.startsWith("192.168.") || 
               ipAddress.startsWith("10.") || 
               ipAddress.startsWith("172.16.") ||
                               ipAddress.equals("127.0.0.1") ||
                ipAddress.equals("localhost");
    }

    /**
     * 查询IP地理位置信息
     * 使用免费的ip-api.com服务查询IP地理位置
     * 
     * @param ipAddress IP地址
     * @return 地理位置信息
     */
    public IpGeoLocationResponse queryIpGeoLocation(String ipAddress) {
        IpGeoLocationResponse response = new IpGeoLocationResponse();
        response.setIp(ipAddress);
        response.setTimestamp(System.currentTimeMillis());

        try {
            // 验证IP地址格式
            if (!isValidIpFormat(ipAddress)) {
                response.setStatus("fail");
                response.setMessage("无效的IP地址格式");
                return response;
            }

            // 内网IP特殊处理
            if (isPrivateIp(ipAddress)) {
                response.setStatus("success");
                response.setCountry("中国");
                response.setCountryCode("CN");
                response.setRegion("本地网络");
                response.setRegionName("内网环境");
                response.setCity("本地");
                response.setIsp("内网");
                response.setMessage("内网IP地址，无法获取精确地理位置");
                return response;
            }

            // 调用第三方API查询地理位置
            String apiUrl = "http://ip-api.com/json/" + ipAddress + "?lang=zh-CN";
            Map<String, Object> apiResponse = restTemplate.getForObject(apiUrl, Map.class);

            if (apiResponse != null) {
                response.setStatus((String) apiResponse.get("status"));
                
                if ("success".equals(response.getStatus())) {
                    // 成功获取地理位置信息
                    response.setCountry((String) apiResponse.get("country"));
                    response.setCountryCode((String) apiResponse.get("countryCode"));
                    response.setRegion((String) apiResponse.get("region"));
                    response.setRegionName((String) apiResponse.get("regionName"));
                    response.setCity((String) apiResponse.get("city"));
                    response.setZip((String) apiResponse.get("zip"));
                    response.setTimezone((String) apiResponse.get("timezone"));
                    response.setIsp((String) apiResponse.get("isp"));
                    response.setOrg((String) apiResponse.get("org"));
                    response.setAsInfo((String) apiResponse.get("as"));
                    
                    // 处理经纬度
                    Object lat = apiResponse.get("lat");
                    Object lon = apiResponse.get("lon");
                    if (lat != null) {
                        response.setLat(Double.valueOf(lat.toString()));
                    }
                    if (lon != null) {
                        response.setLon(Double.valueOf(lon.toString()));
                    }
                    
                    response.setMessage("地理位置查询成功");
                } else {
                    // 查询失败
                    response.setMessage((String) apiResponse.get("message"));
                }
            } else {
                response.setStatus("fail");
                response.setMessage("无法连接到地理位置查询服务");
            }

        } catch (Exception e) {
            response.setStatus("fail");
            response.setMessage("查询地理位置时发生错误: " + e.getMessage());
        }

        return response;
    }

    /**
     * 获取客户端IP的地理位置信息
     * 
     * @param request HTTP请求对象
     * @return 地理位置信息
     */
    public IpGeoLocationResponse getClientIpGeoLocation(HttpServletRequest request) {
        String clientIp = extractClientIpAddress(request);
        return queryIpGeoLocation(clientIp);
    }
} 
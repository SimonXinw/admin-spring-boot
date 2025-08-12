package com.example.springbootdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ip")
public class IpController {

    @GetMapping("/my")
    public Map<String, Object> getClientIp(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("clientIp", clientIp);
        response.put("message", "客户端IP地址获取成功");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    /**
     * 获取客户端真实IP地址
     * @param request HttpServletRequest对象
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // 通过代理服务器转发的客户端IP
        String ipAddress = request.getHeader("X-Forwarded-For");
        
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // 如果通过多级代理，第一个IP为客户端真实IP
        if (ipAddress != null && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
        
        return ipAddress;
    }
} 
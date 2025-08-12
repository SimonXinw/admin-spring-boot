package com.haigaote.admin.controller;

import com.haigaote.admin.service.IpManagementService;
import com.haigaote.admin.dto.IpGeoLocationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端IP控制器
 * 提供客户端IP相关操作的简洁API接口（不带/api前缀）
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/ip")
public class ClientIpController {

    private final IpManagementService ipManagementService;

    /**
     * 构造函数注入IP管理服务
     * 
     * @param ipManagementService IP管理服务实例
     */
    @Autowired
    public ClientIpController(IpManagementService ipManagementService) {
        this.ipManagementService = ipManagementService;
    }

    /**
     * 获取客户端IP地址
     * 提供最直接的客户端IP获取方式
     * 
     * @param request HTTP请求对象
     * @return 客户端IP信息
     */
    @GetMapping("/my")
    public Map<String, Object> getClientIp(HttpServletRequest request) {
        String clientIp = ipManagementService.extractClientIpAddress(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("clientIp", clientIp);
        response.put("message", "客户端IP地址获取成功");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    /**
     * 查询客户端或指定IP的地理位置信息
     * 提供最直接的IP地理位置查询方式
     * 
     * @param ipAddress 待查询的IP地址（可选，默认查询客户端IP）
     * @param request HTTP请求对象
     * @return IP地理位置信息
     */
    @GetMapping("/my/geo")
    public IpGeoLocationResponse getIpGeoLocation(
            @RequestParam(value = "ip", required = false) String ipAddress,
            HttpServletRequest request) {
        
        try {
            if (ipAddress != null && !ipAddress.trim().isEmpty()) {
                // 查询指定IP的地理位置
                return ipManagementService.queryIpGeoLocation(ipAddress.trim());
            } else {
                // 查询客户端IP的地理位置
                return ipManagementService.getClientIpGeoLocation(request);
            }
        } catch (Exception e) {
            // 异常情况下返回错误信息
            IpGeoLocationResponse errorResponse = new IpGeoLocationResponse();
            errorResponse.setIp(ipAddress != null ? ipAddress : "未知");
            errorResponse.setStatus("fail");
            errorResponse.setMessage("查询地理位置失败: " + e.getMessage());
            errorResponse.setTimestamp(System.currentTimeMillis());
            
            return errorResponse;
        }
    }
} 
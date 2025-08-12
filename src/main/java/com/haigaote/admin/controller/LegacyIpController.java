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
 * 兼容性IP控制器
 * 保持原有的API路径，确保向后兼容性
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 * @deprecated 建议使用 IpManagementController 中的新接口
 */
@RestController
@RequestMapping("/ip")
@Deprecated
public class LegacyIpController {

    private final IpManagementService ipManagementService;

    /**
     * 构造函数注入IP管理服务
     * 
     * @param ipManagementService IP管理服务实例
     */
    @Autowired
    public LegacyIpController(IpManagementService ipManagementService) {
        this.ipManagementService = ipManagementService;
    }

    /**
     * 获取客户端IP地址（兼容原有接口）
     * 保持与原有IpController完全相同的响应格式
     * 
     * @param request HTTP请求对象
     * @return 客户端IP信息
     * @deprecated 建议使用 /api/ip/client 接口
     */
    @GetMapping("/my")
    @Deprecated
    public Map<String, Object> getClientIp(HttpServletRequest request) {
        String clientIp = ipManagementService.extractClientIpAddress(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("clientIp", clientIp);
        response.put("message", "客户端IP地址获取成功");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    /**
     * 查询IP地理位置信息（兼容原有接口路径）
     * 
     * @param ipAddress 待查询的IP地址（可选，默认查询客户端IP）
     * @param request HTTP请求对象
     * @return IP地理位置信息
     * @deprecated 建议使用 /api/ip/geo 接口
     */
    @GetMapping("/geo")
    @Deprecated
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
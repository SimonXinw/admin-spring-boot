package com.haigaote.admin.controller;

import com.haigaote.admin.dto.IpInfoResponse;
import com.haigaote.admin.service.IpManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * IP管理控制器
 * 提供IP地址相关的REST API接口
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/api/ip")
public class IpManagementController {

    private final IpManagementService ipManagementService;

    /**
     * 构造函数注入IP管理服务
     * 
     * @param ipManagementService IP管理服务实例
     */
    @Autowired
    public IpManagementController(IpManagementService ipManagementService) {
        this.ipManagementService = ipManagementService;
    }

    /**
     * 获取客户端IP地址信息
     * 
     * @param request HTTP请求对象
     * @return 包含客户端IP信息的响应对象
     */
    @GetMapping("/client")
    public ResponseEntity<IpInfoResponse> getClientIp(HttpServletRequest request) {
        try {
            IpInfoResponse response = ipManagementService.getClientIpInfo(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 异常情况下返回错误信息
            IpInfoResponse errorResponse = new IpInfoResponse(
                "未知",
                "获取IP地址失败: " + e.getMessage(),
                System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 验证IP地址格式是否正确
     * 
     * @param ipAddress 待验证的IP地址
     * @return 验证结果
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateIp(@RequestParam("ip") String ipAddress) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isValid = ipManagementService.isValidIpFormat(ipAddress);
            boolean isPrivate = ipManagementService.isPrivateIp(ipAddress);
            
            result.put("ip", ipAddress);
            result.put("valid", isValid);
            result.put("private", isPrivate);
            result.put("message", isValid ? "IP地址格式正确" : "IP地址格式错误");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("ip", ipAddress);
            result.put("valid", false);
            result.put("private", false);
            result.put("message", "验证IP地址时发生错误: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取客户端IP详细信息
     * 包括IP地址、是否为内网IP等信息
     * 
     * @param request HTTP请求对象
     * @return 详细的IP信息
     */
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> getIpDetail(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String clientIp = ipManagementService.extractClientIpAddress(request);
            boolean isValidFormat = ipManagementService.isValidIpFormat(clientIp);
            boolean isPrivate = ipManagementService.isPrivateIp(clientIp);
            
            result.put("clientIp", clientIp);
            result.put("validFormat", isValidFormat);
            result.put("privateIp", isPrivate);
            result.put("ipType", isPrivate ? "内网IP" : "公网IP");
            result.put("userAgent", request.getHeader("User-Agent"));
            result.put("remoteHost", request.getRemoteHost());
            result.put("remotePort", request.getRemotePort());
            result.put("serverName", request.getServerName());
            result.put("serverPort", request.getServerPort());
            result.put("message", "IP详细信息获取成功");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("message", "获取IP详细信息失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
} 
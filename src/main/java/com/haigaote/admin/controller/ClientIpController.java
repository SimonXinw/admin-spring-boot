package com.haigaote.admin.controller;

import com.haigaote.admin.service.IpManagementService;
import com.haigaote.admin.dto.IpGeoLocationResponse;
import com.haigaote.admin.config.PerformanceMonitor;
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
    private final PerformanceMonitor performanceMonitor;

    /**
     * 构造函数注入服务
     * 
     * @param ipManagementService IP管理服务实例
     * @param performanceMonitor 性能监控实例
     */
    @Autowired
    public ClientIpController(IpManagementService ipManagementService, PerformanceMonitor performanceMonitor) {
        this.ipManagementService = ipManagementService;
        this.performanceMonitor = performanceMonitor;
    }

    /**
     * 获取客户端IP地址
     * 优先快速返回IP地址，异步保存到数据库以提高响应速度
     * 
     * @param request HTTP请求对象
     * @return 客户端IP信息
     */
    @GetMapping("/my")
    public Map<String, Object> getClientIp(HttpServletRequest request) {
        try {
            // 快速提取IP地址
            String clientIp = ipManagementService.extractClientIpAddress(request);
            
            // 异步保存到数据库，不阻塞响应
            ipManagementService.saveClientIpInfoAsyncSimple(request);
            
            // 立即返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("success", true);
            response.put("clientIp", clientIp);
            response.put("message", "客户端IP地址获取成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return response;
            
        } catch (Exception e) {
            // 异常情况下返回错误信息
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("success", false);
            errorResponse.put("clientIp", "未知");
            errorResponse.put("message", "获取IP地址失败: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return errorResponse;
        }
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

    /**
     * 获取性能监控报告
     * 查看异步线程池的运行状态
     * 
     * @return 性能监控信息
     */
    @GetMapping("/performance")
    public Map<String, Object> getPerformanceReport() {
        try {
            String report = performanceMonitor.getPerformanceReport();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "性能监控报告获取成功");
            response.put("report", report);
            response.put("timestamp", System.currentTimeMillis());
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "获取性能监控报告失败: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return errorResponse;
        }
    }
} 
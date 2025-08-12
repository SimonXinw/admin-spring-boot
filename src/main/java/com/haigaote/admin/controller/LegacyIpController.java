package com.haigaote.admin.controller;

import com.haigaote.admin.service.IpManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
} 
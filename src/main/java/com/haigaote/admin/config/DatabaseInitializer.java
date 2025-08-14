package com.haigaote.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * 数据库初始化组件
 * 在应用启动时自动创建数据库和表
 * 
 * @author xw
 * @version 1.0
 * @since 2025-01-01
 */
@Component
@Order(1) // 确保在其他组件之前执行
public class DatabaseInitializer implements CommandLineRunner {

    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始初始化数据库...");
        
        try {
            // 1. 连接到MySQL服务器（不指定数据库）创建admin数据库
            String mysqlUrl = "jdbc:mysql://localhost:3306/?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";
            try (Connection mysqlConnection = DriverManager.getConnection(mysqlUrl, username, password)) {
                createDatabaseIfNotExists(mysqlConnection);
            }
            
            // 2. 连接到admin数据库创建表
            String adminUrl = "jdbc:mysql://localhost:3306/admin?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";
            try (Connection adminConnection = DriverManager.getConnection(adminUrl, username, password)) {
                createClientIpTableIfNotExists(adminConnection);
            }
            
            System.out.println("数据库初始化完成！");
            
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 创建admin数据库（如果不存在）
     */
    private void createDatabaseIfNotExists(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            // 检查数据库是否存在
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES LIKE 'admin'");
            
            if (!resultSet.next()) {
                // 数据库不存在，创建它
                System.out.println("数据库 'admin' 不存在，正在创建...");
                String createDbSql = "CREATE DATABASE `admin` " +
                        "DEFAULT CHARACTER SET utf8mb4 " +
                        "COLLATE utf8mb4_unicode_ci";
                statement.executeUpdate(createDbSql);
                System.out.println("数据库 'admin' 创建成功！");
            } else {
                System.out.println("数据库 'admin' 已存在。");
            }
            resultSet.close();
        }
    }

    /**
     * 创建client_ip表（如果不存在）
     */
    private void createClientIpTableIfNotExists(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            // 检查表是否存在
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables("admin", null, "client_ip", null);
            
            if (!tables.next()) {
                // 表不存在，创建它
                System.out.println("表 'client_ip' 不存在，正在创建...");
                String createTableSql = """
                    CREATE TABLE `client_ip` (
                      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                      `client_ip` varchar(50) NOT NULL COMMENT '客户端IP地址',
                      `user_agent` text COMMENT '用户代理信息',
                      `remote_host` varchar(255) COMMENT '远程主机',
                      `remote_port` int(11) COMMENT '远程端口',
                      `server_name` varchar(255) COMMENT '服务器名称',
                      `server_port` int(11) COMMENT '服务器端口',
                      `ip_type` varchar(20) COMMENT 'IP类型（内网IP/公网IP）',
                      `valid_format` tinyint(1) COMMENT '是否为有效格式',
                      `private_ip` tinyint(1) COMMENT '是否为内网IP',
                      `request_timestamp` bigint(20) COMMENT '请求时间戳（毫秒）',
                      `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      PRIMARY KEY (`id`),
                      KEY `idx_client_ip` (`client_ip`),
                      KEY `idx_create_time` (`create_time`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户端IP信息表'
                    """;
                statement.executeUpdate(createTableSql);
                System.out.println("表 'client_ip' 创建成功！");
            } else {
                System.out.println("表 'client_ip' 已存在。");
            }
            tables.close();
        }
    }
} 
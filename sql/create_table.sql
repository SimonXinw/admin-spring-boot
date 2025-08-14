-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `admin` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用admin数据库
USE `admin`;

-- 创建client_ip表
CREATE TABLE IF NOT EXISTS `client_ip` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户端IP信息表'; 
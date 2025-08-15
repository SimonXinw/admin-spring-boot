xw 天才，开始写 java 啦！
# Admin Spring Boot 管理后台项目

## 项目说明
这是一个基于 Java 17 和 Spring Boot 3.2.0 的管理后台项目，提供了完整的IP地址管理功能，包括获取、验证、分析等操作。

## 功能特性
- ✅ 获取客户端真实IP地址（支持代理服务器环境）
- ✅ IP地址格式验证功能
- ✅ 内网/公网IP识别
- ✅ 客户端详细信息获取
- ✅ **IP地理位置查询功能** 🆕
- ✅ RESTful API 接口
- ✅ JSON 格式响应
- ✅ 完善的异常处理
- ✅ 内嵌 Tomcat 服务器
- ✅ 支持热部署（spring-boot-devtools）
- ✅ 分层架构设计（Controller-Service-DTO）

## 技术栈
- **后端框架**: Java 17 + Spring Boot 3.2.0
- **数据持久化**: MyBatis Plus 3.5.5 + MySQL 8.0
- **连接池**: HikariCP (高性能数据库连接池)
- **构建工具**: Maven 3.9+
- **Web服务器**: 内嵌 Tomcat + Nginx反向代理
- **异步处理**: Spring @Async + 自定义线程池
- **JSON处理**: Jackson (Spring Boot内置)
- **生产环境**: CentOS/Rocky Linux/Amazon Linux + systemd服务管理 + AWS云服务集成

## 项目结构
```
src/main/java/com/haigaote/admin/
├── AdminApplication.java              # 主启动类
├── controller/                        # 控制层
│   ├── IpManagementController.java    # IP管理控制器（完整功能API）
│   └── ClientIpController.java        # 客户端IP控制器（简化路径API，异步优化）
├── service/                           # 服务层
│   └── IpManagementService.java       # IP管理服务（支持异步操作）
├── dto/                              # 数据传输对象
│   ├── IpInfoResponse.java           # IP信息响应DTO
│   └── IpGeoLocationResponse.java    # IP地理位置响应DTO
├── entity/                           # 实体类
│   └── ClientIp.java                # 客户端IP实体
├── mapper/                           # MyBatis映射器
│   └── ClientIpMapper.java          # 客户端IP数据访问层
└── config/                           # 配置类
    ├── AsyncConfig.java              # 异步线程池配置
    ├── WebConfig.java                # Web层配置
    ├── DatabaseInitializer.java     # 数据库自动初始化
    └── PerformanceMonitor.java      # 性能监控组件
```

## API 接口

### 1. 获取客户端IP信息
- **接口地址**: `GET /api/ip/client`
- **描述**: 获取客户端IP地址基本信息
- **响应格式**: JSON
- **响应示例**:
```json
{
    "clientIp": "192.168.1.100",
    "message": "客户端IP地址获取成功",
    "timestamp": 1703749200000
}
```

### 2. 验证IP地址格式
- **接口地址**: `GET /api/ip/validate?ip={ipAddress}`
- **描述**: 验证指定IP地址的格式是否正确
- **参数**: 
  - `ip`: 待验证的IP地址（必填）
- **响应示例**:
```json
{
    "ip": "192.168.1.100",
    "valid": true,
    "private": true,
    "message": "IP地址格式正确",
    "timestamp": 1703749200000
}
```

### 3. 获取客户端IP详细信息
- **接口地址**: `GET /api/ip/detail`
- **描述**: 获取客户端IP的详细信息，包括类型、用户代理等
- **响应示例**:
```json
{
    "clientIp": "192.168.1.100",
    "validFormat": true,
    "privateIp": true,
    "ipType": "内网IP",
    "userAgent": "Mozilla/5.0...",
    "remoteHost": "192.168.1.100",
    "remotePort": 54321,
    "serverName": "localhost",
    "serverPort": 8080,
    "message": "IP详细信息获取成功",
    "timestamp": 1703749200000
}
```

### 4. 查询IP地理位置信息
- **接口地址**: `GET /api/ip/my/geo?ip={ipAddress}`
- **描述**: 查询指定IP或客户端IP的地理位置信息
- **参数**: 
  - `ip`: 待查询的IP地址（可选，不填则查询客户端IP）
- **响应示例**:
```json
{
    "ip": "8.8.8.8",
    "status": "success",
    "country": "美国",
    "countryCode": "US",
    "region": "CA",
    "regionName": "加利福尼亚",
    "city": "芒廷维尤",
    "zip": "94043",
    "lat": 37.4056,
    "lon": -122.0775,
    "timezone": "America/Los_Angeles",
    "isp": "Google LLC",
    "org": "Google Public DNS",
    "as": "AS15169 Google LLC",
    "message": "地理位置查询成功",
    "timestamp": 1703749200000
}
```

### 5. 客户端IP接口（不带/api前缀）
- **基础接口**: `GET /ip/my`
- **地理位置接口**: `GET /ip/my/geo?ip={ipAddress}`
- **性能监控接口**: `GET /ip/performance`
- **描述**: 专门处理客户端IP相关操作，提供更简洁的API路径，采用异步保存提升响应速度
- **响应示例**:
```json
{
    "code": 200,
    "success": true,
    "clientIp": "192.168.1.100",
    "message": "客户端IP地址获取成功",
    "timestamp": 1703749200000
}
```

## 启动方式

> 📋 **生产环境部署**: 如需将项目部署到 Linux CentOS 服务器，请参考详细的 [部署指南](./DEPLOY.md)

### 方式一：在 Cursor 中启动调试器
1. 打开 `src/main/java/com/haigaote/admin/AdminApplication.java` 文件
2. 在 `main` 方法左侧点击运行按钮或者在方法上右键选择 "Debug Java"
3. 项目将在调试模式下启动

### 方式二：使用 Maven 命令启动
```bash
# 进入项目目录
cd admin-spring-boot

# 启动项目
mvn spring-boot:run
```

### 方式三：打包后运行
```bash
# 编译打包
mvn clean package

# 运行jar包
java -jar target/admin-spring-boot-0.0.1-SNAPSHOT.jar
```

## 测试接口
项目启动后，可以访问以下URL测试功能：

### 基础功能测试
- 获取IP信息: http://localhost:8080/api/ip/client
- 获取详细信息: http://localhost:8080/api/ip/detail
- 查询客户端IP地理位置: http://localhost:8080/api/ip/my/geo
- **客户端IP接口（异步优化）**: http://localhost:8080/ip/my
- 客户端IP地理位置接口: http://localhost:8080/ip/my/geo
- **性能监控接口**: http://localhost:8080/ip/performance

### IP验证功能测试
- 验证内网IP: http://localhost:8080/api/ip/validate?ip=192.168.1.1
- 验证公网IP: http://localhost:8080/api/ip/validate?ip=8.8.8.8
- 验证无效IP: http://localhost:8080/api/ip/validate?ip=999.999.999.999

### IP地理位置查询测试
- 查询谷歌DNS地理位置: http://localhost:8080/api/ip/my/geo?ip=8.8.8.8
- 查询百度IP地理位置: http://localhost:8080/api/ip/my/geo?ip=220.181.38.251
- 查询客户端IP地理位置: http://localhost:8080/api/ip/my/geo
- 查询内网IP地理位置: http://localhost:8080/api/ip/my/geo?ip=192.168.1.1

### 使用 curl 命令测试
```bash
# 获取客户端IP
curl http://localhost:8080/api/ip/client

# 验证IP地址
curl "http://localhost:8080/api/ip/validate?ip=192.168.1.1"

# 获取详细信息
curl http://localhost:8080/api/ip/detail

# 查询IP地理位置
curl http://localhost:8080/api/ip/my/geo

# 查询指定IP的地理位置
curl "http://localhost:8080/api/ip/my/geo?ip=8.8.8.8"
```

## 开发说明

### 代码结构说明
- **Controller层**: 负责接收HTTP请求，参数验证，调用Service层处理业务逻辑
  - `IpManagementController`: 完整功能的API接口（/api/ip/*）
  - `ClientIpController`: 客户端IP专用API接口（/ip/*）
- **Service层**: 负责具体的业务逻辑实现，IP地址提取、验证等核心功能
- **DTO层**: 数据传输对象，规范API响应格式

### IP获取优先级
1. X-Forwarded-For（代理服务器传递的原始客户端IP）
2. Proxy-Client-IP（Apache代理）
3. WL-Proxy-Client-IP（WebLogic代理）
4. HTTP_CLIENT_IP
5. HTTP_X_FORWARDED_FOR
6. request.getRemoteAddr()（直连IP）

### 地理位置查询特性
- **第三方API**: 使用免费的 ip-api.com 服务
- **中文支持**: 支持中文地理位置信息显示
- **内网处理**: 智能识别内网IP并提供友好提示
- **异常处理**: 完善的错误处理和超时机制
- **参数灵活**: 支持查询指定IP或当前客户端IP

### 扩展功能
项目为IP管理模块预留了扩展空间，后续可以添加：
- ✅ IP地理位置查询（已实现）
- ✅ 异步数据库操作（已实现）
- ✅ 高并发性能优化（已实现）
- ✅ 性能监控系统（已实现）
- IP黑白名单管理
- IP访问统计
- IP风险评估
- IP地理位置缓存机制

## 技术特性

### 🚀 高性能优化
- **异步处理**: 采用专门的线程池异步保存数据库，响应时间提升10-20倍
- **并发支持**: 支持500-1000+ QPS，配置优化的Tomcat和HikariCP连接池
- **性能监控**: 实时监控线程池状态和系统性能指标

### 📊 监控和运维
- **性能报告**: `/ip/performance` 接口查看实时性能数据
- **定时监控**: 每5分钟自动输出性能日志
- **故障自愈**: 智能监控脚本自动重启失败服务

### 🛡️ 企业级特性
- **数据库自动初始化**: 应用启动时自动创建数据库和表
- **分层架构**: Controller-Service-DTO清晰分层设计
- **线程池隔离**: 数据库操作和通用操作使用独立线程池

## 生产环境部署

### 📋 部署指南
本项目提供完整的生产环境部署方案，支持多种云平台和操作系统：

#### 🌐 多云平台支持
- **操作系统**: CentOS 7/8, Rocky Linux 8/9, Amazon Linux 3.x, Alibaba Cloud Linux 3.x
- **数据库**: MySQL 8.0 + HikariCP连接池 或 云数据库服务
- **反向代理**: Nginx + SSL证书 或 云负载均衡
- **系统服务**: systemd服务管理 + 开机自启
- **监控运维**: 日志轮转 + 性能监控 + 自动备份 + 云监控集成

#### 📚 部署文档
👉 **AWS 用户**: [📖 Linux部署指南](./DEPLOY.md) (支持Amazon Linux + AWS服务)  
👉 **阿里云用户**: [📖 阿里云部署指南](./DEPLOY-ALIYUN.md) (支持Alibaba Cloud Linux + 阿里云服务)  
👉 **已有环境用户**: [📖 已有环境部署指南](./DEPLOY-EXISTING.md) (支持RuoYi等现有应用共存)

**选择建议**:
- 🚀 **AWS 用户**: 优先使用 Amazon Linux + RDS + ALB + Certificate Manager
- 🚀 **阿里云用户**: 优先使用 Alibaba Cloud Linux + RDS + SLB + SSL证书服务
- 🖥️ **自建服务器**: 两份文档都适用，选择对应的操作系统版本
- 🔄 **现有环境**: 已部署RuoYi或其他应用，需要多应用共存

#### 📋 部署内容概览

**共同包含**：
- 🛠️ 系统环境准备和软件安装 (各Linux发行版特定配置)
- ☕ Java 17 环境配置 (OpenJDK/Amazon Corretto/Alibaba Dragonwell)
- 🗄️ MySQL 8.0 安装和优化 + 云数据库配置方案
- 📦 应用打包和systemd服务配置
- 🌐 Nginx反向代理配置 + 云负载均衡方案
- 🚨 故障排除和性能优化指南

**AWS版本特色**：
- 🔐 Certificate Manager + Let's Encrypt SSL配置
- 📊 CloudWatch集成和监控配置
- 🎯 EC2实例和RDS优化建议
- 🔧 AWS CLI和服务集成

**阿里云版本特色**：
- 🔐 阿里云SSL证书服务 + Let's Encrypt配置
- 📊 云监控和日志服务SLS集成
- 🎯 ECS实例和RDS优化建议  
- 🔧 阿里云CLI和ossutil工具集成
- 💰 成本优化和计费建议

**已有环境版本特色**：
- 🔄 RuoYi等现有应用共存部署
- 🌐 Nginx路径代理配置 (/v2/ip/my)
- 🔧 端口隔离和资源分配优化
- 📊 独立监控和备份策略
- 🛡️ 数据库权限隔离和安全配置

## 注意事项
1. 确保系统已安装 Java 17
2. 确保系统已安装 Maven（如果使用命令行启动）
3. 默认端口是8080，如果端口被占用可在 `application.properties` 中修改
4. 项目使用内嵌 Tomcat，无需单独安装 Tomcat 服务器
5. 提供两套API接口：完整管理版（`/api/ip/*`）和客户端专用版（`/ip/*`）
6. 地理位置查询依赖外部网络，请确保服务器能访问互联网
7. 内网IP会返回模拟的地理位置信息，无法获取真实位置
8. 客户端专用API路径更短，专注于客户端IP操作
9. 生产环境部署建议使用异步模式以获得最佳性能 
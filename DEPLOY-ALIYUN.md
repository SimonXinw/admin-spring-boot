# 🚀 Admin Spring Boot 项目阿里云部署指南

## 📋 部署前准备

### 系统要求
- **操作系统**: 
  - **Alibaba Cloud Linux 3.x** (阿里云推荐)
  - CentOS 7/8 或 Anolis OS 8.x
  - Debian 10/11 或 Ubuntu 20.04/22.04
- **内存**: 最少 2GB，推荐 4GB+
- **磁盘空间**: 最少 20GB 可用空间
- **网络**: 需要互联网连接（用于下载软件包和IP地理位置查询）

### 阿里云 ECS 特别说明

#### 📦 ECS 实例配置建议
- **实例规格**: 
  - 入门配置: ecs.t6-c1m2.large (2vCPU, 4GB内存)
  - 标准配置: ecs.c6.large (2vCPU, 4GB内存)
  - 高性能配置: ecs.c6.xlarge (4vCPU, 8GB内存)
- **镜像**: Alibaba Cloud Linux 3.2104 LTS 64位
- **存储**: 40GB 高效云盘或 SSD 云盘
- **网络**: 专有网络VPC，分配公网IP或绑定弹性公网IP

#### 🔒 安全组配置
- **Web服务器安全组**:
  - 80/tcp (HTTP) ← 0.0.0.0/0
  - 443/tcp (HTTPS) ← 0.0.0.0/0
  - 22/tcp (SSH) ← 你的公网IP/32
  - 8080/tcp (应用端口) ← 内部测试用

#### 🗄️ 阿里云数据库 RDS 配置（推荐）
- **数据库引擎**: MySQL 8.0 高可用版
- **实例规格**: 
  - 入门: mysql.n2.medium.1 (2核4GB)
  - 标准: mysql.n4.medium.1 (2核8GB)
- **存储空间**: 40GB SSD云盘，开启自动扩容
- **网络**: 专有网络VPC，内网访问
- **备份设置**: 自动备份7天，开启binlog备份

#### 🌐 其他阿里云服务
- **负载均衡 SLB**: 应用型负载均衡 ALB
- **SSL证书服务**: 免费证书或付费证书
- **云监控**: 自动监控 ECS 和应用性能
- **日志服务 SLS**: 集中日志管理
- **对象存储 OSS**: 静态文件存储（可选）

### 部署架构图
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   阿里云 SLB     │    │   ECS 实例      │    │   阿里云 RDS    │
│   (端口: 80/443) │───▶│  Alibaba Linux  │───▶│   MySQL 8.0     │
│   负载均衡      │    │   (端口: 8080)  │    │    托管数据库   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 🛠️ 第一步：系统环境准备

### 1.1 检查系统版本
```bash
# 检查系统版本
cat /etc/os-release

# Alibaba Cloud Linux 3.x 输出示例：
# NAME="Alibaba Cloud Linux"
# VERSION="3 (Soaring Falcon)"
# ID="alios"
# ANSI_COLOR="0;31"

# 检查内核版本
uname -r

# 检查CPU和内存信息
cat /proc/cpuinfo | grep "model name" | head -1
free -h
```

### 1.2 更新系统包
```bash
# Alibaba Cloud Linux 3.x 使用 dnf 包管理器
sudo dnf update -y

# 安装必要的基础工具
sudo dnf install -y wget curl vim unzip git tar net-tools

# 安装开发工具
sudo dnf install -y gcc gcc-c++ make cmake

# 安装阿里云特有工具
sudo dnf install -y aliyun-cli ecs-utils
```

### 1.3 安装开发工具包
```bash
# 安装开发工具组
sudo dnf groupinstall -y "Development Tools"

# 安装其他必要工具
sudo dnf install -y epel-release
sudo dnf install -y htop iotop lsof
```

### 1.4 配置系统时区和语言
```bash
# 设置时区为中国标准时间
sudo timedatectl set-timezone Asia/Shanghai

# 检查时区设置
timedatectl status

# 设置语言环境（可选）
echo 'LANG=en_US.UTF-8' | sudo tee -a /etc/environment
```

### 1.5 配置防火墙

#### 阿里云安全组配置（推荐方式）
```bash
# 在阿里云控制台配置安全组规则：
# 1. 登录阿里云ECS控制台
# 2. 选择实例 -> 安全组 -> 配置规则
# 3. 添加安全组规则：
#    - 协议类型: TCP, 端口范围: 80/80, 源: 0.0.0.0/0
#    - 协议类型: TCP, 端口范围: 443/443, 源: 0.0.0.0/0  
#    - 协议类型: TCP, 端口范围: 22/22, 源: 你的公网IP
#    - 协议类型: TCP, 端口范围: 8080/8080, 源: 内网测试
```

#### 系统防火墙配置
```bash
# 检查防火墙状态
sudo systemctl status firewalld

# 如果防火墙未安装，先安装
sudo dnf install -y firewalld

# 启动并启用防火墙
sudo systemctl start firewalld
sudo systemctl enable firewalld

# 配置防火墙规则
sudo firewall-cmd --permanent --add-port=80/tcp     # HTTP
sudo firewall-cmd --permanent --add-port=443/tcp    # HTTPS  
sudo firewall-cmd --permanent --add-port=8080/tcp   # 应用端口

# 重新加载防火墙规则
sudo firewall-cmd --reload

# 查看当前规则
sudo firewall-cmd --list-all
```

---

## ☕ 第二步：安装 Java 17

### 2.1 检查并安装 OpenJDK 17

#### Alibaba Cloud Linux 推荐方式
```bash
# 检查可用的 Java 包
sudo dnf search openjdk

# Alibaba Cloud Linux 安装 OpenJDK 17
sudo dnf install -y java-17-openjdk java-17-openjdk-devel

# 或者安装 Alibaba Dragonwell JDK（阿里云优化版本）
sudo dnf install -y java-17-alibaba-dragonwell java-17-alibaba-dragonwell-devel
```

#### 检查安装结果
```bash
# 验证安装
java -version
javac -version

# 查看可用的 Java 版本
sudo alternatives --config java
```

### 2.2 配置 Java 环境变量

#### 自动检测Java路径
```bash
# 查找 Java 安装路径
sudo find /usr -name "java" -type f 2>/dev/null | grep bin

# Alibaba Dragonwell 路径通常是：
# /usr/lib/jvm/java-17-alibaba-dragonwell

# OpenJDK 路径通常是：
# /usr/lib/jvm/java-17-openjdk
```

#### 配置系统环境变量
```bash
# 编辑系统环境变量文件
sudo vim /etc/profile

# 添加以下内容到文件末尾（根据实际安装选择）：

# Alibaba Dragonwell JDK (推荐)
export JAVA_HOME=/usr/lib/jvm/java-17-alibaba-dragonwell
export PATH=$JAVA_HOME/bin:$PATH

# 或者 OpenJDK (使用时请注释掉上面的配置)
# export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
# export PATH=$JAVA_HOME/bin:$PATH

# 通用配置
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

# 使环境变量生效
source /etc/profile

# 验证配置
java -version
echo $JAVA_HOME
```

---

## 🗄️ 第三步：安装和配置 MySQL 8.0

### 阿里云 RDS 推荐方案（生产环境推荐）
> 💡 **阿里云用户建议**: 使用阿里云 RDS MySQL 服务，具有自动备份、高可用、安全防护等优势

#### 创建 RDS 实例
1. **在阿里云控制台创建 RDS 实例**:
```bash
# 推荐配置：
# - 数据库类型: MySQL 8.0 高可用版
# - 实例规格: mysql.n2.medium.1 (2核4GB) 
# - 存储类型: SSD云盘 40GB
# - 网络类型: 专有网络VPC
# - 可用区: 选择与ECS相同或临近的可用区
```

2. **配置数据库白名单**:
```bash
# 在 RDS 控制台设置白名单：
# - 添加 ECS 实例的内网IP
# - 或者添加整个VPC网段（如：172.16.0.0/16）
```

3. **创建数据库账号**:
```sql
-- 在 RDS 控制台创建数据库账号
-- 账号名称: admin_user
-- 密码: Admin123!@#
-- 账号类型: 普通账号
-- 授权数据库: admin（需要先创建数据库）
```

4. **连接测试**:
```bash
# 安装 MySQL 客户端
sudo dnf install -y mysql

# 测试连接 RDS
mysql -h your-rds-instance.mysql.rds.aliyuncs.com -u admin_user -p

# 创建应用数据库
CREATE DATABASE admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 本地 MySQL 安装方案（开发环境）

#### 安装 MySQL 8.0
```bash
# 添加 MySQL 官方仓库
sudo dnf install -y https://dev.mysql.com/get/mysql80-community-release-el8-1.noarch.rpm

# 安装 MySQL 服务器
sudo dnf install -y mysql-community-server mysql-community-devel

# 启动并启用 MySQL 服务
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 检查服务状态
sudo systemctl status mysqld
```

#### 配置 MySQL
```bash
# 获取初始密码
sudo grep 'temporary password' /var/log/mysqld.log

# 运行安全配置脚本
sudo mysql_secure_installation

# 配置过程：
# - 输入临时密码
# - 设置新的 root 密码：Admin123!@#
# - 移除匿名用户：Y
# - 禁止 root 远程登录：n
# - 移除测试数据库：Y
# - 重新加载权限表：Y
```

#### 创建应用数据库和用户
```bash
# 登录 MySQL
mysql -u root -p
```

```sql
-- 创建数据库
CREATE DATABASE admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建应用用户
CREATE USER 'admin_user'@'localhost' IDENTIFIED BY 'Admin123!@#';
CREATE USER 'admin_user'@'%' IDENTIFIED BY 'Admin123!@#';

-- 授权
GRANT ALL PRIVILEGES ON admin.* TO 'admin_user'@'localhost';
GRANT ALL PRIVILEGES ON admin.* TO 'admin_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

#### 优化 MySQL 配置
```bash
# 编辑 MySQL 配置文件
sudo vim /etc/my.cnf

# 添加以下配置：
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default-time-zone='+8:00'
max_connections=200
innodb_buffer_pool_size=1024M
innodb_log_file_size=256M
innodb_flush_log_at_trx_commit=2
innodb_lock_wait_timeout=120

# 重启 MySQL 服务
sudo systemctl restart mysqld
```

---

## 🔧 第四步：安装 Maven（可选）

> 💡 **说明**: 如果只是部署已编译的 jar 文件，可以跳过 Maven 安装

### 4.1 安装 Maven

#### 包管理器安装（推荐）
```bash
# Alibaba Cloud Linux 3.x
sudo dnf install -y maven

# 验证安装
mvn -version
```

#### 手动安装最新版本
```bash
# 下载 Maven 最新版本
cd /opt
sudo wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
sudo tar -xzf apache-maven-3.9.6-bin.tar.gz
sudo mv apache-maven-3.9.6 maven
sudo chown -R root:root /opt/maven

# 配置环境变量
sudo vim /etc/profile

# 添加：
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH

# 生效配置
source /etc/profile
mvn -version
```

### 4.2 配置 Maven 阿里云镜像
```bash
# 编辑 Maven 配置文件
sudo mkdir -p ~/.m2
vim ~/.m2/settings.xml
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <mirrors>
    <mirror>
      <id>aliyun-central</id>
      <name>阿里云maven中央仓库</name>
      <url>https://maven.aliyun.com/repository/central</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
    <mirror>
      <id>aliyun-public</id>
      <name>阿里云maven公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

---

## 📦 第五步：部署 Spring Boot 应用

### 5.1 创建应用目录和用户
```bash
# 创建应用目录
sudo mkdir -p /opt/admin-spring-boot
sudo mkdir -p /opt/admin-spring-boot/logs
sudo mkdir -p /opt/admin-spring-boot/config

# 创建应用用户
sudo useradd -r -s /bin/false admin-app
sudo chown -R admin-app:admin-app /opt/admin-spring-boot
```

### 5.2 上传并部署应用

#### 方法一：从本地上传（推荐）
```bash
# 在本地开发机器上打包
mvn clean package -DskipTests

# 使用 scp 上传到阿里云 ECS
scp target/admin-spring-boot-0.0.1-SNAPSHOT.jar root@your-ecs-ip:/opt/admin-spring-boot/app.jar

# 或者使用阿里云 OSS 中转
# 1. 上传到 OSS
# 2. 在 ECS 上下载
wget https://your-bucket.oss-cn-hangzhou.aliyuncs.com/app.jar -O /opt/admin-spring-boot/app.jar
```

#### 方法二：在服务器上编译
```bash
# 克隆代码（如果代码在 Git 仓库）
cd /opt
sudo git clone https://github.com/your-repo/admin-spring-boot.git source
cd source

# 编译打包
sudo mvn clean package -DskipTests

# 复制 jar 文件
sudo cp target/admin-spring-boot-0.0.1-SNAPSHOT.jar /opt/admin-spring-boot/app.jar
sudo chown admin-app:admin-app /opt/admin-spring-boot/app.jar
```

### 5.3 配置应用
```bash
# 创建生产环境配置文件
sudo vim /opt/admin-spring-boot/config/application-prod.properties
```

```properties
# 生产环境配置
server.port=8080

# 应用名称
spring.application.name=admin-spring-boot

# 生产环境配置
spring.profiles.active=prod

# 日志配置
logging.level.com.haigaote.admin=INFO
logging.level.org.springframework.web=WARN
logging.file.path=/opt/admin-spring-boot/logs
logging.file.name=admin-spring-boot.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# 数据库配置
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# 阿里云 RDS 配置（推荐）
spring.datasource.url=jdbc:mysql://your-rds-instance.mysql.rds.aliyuncs.com:3306/admin?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai&requireSSL=false
spring.datasource.username=admin_user
spring.datasource.password=Admin123!@#

# 本地 MySQL 配置（如果不使用 RDS，请注释掉上面的配置）
# spring.datasource.url=jdbc:mysql://localhost:3306/admin?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
# spring.datasource.username=admin_user
# spring.datasource.password=Admin123!@#

# 性能优化配置
server.tomcat.threads.max=100
server.tomcat.threads.min-spare=10
server.tomcat.max-connections=2048
server.tomcat.accept-count=50
server.tomcat.connection-timeout=20000

# 数据库连接池配置
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=15
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.pool-name=HikariCP-Prod
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.validation-timeout=5000
```

### 5.4 创建系统服务
```bash
# 创建 systemd 服务文件
sudo vim /etc/systemd/system/admin-spring-boot.service
```

```ini
[Unit]
Description=Admin Spring Boot Application
After=network.target

[Service]
Type=simple
User=admin-app
ExecStart=/usr/lib/jvm/java-17-alibaba-dragonwell/bin/java -Xms1024m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Dspring.profiles.active=prod -Dspring.config.location=classpath:/application.properties,/opt/admin-spring-boot/config/application-prod.properties -jar /opt/admin-spring-boot/app.jar
ExecStop=/bin/kill -15 $MAINPID
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=admin-spring-boot
KillMode=mixed
KillSignal=SIGTERM

[Install]
WantedBy=multi-user.target
```

### 5.5 启动应用服务
```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start admin-spring-boot

# 设置开机自启
sudo systemctl enable admin-spring-boot

# 检查服务状态
sudo systemctl status admin-spring-boot

# 查看日志
sudo journalctl -u admin-spring-boot -f

# 检查端口监听
sudo ss -tlnp | grep 8080
```

---

## 🌐 第六步：安装和配置 Nginx

### 6.1 安装 Nginx

```bash
# Alibaba Cloud Linux 安装 Nginx
sudo dnf install -y nginx

# 启动并启用 Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# 检查服务状态
sudo systemctl status nginx

# 验证安装
nginx -v
curl -I http://localhost
```

### 6.2 配置 Nginx 反向代理
```bash
# 备份默认配置
sudo cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.backup

# 创建应用配置文件
sudo vim /etc/nginx/conf.d/admin-spring-boot.conf
```

```nginx
# Admin Spring Boot 应用配置
upstream admin_backend {
    server 127.0.0.1:8080 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name your-domain.com www.your-domain.com;  # 替换为你的域名
    
    # 日志配置
    access_log /var/log/nginx/admin_access.log;
    error_log /var/log/nginx/admin_error.log;
    
    # 安全头部
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    
    # 客户端最大请求体大小
    client_max_body_size 10M;
    
    # 压缩配置
    gzip on;
    gzip_min_length 1000;
    gzip_types text/plain application/json application/javascript text/css application/xml;
    
    # API 接口代理
    location / {
        proxy_pass http://admin_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        
        # 缓冲配置
        proxy_buffering on;
        proxy_buffer_size 8k;
        proxy_buffers 16 8k;
        
        # 健康检查
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503;
        proxy_next_upstream_tries 3;
    }
    
    # 健康检查接口
    location /actuator/health {
        proxy_pass http://admin_backend/actuator/health;
        access_log off;
    }
    
    # 静态文件缓存
    location ~* \.(css|js|jpg|jpeg|png|gif|ico|svg|woff|woff2|ttf|eot)$ {
        proxy_pass http://admin_backend;
        expires 1y;
        add_header Cache-Control "public, immutable";
        add_header Vary Accept-Encoding;
    }
    
    # 禁止访问敏感文件
    location ~* \.(htaccess|htpasswd|ini|log|sh|sql|conf)$ {
        deny all;
    }
}
```

### 6.3 测试和启动 Nginx
```bash
# 测试 Nginx 配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx

# 检查 Nginx 状态
sudo systemctl status nginx

# 测试反向代理
curl -I http://localhost
curl http://localhost/ip/my
```

---

## ✅ 第七步：验证部署

### 7.1 检查所有服务状态
```bash
# 检查各服务状态
sudo systemctl status admin-spring-boot
sudo systemctl status nginx

# 检查端口监听
sudo ss -tlnp | grep -E ':(80|8080)'

# 检查进程
ps aux | grep -E "(java|nginx)" | grep -v grep
```

### 7.2 测试应用接口
```bash
# 测试应用直接接口
curl http://localhost:8080/ip/my

# 测试通过 Nginx 的接口
curl http://localhost/ip/my

# 测试性能监控接口
curl http://localhost/ip/performance

# 测试从外部访问（替换为你的ECS公网IP）
curl http://your-ecs-public-ip/ip/my
```

### 7.3 检查日志
```bash
# 查看应用日志
sudo tail -f /opt/admin-spring-boot/logs/admin-spring-boot.log

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/admin_access.log
sudo tail -f /var/log/nginx/admin_error.log

# 查看系统服务日志
sudo journalctl -u admin-spring-boot -n 50
sudo journalctl -u nginx -n 20
```

---

## 🔐 第八步：安全加固

### 8.1 配置 SSL 证书

#### 阿里云 SSL 证书服务（推荐方式）
```bash
# 1. 在阿里云控制台申请 SSL 证书
#    - 免费证书（单域名）或付费证书（多域名/通配符）
#    - 完成域名验证
#    - 下载证书文件

# 2. 上传证书到服务器
sudo mkdir -p /etc/nginx/ssl
# 上传 your-domain.pem 和 your-domain.key 到 /etc/nginx/ssl/

# 3. 配置 Nginx SSL
sudo vim /etc/nginx/conf.d/admin-spring-boot-ssl.conf
```

```nginx
# HTTPS 配置
server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;
    
    # SSL 证书配置
    ssl_certificate /etc/nginx/ssl/your-domain.pem;
    ssl_certificate_key /etc/nginx/ssl/your-domain.key;
    
    # SSL 安全配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # 安全头部
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    
    # 其他配置与 HTTP 相同...
    location / {
        proxy_pass http://admin_backend;
        # ... 其他代理配置
    }
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

#### Let's Encrypt 免费证书
```bash
# 安装 Certbot
sudo dnf install -y certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# 设置自动续期
sudo crontab -e
# 添加：
0 12 * * * /usr/bin/certbot renew --quiet --no-self-upgrade
```

### 8.2 系统安全加固
```bash
# 1. 更新系统安全补丁
sudo dnf update -y

# 2. 配置 fail2ban 防止暴力破解
sudo dnf install -y fail2ban
sudo systemctl start fail2ban
sudo systemctl enable fail2ban

# 3. 限制 SSH 访问
sudo vim /etc/ssh/sshd_config
# 修改：
# Port 22 -> Port 2222 (可选，修改默认端口)
# PermitRootLogin no
# PasswordAuthentication no (如果使用密钥登录)

sudo systemctl restart sshd

# 4. 配置系统审计
sudo dnf install -y audit
sudo systemctl start auditd
sudo systemctl enable auditd
```

### 8.3 应用安全配置
```bash
# 1. 创建应用防火墙规则
sudo firewall-cmd --permanent --remove-port=8080/tcp  # 关闭应用端口外部访问
sudo firewall-cmd --reload

# 2. 配置 Nginx 访问限制
sudo vim /etc/nginx/conf.d/security.conf
```

```nginx
# 限制请求频率
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

# 在 server 块中添加
location / {
    limit_req zone=api burst=20 nodelay;
    # ... 其他配置
}

# 禁止特定 User-Agent
if ($http_user_agent ~* (bot|crawler|spider|scraper)) {
    return 403;
}
```

---

## 📊 第九步：监控和运维

### 9.1 配置阿里云监控

#### 安装云监控代理
```bash
# 下载并安装云监控代理
wget http://cms-download.aliyun.com/release/1.3.7/CmsGoAgent.linux-amd64.tar.gz
tar -xzf CmsGoAgent.linux-amd64.tar.gz
cd CmsGoAgent-linux-amd64
sudo ./cms_go_agent_install.sh

# 启动代理
sudo systemctl start CmsGoAgent
sudo systemctl enable CmsGoAgent
```

#### 配置自定义监控指标
```bash
# 创建监控脚本
sudo vim /opt/admin-spring-boot/monitor-metrics.sh
```

```bash
#!/bin/bash
# 应用性能监控脚本

APP_PORT=8080
LOG_FILE="/opt/admin-spring-boot/logs/metrics.log"

# 检查应用是否运行
if curl -s http://localhost:$APP_PORT/actuator/health >/dev/null; then
    echo "$(date): Application is running" >> $LOG_FILE
    # 发送自定义指标到云监控
    /usr/local/bin/CmsGoAgent/cms_go_agent --metricName "app_status" --value 1
else
    echo "$(date): Application is down" >> $LOG_FILE
    /usr/local/bin/CmsGoAgent/cms_go_agent --metricName "app_status" --value 0
fi

# 获取应用内存使用情况
MEMORY_USAGE=$(ps aux | grep "admin-spring-boot" | grep -v grep | awk '{print $4}')
if [ ! -z "$MEMORY_USAGE" ]; then
    /usr/local/bin/CmsGoAgent/cms_go_agent --metricName "app_memory_usage" --value $MEMORY_USAGE
fi
```

```bash
# 设置执行权限
sudo chmod +x /opt/admin-spring-boot/monitor-metrics.sh

# 添加到定时任务
sudo crontab -e
# 添加：
*/5 * * * * /opt/admin-spring-boot/monitor-metrics.sh
```

### 9.2 配置日志服务 SLS

#### 安装 Logtail 客户端
```bash
# 下载并安装 Logtail
wget http://logtail-release-cn-hangzhou.oss-cn-hangzhou.aliyuncs.com/linux64/logtail.sh
sudo sh logtail.sh install cn-hangzhou  # 替换为你的地域

# 配置机器组和采集配置需要在阿里云 SLS 控制台完成
```

### 9.3 设置自动化运维脚本
```bash
# 创建健康检查和自动恢复脚本
sudo vim /opt/admin-spring-boot/health-check.sh
```

```bash
#!/bin/bash
# 应用健康检查和自动恢复脚本

APP_NAME="admin-spring-boot"
APP_PORT=8080
LOG_DIR="/opt/admin-spring-boot/logs"
DATE=$(date '+%Y-%m-%d %H:%M:%S')

# 检查应用端口是否响应
if ! curl -f -s http://localhost:$APP_PORT/actuator/health >/dev/null; then
    echo "[$DATE] Application health check failed, attempting restart..." >> $LOG_DIR/health-check.log
    
    # 尝试重启应用
    systemctl restart $APP_NAME
    sleep 30
    
    # 再次检查
    if curl -f -s http://localhost:$APP_PORT/actuator/health >/dev/null; then
        echo "[$DATE] Application restart successful" >> $LOG_DIR/health-check.log
        # 发送成功通知（可以集成钉钉、企业微信等）
    else
        echo "[$DATE] Application restart failed, requires manual intervention" >> $LOG_DIR/health-check.log
        # 发送告警通知
    fi
fi

# 检查磁盘空间
DISK_USAGE=$(df /opt | tail -1 | awk '{print $5}' | sed 's/%//')
if [ $DISK_USAGE -gt 80 ]; then
    echo "[$DATE] Disk usage high: $DISK_USAGE%" >> $LOG_DIR/health-check.log
    # 清理旧日志
    find $LOG_DIR -name "*.log" -mtime +7 -delete
fi

# 检查内存使用
MEMORY_USAGE=$(free | grep Mem | awk '{printf("%.2f", $3/$2 * 100.0)}')
if (( $(echo "$MEMORY_USAGE > 85" | bc -l) )); then
    echo "[$DATE] Memory usage high: $MEMORY_USAGE%" >> $LOG_DIR/health-check.log
fi
```

```bash
# 设置执行权限
sudo chmod +x /opt/admin-spring-boot/health-check.sh

# 添加到定时任务（每5分钟检查一次）
sudo crontab -e
# 添加：
*/5 * * * * /opt/admin-spring-boot/health-check.sh
```

---

## 📈 第十步：性能优化

### 10.1 JVM 优化

#### 根据阿里云 ECS 规格调整
```bash
# 编辑服务文件
sudo vim /etc/systemd/system/admin-spring-boot.service

# 根据 ECS 实例规格调整 JVM 参数：

# ecs.t6-c1m2.large (2核4GB)
ExecStart=/usr/lib/jvm/java-17-alibaba-dragonwell/bin/java -Xms1024m -Xmx2048m ...

# ecs.c6.large (2核4GB)  
ExecStart=/usr/lib/jvm/java-17-alibaba-dragonwell/bin/java -Xms1024m -Xmx2560m ...

# ecs.c6.xlarge (4核8GB)
ExecStart=/usr/lib/jvm/java-17-alibaba-dragonwell/bin/java -Xms2048m -Xmx6144m ...

# 添加 GC 优化参数
-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication
```

### 10.2 数据库优化

#### 阿里云 RDS 优化
```bash
# RDS 参数优化建议（在 RDS 控制台设置）：
# innodb_buffer_pool_size = 70%的实例内存
# innodb_log_file_size = 256M
# max_connections = 200
# slow_query_log = ON
# long_query_time = 2
```

#### 本地 MySQL 优化
```bash
# 编辑 MySQL 配置
sudo vim /etc/my.cnf

# 优化配置
[mysqld]
# 根据内存大小调整
innodb_buffer_pool_size=2G     # 60-70% 系统内存
innodb_log_file_size=256M
innodb_flush_log_at_trx_commit=2
innodb_flush_method=O_DIRECT

# 连接优化
max_connections=200
wait_timeout=28800
interactive_timeout=28800

# 查询缓存
query_cache_type=1
query_cache_size=64M

# 重启 MySQL
sudo systemctl restart mysqld
```

### 10.3 Nginx 优化
```bash
# 编辑 Nginx 主配置
sudo vim /etc/nginx/nginx.conf

# 优化配置
user nginx;
worker_processes auto;
worker_connections 4096;
worker_rlimit_nofile 65535;

events {
    use epoll;
    worker_connections 4096;
    multi_accept on;
}

http {
    # 开启 gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    
    # 缓冲区优化
    client_body_buffer_size 128k;
    client_max_body_size 10m;
    client_header_buffer_size 3m;
    large_client_header_buffers 4 256k;
    
    # 超时优化
    client_header_timeout 3m;
    client_body_timeout 3m;
    send_timeout 3m;
    
    # 开启文件缓存
    open_file_cache max=65535 inactive=60s;
    open_file_cache_valid 80s;
    open_file_cache_min_uses 1;
}
```

### 10.4 阿里云特定优化

#### ECS 实例优化
```bash
# 1. 开启 ECS 实例性能模式（在控制台设置）

# 2. 检查网络优化
# 查看网卡队列数
cat /proc/interrupts | grep eth0

# 优化网络参数
echo 'net.core.rmem_max = 134217728' | sudo tee -a /etc/sysctl.conf
echo 'net.core.wmem_max = 134217728' | sudo tee -a /etc/sysctl.conf
echo 'net.ipv4.tcp_rmem = 4096 87380 134217728' | sudo tee -a /etc/sysctl.conf
echo 'net.ipv4.tcp_wmem = 4096 65536 134217728' | sudo tee -a /etc/sysctl.conf
sudo sysctl -p

# 3. 磁盘 I/O 优化
# 检查磁盘调度器
cat /sys/block/vda/queue/scheduler

# 设置为 deadline 调度器（对 SSD 友好）
echo deadline | sudo tee /sys/block/vda/queue/scheduler
```

#### 负载均衡 SLB 配置
如果使用阿里云 SLB：
```bash
# 1. 在阿里云控制台创建应用型负载均衡 ALB
# 2. 配置监听器：
#    - 443 端口（HTTPS）-> 后端 80 端口
#    - 80 端口（HTTP）-> 重定向到 443
# 3. 配置健康检查：
#    - 检查路径: /actuator/health
#    - 检查端口: 8080
#    - 健康检查间隔: 5秒
```

---

## 🔄 第十一步：备份和恢复

### 11.1 阿里云自动备份

#### RDS 自动备份
```bash
# RDS 备份在控制台配置：
# 1. 数据备份保留 7-30 天
# 2. 日志备份保留 7 天
# 3. 开启 binlog 备份
# 4. 设置备份时间窗口（业务低峰期）
```

#### ECS 快照备份
```bash
# 1. 在 ECS 控制台创建快照策略
# 2. 设置自动快照：
#    - 创建时间：每天凌晨 2:00
#    - 保留时间：7 天
#    - 应用到：系统盘和数据盘
```

### 11.2 应用数据备份
```bash
# 创建应用备份脚本
sudo vim /opt/admin-spring-boot/backup.sh
```

```bash
#!/bin/bash
# 应用数据备份脚本

BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)
APP_DIR="/opt/admin-spring-boot"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 1. 备份应用配置和日志
echo "$(date): Starting application backup..."
tar -czf $BACKUP_DIR/app_backup_$DATE.tar.gz \
    $APP_DIR/config \
    $APP_DIR/logs \
    $APP_DIR/app.jar

# 2. 备份数据库（如果使用本地 MySQL）
if systemctl is-active --quiet mysqld; then
    echo "$(date): Starting database backup..."
    mysqldump -u admin_user -p'Admin123!@#' admin | gzip > $BACKUP_DIR/db_backup_$DATE.sql.gz
fi

# 3. 上传到阿里云 OSS（需要先配置 ossutil）
if command -v ossutil >/dev/null 2>&1; then
    echo "$(date): Uploading backup to OSS..."
    ossutil cp $BACKUP_DIR/app_backup_$DATE.tar.gz oss://your-backup-bucket/app-backups/
    if [ -f $BACKUP_DIR/db_backup_$DATE.sql.gz ]; then
        ossutil cp $BACKUP_DIR/db_backup_$DATE.sql.gz oss://your-backup-bucket/db-backups/
    fi
fi

# 4. 清理本地旧备份（保留最近 3 天）
find $BACKUP_DIR -name "app_backup_*.tar.gz" -mtime +3 -delete
find $BACKUP_DIR -name "db_backup_*.sql.gz" -mtime +3 -delete

echo "$(date): Backup completed successfully"
```

#### 配置 OSS 备份
```bash
# 1. 安装 ossutil
wget http://gosspublic.alicdn.com/ossutil/1.7.14/ossutil64
sudo chmod +x ossutil64
sudo mv ossutil64 /usr/local/bin/ossutil

# 2. 配置 OSS
ossutil config
# 输入 AccessKey ID、AccessKey Secret、Endpoint

# 3. 设置定时备份
sudo crontab -e
# 添加：
0 2 * * * /opt/admin-spring-boot/backup.sh >> /opt/admin-spring-boot/logs/backup.log 2>&1
```

### 11.3 恢复流程
```bash
# 创建恢复脚本
sudo vim /opt/admin-spring-boot/restore.sh
```

```bash
#!/bin/bash
# 应用恢复脚本

BACKUP_DIR="/opt/backups"
RESTORE_DATE=$1

if [ -z "$RESTORE_DATE" ]; then
    echo "Usage: $0 <backup_date>"
    echo "Example: $0 20241201_020000"
    exit 1
fi

echo "Starting restore from backup: $RESTORE_DATE"

# 1. 停止应用
systemctl stop admin-spring-boot

# 2. 从 OSS 下载备份（如果需要）
if command -v ossutil >/dev/null 2>&1; then
    ossutil cp oss://your-backup-bucket/app-backups/app_backup_$RESTORE_DATE.tar.gz $BACKUP_DIR/
fi

# 3. 恢复应用文件
if [ -f $BACKUP_DIR/app_backup_$RESTORE_DATE.tar.gz ]; then
    echo "Restoring application files..."
    cd /
    tar -xzf $BACKUP_DIR/app_backup_$RESTORE_DATE.tar.gz
fi

# 4. 恢复数据库（如果需要）
if [ -f $BACKUP_DIR/db_backup_$RESTORE_DATE.sql.gz ]; then
    echo "Restoring database..."
    gunzip -c $BACKUP_DIR/db_backup_$RESTORE_DATE.sql.gz | mysql -u admin_user -p'Admin123!@#' admin
fi

# 5. 重启应用
systemctl start admin-spring-boot

echo "Restore completed"
```

---

## 🚨 第十二步：故障排除

### 常见问题和解决方案

#### 1. 应用启动失败
```bash
# 检查详细日志
sudo journalctl -u admin-spring-boot -n 100

# 检查 Java 环境
java -version
echo $JAVA_HOME

# 检查端口占用
sudo ss -tlnp | grep 8080

# 检查配置文件语法
cat /opt/admin-spring-boot/config/application-prod.properties
```

#### 2. 数据库连接失败
```bash
# 测试本地 MySQL 连接
mysql -u admin_user -p -h localhost admin

# 测试 RDS 连接
mysql -u admin_user -p -h your-rds-instance.mysql.rds.aliyuncs.com admin

# 检查网络连通性
ping your-rds-instance.mysql.rds.aliyuncs.com
telnet your-rds-instance.mysql.rds.aliyuncs.com 3306

# 检查安全组和白名单设置
```

#### 3. Nginx 502/504 错误
```bash
# 检查 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log

# 检查后端应用状态
curl http://localhost:8080/ip/my

# 检查 Nginx 配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

#### 4. 性能问题
```bash
# 系统资源监控
htop
iotop
free -h
df -h

# 查看应用性能
curl http://localhost:8080/ip/performance

# 检查数据库性能（MySQL）
mysql -u admin_user -p -e "SHOW PROCESSLIST;"
mysql -u admin_user -p -e "SHOW ENGINE INNODB STATUS\G"
```

#### 5. 阿里云特定问题
```bash
# 检查云监控代理状态
sudo systemctl status CmsGoAgent

# 检查安全组配置
# 在阿里云控制台查看 ECS 安全组规则

# 检查 RDS 白名单
# 在 RDS 控制台查看数据安全白名单

# 查看 SLB 健康检查状态
# 在 SLB 控制台查看后端服务器健康状态
```

---

## 📝 部署检查清单

### 基础部署清单
- [ ] 系统更新和基础工具安装完成
- [ ] Java 17 (Alibaba Dragonwell) 安装并配置
- [ ] 阿里云 RDS MySQL 实例创建并配置（或本地 MySQL）
- [ ] 数据库和用户创建
- [ ] Maven 安装和阿里云镜像配置（可选）
- [ ] 应用打包并上传到服务器
- [ ] systemd 服务配置
- [ ] Nginx 安装并配置反向代理
- [ ] 防火墙和安全组配置
- [ ] SSL 证书配置（阿里云证书服务或 Let's Encrypt）
- [ ] 应用接口测试通过

### 阿里云特定检查清单
- [ ] ECS 实例规格和配置正确
- [ ] 安全组规则配置正确
- [ ] 云盘类型和大小合适
- [ ] 弹性公网IP分配（如需要）
- [ ] RDS 实例配置和连接测试
- [ ] RDS 白名单仅允许 ECS 访问
- [ ] 云监控代理安装和配置
- [ ] RAM 角色和权限配置（如使用阿里云服务）
- [ ] SLB 负载均衡配置（如使用）
- [ ] SSL 证书服务配置
- [ ] OSS 对象存储配置（备份用）
- [ ] 日志服务 SLS 配置（可选）

### 性能和安全检查
- [ ] JVM 内存参数根据 ECS 规格调整
- [ ] 数据库配置根据实例内存优化
- [ ] Nginx gzip 压缩和缓存配置
- [ ] 应用日志轮转配置
- [ ] 云监控和自定义监控配置
- [ ] 自动备份策略配置
- [ ] SSL/TLS 安全配置
- [ ] 防火墙规则最小化原则
- [ ] 数据库连接使用非 root 用户
- [ ] 应用运行使用非特权用户
- [ ] fail2ban 防暴力破解配置

### 运维检查
- [ ] 服务自启动配置
- [ ] 健康检查脚本配置
- [ ] 自动化运维脚本配置
- [ ] 备份和恢复流程测试
- [ ] 监控告警配置
- [ ] 日志文件位置和权限正确
- [ ] 性能基准测试完成
- [ ] 故障切换计划制定
- [ ] 文档更新完成

**🎉 恭喜！您的 Admin Spring Boot 应用已成功部署到阿里云 Alibaba Linux 系统！**

---

## 🔗 相关资源

### 阿里云文档
- [ECS 用户指南](https://help.aliyun.com/product/25365.html)
- [RDS MySQL 用户指南](https://help.aliyun.com/product/26090.html)
- [负载均衡 SLB 用户指南](https://help.aliyun.com/product/27537.html)
- [SSL证书服务](https://help.aliyun.com/product/28533.html)
- [云监控用户指南](https://help.aliyun.com/product/28572.html)
- [日志服务 SLS](https://help.aliyun.com/product/28958.html)

### 技术文档
- [Alibaba Cloud Linux 文档](https://help.aliyun.com/document_detail/111881.html)
- [Alibaba Dragonwell JDK](https://dragonwell-jdk.io/)
- [Spring Boot 部署指南](https://spring.io/guides/gs/spring-boot/)
- [Nginx 官方文档](https://nginx.org/en/docs/)

## 📞 技术支持

如果在部署过程中遇到阿里云相关问题：

1. **查看服务状态**: 检查阿里云服务状态页面
2. **查看监控指标**: 在云监控控制台查看详细指标
3. **查看操作日志**: 在操作审计中查看 API 调用记录
4. **提交工单**: 通过阿里云控制台提交技术支持工单
5. **社区支持**: 访问阿里云开发者社区获取帮助

## 💰 成本优化建议

### 计费优化
- 使用包年包月获得更优惠的价格
- 合理选择实例规格，避免资源浪费
- 使用抢占式实例降低开发测试成本
- 设置监控告警避免意外费用

### 资源优化
- 定期清理不必要的云盘快照
- 合理设置日志保留时间
- 使用生命周期管理优化 OSS 存储成本
- 根据业务量调整 RDS 实例规格

---

**🌟 感谢使用本部署指南！祝您的应用在阿里云上运行愉快！** 
# 🚀 Admin Spring Boot 项目 Linux 部署指南

## 📋 部署前准备

### 系统要求
- **操作系统**: 
  - CentOS 7/8 或 Rocky Linux 8/9
  - **Amazon Linux 3.x** (AWS推荐)
  - Red Hat Enterprise Linux 8/9
- **内存**: 最少 2GB，推荐 4GB+
- **磁盘空间**: 最少 10GB 可用空间
- **网络**: 需要互联网连接（用于下载软件包和IP地理位置查询）

### AWS 特别说明
如果使用 AWS EC2 实例，建议配置：

#### 📦 EC2 实例配置
- **实例类型**: 
  - 开发环境: t3.micro (免费套餐，1vCPU, 1GB内存)
  - 生产环境: t3.small 或以上（2vCPU, 2GB内存）
- **AMI**: Amazon Linux 3.x (ami-xxx) 或 CentOS Stream 9
- **存储**: 至少 20GB gp3 EBS 卷（推荐 30GB 以便升级）
- **网络**: 放置在私有子网，通过 NAT 网关访问互联网

#### 🔒 安全组配置
- **Web服务器安全组**:
  - 80/tcp (HTTP) ← 0.0.0.0/0
  - 443/tcp (HTTPS) ← 0.0.0.0/0
  - 22/tcp (SSH) ← 你的IP地址
  - 8080/tcp (应用) ← 内部测试用

#### 🗄️ RDS 配置（推荐）
- **数据库引擎**: MySQL 8.0
- **实例类型**: db.t3.micro (免费套餐) 或 db.t3.small
- **存储**: 20GB gp2，启用自动扩展
- **备份**: 自动备份 7 天，启用删除保护
- **安全组**: 仅允许 EC2 安全组访问 3306 端口

#### 🌐 其他 AWS 服务
- **弹性IP**: 为 EC2 实例分配固定公网IP
- **Route 53**: 配置域名解析（可选）
- **Certificate Manager**: 免费 SSL 证书
- **CloudWatch**: 监控和日志管理
- **Systems Manager**: 远程管理和参数存储

### 部署架构图
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Nginx       │    │   Spring Boot   │    │     MySQL       │
│   (端口: 80)    │───▶│   (端口: 8080)  │───▶│   (端口: 3306)  │
│   反向代理      │    │    应用服务     │    │    数据库       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 🛠️ 第一步：系统环境准备

### 1.1 检查系统版本
```bash
# 检查系统版本
cat /etc/os-release

# Amazon Linux 3.x 输出示例：
# NAME="Amazon Linux"
# VERSION="3.x"
# ID="amzn"
```

### 1.2 更新系统包
```bash
# Amazon Linux 3.x / CentOS 8+ / Rocky Linux
sudo dnf update -y

# CentOS 7 (如果使用老版本)
# sudo yum update -y

# 安装必要的工具
sudo dnf install -y wget curl vim unzip git tar

# Amazon Linux 特有：安装额外开发工具
sudo dnf install -y gcc gcc-c++ make
```

### 1.3 安装开发工具包
```bash
# Amazon Linux 3.x
sudo dnf groupinstall -y "Development Tools"

# 或者安装具体的开发包
sudo dnf install -y @development-tools

# CentOS 7 (如果使用)
# sudo yum groupinstall -y "Development Tools"
```

### 1.4 配置防火墙

#### AWS 安全组配置（推荐方式）
```bash
# 在 AWS 控制台配置安全组，开放以下端口：
# - 80 (HTTP) - 0.0.0.0/0
# - 443 (HTTPS) - 0.0.0.0/0  
# - 22 (SSH) - 你的IP地址
# - 8080 (应用端口) - 仅用于测试，生产环境可关闭
```

#### 服务器内部防火墙配置
```bash
# 检查是否安装了 firewalld
sudo systemctl status firewalld

# 如果未安装，先安装
sudo dnf install -y firewalld

# 启动并启用防火墙
sudo systemctl start firewalld
sudo systemctl enable firewalld

# 开放必要端口
sudo firewall-cmd --permanent --add-port=80/tcp    # Nginx
sudo firewall-cmd --permanent --add-port=8080/tcp  # Spring Boot (测试用)
sudo firewall-cmd --permanent --add-port=3306/tcp  # MySQL (可选，建议仅内网)

# 重新加载防火墙规则
sudo firewall-cmd --reload

# 查看开放的端口
sudo firewall-cmd --list-all
```

---

## ☕ 第二步：安装 Java 17

### 2.1 检查并安装 OpenJDK 17

#### Amazon Linux 3.x 特别说明
Amazon Linux 可能预装了 Java，先检查现有版本：

```bash
# 检查是否已安装 Java
java -version

# 检查可用的 Java 版本
sudo dnf list available | grep openjdk

# Amazon Linux 3.x 推荐安装方式
sudo dnf install -y java-17-amazon-corretto-devel

# 或者安装标准 OpenJDK 17
sudo dnf install -y java-17-openjdk java-17-openjdk-devel
```

#### CentOS/Rocky Linux 安装方式
```bash
# CentOS 8+ / Rocky Linux
sudo dnf install -y java-17-openjdk java-17-openjdk-devel

# CentOS 7 (如果使用老版本)
# sudo yum install -y java-17-openjdk java-17-openjdk-devel
```

#### 手动安装方式（如果包管理器版本不合适）
```bash
cd /opt
sudo wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.tar.gz
sudo tar -xzf jdk-17_linux-x64_bin.tar.gz
sudo mv jdk-17.* java-17-oracle
sudo chown -R root:root /opt/java-17-oracle
```

### 2.2 配置 Java 环境变量

#### 自动检测Java路径
```bash
# 查找 Java 安装路径
sudo find /usr -name "java" -type f 2>/dev/null | grep bin

# Amazon Corretto 路径通常是：
# /usr/lib/jvm/java-17-amazon-corretto

# OpenJDK 路径通常是：
# /usr/lib/jvm/java-17-openjdk
```

#### 配置环境变量
```bash
# 编辑系统环境变量文件
sudo vim /etc/profile

# 添加以下内容到文件末尾（根据实际安装路径调整）：

# Amazon Corretto
export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto
export PATH=$JAVA_HOME/bin:$PATH

# 或者 OpenJDK (使用时请注释掉上面的 Corretto 配置)
# export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
# export PATH=$JAVA_HOME/bin:$PATH

# 通用配置
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

# 使环境变量生效
source /etc/profile

# 验证安装
java -version
javac -version

# 检查 JAVA_HOME
echo $JAVA_HOME
```

#### 使用 alternatives 管理 Java 版本（推荐）
```bash
# 配置 Java 替代版本管理
sudo alternatives --config java

# 如果需要安装多个版本，可以这样添加：
# sudo alternatives --install /usr/bin/java java /usr/lib/jvm/java-17-amazon-corretto/bin/java 1
# sudo alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-17-amazon-corretto/bin/javac 1
```

---

## 🗄️ 第三步：安装和配置 MySQL 8.0

### AWS RDS 推荐方案（生产环境推荐）
> 💡 **AWS 用户建议**: 使用 AWS RDS MySQL 服务，具有自动备份、高可用、安全补丁等优势

如果选择 RDS，跳过本地 MySQL 安装，直接配置应用连接到 RDS 实例。

### 本地 MySQL 安装方案

### 3.1 安装 MySQL

#### Amazon Linux 3.x 安装方式
```bash
# 检查可用的 MySQL 包
sudo dnf search mysql

# Amazon Linux 推荐安装 MySQL 8.0
sudo dnf install -y mysql-server mysql-devel

# 或者安装 MariaDB（MySQL 兼容）
# sudo dnf install -y mariadb-server mariadb-devel

# 启动并启用 MySQL 服务
sudo systemctl start mysqld
sudo systemctl enable mysqld
```

#### CentOS/Rocky Linux 安装方式
```bash
# 下载 MySQL 官方仓库配置
# CentOS 8/Rocky Linux 8
sudo dnf install -y https://dev.mysql.com/get/mysql80-community-release-el8-1.noarch.rpm

# CentOS 7 (如果使用老版本)
# sudo wget https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm
# sudo yum localinstall -y mysql80-community-release-el7-3.noarch.rpm

# 安装 MySQL 服务器
sudo dnf install -y mysql-community-server

# 启动 MySQL 服务
sudo systemctl start mysqld
sudo systemctl enable mysqld
```

#### 检查 MySQL 服务状态
```bash
# 检查服务状态
sudo systemctl status mysqld

# 如果服务名不是 mysqld，可能是 mysql
sudo systemctl status mysql
```

### 3.2 配置 MySQL
```bash
# 获取临时密码
sudo grep 'temporary password' /var/log/mysqld.log

# 运行安全配置脚本
sudo mysql_secure_installation

# 配置示例：
# - 输入临时密码
# - 设置新的 root 密码：Admin123!@#
# - 移除匿名用户：Y
# - 禁止 root 远程登录：N（如果需要远程连接选择 N）
# - 移除测试数据库：Y
# - 重新加载权限表：Y
```

### 3.3 创建应用数据库和用户

#### 本地 MySQL 配置
```bash
# 登录 MySQL
mysql -u root -p

# 在 MySQL 命令行中执行：
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

#### AWS RDS 配置
如果使用 AWS RDS，需要：

1. **创建 RDS 实例**:
```bash
# 在 AWS 控制台或使用 CLI 创建 MySQL 8.0 RDS 实例
# 推荐配置：
# - 实例类型: db.t3.micro (免费套餐) 或 db.t3.small
# - 存储: 20GB gp2
# - 多可用区: 生产环境建议启用
# - 公开访问: 否（通过VPC内部访问）
```

2. **配置安全组**:
```bash
# RDS 安全组规则：
# - 类型: MySQL/Aurora (3306)
# - 源: EC2 实例的安全组ID
```

3. **连接测试**:
```bash
# 从 EC2 实例测试连接 RDS
mysql -h your-rds-endpoint.amazonaws.com -u admin -p

# 创建数据库（在 RDS 中执行）
CREATE DATABASE admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3.4 配置 MySQL
```bash
# 编辑 MySQL 配置文件
sudo vim /etc/my.cnf

# 添加以下配置：
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default-time-zone='+8:00'
max_connections=200
innodb_buffer_pool_size=512M

# 重启 MySQL 服务
sudo systemctl restart mysqld
```

---

## 🔧 第四步：安装 Maven（可选）

> 💡 **说明**: 如果只是部署已编译的 jar 文件，可以跳过 Maven 安装

### 4.1 安装 Maven

#### 包管理器安装（推荐）
```bash
# Amazon Linux 3.x / CentOS 8+ / Rocky Linux
sudo dnf install -y maven

# CentOS 7 (如果使用老版本)
# sudo yum install -y maven

# 验证安装
mvn -version
```

#### 手动安装（如果需要特定版本）
```bash
# 下载 Maven
cd /opt
sudo wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
sudo tar -xzf apache-maven-3.9.6-bin.tar.gz
sudo mv apache-maven-3.9.6 maven
sudo chown -R root:root /opt/maven
```

### 4.2 配置 Maven 环境变量（仅手动安装需要）
```bash
# 编辑环境变量文件
sudo vim /etc/profile

# 添加 Maven 配置：
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH

# 使配置生效
source /etc/profile

# 验证安装
mvn -version
```

### 4.3 配置 Maven 镜像（可选，提高下载速度）
```bash
# 创建 Maven 配置目录
sudo mkdir -p /opt/maven/conf

# 编辑 settings.xml
sudo vim /opt/maven/conf/settings.xml
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>aliyun maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

---

## 📦 第五步：部署 Spring Boot 应用

### 5.1 创建应用目录
```bash
# 创建应用目录
sudo mkdir -p /opt/admin-spring-boot
sudo mkdir -p /opt/admin-spring-boot/logs
sudo mkdir -p /opt/admin-spring-boot/config

# 创建应用用户
sudo useradd -r -s /bin/false admin-app
sudo chown -R admin-app:admin-app /opt/admin-spring-boot
```

### 5.2 上传并编译应用
```bash
# 方法一：从开发机器上传代码
# 在开发机器上打包：
mvn clean package -DskipTests

# 上传 jar 文件到服务器
scp target/admin-spring-boot-0.0.1-SNAPSHOT.jar root@your-server:/opt/admin-spring-boot/

# 方法二：直接在服务器上编译
cd /opt
sudo git clone https://github.com/your-repo/admin-spring-boot.git
cd admin-spring-boot
sudo mvn clean package -DskipTests
sudo cp target/admin-spring-boot-0.0.1-SNAPSHOT.jar /opt/admin-spring-boot/app.jar
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

# 本地 MySQL 配置
spring.datasource.url=jdbc:mysql://localhost:3306/admin?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
spring.datasource.username=admin_user
spring.datasource.password=Admin123!@#

# AWS RDS 配置（如果使用 RDS，请注释掉上面的配置，启用下面的配置）
# spring.datasource.url=jdbc:mysql://your-rds-endpoint.amazonaws.com:3306/admin?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=GMT%2B8&requireSSL=true
# spring.datasource.username=admin
# spring.datasource.password=your-rds-password

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
```

### 5.4 创建系统服务
```bash
# 创建 systemd 服务文件
sudo vim /etc/systemd/system/admin-spring-boot.service
```

```ini
[Unit]
Description=Admin Spring Boot Application
After=mysql.service
Wants=mysql.service

[Service]
Type=simple
User=admin-app
ExecStart=/opt/java-17-openjdk/bin/java -Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Dspring.profiles.active=prod -Dspring.config.location=classpath:/application.properties,/opt/admin-spring-boot/config/application-prod.properties -jar /opt/admin-spring-boot/app.jar
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
```

---

## 🌐 第六步：安装和配置 Nginx

### 6.1 安装 Nginx

#### Amazon Linux 3.x 安装方式
```bash
# Amazon Linux 通常已包含 nginx 包
sudo dnf install -y nginx

# 如果包不可用，添加 EPEL 仓库
# sudo dnf install -y epel-release
# sudo dnf install -y nginx

# 启动并启用 Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# 检查服务状态
sudo systemctl status nginx
```

#### CentOS/Rocky Linux 安装方式
```bash
# 安装 EPEL 仓库
sudo dnf install -y epel-release

# 安装 Nginx
sudo dnf install -y nginx

# 启动并启用 Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

#### 验证 Nginx 安装
```bash
# 检查 Nginx 版本
nginx -v

# 测试配置文件语法
sudo nginx -t

# 查看 Nginx 状态
sudo systemctl status nginx

# 检查端口监听
sudo ss -tulpn | grep :80
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
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或服务器IP
    
    # 日志配置
    access_log /var/log/nginx/admin_access.log;
    error_log /var/log/nginx/admin_error.log;
    
    # 客户端最大请求体大小
    client_max_body_size 10M;
    
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
    }
    
    # 健康检查接口
    location /actuator/health {
        proxy_pass http://admin_backend/actuator/health;
        access_log off;
    }
    
    # 静态文件缓存（如果有的话）
    location ~* \.(css|js|jpg|jpeg|png|gif|ico|svg)$ {
        proxy_pass http://admin_backend;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### 6.3 测试和重启 Nginx
```bash
# 测试 Nginx 配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx

# 检查 Nginx 状态
sudo systemctl status nginx
```

---

## ✅ 第七步：验证部署

### 7.1 检查服务状态
```bash
# 检查各服务状态
sudo systemctl status mysql
sudo systemctl status admin-spring-boot
sudo systemctl status nginx

# 检查端口监听
sudo netstat -tlnp | grep -E ':(80|8080|3306)'
```

### 7.2 测试应用接口
```bash
# 测试本地接口
curl http://localhost:8080/ip/my

# 测试通过 Nginx 的接口
curl http://localhost/ip/my

# 测试从外部访问（替换为你的服务器IP）
curl http://your-server-ip/ip/my
```

### 7.3 检查日志
```bash
# 查看应用日志
sudo tail -f /opt/admin-spring-boot/logs/admin-spring-boot.log

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/admin_access.log
sudo tail -f /var/log/nginx/admin_error.log

# 查看系统服务日志
sudo journalctl -u admin-spring-boot -f
```

---

## 🔐 第八步：安全加固

### 8.1 配置 SSL 证书（推荐）

#### AWS Certificate Manager（推荐方式）
```bash
# 1. 在 AWS 控制台申请免费 SSL 证书
# 2. 使用 Application Load Balancer (ALB) 终止 SSL
# 3. ALB 转发 HTTP 流量到 EC2 实例的 80 端口

# ALB 配置示例：
# - 监听器 443 (HTTPS) → 目标组 80 (HTTP)
# - 监听器 80 (HTTP) → 重定向到 443 (HTTPS)
```

#### Let's Encrypt 证书（传统方式）
```bash
# 安装 Certbot
sudo dnf install -y certbot python3-certbot-nginx

# 如果包不可用，使用 snap
# sudo dnf install -y snapd
# sudo snap install --classic certbot

# 获取 SSL 证书（替换为你的域名）
sudo certbot --nginx -d your-domain.com

# 设置自动续期
sudo crontab -e
# 添加以下行：
0 12 * * * /usr/bin/certbot renew --quiet
```

#### 测试 SSL 配置
```bash
# 测试 SSL 证书
openssl s_client -connect your-domain.com:443 -servername your-domain.com

# 检查证书有效期
curl -vI https://your-domain.com
```

### 8.2 限制数据库访问
```bash
# 编辑 MySQL 配置，只允许本地连接
sudo vim /etc/my.cnf

# 添加：
[mysqld]
bind-address=127.0.0.1

# 重启 MySQL
sudo systemctl restart mysqld

# 关闭 MySQL 外部端口（如果不需要远程连接）
sudo firewall-cmd --permanent --remove-port=3306/tcp
sudo firewall-cmd --reload
```

### 8.3 配置日志轮转
```bash
# 创建日志轮转配置
sudo vim /etc/logrotate.d/admin-spring-boot
```

```
/opt/admin-spring-boot/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    copytruncate
}
```

---

## 📊 第九步：监控和维护

### 9.1 设置系统监控
```bash
# 安装 htop 用于系统监控
sudo yum install -y htop

# 监控命令
htop                    # 系统资源监控
sudo iotop             # IO 监控
sudo nethogs           # 网络监控
```

### 9.2 性能优化脚本
```bash
# 创建监控脚本
sudo vim /opt/admin-spring-boot/monitor.sh
```

```bash
#!/bin/bash
# 应用监控脚本

APP_NAME="admin-spring-boot"
LOG_DIR="/opt/admin-spring-boot/logs"
DATE=$(date '+%Y-%m-%d %H:%M:%S')

# 检查应用是否运行
if ! systemctl is-active --quiet $APP_NAME; then
    echo "[$DATE] $APP_NAME is not running, restarting..." >> $LOG_DIR/monitor.log
    systemctl start $APP_NAME
fi

# 检查内存使用情况
MEM_USAGE=$(ps aux | grep "admin-spring-boot" | grep -v grep | awk '{print $4}')
if (( $(echo "$MEM_USAGE > 80" | bc -l) )); then
    echo "[$DATE] High memory usage: $MEM_USAGE%" >> $LOG_DIR/monitor.log
fi

# 检查磁盘空间
DISK_USAGE=$(df /opt | tail -1 | awk '{print $5}' | sed 's/%//')
if [ $DISK_USAGE -gt 80 ]; then
    echo "[$DATE] High disk usage: $DISK_USAGE%" >> $LOG_DIR/monitor.log
fi
```

```bash
# 设置可执行权限
sudo chmod +x /opt/admin-spring-boot/monitor.sh

# 添加到定时任务
sudo crontab -e
# 添加：
*/5 * * * * /opt/admin-spring-boot/monitor.sh
```

---

## 🚨 故障排除

### 常见问题和解决方案

#### 1. 应用启动失败
```bash
# 检查日志
sudo journalctl -u admin-spring-boot -n 50

# 检查配置文件
sudo vim /opt/admin-spring-boot/config/application-prod.properties

# 检查 Java 版本
java -version
```

#### 2. 数据库连接失败
```bash
# 测试数据库连接
mysql -u admin_user -p -h localhost admin

# 检查 MySQL 状态
sudo systemctl status mysqld

# 检查防火墙
sudo firewall-cmd --list-all
```

#### 3. Nginx 502 错误
```bash
# 检查后端服务
curl http://localhost:8080/ip/my

# 检查 Nginx 配置
sudo nginx -t

# 查看 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log
```

#### 4. 性能问题
```bash
# 查看系统资源
htop
free -h
df -h

# 查看应用日志中的性能信息
sudo grep "性能监控" /opt/admin-spring-boot/logs/admin-spring-boot.log
```

---

## 📈 性能优化建议

### 1. JVM 优化
```bash
# 编辑服务文件，调整 JVM 参数
sudo vim /etc/systemd/system/admin-spring-boot.service

# 根据 AWS 实例类型调整：
# t3.small (2GB): -Xms512m -Xmx1024m
# t3.medium (4GB): -Xms1024m -Xmx2048m
# t3.large (8GB): -Xms2048m -Xmx4096m
```

### 2. MySQL 优化
```bash
# 本地 MySQL 优化
sudo vim /etc/my.cnf

# 根据实例内存调整：
innodb_buffer_pool_size=512M  # t3.small
innodb_buffer_pool_size=1G    # t3.medium
innodb_buffer_pool_size=2G    # t3.large
```

### 3. Nginx 优化
```bash
# 编辑 Nginx 主配置
sudo vim /etc/nginx/nginx.conf

# 优化工作进程数
worker_processes auto;
worker_connections 2048;

# 启用 gzip 压缩
gzip on;
gzip_types text/plain application/json application/javascript text/css;
```

### 4. AWS 特定优化

#### EBS 存储优化
```bash
# 检查 EBS 优化是否启用
curl -s http://169.254.169.254/latest/meta-data/instance-type

# 对于支持 EBS 优化的实例类型，确保在启动时启用
# 在 AWS 控制台或 CLI 中配置
```

#### CloudWatch 监控
```bash
# 安装 CloudWatch 代理（Amazon Linux 3.x）
sudo dnf install -y amazon-cloudwatch-agent

# 配置 CloudWatch 代理
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-config-wizard

# 启动代理
sudo systemctl start amazon-cloudwatch-agent
sudo systemctl enable amazon-cloudwatch-agent
```

#### 网络优化
```bash
# 启用增强网络（SR-IOV）
# 需要在实例启动时配置，或者使用支持的实例类型

# 检查增强网络状态
modinfo ixgbevf
```

---

## 🔄 备份和恢复

### 1. 数据库备份
```bash
# 创建备份脚本
sudo vim /opt/admin-spring-boot/backup.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u admin_user -p'Admin123!@#' admin > $BACKUP_DIR/admin_db_$DATE.sql

# 保留最近30天的备份
find $BACKUP_DIR -name "admin_db_*.sql" -mtime +30 -delete

echo "Backup completed: admin_db_$DATE.sql"
```

```bash
# 设置定时备份（每天凌晨2点）
sudo crontab -e
0 2 * * * /opt/admin-spring-boot/backup.sh
```

### 2. 应用备份
```bash
# 备份应用配置和日志
tar -czf /opt/backups/app_config_$(date +%Y%m%d).tar.gz \
    /opt/admin-spring-boot/config \
    /opt/admin-spring-boot/logs
```

---

## 📞 联系和支持

如果在部署过程中遇到问题，可以：

1. **查看日志文件** 获取详细错误信息
2. **检查防火墙和端口** 确保网络配置正确
3. **验证服务状态** 确保所有服务正常运行
4. **参考故障排除章节** 解决常见问题

部署完成后，您的应用将通过以下地址访问：
- **HTTP**: `http://your-server-ip/ip/my`
- **HTTPS**: `https://your-domain.com/ip/my` (配置SSL后)

---

## 📝 部署检查清单

### 基础部署清单
- [ ] 系统更新完成
- [ ] Java 17 安装并配置
- [ ] MySQL 8.0 安装并配置（或 RDS 配置完成）
- [ ] 数据库和用户创建
- [ ] Maven 安装（可选）
- [ ] 应用打包并上传
- [ ] 系统服务配置
- [ ] Nginx 安装并配置
- [ ] 防火墙配置
- [ ] SSL 证书配置（推荐）
- [ ] 监控脚本配置
- [ ] 备份脚本配置
- [ ] 应用接口测试通过

### AWS 特定检查清单
- [ ] EC2 实例类型和大小合适
- [ ] 安全组配置正确
- [ ] EBS 卷大小足够并启用加密
- [ ] 弹性IP 分配（如需要）
- [ ] RDS 实例配置和连接测试
- [ ] RDS 安全组仅允许 EC2 访问
- [ ] CloudWatch 代理安装和配置
- [ ] IAM 角色配置（如使用 AWS 服务）
- [ ] 备份策略配置（EBS 快照、RDS 备份）
- [ ] Route 53 域名解析配置（如有域名）
- [ ] Certificate Manager SSL 证书申请
- [ ] Application Load Balancer 配置（如使用）

### 性能和安全检查
- [ ] JVM 内存参数根据实例大小调整
- [ ] MySQL 配置根据实例内存优化
- [ ] Nginx gzip 压缩启用
- [ ] 应用日志轮转配置
- [ ] 系统监控和告警配置
- [ ] 定期备份脚本运行正常
- [ ] SSL 证书自动续期配置
- [ ] 防火墙规则最小化原则
- [ ] 数据库连接使用非 root 用户
- [ ] 应用运行使用非特权用户

### 运维检查
- [ ] 服务自启动配置
- [ ] 监控脚本定时任务配置
- [ ] 日志文件位置和权限正确
- [ ] 备份恢复流程测试
- [ ] 故障切换计划制定
- [ ] 性能基准测试完成
- [ ] 文档更新完成

**🎉 恭喜！您的 Admin Spring Boot 应用已成功部署到 AWS Amazon Linux 系统！**

---

## 🔗 相关资源

- [AWS EC2 用户指南](https://docs.aws.amazon.com/ec2/)
- [AWS RDS 用户指南](https://docs.aws.amazon.com/rds/)
- [Amazon Linux 3 文档](https://docs.aws.amazon.com/linux/)
- [Spring Boot 部署指南](https://spring.io/guides/gs/spring-boot/)
- [Nginx 官方文档](https://nginx.org/en/docs/)

## 📞 技术支持

如果在部署过程中遇到 AWS 相关问题：
1. 检查 AWS 服务状态页面
2. 查看 CloudWatch 日志和指标
3. 使用 AWS Support（如有支持计划）
4. 参考 AWS 技术文档和最佳实践 
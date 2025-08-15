# 🚀 Admin Spring Boot 项目 - 已有环境部署指南

## 📋 部署前说明

本文档专门针对已经部署了 **RuoYi** 或其他 Spring Boot 应用的服务器环境，指导如何在现有环境中添加新的 Admin Spring Boot 应用，实现多应用共存。

### 🎯 部署目标
- 在已有 RuoYi 环境基础上部署新应用
- 通过域名路径 `/v2/ip/my` 访问新应用
- 不影响现有 RuoYi 应用的正常运行
- 共享基础环境（Java、MySQL、Nginx等）

### 📊 现有环境假设
- ✅ **Java 环境**: 已安装 Java 8/11/17
- ✅ **数据库**: 已有 MySQL 实例和 RuoYi 数据库
- ✅ **Nginx**: 已配置并运行，代理 RuoYi 应用
- ✅ **基础工具**: Maven、Git 等开发工具
- ✅ **防火墙**: 已开放 80/443 端口

### 🔄 部署架构
```
                    Nginx (80/443)
                         │
                    ┌────┴────┐
                    │         │
              RuoYi 应用      Admin Spring Boot
             (端口: 8080)      (端口: 8081)
                 │                  │
            ┌────┴────┐        ┌────┴────┐
            │ ruoyi   │        │ admin   │
            │ 数据库  │        │ 数据库  │
            └─────────┘        └─────────┘
                    MySQL (3306)
```

---

## 🔍 第一步：环境检查和评估

### 1.1 检查现有服务状态
```bash
# 检查现有Java版本
java -version

# 检查正在运行的Java应用
ps aux | grep java

# 检查端口占用情况
sudo ss -tlnp | grep -E ':(8080|8081|3306)'

# 检查Nginx状态和配置
sudo systemctl status nginx
sudo nginx -t
sudo cat /etc/nginx/nginx.conf
ls -la /etc/nginx/conf.d/
```

### 1.2 检查现有数据库
```bash
# 连接MySQL检查现有数据库
mysql -u root -p

# 在MySQL中执行以下命令
SHOW DATABASES;
SELECT User, Host FROM mysql.user;
SHOW PROCESSLIST;
EXIT;
```

### 1.3 检查磁盘空间和资源
```bash
# 检查磁盘使用情况
df -h

# 检查内存使用情况
free -h

# 检查CPU使用情况
top
```

---

## 📦 第二步：准备新应用环境

### 2.1 选择新的端口
```bash
# 检查8081端口是否可用
sudo ss -tlnp | grep :8081

# 如果8081被占用，可以选择其他端口如8082、8083等
# 本文档以8081为例
```

### 2.2 创建新应用目录
```bash
# 创建Admin Spring Boot应用目录
sudo mkdir -p /opt/admin-spring-boot
sudo mkdir -p /opt/admin-spring-boot/logs
sudo mkdir -p /opt/admin-spring-boot/config

# 如果已有应用用户，可以复用，否则创建新用户
# 检查是否有现有的应用用户
id ruoyi 2>/dev/null || echo "ruoyi用户不存在"

# 创建新的应用用户（推荐独立用户）
sudo useradd -r -s /bin/false admin-app
sudo chown -R admin-app:admin-app /opt/admin-spring-boot
```

### 2.3 数据库准备
```bash
# 连接MySQL创建新数据库
mysql -u root -p
```

```sql
-- 创建admin数据库（与ruoyi数据库分离）
CREATE DATABASE admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建专用数据库用户（推荐独立用户）
CREATE USER 'admin_user'@'localhost' IDENTIFIED BY 'AdminPass123!@#';
CREATE USER 'admin_user'@'%' IDENTIFIED BY 'AdminPass123!@#';

-- 仅授权admin数据库权限
GRANT ALL PRIVILEGES ON admin.* TO 'admin_user'@'localhost';
GRANT ALL PRIVILEGES ON admin.* TO 'admin_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 验证用户创建
SELECT User, Host FROM mysql.user WHERE User = 'admin_user';

-- 退出
EXIT;
```

---

## 🚀 第三步：部署Admin Spring Boot应用

### 3.1 上传应用文件
```bash
# 方法一：从本地上传编译好的jar文件
# 在本地执行：mvn clean package -DskipTests
# 然后上传：
scp target/admin-spring-boot-0.0.1-SNAPSHOT.jar root@your-server:/opt/admin-spring-boot/app.jar

# 方法二：在服务器上编译（如果有Git仓库）
cd /tmp
git clone https://github.com/your-repo/admin-spring-boot.git
cd admin-spring-boot
mvn clean package -DskipTests
sudo cp target/admin-spring-boot-0.0.1-SNAPSHOT.jar /opt/admin-spring-boot/app.jar
sudo chown admin-app:admin-app /opt/admin-spring-boot/app.jar
```

### 3.2 配置应用属性
```bash
# 创建生产环境配置文件
sudo vim /opt/admin-spring-boot/config/application-prod.properties
```

```properties
# 应用基本配置
spring.application.name=admin-spring-boot
spring.profiles.active=prod

# 服务器配置 - 使用8081端口避免与RuoYi冲突
server.port=8081
server.servlet.context-path=/

# 数据库配置 - 使用独立的admin数据库
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/admin?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=admin_user
spring.datasource.password=AdminPass123!@#

# 数据库连接池配置（较保守的配置，避免影响RuoYi）
spring.datasource.hikari.minimum-idle=3
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.pool-name=AdminHikariCP
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=30000

# MyBatis Plus配置
mybatis-plus.mapper-locations=classpath*:/mapper/**/*.xml
mybatis-plus.type-aliases-package=com.haigaote.admin.entity
mybatis-plus.global-config.db-config.id-type=auto
mybatis-plus.global-config.db-config.table-underline=true
mybatis-plus.configuration.map-underscore-to-camel-case=true

# 日志配置
logging.level.com.haigaote.admin=INFO
logging.level.org.springframework.web=WARN
logging.file.path=/opt/admin-spring-boot/logs
logging.file.name=admin-spring-boot.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n

# 性能配置（较保守，与现有应用共存）
server.tomcat.threads.max=50
server.tomcat.threads.min-spare=5
server.tomcat.max-connections=1000
server.tomcat.accept-count=100
server.tomcat.connection-timeout=20000

# Spring异步配置（较小的线程池）
spring.task.execution.pool.core-size=2
spring.task.execution.pool.max-size=8
spring.task.execution.pool.queue-capacity=100
```

### 3.3 创建系统服务
```bash
# 创建systemd服务文件
sudo vim /etc/systemd/system/admin-spring-boot.service
```

```ini
[Unit]
Description=Admin Spring Boot Application
After=network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
User=admin-app
Group=admin-app

# Java路径 - 复用现有Java环境
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Dspring.profiles.active=prod -Dspring.config.location=classpath:/application.properties,/opt/admin-spring-boot/config/application-prod.properties -jar /opt/admin-spring-boot/app.jar

ExecStop=/bin/kill -15 $MAINPID
Restart=always
RestartSec=10

# 日志配置
StandardOutput=journal
StandardError=journal
SyslogIdentifier=admin-spring-boot

# 进程管理
KillMode=mixed
KillSignal=SIGTERM
TimeoutStopSec=30

# 安全配置
NoNewPrivileges=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
```

### 3.4 启动新应用
```bash
# 重新加载systemd配置
sudo systemctl daemon-reload

# 启动Admin Spring Boot服务
sudo systemctl start admin-spring-boot

# 检查服务状态
sudo systemctl status admin-spring-boot

# 设置开机自启
sudo systemctl enable admin-spring-boot

# 查看应用日志
sudo journalctl -u admin-spring-boot -f

# 测试应用是否正常运行
curl http://localhost:8081/ip/my
```

---

## 🌐 第四步：配置Nginx路径代理

### 4.1 备份现有Nginx配置
```bash
# 备份现有nginx配置
sudo cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.backup.$(date +%Y%m%d)
sudo cp -r /etc/nginx/conf.d /etc/nginx/conf.d.backup.$(date +%Y%m%d)

# 查看现有配置
ls -la /etc/nginx/conf.d/
cat /etc/nginx/conf.d/*.conf
```

### 4.2 创建新的location配置

#### 方法一：修改现有配置文件（推荐）
```bash
# 找到现有的server配置文件
sudo find /etc/nginx -name "*.conf" -exec grep -l "server_name" {} \;

# 假设现有配置文件是 /etc/nginx/conf.d/default.conf
sudo vim /etc/nginx/conf.d/default.conf
```

在现有的server块中添加新的location：
```nginx
server {
    listen 80;
    server_name your-domain.com;  # 现有域名配置
    
    # 现有RuoYi应用配置保持不变
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 新增：Admin Spring Boot应用配置
    location /v2/ {
        # 重写URL，去掉v2前缀
        rewrite ^/v2/(.*)$ /$1 break;
        
        proxy_pass http://127.0.0.1:8081;
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
    
    # Admin应用健康检查
    location /v2/actuator/health {
        rewrite ^/v2/(.*)$ /$1 break;
        proxy_pass http://127.0.0.1:8081;
        access_log off;
    }
    
    # 现有其他location配置保持不变...
}
```

#### 方法二：创建独立配置文件
```bash
# 创建独立的Admin应用配置文件
sudo vim /etc/nginx/conf.d/admin-spring-boot.conf
```

```nginx
# Admin Spring Boot 专用upstream
upstream admin_backend {
    server 127.0.0.1:8081 max_fails=3 fail_timeout=30s;
}

# 如果需要独立域名，可以创建新的server块
# server {
#     listen 80;
#     server_name admin.your-domain.com;
#     
#     location / {
#         proxy_pass http://admin_backend;
#         # ... 代理配置
#     }
# }

# 或者在现有域名上添加location（需要include到主配置中）
# 这个文件主要定义upstream，具体location在主配置文件中添加
```

### 4.3 测试和重启Nginx
```bash
# 测试Nginx配置
sudo nginx -t

# 如果配置正确，重新加载Nginx
sudo systemctl reload nginx

# 检查Nginx状态
sudo systemctl status nginx

# 查看Nginx错误日志（如果有问题）
sudo tail -f /var/log/nginx/error.log
```

---

## ✅ 第五步：测试部署结果

### 5.1 功能测试
```bash
# 测试RuoYi应用是否正常（不应受影响）
curl http://localhost/
curl http://your-domain.com/

# 测试新的Admin应用
curl http://localhost/v2/ip/my
curl http://your-domain.com/v2/ip/my

# 测试Admin应用的其他接口
curl http://your-domain.com/v2/ip/my/geo
curl http://your-domain.com/v2/ip/performance
```

### 5.2 检查服务状态
```bash
# 检查所有相关服务状态
sudo systemctl status nginx
sudo systemctl status mysql
sudo systemctl status admin-spring-boot

# 检查端口监听
sudo ss -tlnp | grep -E ':(80|8080|8081|3306)'

# 检查进程
ps aux | grep -E "(java|nginx|mysql)" | grep -v grep
```

### 5.3 检查日志
```bash
# 查看Admin应用日志
sudo tail -f /opt/admin-spring-boot/logs/admin-spring-boot.log

# 查看Nginx访问日志
sudo tail -f /var/log/nginx/access.log

# 查看系统服务日志
sudo journalctl -u admin-spring-boot -n 20
sudo journalctl -u nginx -n 20
```

---

## 🔧 第六步：资源优化和调优

### 6.1 内存和CPU优化
```bash
# 检查当前系统资源使用
free -h
htop

# 根据实际情况调整JVM参数
sudo vim /etc/systemd/system/admin-spring-boot.service

# 示例调整：
# 如果服务器内存充足（8GB+）：-Xms1024m -Xmx2048m
# 如果服务器内存有限（4GB）：-Xms512m -Xmx1024m
# 如果服务器内存紧张（2GB）：-Xms256m -Xmx512m

# 重启服务使配置生效
sudo systemctl daemon-reload
sudo systemctl restart admin-spring-boot
```

### 6.2 数据库连接池优化
```bash
# 检查MySQL连接数
mysql -u root -p -e "SHOW PROCESSLIST;"
mysql -u root -p -e "SHOW STATUS LIKE 'Threads_connected';"

# 根据连接数调整Admin应用的连接池配置
sudo vim /opt/admin-spring-boot/config/application-prod.properties

# 调整连接池参数：
# spring.datasource.hikari.maximum-pool-size=5  # 如果连接数紧张
# spring.datasource.hikari.maximum-pool-size=15 # 如果连接数充足
```

### 6.3 Nginx性能优化
```bash
# 检查当前worker进程数
ps aux | grep "nginx: worker"

# 优化worker配置（在现有配置基础上调整）
sudo vim /etc/nginx/nginx.conf

# 确保worker_processes设置合理
# worker_processes auto;  # 或者设置为CPU核心数
# worker_connections 1024; # 根据需要调整
```

---

## 📊 第七步：监控和维护

### 7.1 设置独立监控
```bash
# 创建Admin应用专用监控脚本
sudo vim /opt/admin-spring-boot/monitor.sh
```

```bash
#!/bin/bash
# Admin Spring Boot 监控脚本

APP_NAME="admin-spring-boot"
APP_PORT=8081
LOG_DIR="/opt/admin-spring-boot/logs"
DATE=$(date '+%Y-%m-%d %H:%M:%S')

# 检查应用是否运行
if ! curl -f -s http://localhost:$APP_PORT/ip/my >/dev/null 2>&1; then
    echo "[$DATE] Admin应用健康检查失败，尝试重启..." >> $LOG_DIR/monitor.log
    
    # 检查进程是否存在
    if ! systemctl is-active --quiet $APP_NAME; then
        echo "[$DATE] Admin应用服务已停止，正在重启..." >> $LOG_DIR/monitor.log
        systemctl restart $APP_NAME
        sleep 30
        
        # 再次检查
        if curl -f -s http://localhost:$APP_PORT/ip/my >/dev/null 2>&1; then
            echo "[$DATE] Admin应用重启成功" >> $LOG_DIR/monitor.log
        else
            echo "[$DATE] Admin应用重启失败，需要人工干预" >> $LOG_DIR/monitor.log
        fi
    fi
fi

# 检查与RuoYi应用的端口冲突
if ss -tlnp | grep :8080 | grep -v LISTEN >/dev/null 2>&1; then
    echo "[$DATE] 检测到8080端口异常，可能影响RuoYi应用" >> $LOG_DIR/monitor.log
fi

# 检查内存使用（只检查Admin应用）
ADMIN_PID=$(pgrep -f "admin-spring-boot")
if [ ! -z "$ADMIN_PID" ]; then
    MEMORY_USAGE=$(ps -p $ADMIN_PID -o %mem --no-headers)
    if (( $(echo "$MEMORY_USAGE > 30" | bc -l 2>/dev/null || echo 0) )); then
        echo "[$DATE] Admin应用内存使用率较高: $MEMORY_USAGE%" >> $LOG_DIR/monitor.log
    fi
fi
```

```bash
# 设置执行权限
sudo chmod +x /opt/admin-spring-boot/monitor.sh

# 添加到定时任务（每5分钟检查一次）
sudo crontab -e
# 添加：
*/5 * * * * /opt/admin-spring-boot/monitor.sh
```

### 7.2 日志轮转配置
```bash
# 创建Admin应用专用日志轮转配置
sudo vim /etc/logrotate.d/admin-spring-boot
```

```
/opt/admin-spring-boot/logs/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    copytruncate
    su admin-app admin-app
}
```

### 7.3 备份策略
```bash
# 创建Admin应用专用备份脚本
sudo vim /opt/admin-spring-boot/backup.sh
```

```bash
#!/bin/bash
# Admin应用专用备份脚本

BACKUP_DIR="/opt/backups/admin"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 备份Admin数据库
mysqldump -u admin_user -p'AdminPass123!@#' admin | gzip > $BACKUP_DIR/admin_db_$DATE.sql.gz

# 备份应用配置
tar -czf $BACKUP_DIR/admin_config_$DATE.tar.gz /opt/admin-spring-boot/config

# 清理7天前的备份
find $BACKUP_DIR -name "admin_*" -mtime +7 -delete

echo "$(date): Admin应用备份完成"
```

```bash
# 设置执行权限和定时任务
sudo chmod +x /opt/admin-spring-boot/backup.sh

# 添加到定时任务（每天凌晨3点备份）
sudo crontab -e
# 添加：
0 3 * * * /opt/admin-spring-boot/backup.sh >> /opt/admin-spring-boot/logs/backup.log 2>&1
```

---

## 🚨 故障排除

### 常见问题和解决方案

#### 1. 端口冲突问题
```bash
# 检查端口占用
sudo ss -tlnp | grep :8081

# 如果8081被占用，修改配置使用其他端口
sudo vim /opt/admin-spring-boot/config/application-prod.properties
# 修改：server.port=8082

# 同时修改Nginx配置
sudo vim /etc/nginx/conf.d/default.conf
# 修改：proxy_pass http://127.0.0.1:8082;
```

#### 2. 数据库连接问题
```bash
# 检查Admin数据库用户权限
mysql -u admin_user -p'AdminPass123!@#' -e "SELECT DATABASE(); SHOW TABLES;"

# 检查数据库连接数
mysql -u root -p -e "SHOW PROCESSLIST;" | grep admin_user

# 如果连接数过多，调整连接池配置
sudo vim /opt/admin-spring-boot/config/application-prod.properties
# 减少：spring.datasource.hikari.maximum-pool-size=5
```

#### 3. Nginx配置冲突
```bash
# 检查Nginx配置语法
sudo nginx -t

# 查看Nginx错误日志
sudo tail -f /var/log/nginx/error.log

# 测试location匹配
curl -I http://localhost/v2/ip/my
curl -I http://localhost/ip/my  # 这应该仍然指向RuoYi应用
```

#### 4. 内存不足问题
```bash
# 检查系统内存使用
free -h
ps aux --sort=-%mem | head -10

# 如果内存紧张，降低Admin应用内存使用
sudo vim /etc/systemd/system/admin-spring-boot.service
# 修改：-Xms256m -Xmx512m

# 重启服务
sudo systemctl daemon-reload
sudo systemctl restart admin-spring-boot
```

#### 5. URL路径问题
```bash
# 测试URL重写是否正确
curl -v http://localhost/v2/ip/my

# 检查Nginx重写规则
sudo vim /etc/nginx/conf.d/default.conf

# 确保rewrite规则正确：
# rewrite ^/v2/(.*)$ /$1 break;
```

---

## 📝 部署检查清单

### 环境检查
- [ ] 现有RuoYi应用运行正常
- [ ] Java环境版本兼容
- [ ] MySQL服务正常运行
- [ ] Nginx服务正常运行
- [ ] 8081端口可用
- [ ] 磁盘空间充足（至少1GB可用）

### 应用部署
- [ ] Admin数据库创建成功
- [ ] 数据库用户权限配置正确
- [ ] 应用配置文件设置正确
- [ ] systemd服务创建并启动成功
- [ ] 应用日志正常输出
- [ ] 应用接口响应正常

### Nginx配置
- [ ] 现有配置备份完成
- [ ] `/v2/` location配置添加成功
- [ ] Nginx配置语法检查通过
- [ ] Nginx重新加载成功
- [ ] URL重写规则工作正常

### 功能测试
- [ ] RuoYi应用仍然正常访问
- [ ] Admin应用通过 `/v2/ip/my` 正常访问
- [ ] 两个应用互不影响
- [ ] 数据库操作正常
- [ ] 日志记录正常

### 监控和维护
- [ ] 监控脚本配置并运行
- [ ] 日志轮转配置完成
- [ ] 备份脚本配置完成
- [ ] 定时任务设置正确

---

## 🎯 访问示例

部署完成后，您可以通过以下方式访问：

### RuoYi应用（保持不变）
```bash
# 原有访问方式保持不变
http://your-domain.com/
http://your-domain.com/login
http://your-domain.com/system/user
```

### Admin Spring Boot应用（新增）
```bash
# 新的访问方式
http://your-domain.com/v2/ip/my
http://your-domain.com/v2/ip/my/geo
http://your-domain.com/v2/ip/performance
```

### API调用示例
```bash
# 获取客户端IP
curl http://your-domain.com/v2/ip/my

# 响应示例：
{
  "code": 200,
  "success": true,
  "clientIp": "192.168.1.100",
  "message": "客户端IP地址获取成功",
  "timestamp": 1703749200000
}
```

---

## 💡 最佳实践建议

### 1. 资源分配
- **内存分配**: Admin应用建议分配1GB内存，不要超过系统总内存的25%
- **数据库连接**: Admin应用连接池不超过10个连接，避免影响RuoYi应用
- **CPU使用**: 监控CPU使用率，确保两个应用总使用率不超过80%

### 2. 安全考虑
- **数据库隔离**: 使用独立的数据库和用户，避免权限冲突
- **端口管理**: 使用防火墙限制8081端口只允许本地访问
- **日志管理**: 定期清理日志，避免磁盘空间不足

### 3. 维护策略
- **更新策略**: 错峰更新，先更新Admin应用，再更新RuoYi应用
- **备份策略**: 分别备份两个应用的数据，恢复时可以独立操作
- **监控策略**: 分别监控两个应用的性能，避免相互影响

---

**🎉 恭喜！您已成功在现有RuoYi环境中部署了Admin Spring Boot应用！**

现在您可以通过 `https://your-domain.com/v2/ip/my` 访问新的IP管理功能，同时保持原有RuoYi系统的正常运行。 
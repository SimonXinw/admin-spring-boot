xw 天才，开始写 java 啦！
# Spring Boot Demo 项目

## 项目说明
这是一个基于 Java 17 和 Spring Boot 3.2.0 的演示项目，实现了获取客户端IP地址的功能。

## 功能特性
- ✅ 获取客户端真实IP地址（支持代理服务器环境）
- ✅ RESTful API 接口
- ✅ JSON 格式响应
- ✅ 内嵌 Tomcat 服务器
- ✅ 支持热部署（spring-boot-devtools）

## 技术栈
- Java 17
- Spring Boot 3.2.0
- Maven 构建工具
- 内嵌 Tomcat 服务器

## API 接口

### 获取客户端IP
- **接口地址**: `GET /ip/my`
- **响应格式**: JSON
- **响应示例**:
```json
{
    "clientIp": "192.168.1.100",
    "message": "客户端IP地址获取成功",
    "timestamp": 1703749200000
}
```

## 启动方式

### 方式一：在 Cursor 中启动调试器
1. 打开 `SpringBootDemoApplication.java` 文件
2. 在 `main` 方法左侧点击运行按钮或者在方法上右键选择 "Debug Java"
3. 项目将在调试模式下启动

### 方式二：使用 Maven 命令启动
```bash
# 进入项目目录
cd spring-boot-demo

# 启动项目
mvn spring-boot:run
```

### 方式三：打包后运行
```bash
# 编译打包
mvn clean package

# 运行jar包
java -jar target/spring-boot-demo-0.0.1-SNAPSHOT.jar
```

## 测试接口
项目启动后，访问以下URL测试功能：
- 浏览器访问: http://localhost:8080/ip/my
- 或使用 curl 命令: `curl http://localhost:8080/ip/my`

## 注意事项
1. 确保系统已安装 Java 17
2. 确保系统已安装 Maven（如果使用命令行启动）
3. 默认端口是8080，如果端口被占用可在 `application.properties` 中修改
4. 项目使用内嵌 Tomcat，无需单独安装 Tomcat 服务器 
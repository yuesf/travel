# 旅游平台系统

## 项目简介

旅游平台系统是一个完整的旅游服务管理平台，包括后台管理系统和微信小程序前端。

## 技术栈

- **后端框架**: Spring Boot 2.7.18
- **数据库**: MySQL 8.0+
- **ORM框架**: MyBatis (XML模式)
- **缓存**: Redis
- **认证授权**: Spring Security + JWT
- **消息队列**: RocketMQ (可选)
- **API文档**: Swagger
- **连接池**: Druid

## 项目结构

```
travel-platform/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── travel/
│   │   │           └── TravelPlatformApplication.java  # 主启动类
│   │   └── resources/
│   │       ├── application.yml                          # 应用配置文件
│   │       ├── mybatis-config.xml                      # MyBatis配置
│   │       ├── mapper/                                  # Mapper XML文件目录
│   │       └── db/
│   │           └── migration/
│   │               └── V1__init_schema.sql             # 数据库初始化脚本
│   └── test/
├── pom.xml                                              # Maven依赖配置
└── README.md
```

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE travel_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p travel_platform < src/main/resources/db/migration/V1__init_schema.sql
```

### 配置修改

修改 `src/main/resources/application.yml` 中的数据库和Redis连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/travel_platform?...
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
```

### 运行项目

```bash
mvn spring-boot:run
```

或打包后运行：

```bash
mvn clean package
java -jar target/travel-platform-1.0.0.jar
```

## 数据库表结构

系统包含以下主要表：

- **用户相关**: admin_user, user, role, permission, role_permission
- **业务相关**: attraction, hotel, hotel_room, product_category, product, coupon, user_coupon, order, order_item, cart
- **系统相关**: sync_config, sync_log, operation_log, miniprogram_config

详细表结构请参考 `src/main/resources/db/migration/V1__init_schema.sql`

## 开发规范

- MyBatis使用XML模式，Mapper接口放在 `com.travel.mapper` 包下
- Mapper XML文件放在 `src/main/resources/mapper/` 目录下
- 实体类放在 `com.travel.entity` 包下
- Service层放在 `com.travel.service` 包下
- Controller层放在 `com.travel.controller` 包下

## 许可证

Copyright (c) 2024 Travel Platform

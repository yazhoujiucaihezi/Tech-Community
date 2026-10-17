# 🎓 Campus Forum - 校园社区互动平台

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.x-4FC08D.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![Redis](https://img.shields.io/badge/Redis-7.0-DC382D.svg)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.8-FF6600.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

基于 **Spring Boot 3 + Vue 3** 构建的前后端分离校园社区互动系统。面向高校交流场景，涵盖帖子流转、多级互动、实时热榜（时间衰减模型）、异步消息通知、AI 智能问答助手及后台管理闭环。

> **💡 开发说明**：本项目采用前后端分离架构。**后端服务由本人从 0 到 1 独立设计与重构实现**（涵盖数据库建模、RESTful 接口设计、中间件集成与核心业务逻辑）；前端基于开源工程进行二次开发与全链路接口适配联调。

---

<img width="2880" height="1530" alt="5384b87843aca68d37786dfc342b6b26" src="https://github.com/user-attachments/assets/b0f65128-a015-46ed-996d-7b7a02a2715c" />

<img width="2880" height="1530" alt="018bcd577c29dd31541f898a30acf1d9" src="https://github.com/user-attachments/assets/4dec428f-af0f-4f75-8487-a5a3979fb2b9" />

<img width="2880" height="1530" alt="e50a6353c2dfd538796c9c536ab91eaf" src="https://github.com/user-attachments/assets/b7380668-2137-4ef3-961e-90e25acc5d21" />

<img width="2880" height="1530" alt="9af96796a1e75121d97e0dfb782de257" src="https://github.com/user-attachments/assets/be70424b-9b3c-4ea9-85f6-d206e30cc28d" />

<img width="1934" height="1130" alt="d95c020d237b8d9e1bb6283c8a3cdf34" src="https://github.com/user-attachments/assets/281a8fb0-619a-468d-995e-d79c2b3b6dd0" />


## ✨ 功能特性

| 模块 | 核心功能模块 | 功能描述 |
| :--- | :--- | :--- |
| **用户端** | **账号与安全** | 用户注册、基于 `JwtUtils` 的无状态登录、`BCrypt` 密码加密、邮箱验证码校验及密码找回 |
| | **个人中心** | 个人资料维护、头像上传、账号安全与隐私权限设置 |
| | **内容与互动** | 富文本发帖（图片按日期归档映射）、帖子编辑/删除、多分类检索、多级评论回复、点赞与收藏 |
| | **实时热榜** | 基于 **Redis ZSet + 时间重力衰减算法** 的动态热门帖子排行榜 |
| | **异步通知** | 基于 **RabbitMQ** 异步解耦的评论、点赞、收藏站内消息通知系统 |
| | **AI 与扩展** | 接入大模型流式对话助手（SSE）、和风天气实时气象信息展示 |
| **管理端** | **内容治理** | 帖子置顶、加精、锁定、违规屏蔽、删除及版块分类迁移 |
| | **版块与用户** | 社区版块分类 CRUD 管理；用户信息检视、状态管控与密码重置 |
| | **运维监控** | 系统邮件发送记录追踪与异常邮件手动重发机制 |

---

## 🛠️ 技术栈

### 1. 后端技术栈
* **核心框架**：Java 17、Spring Boot 3.5.8、Spring Web、Spring Validation
* **持久层框架**：MyBatis-Plus 3.5.15、MySQL 8.0
* **缓存与中间件**：Redis 7（验证码 TTL 管理、ZSet 实时热榜）、RabbitMQ 3.8（异步通知削峰解耦）
* **AI 与第三方集成**：Spring AI / OpenAI 兼容接口（Reactor `Flux` 流式推送）、Spring Mail（QQ SMTP）、和风天气 API
* **安全与工具**：JJWT（封装 `JwtUtils` 无状态认证）、BCrypt（加密）、FastJSON2、Lombok、Maven

### 2. 前端技术栈
* **核心框架**：Vue 3、Vue Router、Pinia、Vite
* **UI 与组件**：Element Plus、Axios、VueQuill（富文本编辑器）

---

## 🏗️ 系统架构设计

```text
┌─────────────────────────────────────────────────────────────┐
│                        Vue 3 前端应用                        │
│          (Element Plus + Pinia + Axios + SSE 监听)          │
└──────────────────────────────┬──────────────────────────────┘
                               │ RESTful API / SSE Stream (JWT)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     Spring Boot 3 后端服务                   │
│   Controller 层 (参数校验 / JwtUtils 鉴权拦截)                │
│       ↓                                                     │
│   Service 层 (核心业务 / 热度计算 / AI 上下文裁剪)             │
│       ↓                                                     │
│   Mapper / Manager 层                                       │
└──────┬───────────────────────┬───────────────────────┬──────┘
       │                       │                       │
       ▼                       ▼                       ▼
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│  MySQL 8.0  │         │   Redis 7   │         │ RabbitMQ 3.8│
│ (核心持久化) │         │ (热榜/验证码)│         │ (消息交换机) │
└─────────────┘         └─────────────┘         └──────┬──────┘
                                                       │ 异步消费
                                                       ▼
                                                ┌─────────────┐
                                                │ Notification│
                                                │  Consumer   │
                                                └──────┬──────┘
                                                       │
                                                       ▼
                                                (通知数据落库)
```

---

## 🔥 核心技术实现

### 1. 基于时间衰减模型的 Redis ZSet 实时热榜与无感刷新
为解决传统按点赞量排序导致的“老帖长期霸榜、新帖无法曝光”问题，引入时间衰减算法结合 Redis `ZSet` 构建动态热榜：
* **热度重力衰减公式**：`Score = (点赞 × 1 + 评论 × 3 + 收藏 × 2) / (发帖小时数 + 2)^1.5`
* **实时增量 + 定时全量重算**：用户发生点赞、评论、收藏行为时，实时重算当前帖子分值并更新 `ZSet`；同时配置定时任务每小时全量重算热度分值，使历史帖子分数随时间自然衰减下沉。
* **缓存无感切换机制**：全量重算期间采用双层缓存与临时 Key 策略，后台任务将最新分值写入临时 `ZSet Key`，计算完成后通过原子操作覆盖线上主 Key，避免全量刷新期间的读阻塞与缓存击穿。查询时通过 `reverseRange` 毫秒级获取 Top N 帖子 ID 并批量回表组装数据。

### 2. RabbitMQ 异步消息通知解耦
针对社区高频的点赞、评论与收藏操作，采用异步化改造以缩短主链路响应耗时：

```text
用户互动请求 ──► 核心业务落库 ──► 投递 RabbitMQ 消息 ──► 主线程立即返回响应
                                        │
                                        ▼ (异步解耦)
                              NotificationConsumer 监听消费
                                        │
                                        ▼
                                生成站内通知并持久化入库
```

通过将非核心的“通知生成与入库”逻辑从主业务事务中剥离，避免了通知模块异常或慢写操作阻塞主接口响应，实现业务削峰与解耦。

### 3. AI 智能问答助手（流式响应与上下文裁剪）
* **非阻塞流式输出**：基于响应式流 `Flux`（Server-Sent Events）对接大模型服务，实现服务端长连接“打字机”式逐字推送，显著降低用户首字等待延迟（TTFB）。
* **滑动窗口上下文治理**：针对前端传入的多轮对话历史列表（`List<Map<String, Object>>`），在后端实施滑动窗口截断（仅保留最近 N 轮上下文）并动态注入 `System Prompt` 角色约束，在保障多轮对话连贯性的同时严格防范单次请求 Token 超限。

### 4. 无状态认证与安全防护体系
* **JWT 鉴权机制**：用户登录后由后端通过 `JwtUtils` 签发携带用户身份与角色声明的 JWT 令牌；前端通过 `Authorization: Bearer <token>` 请求头传递，后端通过拦截器解析上下文，实现无 Session 状态扩展。
* **验证码防重放与密码安全**：基于 Redis 存储注册、密码找回等场景的邮箱验证码并绑定严格 TTL 过期时间，校验通过后立即主动销毁 Key 防止重放攻击；用户密码全链路采用 `BCrypt` 强哈希加盐加密存储。

---

## 📂 项目目录结构

```text
campus-forum/
├── backend/                                 # Spring Boot 后端工程
│   ├── src/main/java/com/box/
│   │   ├── common/                          # 全局通用组件与统一返回结果封装
│   │   ├── config/                          # Redis / RabbitMQ / 跨域 / 拦截器配置
│   │   ├── consumer/                        # RabbitMQ 消息队列消费者 (异步通知处理)
│   │   ├── controller/                      # RESTful 接口控制层
│   │   ├── dto/                             # 请求数据传输对象 (含参数校验)
│   │   ├── entity/                          # 数据库表映射实体类
│   │   ├── mapper/                          # MyBatis-Plus 数据访问层
│   │   ├── service/                         # 核心业务逻辑层与实现类
│   │   ├── utils/                           # JwtUtils / 加密 / Redis 等工具类
│   │   └── vo/                              # 前端视图响应对象
│   └── src/main/resources/                  # 配置文件与 Mapper XML
├── frontend/                                # Vue 3 前端工程
│   └── src/
│       ├── components/                      # 公共复用 UI 组件
│       ├── net/                             # Axios 请求封装与拦截器
│       ├── router/                          # 前端路由配置
│       ├── store/                           # Pinia 状态管理
│       └── views/                           # 页面视图组件 (用户端 & 管理端)
├── forum.sql                                # 数据库初始化脚本
└── README.md                                # 项目工程文档
```

---

## 🚀 快速开始

### 1. 环境依赖
* **JDK**：17+
* **Maven**：3.8+
* **MySQL**：8.0+
* **Redis**：6.0+
* **RabbitMQ**：3.8+
* **Node.js**：18+

### 2. 数据库初始化
创建 MySQL 数据库（建议字符集 `utf8mb4`），并导入项目根目录下的初始化脚本：

```sql
SOURCE forum.sql;
```

### 3. 后端配置与启动
修改 `backend/src/main/resources/application.yml`，配置本地的 MySQL、Redis、RabbitMQ 连接信息，以及 SMTP 邮箱密钥、和风天气 Key 与大模型 API 密钥（**注意：提交代码时请勿上传个人真实密钥**）：

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 4. 前端配置与启动

```bash
cd frontend
npm install
npm run dev
```

---

## 📄 开源协议
本项目基于 [MIT License](LICENSE) 开源协议发布。

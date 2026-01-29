# 直播辩论系统 - 后端服务

本项目为直播辩论系统提供 Java Spring Boot 后端服务，替换原有的 Node.js 逻辑，同时保留网关架构。

## 📌 基本信息
- **项目名称**: 直播辩论后端服务 (Live Debate Backend Service)

## 🚀 演示地址
- **前端访问地址**: http://localhost:8080/admin (后台管理面板) 或通过小程序访问
- **后端 API 地址**: http://localhost:8081/api
- **网关地址**: http://localhost:8080 (代理 /api 和 /ws 请求到后端)

## 🧱 技术栈说明
- **后端框架**: Java 17, Spring Boot 3.2.0 (Web, WebSocket)
- **Mock 数据生成方案**: Java 内存集合 (ConcurrentHashMap, CopyOnWriteArrayList) 配合定时任务 (@Scheduled) 进行模拟。
- **部署平台与方式**: 本地部署 (网关运行在 8080 端口, 后端运行在 8081 端口)。

## 🔗 项目结构与接口说明

### 简述后端目录结构
```
/
├── frontend/        # 前端项目 (原 Live 项目)
├── gateway/         # 网关项目 (Node.js 代理)
├── backend/         # 后端项目 (Spring Boot)
│   ├── src/main/java/com/example/livedebate/
│   │   ├── controller/   # REST 控制器 (API 接口)
│   │   ├── service/      # Mock 服务与 WebSocket 逻辑
│   │   ├── model/        # 数据模型 (实体类)
│   │   └── config/       # WebSocket 配置
│   └── pom.xml
└── README.md
```

### 主要接口列表
| 功能 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 获取票数 | GET | `/api/votes` | 返回当前正反方票数 |
| 用户投票 | POST | `/api/user-vote` | 提交用户投票 |
| 获取辩题 | GET | `/api/debate-topic` | 获取当前辩题信息 |
| 获取AI内容 | GET | `/api/ai-content` | 获取AI辩论观点列表 |
| 发布评论 | POST | `/api/comment` | 发布新评论 |
| 管理端仪表盘 | GET | `/api/admin/dashboard` | 获取后台管理概览数据 |
| 开始直播 | POST | `/api/admin/live/start` | 启动直播流 |
| WebSocket | WS | `/ws` | 实时推送更新 (票数, AI内容, 直播状态) |

## 🧠 项目开发过程笔记

### 项目实现思路
1. **架构解耦**: 将原本单体式的 Node.js 网关进行拆分。
   - **Gateway (Node.js)**: 转变为纯粹的反向代理 (使用 `http-proxy-middleware`)，负责将 `/api` 和 `/ws` 流量转发给 Java 后端，同时继续提供后台管理的静态资源服务。
   - **Backend (Java)**: 接管所有业务逻辑、状态管理 (票数、内容) 以及 WebSocket 广播功能。

2. **Mock 数据**: 
   - 使用 `MockDataService` 在内存中维护应用状态。
   - 实现了 `@Scheduled` 定时任务来模拟真实环境下的票数增长和 AI 内容生成，还原了原项目的演示效果。

3. **WebSocket 实现**: 
   - 在 Spring Boot 中使用 `TextWebSocketHandler` 重写了 WebSocket 处理逻辑。
   - 网关层配置了 WebSocket 代理，确保实时连接能无缝穿透到 Java 后端。

### 部署步骤与踩坑记录
1. **后端 (Backend)**:
   ```bash
   cd backend
   mvn clean spring-boot:run
   ```
   (运行在 8081 端口)

2. **网关 (Gateway)**:
   ```bash
   cd gateway
   npm install
   node gateway.js
   ```
   (运行在 8080 端口)

3. **前端 (Frontend)**:
   - 浏览器访问 http://localhost:8080/admin 查看后台管理面板。
   - 使用 HBuilderX 或相关工具运行小程序前端，配置连接地址为 `http://localhost:8080`。

### 遇到的问题与解决方案
- **文件夹重命名**: 在重构目录结构时遇到权限问题，通过分步操作确保了 `frontend` 和 `gateway` 目录被正确重命名和移动。
- **WebSocket 代理**: 在配置 Node.js 网关代理时，最初未开启 WebSocket 支持，导致连接失败。解决方案是在 `http-proxy-middleware` 配置中显式添加 `ws: true` 选项，从而成功打通了前后端的实时通信通道。

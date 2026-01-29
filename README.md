# 直播辩论系统 (Live Debate System)

## 📌 基本信息

- **项目名称**: 直播辩论后端服务 (Live Debate Backend Service)

## 🚀 演示地址

- **前端访问地址（后台管理）**: http://localhost:8080/admin
  - 公网访问需配合内网穿透工具（如 Ngrok/Frp）映射 8080 端口。
- **后端 API 地址**: 
  - 网关代理地址: http://localhost:8080/api
  - 直连地址: http://localhost:8081/api

## 🧱 技术栈说明

- **后端框架**: 
  - **Java**: JDK 17
  - **Framework**: Spring Boot 3.2.0 (Spring Web, Spring WebSocket)
  - **Gateway**: Node.js (Express + http-proxy-middleware v3)

- **Mock 数据生成方案**: 
  - **纯代码模拟**: 使用 Java `ConcurrentHashMap` 存储运行时状态（直播流、辩题、票数）。
  - **动态更新**: 通过 `MockDataService` 逻辑处理数据变更（如防止 ID 丢失）。
  - **定时任务**: 使用 Spring `@Scheduled` 模拟票数实时增长和 AI 观点生成。

- **部署平台与方式**: 
  - **本地混合部署**: 
    - Java 后端运行于 `8081` 端口 (负责业务逻辑 & WebSocket)。
    - Node.js 网关运行于 `8080` 端口 (负责静态资源服务 & 反向代理)。
    - 前端页面由网关托管，通过 API 代理解决跨域问题。

## 🔗 项目结构与接口说明

### 简述后端目录结构

```
/
├── frontend/             # 前端静态资源 (Admin & App)
├── gateway/              # Node.js 网关服务
│   ├── gateway.js        # 代理配置入口 (核心)
│   └── package.json
├── backend/              # Spring Boot 后端服务
│   ├── src/main/java/com/example/livedebate/
│   │   ├── config/       # WebSocket 配置 (LiveWebSocketHandler)
│   │   ├── controller/   # REST API 控制器 (AdminController, ApiController)
│   │   ├── model/        # 数据实体 (Stream, Vote, LiveStatus)
│   │   └── service/      # 业务逻辑 (MockDataService)
│   └── pom.xml
└── README.md
```

### 主要接口列表

| 功能 | 方法 | 路径 | 描述 |
|---|---|---|---|
| **后台管理** | | | |
| 获取直播流列表 | GET | `/api/admin/streams` | 返回配置的直播流信息 |
| 获取仪表盘数据 | GET | `/api/admin/dashboard` | 包含票数、直播状态、当前辩题 |
| 直播控制 | POST | `/api/admin/live/control` | 控制直播开始/停止 (Payload: `{action: "start/stop"}`) |
| 更新辩题 | PUT | `/api/streams/{id}/debate` | 修改辩题内容 |
| **客户端/公共** | | | |
| 获取票数统计 | GET | `/api/votes/statistics` | 获取实时票数分布 |
| WebSocket连接 | WS | `/ws` | 建立长连接，接收实时推送 |
| 获取 AI 观点 | GET | `/api/ai-content/list` | 获取 AI 生成的辩论内容 |

## 🧠 项目开发过程笔记

### 简述项目实现思路
1.  **前后端分离与融合**: 采用 Java 处理核心业务，保留原有的 Node.js 网关作为“胶水层”，既复用了前端静态资源服务，又实现了向 Java 后端的平滑迁移。
2.  **WebSocket 代理穿透**: 关键在于如何让 WebSocket 连接穿过 Node.js 代理到达 Spring Boot。使用了 `http-proxy-middleware` 的 `ws: true` 模式，并手动接管 `upgrade` 事件。
3.  **Mock 数据服务化**: 将数据存储在内存 Service 中，而不是硬编码在 Controller 里，模拟了真实数据库的 CRUD 操作体验。

### 遇到的问题与解决方案

1.  **WebSocket 连接失败 (400/404)**
    *   **问题**: 客户端连接 `ws://localhost:8080/ws` 时不断报错，Gateway 返回 400 Bad Request 或 404。
    *   **原因**: `http-proxy-middleware` v3 版本不会自动监听服务器的 `upgrade` 事件；同时 Express 的路由匹配可能剥离了路径。
    *   **解决方案**: 
        1. 在 `gateway.js` 中显式添加 `server.on('upgrade', wsProxy.upgrade)`。
        2. 设置 `changeOrigin: false` 以保留原始 Host 头（便于后端校验 Origin）。
        3. 使用 `pathFilter` 精确匹配 `/ws` 路径，避免路径被错误重写。

2.  **API 路径 404**
    *   **问题**: 访问 `/api/admin/streams` 返回 404。
    *   **原因**: `pathRewrite` 配置错误导致路径被截断（如变成 `/admin/streams`），而后端期望 `/api/admin/streams`。
    *   **解决方案**: 移除 `pathRewrite`，改用 `pathFilter: (path) => path.startsWith('/api')`，确保路径原样透传。

3.  **数据更新丢失 ID ("Not Set" 问题)**
    *   **问题**: 在后台编辑辩题后，列表显示 "ID: 未设置"，导致后续操作失败。
    *   **原因**: 前端提交的更新 Payload 中不包含 ID，后端 Mock 服务直接覆盖对象导致 ID 字段为 null。
    *   **解决方案**: 修改 `MockDataService` 的更新逻辑，如果输入对象 ID 为空，则保留原对象的 ID。

### 本地联调的经验
- **CURL 是神兵利器**: 在浏览器调试 WebSocket 困难时，使用 `curl -i -N -H "Connection: Upgrade" ...` 能清晰看到握手响应头，快速定位是 Gateway 拒绝还是 Backend 拒绝。
- **日志分层**: 在 Gateway 开启 `logLevel: 'debug'`，在 Spring Boot 开启 `logging.level.web=DEBUG`，两边对照日志能迅速发现请求转发中的路径或参数丢失问题。

### 部署步骤与踩坑记录
1.  **启动后端**: `cd backend && mvn spring-boot:run` (确保 8081 端口空闲)。
2.  **启动网关**: `cd gateway && npm install && node gateway.js` (确保 8080 端口空闲)。
3.  **踩坑**: 
    - 确保 `node_modules` 是最新的，特别是 `http-proxy-middleware` 版本差异大。
    - Windows 下如果端口被占用，使用 `netstat -ano | findstr <port>` 查找 PID 并杀掉进程。

## 🧍 个人介绍

- **角色**: 全栈开发工程师 (Java/Node.js)
- **擅长**: Spring Boot 微服务架构、WebSocket 实时通信、异构系统集成。
- **本次测试感悟**: 在处理即有系统改造时，保持对旧架构（Node 网关）的兼容性与引入新架构（Java 后端）的稳定性之间需要精细的配置，特别是在网络协议（WebSocket）的代理层面。

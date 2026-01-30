# 部署指南 (Deployment Guide)

本指南介绍如何将直播辩论系统（Gateway + Backend + Frontend）部署到 Linux 服务器。

## 1. 环境准备 (Prerequisites)

服务器需安装以下软件：

*   **Java**: JDK 17 或更高版本
    ```bash
    java -version
    ```
*   **Node.js**: v18+ (推荐)
    ```bash
    node -v
    ```
*   **Maven**: 用于构建 Java 后端
    ```bash
    mvn -v
    ```
*   **PM2**: 用于进程管理
    ```bash
    npm install -g pm2
    ```

## 2. 构建项目 (Build)

### 2.1 构建 Java 后端

在项目根目录下执行：

```bash
cd backend
mvn clean package -DskipTests
cd ..
```

成功后，应在 `backend/target` 目录下看到 `livedebate-0.0.1-SNAPSHOT.jar`。

### 2.2 构建 Frontend (H5)

脚本会自动执行以下步骤，但你也可以手动执行：

```bash
cd frontend
npm install
npm run build:h5
cd ..
```

构建产物将生成在 `frontend/unpackage/dist/build/h5`，Gateway 会自动代理此目录。

### 2.3 安装 Gateway 依赖

```bash
cd gateway
npm install
cd ..
```

## 3. 启动服务 (Start Services)

本项目使用 `PM2` 统一管理 Gateway 和 Backend 服务。

在项目根目录下，确保已创建 `ecosystem.config.js`（已包含在代码库中），然后执行：

```bash
# 启动所有服务
pm2 start ecosystem.config.js

# 查看服务状态
pm2 status

# 查看日志
pm2 logs
```

## 4. 验证部署 (Verify)

*   **Gateway (入口)**: `http://<服务器IP>:8080`
    *   **用户端 (H5)**: `http://<服务器IP>:8080/`
    *   **后台管理**: `http://<服务器IP>:8080/admin`
    *   API 代理: `http://<服务器IP>:8080/api/...`
    *   WebSocket 代理: `ws://<服务器IP>:8080/ws`
*   **Backend (内部)**: `http://localhost:8081` (通常不对外暴露)

## 5. 常见问题

### WebSocket 连接失败
确保服务器防火墙开放了 **8080** 端口。
如果使用 Nginx 反向代理，请配置 Upgrade 头：

```nginx
location /ws {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

### 数据库连接
目前后端使用 H2 内存数据库/模拟数据。如需连接真实 MySQL，请修改 `backend/src/main/resources/application.properties`。
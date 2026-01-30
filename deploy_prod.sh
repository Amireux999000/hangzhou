#!/bin/bash

# 部署脚本 (Production Deployment Script)

echo "🚀 开始部署直播辩论系统..."

# 1. 构建 Java 后端
echo "📦 构建 Backend..."
cd backend
if [ -f "mvnw" ]; then
    ./mvnw clean package -DskipTests
else
    mvn clean package -DskipTests
fi

if [ $? -ne 0 ]; then
    echo "❌ Backend 构建失败！"
    exit 1
fi
cd ..

# 2. 安装 Gateway 依赖
echo "📦 安装 Gateway 依赖..."
cd gateway
npm install --production
cd ..

# 3. 构建 Frontend (H5)
echo "📦 构建 Frontend H5..."
cd frontend
npm install
npm run build:h5
cd ..

# 4. 启动/重启服务
echo "🔄 重启 PM2 服务..."
pm2 reload ecosystem.config.js --env production || pm2 start ecosystem.config.js --env production

echo "✅ 部署完成！"
echo "Gateway (App): http://localhost:8080"
echo "Gateway (Admin): http://localhost:8080/admin"

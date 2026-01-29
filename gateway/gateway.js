const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');
const cors = require('cors');

const app = express();
const port = 8080;

// Enable CORS for all routes
app.use(cors({
    origin: '*',
    credentials: true
}));

// ==================== Backend Proxy ====================

// API Proxy
// Matches paths starting with /api
// Forwards to http://127.0.0.1:8081/api/...
const apiProxy = createProxyMiddleware({
    target: 'http://127.0.0.1:8081',
    changeOrigin: true,
    pathFilter: (path) => path.startsWith('/api'),
    logger: console
});
app.use(apiProxy);

// WebSocket Proxy
// Matches paths starting with /ws
// Forwards to http://127.0.0.1:8081/ws
const wsProxy = createProxyMiddleware({
    target: 'http://127.0.0.1:8081',
    // changeOrigin: false preserves the original Host header (localhost:8080)
    // This helps pass the Origin check if the backend is lenient or allows *
    changeOrigin: false, 
    ws: true,
    pathFilter: (path) => path.startsWith('/ws'),
    logger: console
});
app.use(wsProxy);


// ==================== Frontend Serving ====================

const adminPath = path.join(__dirname, '../frontend/admin');
const staticPath = path.join(__dirname, '../frontend/static');

// Serve Admin Panel HTML
app.get('/admin', (req, res) => {
    res.sendFile(path.join(adminPath, 'index.html'));
});

// Serve Admin Static Assets
app.use('/admin', express.static(adminPath));

// Serve Shared Static Assets
app.use('/static', express.static(staticPath));


// ==================== Start Server ====================

const server = app.listen(port, '0.0.0.0', () => {
    console.log(`Gateway running on http://localhost:${port}`);
    console.log(`- API Proxy: /api -> http://127.0.0.1:8081/api`);
    console.log(`- WebSocket Proxy: /ws -> http://127.0.0.1:8081/ws`);
    console.log(`- Admin Panel: http://localhost:${port}/admin`);
});

// Handle WebSocket Upgrades
// Critical for ws: true to work with Express + http-proxy-middleware
server.on('upgrade', wsProxy.upgrade);

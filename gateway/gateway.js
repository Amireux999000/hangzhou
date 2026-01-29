const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');
const cors = require('cors');

const app = express();
const port = 8080;

// CORS - Allow all
app.use(cors({
    origin: '*',
    credentials: true
}));

// ==================== Backend Proxy ====================
// Proxy /api requests to Java Backend (8081)
app.use('/api', createProxyMiddleware({
    target: 'http://localhost:8081/api',
    changeOrigin: true,
    ws: true, // Support WebSocket if needed (though we handle /ws separately)
    logLevel: 'debug'
}));

// Proxy /ws WebSocket requests to Java Backend (8081)
app.use('/ws', createProxyMiddleware({
    target: 'http://localhost:8081/ws',
    changeOrigin: true,
    ws: true,
    logLevel: 'debug'
}));

// ==================== Admin Panel (Static) ====================
// Serve Admin HTML
const adminPath = path.join(__dirname, '../frontend/admin');

app.get('/admin', (req, res) => {
    res.sendFile(path.join(adminPath, 'index.html'));
});

// Serve Admin Static Assets
app.use('/admin', express.static(adminPath));

// Serve Static Assets (images, icons from frontend/static)
const staticPath = path.join(__dirname, '../frontend/static');
app.use('/static', express.static(staticPath));

// Start Gateway
const server = app.listen(port, '0.0.0.0', () => {
    console.log(`Gateway running on http://localhost:${port}`);
    console.log(`Proxying /api and /ws to Java Backend at http://localhost:8081`);
    console.log(`Serving Admin Panel at http://localhost:${port}/admin`);
});

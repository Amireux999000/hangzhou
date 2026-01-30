module.exports = {
  apps: [
    {
      name: 'live-backend',
      script: 'java',
      args: ['-jar', 'backend/target/livedebate-0.0.1-SNAPSHOT.jar'],
      instances: 1,
      exec_mode: 'fork',
      watch: false,
      cwd: '.', // Execute from root
      error_file: './logs/backend-error.log',
      out_file: './logs/backend-out.log',
    },
    {
      name: 'live-gateway',
      script: 'gateway/gateway.js',
      instances: 1,
      exec_mode: 'fork',
      watch: ['gateway'],
      ignore_watch: ['node_modules', 'logs'],
      env: {
        NODE_ENV: 'production',
        PORT: 8080
      },
      error_file: './logs/gateway-error.log',
      out_file: './logs/gateway-out.log',
    }
  ]
};
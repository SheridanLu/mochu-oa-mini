# 墨初OA施工管理系统 - 部署指南

## 快速部署(单服务器)

### 1. 安装基础环境

```bash
# CentOS/RHEL
yum update -y
yum install -y java-17-openjdk java-17-openjdk-devel mysql-server nginx

# Ubuntu
apt update -y
apt install -y openjdk-17-jdk mysql-server nginx nodejs npm
```

### 2. 配置MySQL

```bash
# 启动MySQL
systemctl start mysqld
systemctl enable mysqld

# 登录MySQL
mysql -u root -p

# 创建数据库和用户
CREATE DATABASE mochu_oa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'mochu'@'localhost' IDENTIFIED BY 'mochu@2026';
GRANT ALL PRIVILEGES ON mochu_oa.* TO 'mochu'@'localhost';
FLUSH PRIVILEGES;

# 导入SQL
mysql -u mochu -pmochu@2026 mochu_oa < sql/*.sql
```

### 3. 部署后端

```bash
# 编译后端
cd backend
mvn clean package -DskipTests

# 运行后端
nohup java -jar target/mochu-oa-1.0.0.jar --server.port=9090 \
  --spring.datasource.password=mochu@2026 \
  > /var/log/mochu-oa.log 2>&1 &

# 检查启动
tail -f /var/log/mochu-oa.log
```

### 4. 部署前端

```bash
# 编译前端
cd frontend
npm install
npm run build

# 配置Nginx
cat > /etc/nginx/conf.d/mochu-oa.conf << 'EOF'
server {
    listen 80;
    server_name _;
    
    location / {
        root /var/www/mochu-oa/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://127.0.0.1:9090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /sockjs-node {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

# 重启Nginx
nginx -t && systemctl restart nginx
```

---

## Docker Compose 部署(推荐)

### 1. 创建部署目录

```bash
mkdir -p /opt/mochu-oa
cd /opt/mochu-oa
```

### 2. 创建 Docker Compose 文件

```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: mochu-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: mochu@2026
      MYSQL_DATABASE: mochu_oa
      MYSQL_USER: mochu
      MYSQL_PASSWORD: mochu@2026
    ports:
      - "3307:3306"
    volumes:
      - ./mysql/data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    networks:
      - mochu-network

  # 后端服务
  backend:
    build: ./backend
    container_name: mochu-backend
    restart: always
    ports:
      - "9090:9090"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/mochu_oa?useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: mochu
      SPRING_DATASOURCE_PASSWORD: mochu@2026
      SPRING_REDIS_HOST: redis
    depends_on:
      - mysql
      - redis
    networks:
      - mochu-network

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: mochu-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - ./redis/data:/data
    networks:
      - mochu-network

  # 前端服务
  frontend:
    build: ./frontend
    container_name: mochu-frontend
    restart: always
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - mochu-network

networks:
  mochu-network:
    driver: bridge
```

### 3. 构建并启动

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 检查状态
docker-compose ps
```

---

## 生产环境Nginx配置

```nginx
upstream mochu_backend {
    server 127.0.0.1:9090;
}

server {
    listen 443 ssl http2;
    server_name mochu.yourdomain.com;
    
    # SSL证书配置
    ssl_certificate /etc/nginx/ssl/mochu.crt;
    ssl_certificate_key /etc/nginx/ssl/mochu.key;
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
    
    # 前端静态文件
    location / {
        root /var/www/mochu-oa/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
        
        # 缓存配置
        expires 7d;
        add_header Cache-Control "public, immutable";
    }
    
    # API代理
    location /api {
        proxy_pass http://mochu_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # WebSocket支持
    location /sockjs-node {
        proxy_pass http://mochu_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name mochu.yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

---

## 备份与恢复

### 1. 数据库备份

```bash
# 手动备份
mysqldump -u mochu -pmochu@2026 mochu_oa > backup_$(date +%Y%m%d).sql

# 自动备份脚本
#!/bin/bash
BACKUP_DIR="/opt/backup"
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u mochu -pmochu@2026 mochu_oa > $BACKUP_DIR/mochu_oa_$DATE.sql
# 保留30天
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
```

### 2. 数据恢复

```bash
mysql -u mochu -pmochu@2026 mochu_oa < backup_20260414.sql
```

---

## 监控与日志

### 1. 日志位置

- 后端日志: `/var/log/mochu-oa.log` 或 Docker日志 `docker logs mochu-backend`
- Nginx访问日志: `/var/log/nginx/access.log`
- Nginx错误日志: `/var/log/nginx/error.log`

### 2. 健康检查

```bash
# 检查后端API
curl http://localhost:9090/api/common/configs

# 检查前端
curl http://localhost:3000

# 检查MySQL
mysql -u mochu -pmochu@2026 -e "SELECT 1"
```

---

## 常见问题

1. **端口被占用**: 检查并释放端口 `lsof -i:9090`
2. **数据库连接失败**: 检查MySQL服务状态和网络配置
3. **前端加载失败**: 检查Nginx配置和静态文件路径
4. **登录失败**: 确认数据库中的用户数据和密码哈希
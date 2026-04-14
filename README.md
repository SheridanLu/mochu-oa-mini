# 墨初OA施工管理系统

基于 SpringBoot + Vue3 的企业级 OA 办公系统

## 技术栈

- 后端：SpringBoot、MyBatis-Plus、MySQL、Redis
- 前端：Vue3、Vite、Element Plus
- 部署：Docker、Docker Compose

## 项目结构

```
mochu-oa/
├── backend/           # SpringBoot后端
├── frontend/          # Vue3前端
├── sql/               # SQL脚本
├── docker-compose.yml # Docker Compose配置
├── Dockerfile         # 后端Dockerfile
├── DEPLOYMENT.md     # 部署详细文档
└── README.md
```

## 快速部署

### Docker Compose (推荐)

```bash
# 克隆项目
git clone https://github.com/SheridanLu/mochu-oa-mini.git
cd mochu-oa-mini

# 启动所有服务
docker-compose up -d

# 访问地址
# 前端: http://localhost:3000
# 后端: http://localhost:9090
```

### 使用预构建镜像

```bash
# MySQL
docker run -d --name mochu-mysql \
  -e MYSQL_ROOT_PASSWORD=mochu@2026 \
  -e MYSQL_DATABASE=mochu_oa \
  -e MYSQL_USER=mochu \
  -e MYSQL_PASSWORD=mochu@2026 \
  -p 3307:3306 \
  mysql:8.0

# 后端
docker run -d --name mochu-backend \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/mochu_oa \
  -e SPRING_DATASOURCE_USERNAME=mochu \
  -e SPRING_DATASOURCE_PASSWORD=mochu@2026 \
  -p 9090:9090 \
  ghcr.io/sheridanlu/mochu-oa-mini/backend:latest

# 前端
docker run -d --name mochu-frontend \
  -p 3000:80 \
  ghcr.io/sheridanlu/mochu-oa-mini/frontend:latest
```

### 手动部署

```bash
# 1. 数据库
mysql -u root -p < sql/*.sql

# 2. 后端
cd backend
mvn spring-boot:run
# 端口：9090

# 3. 前端
cd frontend
npm install
npm run dev
# 访问：http://localhost:3000
```

## 登录账号

- 用户名：admin
- 密码：admin123

## 功能模块

- 项目管理
- 合同管理
- 采购管理
- 物资管理
- 施工管理（甘特图）
- 财务管理
- 审批流程引擎
- 系统设置

## Docker镜像

- 后端: `ghcr.io/sheridanlu/mochu-oa-mini/backend:latest`
- 前端: `ghcr.io/sheridanlu/mochu-oa-mini/frontend:latest`

## 环境变量

### 后端

| 变量 | 默认值 | 说明 |
|------|--------|------|
| SERVER_PORT | 9090 | 服务端口 |
| SPRING_DATASOURCE_URL | - | 数据库连接 |
| SPRING_DATASOURCE_USERNAME | root | 数据库用户名 |
| SPRING_DATASOURCE_PASSWORD | mochu@2026 | 数据库密码 |
| SPRING_REDIS_HOST | localhost | Redis地址 |
| SPRING_REDIS_PORT | 6379 | Redis端口 |

## 许可证

MIT License
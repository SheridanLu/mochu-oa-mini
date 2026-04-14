# 墨初OA施工管理系统

基于 SpringBoot + Vue3 的企业级 OA 办公系统

## 技术栈
- 后端：SpringBoot、MyBatis-Plus、MySQL
- 前端：Vue3、Vite、Element Plus

## 项目结构
```
mochu-oa/
├── backend/          # SpringBoot后端
├── frontend/         # Vue3前端
├── sql/              # SQL脚本
└── docs/             # 需求文档
```

## 快速启动

### 1. 数据库
```bash
# 创建数据库并导入SQL
mysql -u root -p < sql/mochu_oa.sql
```

### 2. 后端
```bash
cd backend
mvn spring-boot:run
# 端口：9090
```

### 3. 前端
```bash
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
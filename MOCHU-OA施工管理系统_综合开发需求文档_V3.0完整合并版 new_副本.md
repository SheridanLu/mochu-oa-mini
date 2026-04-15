# MOCHU-OA 施工管理系统
## 综合开发需求文档（完整合并版）

**密级：商业机密**
**版本号：V3.0（完整合并版）**
**编制日期：2026年4月4日**
**最后更新：2026年4月13日**

---

## 文档说明

本文档为 MOCHU-OA 施工管理系统的**综合开发需求文档**，**完整合并了以下五个来源文档的全部内容**：

1. 《MOCHU-OA施工管理系统_软件开发需求规格说明书_V2.0完整版》—— 核心需求规格（20个功能模块详细设计）
2. 《MOCHU补充设计说明书》—— 新增工作待办中心、公告审批化改造、智能价格计算、公司信息自动填充
3. 《软件详细设计说明书》—— 系统分层架构、编码规范、审批引擎、缓存设计、数据模型
4. 《开发文档补充开发需求》—— 10项新增功能（广联达导入、照片水印校验、分部分项工程、隐蔽工程联动、DWG图纸管理、设计变更联动、材料采购提醒等），正文整理见**第 3A 章**
5. 《墨初增加需求文档》（`墨初oa系统建设/增加需求文档.txt`）—— 产品价格双库与询价比价、基准价扩展与合同价格确认、出库与跨项目调拨约束、甘特与收入合同拆分明细一体化增强、供应商价格水平等级判定、零星采购超限额（1.5%）审批切换；正文整理见**第 3C 章**

本文档涵盖**功能需求、非功能需求、数据设计、接口设计、流程设计、权限设计、技术架构设计、部署运维、测试交付**等全部内容，适用于开发团队进行系统设计、编码实现、测试验证的全过程指导依据。

---

## 文档修订记录

| 版本号 | 修订日期 | 修订内容 | 修订人 |
|--------|----------|----------|--------|
| V1.0 | 2024-03-15 | 初始版本：涵盖20个功能模块需求规格 | 产品团队 |
| V2.0 | 2026-04-04 | 全面检验与完善：补充系统边界、技术架构、异常处理、UI/UE规范、接口定义 | 产品团队 |
| V2.1 | 2026-04-11 | 整合补充设计：新增待办中心、公告审批化改造、价格联动、公司信息填充、系统分层架构 | 产品团队 + 开发方 |
| V3.0 | 2026-04-11 | **完整合并版**：整合全部四份文档内容，新增10项功能需求（广联达导入、照片水印校验、分部分项工程、隐蔽工程、DWG图纸管理等），补全20个核心业务模块详细设计 | 产品团队 + 开发方 |

---

## 文档修订说明与检验总览

### 一、可行性检验结论

#### 1.1 技术架构选型评估

本系统选用 **Vue3 + Element Plus / Spring Boot 3.x / MySQL 8.0 / Redis 7.x / MinIO / XXL-JOB** 的技术栈组合，经评估整体适配中小型施工企业（50-100 人规模、500 并发）的业务需求。

**前端架构（Vue3 + Element Plus）：** 
- 适配性良好。Vue3 的 Composition API 和 Element Plus 的企业级组件库能满足复杂表单、审批流程、甘特图集成等场景
- 移动端小程序采用 uni-app + Vue3，复用大部分 PC 端业务逻辑
- H5 端直接使用 Element Plus 响应式布局

**后端架构（Spring Boot 3.x + MyBatis-Plus）：** 
- 适配性良好。Spring Boot 3.x 要求 JDK 17+，生态成熟
- MyBatis-Plus 的动态 SQL 拼接能力适合数据权限过滤场景
- Spring Security + JWT 方案满足认证授权需求
- 500 并发场景下单机即可承载

**数据库（MySQL 8.0）：** 
- 适配性良好。MySQL 8.0 的窗口函数、CTE 递归查询能力适合部门树查询、报表聚合等场景
- 500 并发下单库即可满足性能要求，配合连接池（HikariCP，最大连接数 50-80）使用

**缓存（Redis 7.x）：** 
- 部署模式为哨兵模式（1 主 2 从 3 哨兵），内存配置 2-4GB
- 用于 Token 存储、编号生成、权限缓存、登录失败计数

**文件存储（MinIO）：** 
- MinIO 的 S3 兼容 API 适合附件管理场景
- 初期采用单节点 + 定期备份模式，待业务规模扩大后升级为纠删码集群

#### 1.2 开发阶段划分评估

**五阶段开发划分总体合理，模块依赖关系无循环依赖：**

- Phase 0 核心模块（登录 + 权限）的完成作为 Phase 1 可集成的前置里程碑
- Phase 2 拆分为 Phase 2A（采购 + 物资 + 人力资源）和 Phase 2B（施工进度 + 变更 + 财务）
- 各阶段完成标准增加量化验收指标：项目通过率 ≥ 100%、接口自动化测试通过率 ≥ 95%、Bug 清零

#### 1.3 技术栈推荐

**后端：**
- Spring Boot 3.x（JDK 17+）
- Spring Security 6.x
- MyBatis-Plus 3.x
- Redisson（Redis 客户端）
- MinIO SDK
- XXL-JOB 2.4.0+
- Knife4j（OpenAPI 文档）

**前端：**
- Vue 3 + Vite
- TypeScript
- Element Plus 2.x
- axios
- pinia（状态管理）
- vue-router 4
- day.js（时间处理）

**数据库：**
- MySQL 8.0+
- Redis 7.x
- MinIO

---


## 第 1 章 项目概述

### 1.1 项目名称与定位

本系统全称为 **"MOCHU-OA 施工管理系统"**，定位为面向施工企业的综合办公自动化系统。

系统聚焦施工企业在以下核心业务场景中的信息化需求：
- 项目全生命周期管理
- 合同审批管理
- 物资采购与库存管控
- 施工进度跟踪
- 财务成本控制
- 人力资源管理
- 数据分析与决策支持

系统适用于中小型施工企业（员工规模 50-100 人），支持 **Web 端、移动端 H5 和小程序**多端访问。

### 1.2 建设目标

#### 业务目标

实现施工项目从立项、投标、合同签订、施工执行、物资管控、竣工验收到结算归档的全生命周期管理。

**具体目标：**
1. 规范合同审批流程和采购管理流程，杜绝流程外操作
2. 实现精细化库存管理和成本控制，支持项目级成本归集和实时成本分析
3. 建立多角色协同办公体系，实现待办自动推送、审批在线完成、信息实时共享
4. 建立工作待办中心，集中管理所有待处理事项
5. 实现公告审批化管理，确保信息发布的规范性

#### 技术目标

- **可用性：** 系统支持 500 并发用户在线操作，页面加载时间不超过 3 秒
- **接口性能：** 接口响应时间不超过 1 秒，报表查询响应时间不超过 10 秒
- **高可用：** 系统可用性 SLA ≥ 99.9%，核心业务支持高可用部署
- **架构灵活性：** 采用模块化架构设计，支持功能模块独立开发、独立部署、独立升级
- **安全性：** 所有敏感数据加密存储，接口全链路防护，审计日志完整可追溯

### 1.3 目标用户与角色定义

系统设计了 11 个业务角色，核心概述：

| 角色 | 简称 | 核心职责 | 业务范围 |
|------|------|---------|---------|
| 总经理 | GM | 全局审批、系统配置、权限管理 | 全部数据 |
| 项目经理 | PROJ_MGR | 施工进度管理、对账编制、物资出库 | 负责项目 |
| 预算员 | BUDGET | 采购清单编制、收入拆分、成本控制 | 全部项目预算 |
| 采购员 | PURCHASE | 项目立项、合同签订、供应商管理 | 全部项目合同 |
| 财务人员 | FINANCE | 合同财审、付款审批、资金管理 | 全部财务数据 |
| 法务 | LEGAL | 合同法律审核 | 合同管理 |
| 资料员 | DATA | 文档归档、图纸管理、数据维护 | 全部项目资料 |
| 综合人员 | HR | 人事管理、资质维护、工资表 | 全部人员 |
| 基础业务部员工 | BASE | 查看项目、文档上传、日常报销 | 本部门项目 |
| 软件业务部员工 | SOFT | 查看项目、文档上传、日常报销 | 本部门项目 |
| 项目团队成员 | TEAM_MEMBER | 查看项目、文档下载、协作沟通 | 所属项目 |

### 1.4 系统边界与非功能范围

#### 1.4.1 明确排除的功能

以下功能明确**不在本系统建设范围内**：

- **第三方在线支付集成**：系统不集成支付宝、微信支付等渠道。付款通过线下银行转账完成，系统仅记录信息和审批流程
- **智能造价/BIM 集成**：系统不提供基于 AI 的智能造价核算功能，不集成 BIM 三维建模工具
- **招投标管理**：系统不支持在线招投标流程（标书制作、开标评标、中标公示等）
- **考勤打卡**：系统不内置考勤打卡功能，考勤数据通过企业微信或第三方考勤系统获取（预留接口）
- **客户关系管理（CRM）**：系统不提供客户营销、商机管理、客户跟进等功能
- **工程量自动计算**：系统不提供基于图纸的工程量自动计算功能

#### 1.4.2 上下游系统对接边界

| 外部系统 | 对接方式 | 数据交互 | 对接方 | 标准 |
|---------|---------|---------|--------|------|
| 企业微信 | HTTP REST API | 消息推送、通讯录同步、审批通知 | 本系统 | 企业微信 API v2.0 |
| 第三方企业邮箱 | 抽象 EmailService 接口 | 邮箱创建/禁用/密码重置 | 本系统 | 各厂商 API |
| 短信服务 | 抽象 SmsService 接口 | 验证码、系统通知 | 本系统 | 阿里云/腾讯云 SMS API |
| 财务软件 | 标准 API 接口（预留） | 付款凭证导出、收款数据同步 | 双方协商 | JSON/CSV 数据交换 |
| 考勤系统 | 标准 API 接口（预留） | 考勤数据读取 | 待定 | 待定 |

### 1.5 项目实施周期预估

基于模块复杂度，按 6 人开发团队（2 前端 + 3 后端 + 1 测试）配置，实施周期如下：

| 阶段 | 核心模块 | 开发工作量 | 测试工作量 | 日历周期 | 里程碑 |
|------|---------|-----------|-----------|---------|--------|
| Phase 0 | 登录/组织/权限/主页 | 35 人天 | 8 人天 | 4 周 | M0: 系统基座可用 |
| Phase 1 | 项目/合同/模板/供应商 | 60 人天 | 12 人天 | 6 周 | M1: 项目合同流程可用 |
| Phase 2A | 采购/物资/人力资源 | 50 人天 | 10 人天 | 5 周 | M2A: 物资管理可用 |
| Phase 2B | 施工进度/变更/财务 | 60 人天 | 12 人天 | 6 周 | M2B: 项目执行全流程可用 |
| Phase 3 | 竣工/通知/案例展示 | 35 人天 | 8 人天 | 4 周 | M3: 项目闭环可用 |
| Phase 4 | 报表/审计日志 | 35 人天 | 10 人天 | 4 周 | M4: 全系统可用 |

**总计：** 开发约 275 人天，测试约 60 人天，总周期约 29 周，考虑缓冲后为 **7-8 个月**。

---


## 第 2 章 系统总体设计

### 2.1 技术架构选型

系统采用 **前后端分离架构** 设计。

#### 2.1.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                        客户端层                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   PC Web     │  │  Mobile H5   │  │  小程序       │   │
│  │ Vue3 + UI    │  │  Vue3 + UI   │  │  uni-app     │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└────────────────────────┬─────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
┌───────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
│  Nginx       │ │  Nginx      │ │  Nginx      │
│  反向代理    │ │  负载均衡    │ │  静态服务   │
└───────┬──────┘ └──────┬──────┘ └──────┬──────┘
        │                │                │
        └────────────────┼────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   API 网关层                              │
│  ┌──────────────────────────────────────────────────┐  │
│  │  认证/授权  │  限流  │  路由  │  日志  │  追踪    │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
┌───────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
│  模块A       │ │  模块B      │ │  模块C      │
│  微服务/应用 │ │  微服务/应用 │ │  微服务/应用 │
└───────┬──────┘ └──────┬──────┘ └──────┬──────┘
        │                │                │
        └────────────────┼────────────────┘
                         │
       ┌─────────────────┼─────────────────┐
       │                 │                 │
┌──────▼──────┐ ┌────────▼───────┐ ┌──────▼──────┐
│  MySQL 8.0  │ │  Redis 7.x     │ │  MinIO      │
│  主 + 从    │ │  哨兵模式      │ │  对象存储   │
└─────────────┘ └────────────────┘ └─────────────┘
       │                 │                 │
       └─────────────────┼─────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
    ┌────▼────┐   ┌──────▼──────┐  ┌────▼─────┐
    │企业微信  │   │  短信/邮件  │  │ XXL-JOB  │
    │集成      │   │  服务       │  │定时任务  │
    └──────────┘   └─────────────┘  └──────────┘
```

#### 2.1.2 技术组件配置

**MySQL 8.0 配置要求**

| 配置项 | 推荐值 | 说明 |
|--------|--------|------|
| innodb_buffer_pool_size | 物理内存 50%-70%（4GB） | InnoDB 缓冲池大小，直接影响查询性能 |
| max_connections | 200 | 最大连接数，应用层连接池 50-80 |
| innodb_log_file_size | 256MB | redo log 文件大小 |
| character_set_server | utf8mb4 | 统一字符集，支持中文和 emoji |
| collation_server | utf8mb4_general_ci | 排序规则 |
| innodb_flush_log_at_trx_commit | 1 | 每次事务提交刷盘，保证数据安全 |
| slow_query_log | ON（1 秒） | 慢查询日志记录 |
| binlog_format | ROW | 主从复制行模式 |

**Redis 7.x 缓存策略**

| 缓存场景 | Key 格式 | Value | 过期时间 | 更新策略 |
|---------|---------|-------|---------|---------|
| 用户 Token | auth:token:{userId}:{clientType} | JWT Token | 30 天 | 登录时写入，登出时删除 |
| 用户权限 | user:permissions:{userId} | JSON 权限列表 | 与 Token 同步 | 角色变更时删除 |
| 登录失败计数 | auth:login_fail:{username} | 失败次数 | 30 分钟 | INCR 自增 |
| 短信验证码 | sms:phone:{phone} | 6 位验证码 | 5 分钟 | 发送时 SET，验证后 DEL |
| 业务编号 | biz_no:{前缀}:{日期} | 顺序号 | 重置周期 +1 天 | INCR 自增 |
| 通讯录缓存 | contact:list:{deptId} | JSON 员工列表 | 30 分钟 | 人事变动时删除 |
| 首页待办数 | home:todo_count:{userId} | 待办数 | 5 分钟 | 待办产生时删除 |
| 防重放 nonce | api:nonce:{nonce} | 1 | 5 分钟 | SETNX 检查 |

**MinIO 对象存储配置**

- **部署模式**：初期单节点模式，存储桶：mochu-contract、mochu-project、mochu-attachment 等
- **访问策略**：所有桶私有访问，文件下载通过后端签发预签名 URL（有效期 1 小时）
- **文件大小**：单文件最大 50MB，超过 50MB 采用分片上传（5MB 分片）
- **存储路径**：/{桶名}/{业务类型}/{年月}/{原始文件名_时间戳}.{扩展名}

**XXL-JOB 定时任务配置**

| 任务名称 | Cron 表达式 | 执行内容 | 超时时间 | 失败策略 |
|---------|------------|---------|---------|---------|
| 对账单自动生成 | 0 0 6 26 * ? | 为进行中项目生成对账单草稿 | 300 秒 | 失败告警 |
| 工资表自动生成 | 0 0 0 25 * ? | 生成下月工资表草稿 | 300 秒 | 失败告警 |
| 进度预警扫描 | 0 0 0 * * ? | 扫描甘特图预警节点 | 120 秒 | 重试 1 次 |
| 资质到期预警 | 0 30 0 * * ? | 检查资质到期 | 60 秒 | 重试 1 次 |
| 人员合同到期预警 | 0 30 0 * * ? | 检查员工合同到期 | 60 秒 | 重试 1 次 |
| 审计日志归档 | 0 0 2 1 * ? | 归档 12 月前日志 | 600 秒 | 失败告警 |
| 报表预聚合 | 0 0 3 * * ? | 更新报表中间表 | 600 秒 | 失败告警 |

#### 2.1.3 前后端交互规范

**请求头统一配置**

| Header 名称 | 值 | 说明 |
|------------|---|------|
| Content-Type | application/json | 请求体格式，文件上传时为 multipart/form-data |
| Authorization | Bearer {JWT Token} | 认证令牌，登录后必须携带 |
| X-Request-Id | UUID 格式 | 请求唯一标识，用于链路追踪 |
| X-Client-Type | pc / h5 / wxapp | 客户端类型标识 |
| X-Timestamp | 毫秒时间戳 | 防重放攻击用 |
| X-Nonce | 随机字符串（16 位） | 防重放攻击用 |
| X-Sign | HMAC-SHA256 签名 | 对关键参数签名，防篡改 |

**业务异常处理规范**

| 异常类型 | HTTP 状态码 | 处理方式 | 日志级别 |
|---------|------------|---------|---------|
| 请求参数校验失败 | 400 | 返回具体字段错误信息，前端高亮错误字段 | WARN |
| 业务规则校验失败 | 400 | 返回业务错误码和中文提示 | INFO |
| 认证失败/Token 过期 | 401 | 清除本地 Token，跳转登录页 | INFO |
| 权限不足 | 403 | 前端展示"您没有权限执行此操作" | WARN |
| 资源不存在 | 404 | 返回"数据不存在或已被删除" | WARN |
| 数据库唯一约束冲突 | 409 | 返回"数据已存在"提示 | WARN |
| 接口调用频率超限 | 429 | 返回"操作过于频繁，请稍后重试" | WARN |
| 第三方服务调用失败 | 502 | 返回"服务暂时不可用" | ERROR |
| 数据库操作失败 | 500 | 事务回滚，返回"系统繁忙" | ERROR |
| 未知异常 | 500 | 记录完整堆栈，返回"系统繁忙，请稍后重试" | ERROR |

### 2.2 业务架构设计

系统采用 **四层业务架构** 设计：

```
┌─────────────────────────────────────────┐
│   决策分析层                               │
│  报表管理、成本分析、决策支持              │
└─────────────────────────────────────────┘
              △
┌─────────────────────────────────────────┐
│   管理控制层                               │
│  权限RBAC、流程配置、系统维护             │
└─────────────────────────────────────────┘
              △
┌─────────────────────────────────────────┐
│   核心业务层                               │
│  项目、合同、采购、物资、施工、财务        │
└─────────────────────────────────────────┘
              △
┌─────────────────────────────────────────┐
│   基础支撑层                               │
│  登录、通讯录、邮箱、文档管理              │
└─────────────────────────────────────────┘
```

**各层职责详解：**

**基础支撑层** - 系统入口、基础数据管理和公共服务：
- 用户登录认证（用户名/密码、短信验证码）
- 通讯录管理与人员查询
- 企业邮箱集成
- 文档管理与附件存取
- 为上层业务提供用户身份、组织人员、文档附件等公共能力

**核心业务层** - 项目全过程管理和物资管控：
- 项目管理（虚拟/实体立项、状态转换、中止）
- 合同管理（收入/支出合同、状态流转）
- 采购管理（采购清单、批量/零星采购、材料基准价、价格联动计算）
- 物资管理（入库/出库/退库）
- 施工进度管理（里程碑、甘特图、进度填报）
- 变更管理（现场签证、甲方变更、超量采购、劳务签证）
- 财务管理（收入拆分、对账单、收款、付款）
- 竣工管理（完工验收、竣工图纸、劳务结算）

**管理控制层** - 权限控制、流程配置和系统维护：
- 用户管理与权限分配
- 角色权限管理（RBAC 模型）
- 组织架构管理
- 审批流程引擎
- 系统配置管理

**决策分析层** - 数据汇总、经营分析和决策支持：
- 项目综合报表
- 成本分析报表
- 采购分析报表
- 财务分析报表
- 自定义报表

### 2.3 系统分层架构（代码层）

#### 2.3.1 五层应用分层

```
Controller 层
    ↓ (接收请求、参数校验、鉴权)
Application/Facade 层
    ↓ (业务编排、DTO 装配)
Domain Service 层
    ↓ (领域规则、事务控制)
Repository/Mapper 层
    ↓ (数据持久化)
Infrastructure 层
    (外部系统集成、编号生成、缓存等)
```

#### 2.3.2 分层命名规范

| 层级 | 类名后缀 | 职责 | 示例 |
|------|---------|------|------|
| Controller | Controller | 接收请求、参数校验、鉴权入口、响应封装 | ProjectController |
| Application/Facade | AppService | 跨领域业务编排、审批/附件/通知联动、DTO/VO 装配 | ProjectAppService |
| Domain Service | DomainService | 承载领域规则、事务边界控制、状态流转管理 | ProjectDomainService |
| Repository | Repository | 查询与单表操作 | ProjectRepository |
| Mapper | Mapper | 数据持久化与复杂查询 | ProjectMapper |
| Infrastructure | Service/Helper | Redis、MinIO、邮件、编号生成、文件预览 | FileUploadService、NoGenerator |

### 2.4 模块依赖关系与开发阶段

#### 2.4.1 Phase 0（基础期）

**核心任务**：搭建系统基座
**模块**：登录、组织架构、权限、主页
**依赖关系**：

```
用户登录 (无依赖) [P0]
    ↓
认证 Token 生成 → 权限模块
    ↓
组织架构 [P0] → 权限管理 [P0]
    ↓
主页框架 [P0]
```

**完成标准**：
- 系统可正常登录
- 管理员可创建用户、分配角色、配置权限
- 主页可展示导航菜单和待办区域
- 功能点通过率 ≥ 100%、接口测试通过率 ≥ 95%、Bug 清零

#### 2.4.2 Phase 1（核心期）

**核心任务**：建立项目和合同管理
**模块**：项目管理、合同管理、模板、供应商、通讯录
**依赖关系**：

```
权限管理 (Phase 0) → 项目管理 [P1]
项目管理 [P1] → 合同管理 [P1]
项目管理 [P1] + 合同模板 [P1] → 合同管理 [P1]
供应商管理 [P1] (无额外依赖)
通讯录集成 [P1] (无额外依赖)
```

#### 2.4.3 Phase 2A（执行期 A）

**核心任务**：物资采购与人力资源
**模块**：采购管理、物资管理、人力资源

#### 2.4.4 Phase 2B（执行期 B）

**核心任务**：施工执行过程管理
**模块**：施工进度、变更管理、财务管理

#### 2.4.5 Phase 3（收尾期）

**核心任务**：完成项目闭环
**模块**：竣工管理、公告、工程成果展示

#### 2.4.6 Phase 4（分析期）

**核心任务**：建立数据分析能力
**模块**：报表管理、审计日志

---


## 第 3 章 功能模块详细设计

> 本章完整涵盖系统全部功能模块的详细设计，包括 V2.0 原有 20 个核心业务模块、V2.1 新增的待办中心与公告改造、以及 V3.0 新增的 10 项功能需求。


### 3.0 UI/UE 通用设计规范

#### 3.0.1 页面布局统一标准

**列表页统一结构：**
- 顶部：搜索/筛选区域（左对齐表单控件，右对齐搜索/重置按钮）
- 中部：功能按钮区域（左对齐新增/导出按钮，右对齐刷新按钮）
- 下方：数据表格（固定表头，支持列宽拖拽调整）
- 底部：分页控件

**详情页统一结构：**
- 顶部：页面标题和操作按钮
- 主体：信息分组展示（卡片容器，组标题左对齐）
- 底部：关联信息标签页

**表单页统一结构：**
- 表单字段按业务逻辑分组
- 必填字段标签前显示红色星号
- 表单底部：提交/保存草稿/取消按钮
- PC 端最大 800px 居中展示，移动端全宽

#### 3.0.2 通用交互规范

**按钮规范**：
- 主操作（提交/确认）：蓝色实心按钮
- 次操作（取消/返回）：灰色边框按钮
- 危险操作（删除/中止）：红色边框按钮
- 点击后立即 loading 状态，防止重复提交

**确认弹窗规范**：
- 删除操作必须二次确认
- 状态变更操作必须二次确认
- 审批提交弹窗展示审批流程节点列表

**表单校验规范**：
- 前端校验在 blur 事件触发
- 提交时进行全量校验
- 后端校验唯一性/业务规则/关联数据
- 校验不通过时自动滚动到错误字段

**数据展示规范**：
- 金额：千分位格式，保留 2 位小数（如 1,234,567.89）
- 日期：YYYY-MM-DD 格式
- 时间：YYYY-MM-DD HH:mm 格式
- 百分比：XX.XX% 格式
- 手机号：默认脱敏（138****1234）


### 3.1 登录与首页模块

#### 3.1.1 登录功能设计

**二级登录页面**：
- 第一级：输入用户名或手机号，系统能自动识别登录方式
- 第二级：根据输入格式进入密码登录或验证码登录页面

**密码登录流程**：
1. 用户输入用户名和密码
2. 系统检查账号是否锁定（lock_until > 当前时间）
3. 若未锁定，BCrypt 验证密码
4. 密码正确：重置 login_attempts 为 0、更新 last_login_time、生成 JWT Token（30 天有效期）
5. 密码错误：login_attempts 加 1，达到 5 次则锁定 30 分钟

**验证码登录流程**：
1. 用户输入手机号
2. 点击"获取验证码"，系统生成 6 位验证码并发送短信
3. 用户输入验证码，系统校验是否正确且未过期
4. 验证码正确：生成 Token 并跳转主页

**Token 管理**：
- 有效期：30 天
- 格式：JWT，Payload 包含用户 ID、用户名、角色列表、过期时间
- 刷新策略：剩余有效期不足 7 天时自动刷新 Token
- 多设备登录：支持，每个设备持有独立 Token

**设备管理**：
- 同一账号在同类型设备只支持 1 个活跃会话
- 不同类型设备（PC/移动/小程序）各自独立
- 通过 X-Client-Type 请求头区分设备类型

**忘记密码/找回账号**：
- 忘记密码：输入账号 → 验证码 → 新密码设置 → 强制重新登录
- 找回账号：输入绑定手机号 → 系统通过短信发送账号

#### 3.1.2 首页功能设计

**常用功能入口**：
- 系统级默认入口（所有用户可见）：项目列表、通讯录、系统公告
- 角色级默认入口（不同角色展示不同默认入口）
- 用户级个性化入口（用户可自行添加/删除/排序，最多 12 个）

**待办事项提醒**：
- 展示当前用户待办列表（最近 10 条）
- 支持"查看全部"跳转待办列表页
- 待办数据每 5 分钟轮询刷新

**系统公告**：
- 展示最新 5 条公告（按发布时间倒序）
- 过期公告自动隐藏
- 置顶公告始终排在最前

**页面布局**：
- 头部：Logo + 欢迎语 + 用户头像下拉菜单
- 左侧：可折叠导航栏（根据权限动态渲染）
- 主体：常用入口 + 待办 + 公告 + 最近访问


### 3.2 工作待办中心（新增）

#### 3.2.1 功能概述

工作待办中心是系统的核心工作入口，集中管理所有待处理事项，包括待办、已办、已阅三个分类，并与审批引擎、阅办管理、附件查看、流程留痕统一集成。

#### 3.2.2 待办分类规则

| 分类 | 说明 | 数据来源 |
|------|------|---------|
| 待办 | 当前用户待处理事项 | 审批实例当前节点、阅办节点、指派任务 |
| 已办 | 当前用户已处理完成事项 | 审批记录、处理记录 |
| 已阅 | 当前用户已确认阅读事项 | 阅知/已阅记录 |

#### 3.2.3 待办数据模型

**主表 biz_todo：**
```sql
CREATE TABLE biz_todo (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    todo_no VARCHAR(50) UNIQUE NOT NULL,  -- 待办编号
    biz_type VARCHAR(50) NOT NULL,        -- 业务类型（project/contract/approval 等）
    biz_id BIGINT NOT NULL,               -- 业务数据 ID
    title VARCHAR(200) NOT NULL,          -- 待办标题
    todo_category ENUM('todo','done','read') NOT NULL,  -- 分类
    source_type ENUM('approval','notice','system','task') NOT NULL,  -- 来源类型
    handler_id BIGINT NOT NULL,           -- 处理人 ID
    applicant_id BIGINT,                  -- 申请人 ID
    current_node VARCHAR(100),            -- 当前审批节点
    status VARCHAR(50) NOT NULL,          -- 状态
    priority INT DEFAULT 0,               -- 紧急程度
    action_time DATETIME,                 -- 处理时间
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_handler_category_time (handler_id, todo_category, action_time)
);
```

**子表 biz_todo_action_log：**
```sql
CREATE TABLE biz_todo_action_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    todo_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,    -- 操作类型（approve/reject/read 等）
    action_result VARCHAR(50),           -- 操作结果
    opinion TEXT,                        -- 审批意见
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (todo_id) REFERENCES biz_todo(id)
);
```

#### 3.2.4 待办与审批联动机制

**审批流创建时**：
- 为当前审批节点的审批人生成待办
- 为阅办人生成阅办待办
- 为阅知人生成已阅待确认

**审批完成时**：
- 当前待办转为已办
- 审批结束后所有相关节点状态归档
- 阅知确认后转为已阅

#### 3.2.5 待办详情页面

包含以下内容：
- 事项基本信息
- 所属业务单据摘要
- 附件区（支持预览）
- 流程办理时间线
- 节点审批情况
- 当前可执行操作按钮

**时间线内容**：
- 发起时间
- 提交审批
- 各节点审批通过/驳回/转办/加签
- 阅办完成
- 发布完成
- 归档完成

#### 3.2.6 待办接口设计

**查询列表** — GET /api/v1/todo/list

请求参数：
- category：todo / done / read
- keyword：关键词搜索
- startTime / endTime：时间范围
- sortField：time / name
- sortOrder：asc / desc
- page / size

返回：分页列表、数量统计、筛选条件回显

**查询详情** — GET /api/v1/todo/{id}

返回：事项信息、业务摘要、附件列表、流程节点列表、操作日志

**待办数量** — GET /api/v1/todo/count

返回：todoCount、doneCount、readCount

#### 3.2.7 待办性能优化

- 首页仅加载最近 10 条待办
- 待办计数缓存 5 分钟（Redis）
- 详情信息实时查询，不做强缓存
- 列表采用分页查询，索引覆盖 (handler_id, todo_category, action_time)
- 详情聚合时附件与流程可并发查询


### 3.3 公告管理模块（改造升级）

#### 3.3.1 改造目标

1. **BUG 修复**
   - 图片上传无响应问题
   - 文本编辑乱码、不可删除问题
   - 首页图片轮播缺失问题
   - 首页工作待办模块不显示问题

2. **功能增强**
   - 公告支持图片展示
   - 首页公告轮播支持渲染图片

3. **流程改造**
   - 公告不能直接发布，必须进入审批
   - 审批通过后才可正式发布
   - 公告审批任务同步进入工作待办中心

#### 3.3.2 公告状态机

```
┌─────────┐    ┌──────────┐    ┌─────────┐    ┌───────────┐    ┌──────────┐
│  草稿   │───▶│ 待审批   │───▶│ 审批中  │───▶│ 审批通过  │───▶│ 已发布   │
└─────────┘    └──────────┘    └─────────┘    └───────────┘    └──────────┘
                                     ▲              │
                                     │              ▼
                                     └──── 驳回 ────┘

也可直接：已发布 ──▶ 已下线
```

#### 3.3.3 公告生命周期流程

1. **编辑阶段**：用户编辑公告内容、上传图片
2. **草稿保存**：保存至草稿状态，支持后续编辑
3. **提交审批**：点击"提交审批"，公告转为"待审批"状态，生成审批实例
4. **审批阶段**：
   - 审批人审阅公告内容和图片
   - 可通过/驳回/退回修改
   - 所有审批任务进入工作待办中心
5. **发布**：审批通过后自动发布或需点击"发布"按钮
6. **下线**：已发布的公告可手动下线或到期自动下线

#### 3.3.4 公告数据模型

**补充字段 sys_announcement：**
```sql
ALTER TABLE sys_announcement ADD COLUMN (
    cover_image_id BIGINT,               -- 封面图片 ID
    content_images LONGTEXT,             -- 内容中的图片 ID 列表（JSON）
    status ENUM('draft','pending','approving','approved','rejected','published','offline'),
    approval_instance_id BIGINT,         -- 关联审批流实例 ID
    publish_time DATETIME,               -- 发布时间
    expire_time DATETIME,                -- 过期时间
    INDEX idx_status_publish_time (status, publish_time)
);
```

#### 3.3.5 图片上传问题修复

**修复方案**：
- 公告图片上传统一复用附件上传组件
- 富文本内嵌图片与封面图片统一走 `/api/v1/attachment/upload`
- 上传完成后返回 attachmentId、previewUrl、fileName
- 写入 biz_attachment 表，biz_type='announcement'

#### 3.3.6 文本编辑异常修复

**修复方向**：
- 统一采用稳定富文本组件封装
- 全链路 UTF-8/utf8mb4 编码
- 禁止对编辑内容做错误转义二次处理
- 入库前做 XSS 白名单过滤，不破坏 HTML 结构
- 删除操作由编辑器内部 DOM 管理

#### 3.3.7 首页轮播修复

**修复方案**：
- 首页轮播读取已发布且带图片的公告
- 独立定义轮播数据接口
- 增加空态与图片加载失败兜底图
- 轮播组件异步加载失败不影响首页渲染

**轮播数据接口**：GET /api/v1/announcement/carousel

返回：最新 5 条已发布且带封面图的公告

#### 3.3.7.1 首页公告图片滚动播出功能详细设计

##### 功能概述

首页公告图片滚动播出功能是系统首页的核心信息展示模块，用于展示最新的公告信息及其配套的图片。该功能支持自动滚动、手动切换、暂停/继续播放等交互方式。

##### 功能需求

1. **轮播展示**
   - 自动循环播放已发布公告的封面图片
   - 每张图片展示 5 秒后自动切换到下一张
   - 支持手动切换（上一张/下一张按钮）
   - 支持点击圆形指示器快速切换到指定公告
   - 鼠标悬浮时暂停自动播放，移开时恢复

2. **信息展示**
   - 每张轮播卡片展示：标题、摘要、发布日期
   - 标题最多 2 行，超出用省略号处理
   - 摘要最多 1 行，超出用省略号处理
   - 发布日期显示格式：YYYY-MM-DD

3. **数据源**
   - 数据从已审批通过且已发布的公告中获取
   - 优先展示最新发布的公告
   - 显示最多 5 条公告
   - 若公告数少于 2 条，不展示自动播放功能

4. **响应式设计**
   - PC 端：1200px 宽度，高度 400px
   - 平板：768px 宽度，高度 300px
   - 手机端：全宽，高度 240px
   - 图片采用 cover 模式填充，不拉伸变形

##### UI/UX 详细设计

**轮播容器结构**：
```
┌─────────────────────────────────────┐
│    首页公告轮播 - Announcement Carousel
├─────────────────────────────────────┤
│                                         │
│   ┌───────────────────────────┐      │
│   │                           │      │
│   │  [公告图片]   [标题]      │      │
│   │              [摘要]       │      │
│   │              [日期]       │      │
│   │                           │      │
│   └───────────────────────────┘      │
│                                         │
│  ◀️  ●  ●  ○  ○  ▶️                   │
│     1  2  3  4  5                     │
│                                         │
│   Hover: 显示标题、摘要、日期          │
│   Click: 跳转到公告详情                │
│                                         │
└─────────────────────────────────────┘
```

**视觉设计要素**：
- 圆形指示器：直径 10px，活跃状态为蓝色 (#1890FF)，非活跃状态为灰色 (#E8E8E8)
- 上下一张按钮：鼠标悬浮时显示，背景半透明黑色 (#000000 66%)
- 标题区域：背景渐变黑色，顶部透明，底部不透明，文字白色
- 暂停/播放提示：鼠标悬浮时显示 "点击查看详情" 提示
- 图片加载失败：显示灰色占位符 + "图片加载失败" 文案

##### 前端组件设计

**Vue 3 组件代码结构**：

```vue
<template>
  <div class="announcement-carousel" @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave">
    <!-- 轮播容器 -->
    <div class="carousel-container">
      <!-- 图片区域 -->
      <div class="carousel-images">
        <img 
          v-for="(item, index) in announcements" 
          :key="item.id"
          :src="item.coverImage"
          :alt="item.title"
          :class="['carousel-image', { active: currentIndex === index }]"
          @error="handleImageError"
          @click="goToDetail(item.id)"
        />
      </div>
      
      <!-- 标题和摘要区域 -->
      <div class="carousel-info">
        <h3 class="carousel-title">{{ currentAnnouncement?.title }}</h3>
        <p class="carousel-summary">{{ currentAnnouncement?.summary }}</p>
        <p class="carousel-date">{{ formatDate(currentAnnouncement?.publishTime) }}</p>
      </div>
      
      <!-- 上一张按钮 -->
      <button 
        class="carousel-btn carousel-btn-prev"
        @click="prevSlide"
        v-show="announcements.length > 1"
      >
        ◀
      </button>
      
      <!-- 下一张按钮 -->
      <button 
        class="carousel-btn carousel-btn-next"
        @click="nextSlide"
        v-show="announcements.length > 1"
      >
        ▶
      </button>
    </div>
    
    <!-- 指示器 -->
    <div v-if="announcements.length > 1" class="carousel-indicators">
      <span 
        v-for="(_, index) in announcements"
        :key="index"
        :class="['indicator', { active: currentIndex === index }]"
        @click="goToSlide(index)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';

interface Announcement {
  id: number;
  title: string;
  summary: string;
  coverImage: string;
  publishTime: string;
}

const props = defineProps<{
  announcements: Announcement[];
}>();

const currentIndex = ref(0);
const autoPlayTimer = ref<NodeJS.Timeout | null>(null);
const isHovered = ref(false);

const currentAnnouncement = computed(() => props.announcements[currentIndex.value]);

const startAutoPlay = () => {
  if (props.announcements.length < 2 || isHovered.value) return;
  
  autoPlayTimer.value = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % props.announcements.length;
  }, 5000);
};

const stopAutoPlay = () => {
  if (autoPlayTimer.value) {
    clearInterval(autoPlayTimer.value);
    autoPlayTimer.value = null;
  }
};

const handleMouseEnter = () => {
  isHovered.value = true;
  stopAutoPlay();
};

const handleMouseLeave = () => {
  isHovered.value = false;
  startAutoPlay();
};

const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % props.announcements.length;
  stopAutoPlay();
  startAutoPlay();
};

const prevSlide = () => {
  currentIndex.value = (currentIndex.value - 1 + props.announcements.length) % props.announcements.length;
  stopAutoPlay();
  startAutoPlay();
};

const goToSlide = (index: number) => {
  currentIndex.value = index;
  stopAutoPlay();
  startAutoPlay();
};

const goToDetail = (id: number) => {
  // 跳转到公告详情页
  window.location.href = `/announcement/${id}`;
};

const handleImageError = () => {
  console.error('Image load failed for announcement:', currentAnnouncement.value?.id);
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN');
};

onMounted(() => {
  startAutoPlay();
});

onUnmounted(() => {
  stopAutoPlay();
});
</script>

<style scoped>
.announcement-carousel {
  position: relative;
  width: 100%;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
}

.carousel-container {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
}

@media (max-width: 768px) {
  .carousel-container {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .carousel-container {
    height: 240px;
  }
}

.carousel-images {
  position: relative;
  width: 100%;
  height: 100%;
}

.carousel-image {
  position: absolute;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.5s ease-in-out;
  cursor: pointer;
}

.carousel-image.active {
  opacity: 1;
}

.carousel-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  color: white;
  padding: 30px 20px 20px;
  z-index: 2;
}

.carousel-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: bold;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.carousel-summary {
  margin: 0 0 8px 0;
  font-size: 14px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  opacity: 0.9;
}

.carousel-date {
  margin: 0;
  font-size: 12px;
  opacity: 0.8;
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 20px;
  z-index: 3;
  transition: background 0.3s ease;
}

.carousel-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.carousel-btn-prev {
  left: 10px;
}

.carousel-btn-next {
  right: 10px;
}

.carousel-indicators {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 3;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #e8e8e8;
  cursor: pointer;
  transition: background 0.3s ease;
}

.indicator.active {
  background: #1890ff;
}

.indicator:hover {
  background: #1890ff;
}
</style>
```

##### 接口详细定义

**获取轮播数据** — GET /api/v1/announcement/carousel

请求参数：
- limit（可选）：返回条数，默认 5，最大 10

响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 101,
      "title": "2026年安全生产制度发布",
      "summary": "为进一步加强安全生产管理，特发布新版安全生产制度...",
      "coverImage": "https://minio.example.com/mochu-announcement/cover_01.jpg",
      "publishTime": "2026-04-10 14:30:00"
    },
    {
      "id": 102,
      "title": "第一季度项目进度通报",
      "summary": "截至 2026 年 3 月底，完成投资累计 1.2 亿元...",
      "coverImage": "https://minio.example.com/mochu-announcement/cover_02.jpg",
      "publishTime": "2026-04-08 09:00:00"
    }
  ],
  "timestamp": 1680000000000
}
```

##### 性能优化策略

1. **图片加载优化**
   - 使用图片懒加载：仅加载当前及前后两张图片
   - 使用 WebP 格式配合 JPEG 降级方案
   - 图片压缩：建议尺寸 1200×400，文件大小 < 100KB

2. **缓存策略**
   - 轮播数据缓存 10 分钟
   - Redis Key：`home:announcements:carousel`
   - 发布新公告或更新封面时主动删除缓存

3. **渲染优化**
   - 使用 opacity 过渡而非 display 切换，减少重排
   - 定时器使用 requestAnimationFrame 优化
   - 轮播容器使用 will-change 属性优化渲染

##### 错误处理与降级

1. **图片加载失败**
   - 显示灰色占位符
   - 显示文案："图片加载失败，请稍后重试"
   - 提供重试按钮

2. **接口加载失败**
   - 整个轮播组件不渲染
   - 不影响首页其他模块
   - 记录错误日志供运维排查

3. **无数据处理**
   - 公告数 < 1：隐藏轮播组件，改显示"暂无公告"提示
   - 公告数 1：显示单张图片，不显示导航按钮和指示器

##### 轮播数据库设计

**补充 sys_announcement 表字段**：
```sql
ALTER TABLE sys_announcement ADD COLUMN (
    cover_attachment_id BIGINT COMMENT '封面图片ID',
    cover_image_url VARCHAR(500) COMMENT '封面图片URL',
    carousel_display TINYINT DEFAULT 1 COMMENT '是否在轮播显示(0-否,1-是)',
    display_order INT DEFAULT 0 COMMENT '轮播显示顺序',
    INDEX idx_carousel_display (carousel_display, display_order, publish_time)
);
```

#### 3.3.8 审批配置

**支持的审批节点**：
- 部门主管
- HR
- GM
- 自定义角色

**审批动作**：
- 通过
- 驳回
- 退回修改

#### 3.3.9 公告日志记录

**记录范围**：
- 创建草稿
- 编辑内容
- 上传/删除图片
- 提交审批
- 审批通过/驳回/退回修改
- 重新提交
- 发布
- 下线

**日志表 biz_announcement_log：**
```sql
CREATE TABLE biz_announcement_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    announcement_id BIGINT NOT NULL,
    operate_type VARCHAR(50) NOT NULL,
    operate_content TEXT,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(100),
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark TEXT
);
```


### 3.4 价格联动计算与税率管理（新增）

#### 3.4.1 智能价格计算

**适用场景**：
- 项目立项
- 收入/支出合同录入
- 供应商/公司信息录入
- 采购、付款等表单

#### 3.4.2 含税价/不含税价/税率联动

**字段定义**：
- amountWithTax：含税价
- amountWithoutTax：不含税价
- taxRate：税率
- taxAmount：税额

**计算规则**：
```
含税价 = 不含税价 × (1 + 税率)
不含税价 = 含税价 ÷ (1 + 税率)
税额 = 含税价 - 不含税价
```

**税率枚举值**（不允许自由输入）：
- 0% → 0
- 1% → 0.01
- 3% → 0.03
- 6% → 0.06
- 9% → 0.09
- 13% → 0.13

#### 3.4.3 前端交互逻辑

**联动规则**：
- 输入含税价 + 选择税率 → 自动回填不含税价和税额
- 输入不含税价 + 选择税率 → 自动回填含税价和税额
- 未选择税率时禁止联动，提示"请先选择税率"
- 联动后字段保留两位小数
- 记录"最后编辑字段"，避免循环触发

#### 3.4.4 后端校验

- 三值计算一致，允许误差 0.01
- 拒绝非法数值（负数、超范围）
- 税率只接收固定枚举值

#### 3.4.5 前端统一能力

**使用 useTaxAmountLinkage Hook**：

```typescript
const { amountWithTax, amountWithoutTax, taxRate, taxAmount } = useTaxAmountLinkage({
    onAmountWithTaxChange: (value) => { /* 含税价变化时 */ },
    onAmountWithoutTaxChange: (value) => { /* 不含税价变化时 */ },
    onTaxRateChange: (value) => { /* 税率变化时 */ }
});
```


### 3.5 公司信息自动填充（新增）

#### 3.5.1 功能说明

用户在公司名称输入框录入时，系统自动模糊匹配公司基础库，回填：
- 统一社会信用代码
- 公司地址
- 联系人
- 联系电话
- 开户银行
- 银行账号
- 纳税人识别号
- 邮箱

#### 3.5.2 数据源

**新增/统一公司主数据表 biz_company_info**：
```sql
CREATE TABLE biz_company_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(200) NOT NULL,
    credit_code VARCHAR(50),
    address VARCHAR(500),
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    tax_no VARCHAR(50),
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_company_name (company_name)
);
```

#### 3.5.3 交互逻辑

1. 输入公司名称 2 个字符以上触发联想
2. 显示下拉候选列表（模糊匹配）
3. 用户选中后自动回填关联字段
4. 自动填充后的字段可手动修改
5. 若修改后提交，可选反向更新公司基础库

#### 3.5.4 接口设计

**公司模糊搜索**：GET /api/v1/company/search?keyword=xxx
**公司详情**：GET /api/v1/company/{id}
**创建公司**：POST /api/v1/company


### 3.6 通讯录模块

#### 3.6.1 功能设计

**展示方式**：
- 左侧：部门树形结构（与组织架构模块关联）
- 右侧：选中部门下的员工列表

**展示字段**：
- 员工姓名
- 部门
- 职位
- 电话（默认脱敏，管理员可查看完整号码）
- 邮箱

**搜索功能**：
- 支持实时输入联想（2 字符以上触发）
- 搜索范围：姓名、部门名称、职位
- 模糊匹配，按拼音排序

**自动化管理**：
- **入职自动加入**：入职审批通过后自动加入通讯录，分配默认角色，创建邮箱账号
- **离职自动隐藏**：离职审批通过后自动隐藏，停用邮箱账号


---

> 以下为 V2.0 原有 20 个核心业务模块的完整详细设计（组织架构、权限、用户、项目、合同、供应商、采购、物资、施工、变更、财务、竣工、模板、人力资源、通知、报表、审计、工程案例），已从原 V2.0 文档完整迁入。


### 3.7 组织架构模块

#### 3.7.1 功能概述

组织架构模块管理公司的部门层级结构，支持无限级子部门，提供部门的增删改查和树形展示功能。

#### 3.7.2 部门数据表(sys_dept)

id 字段 INT 主键自增；name 字段 VARCHAR(100) 非空；parent_id 字段 INT 默认 0（0 表示根部门）；level 字段 INT 非空（根部门=1）；path 字段 VARCHAR(500) 非空（如"/1/2/21/"）；sort 字段 INT 默认 0；leader_id 字段 INT 可空外键；phone 字段 VARCHAR(20) 可空；remark 字段 VARCHAR(255) 可空；status 字段 TINYINT 默认 1；created_at 和 updated_at 字段 DATETIME。索引：parent_id 普通索引。

#### 3.7.3 预设部门树

总公司（ID=1，根部门）下设五个一级部门：工程项目管理部（ID=2，下设项目团队 A/B/C，ID=21/22/23）、基础业务部（ID=3）、软件业务部（ID=4）、财务/综合部（ID=5，下设财务组 ID=51、综合组 ID=52）、技术支撑部（ID=6，下设预算组 ID=61、采购组 ID=62、资料组 ID=63）。

#### 3.7.4 操作与权限

部门管理操作包括新增部门（必填名称和上级部门）、编辑部门信息、停用部门（停用前校验部门下是否有在职员工，有则禁止停用）、查看部门树。操作权限仅 GM 角色和具有 system:dept-manage 权限的用户可操作。

#### 3.7.5 接口设计

获取部门树：GET /api/v1/admin/depts/tree。新增部门：POST /api/v1/admin/depts。编辑部门：PUT /api/v1/admin/depts/{id}。停用/启用部门：PATCH /api/v1/admin/depts/{id}/status。

### 3.8 权限管理模块

#### 3.8.1 RBAC 权限模型

系统采用基于角色的访问控制（RBAC）模型。核心设计原则：用户是系统的操作主体；角色是权限的集合，代表一类职责；权限是对某个资源或操作的访问许可；用户通过被赋予一个或多个角色获得相应的权限集合，权限不直接授予用户。

**权限分类：** 功能权限控制用户能否访问某个功能或执行某个操作，命名规范为"模块:操作"（如 project:create）。数据权限控制用户能查看或操作哪些范围内的数据，分为四个级别——全部数据、本部门数据、本人负责数据、指定范围。指定范围通过 sys_role_data_scope 表关联角色与具体项目/部门 ID，MyBatis 拦截器根据此表动态拼接 WHERE 条件。

**角色互斥规则：** 采购员与财务人员不可同时赋予同一用户，以确保审批流程的职责分离。系统在角色分配时自动校验互斥规则。

**委托代理机制：** 用户 A 可将自己的部分权限临时委托给用户 B，设置生效时间段，到期自动回收。委托记录存储在 sys_delegation 表中。

#### 3.8.2 权限点完整列表

**项目管理权限（6 个）：** project:create（发起项目立项）、project:approve（审批项目立项）、project:convert（虚拟项目转实体）、project:terminate（虚拟项目中止）、project:view-all（查看所有项目）、project:view-own（查看本人相关项目）。

**合同管理权限（7 个）：** contract:template-manage（合同模板管理）、contract:sign-income（签订收入合同）、contract:sign-expense（签订支出合同）、contract:approve-finance（合同财务审批）、contract:approve-legal（合同法务审批）、contract:approve-gm（合同总经理审批）、contract:link（合同关联操作）。

**采购与物资权限（7 个）：** purchase:list-manage（采购清单管理）、purchase:check-overbuy（超量采购校验）、material:inbound（物资入库操作）、material:inbound-approve（物资入库审批）、material:outbound（物资出库操作）、material:outbound-approve（物资出库审批）、material:return（物资退库操作）。

**施工管理权限（7 个）：** progress:report（进度填报）、progress:view（进度查看）、progress:correct（进度纠偏）、change:apply（变更申请）、change:approve（变更审批）、statement:apply（对账单申请）、statement:approve（对账单审批）。

**人事管理权限（8 个）：** hr:entry-process、hr:resign-process、hr:certificate-manage、hr:salary-adjust、hr:salary-approve、hr:asset-transfer、hr:contract-manage、hr:contract-view-own。

**财务管理权限（3 个）：** finance:reimburse-approve、finance:payment、finance:report-view。

**文档知识库权限（3 个）：** doc:upload、doc:download、doc:manage。

**报表权限（2 个）：** report:view-all、report:view-project。

**系统管理权限（4 个）：** system:user-manage、system:role-manage、system:dept-manage、system:log-view。

#### 3.8.3 角色与权限映射

**GM（总经理）：** 拥有全部权限点，数据权限为"全部数据"。

**PROJ_MGR（项目经理）：** 拥有 project:view-own、progress:report、progress:view、change:apply、statement:apply、material:outbound、doc:upload、doc:download、report:view-project、hr:contract-view-own。数据权限为"本人负责的项目数据"。

**BUDGET（预算员）：** 拥有 purchase:list-manage、purchase:check-overbuy、change:approve、project:view-all、progress:view、report:view-all、doc:upload、doc:download。数据权限为"全部项目数据"（预算视角）。

**PURCHASE（采购员）：** 拥有 project:create、project:convert、project:terminate、contract:sign-income、contract:sign-expense、contract:template-manage、material:inbound、doc:upload、doc:download、project:view-all。数据权限为"全部项目数据"（采购视角）。

**FINANCE（财务人员）：** 拥有 project:approve、contract:approve-finance、material:inbound-approve、material:outbound-approve、finance:reimburse-approve、finance:payment、finance:report-view、hr:salary-adjust、report:view-all。数据权限为"全部财务数据"。

**LEGAL（法务）：** 拥有 contract:approve-legal、contract:template-manage（只读审核）。数据权限为"合同模块数据"。

**DATA（资料员）：** 拥有 doc:upload、doc:download、doc:manage、material:inbound（查看）、material:outbound（查看）、progress:view、report:view-project。数据权限为"全部项目文档数据"。

**HR（综合人员）：** 拥有 hr:entry-process、hr:resign-process、hr:certificate-manage、hr:salary-adjust、hr:asset-transfer、hr:contract-manage、system:user-manage。数据权限为"全部人员数据"。

**BASE（基础业务部员工）和 SOFT（软件业务部员工）：** 拥有 project:view-own、doc:upload、doc:download、hr:contract-view-own、progress:view。数据权限为"本人关联数据"。

**TEAM_MEMBER（项目团队成员）：** 拥有 project:view-own、progress:view、doc:download、hr:contract-view-own。数据权限为"所属项目数据"。

#### 3.8.4 权限校验实现

**前端实现：** 菜单动态加载——用户登录后根据权限列表动态生成导航菜单。按钮级控制——使用自定义指令 v-permission="'project:create'"。路由守卫——在 Vue Router 的 beforeEach 钩子中校验权限。除 GM 外，所有角色的导航栏不展示"角色管理"和"权限分配"功能菜单。

**后端实现：** 接口拦截——使用 Spring Security 的 FilterChain 提取 Token 解析权限。注解控制——@PreAuthorize("hasPermission('project:create')")。数据权限过滤——MyBatis 拦截器中根据数据权限级别动态拼接 SQL WHERE 条件。

**权限缓存：** 用户登录后将权限列表存入 Redis（key: user:permissions:{userId}）。角色权限变更时通过 Redis Pub/Sub 清除受影响用户的缓存。

#### 3.8.5 权限相关数据表

**角色表(sys_role)：** id INT 主键自增；role_code VARCHAR(50) 非空唯一；role_name VARCHAR(100) 非空；data_scope TINYINT 默认 3（1 全部/2 本部门/3 本人/4 指定范围）；remark VARCHAR(255)；status TINYINT 默认 1；created_at、updated_at。

**权限表(sys_permission)：** id INT 主键自增；perm_code VARCHAR(100) 非空唯一；perm_name VARCHAR(100) 非空；module VARCHAR(50)；perm_type TINYINT（1 功能权限/2 数据权限）。

**角色权限关联表(sys_role_permission)：** role_id INT，permission_id INT，联合主键。

**用户角色关联表(sys_user_role)：** user_id INT，role_id INT，联合主键。

### 3.9 用户管理模块

#### 3.9.1 功能概述

用户管理模块提供系统用户的增删改查、启用禁用、密码重置等管理功能。仅具有 system:user-manage 权限的角色（GM、HR）可操作。

#### 3.9.2 用户列表与搜索

列表字段：姓名、登录名、部门、职位、角色、手机号（脱敏）、邮箱、状态、最后登录时间、操作（编辑/启禁用/重置密码）。搜索条件：姓名（模糊）、部门（下拉选择）、角色（下拉选择）、状态（下拉选择）。支持分页，默认每页 20 条。

#### 3.9.3 用户数据表(sys_user)

id INT 主键自增；username VARCHAR(50) 非空唯一索引；real_name VARCHAR(50) 非空；phone VARCHAR(20) 非空唯一索引；email VARCHAR(100) 可空；dept_id INT 非空外键；position VARCHAR(100) 可空；password_hash VARCHAR(255) 非空；avatar VARCHAR(500) 可空；status TINYINT 默认 1；flag_contact TINYINT 默认 1；privacy_mode TINYINT 默认 0；login_attempts INT 默认 0；last_login_time DATETIME 可空；lock_until DATETIME 可空；force_change_pwd TINYINT 默认 0；wx_userid VARCHAR(100) 可空（企业微信用户 ID）；created_at、updated_at DATETIME。

#### 3.9.4 新增用户流程

管理员填写表单：姓名（必填）、登录名（必填，支持自动生成）、手机号（必填，校验唯一性和格式）、邮箱（选填）、部门（必填，从部门树选择）、职位（选填）、角色（必填，多选，校验互斥规则）、初始密码、是否首次登录修改密码。提交后创建用户记录、角色关联记录，同步创建邮箱。

#### 3.9.5 禁用影响

账号禁用后：用户无法登录，已登录 Token 立即失效（Redis 删除）；通讯录隐藏；企业邮箱自动禁用；权限缓存清除；待办任务保留，待重新启用后可继续处理。

#### 3.9.6 接口设计

用户列表：GET /api/v1/admin/users。新增用户：POST /api/v1/admin/users。编辑用户：PUT /api/v1/admin/users/{id}。启用/禁用：PATCH /api/v1/admin/users/{id}/status。重置密码：POST /api/v1/admin/users/{id}/reset-password。

### 3.10 项目管理模块

#### 3.10.1 功能概述

项目管理模块支持虚拟项目和实体项目两种类型的全生命周期管理，包括立项、转换、中止、暂停、关闭等操作。

#### 3.10.2 项目类型与编号规则

**虚拟项目编号：** V+YYMM+3 位顺序号（如 V2403001），共 8 位字符。每月重置。虚拟项目状态：跟踪中（初始）、已转实体（终态）、已中止（终态）。

**实体项目编号：** P+YYMMDD+3 位顺序号（如 P240315001），共 10 位字符。每日重置。实体项目状态：进行中、已暂停、已完工验收、已竣工验收、已完成审计、已关闭。

**项目分类（合同类型）：** 设备类采购合同、材料类采购合同、劳务施工合同、技术服务合同、软件工程合同、专业工程分包合同、其他类型采购合同。

#### 3.10.3 实体项目立项

**操作角色：** 采购员发起。

**立项字段：** 合同名称（VARCHAR(200)，必填）、项目别名（VARCHAR(100)，选填）、项目地点（VARCHAR(200)，必填）、合同含税金额（DECIMAL(14,2)，必填）、不含税金额（DECIMAL(14,2)，必填）、税金（DECIMAL(14,2)，自动计算）、税率（DECIMAL(5,2)，必填，下拉选择 0%/1%/3%/6%/9%/13%）、甲方信息（VARCHAR(200)，必填）、合同类型（VARCHAR(50)，必填）、计划开始时间（DATE，必填）、计划结束时间（DATE，必填，须晚于开始时间）、合同约定付款批次（子表，至少一条）、保修期计划时间（DATE，选填）、项目经理（INT，必填）、附件（支持多文件上传）、备注（TEXT，选填）。

**分部分项工程类型选择：** 新增**下拉多选菜单**（必填，至少选 1 项），支持勾选以下 12 类标准工程类型：地基与基础工程、主体结构工程、建筑装饰装修工程、屋面及防水工程、建筑给水排水及供暖工程、建筑电气工程、通风与空调工程、智能建筑工程、市政道路工程、市政管网工程、园林绿化工程、其他专业工程。选中的工程类型将逗号分隔存储至 biz_project 表的 subsection_types 字段，用于后续竣工资料校验、验收节点自动生成。

**安全施工险预警等级：** 新增**下拉单选字段**（选填），系统根据项目规模、工程类型自动推荐初始等级，采购员可手动调整：
- 无特殊风险（默认）
- 一般风险（可能影响工期或安全）
- 中等风险（需要重点预防）
- 高风险（需补充风险预防方案）

高风险等级的项目在审批时需补充风险预防措施方案（富文本字段）。

**项目风险预评级：** 系统自动展示（只读，彩色标签）：根据合同金额规模、工程类型数量、工期紧张度自动评估：
- 绿色低风险（< 100万）
- 黄色中风险（100-500万或≥8项工程类型）
- 红色高风险（> 500万或工期< 90天或高风险等级）

**审批流程：** 采购员提交 → 财务审批 → 总经理审批。驳回至发起人。审批全部通过后自动生成实体项目编号，项目状态置为"进行中"。

**异常处理：** 含税金额、不含税金额、税率三者校验不一致时（允许误差 0.01 元）提示检查。付款批次比例之和超过 100% 时禁止提交。未选择分部分项工程类型时禁止提交。红色高风险评级的项目必须补充风险预防方案才允许提交审批。

#### 3.10.4 虚拟项目立项

**操作角色：** 采购员发起。

**立项字段：** 虚拟合同名称（必填）、项目地点（必填）、虚拟合同含税金额（必填）、甲方信息（必填）、合同类型（必填）、拟投入项目金额限额（DECIMAL(14,2)，必填，单位元，前端展示时可转换为万元）、预计投标时间（DATE，选填）、备注、附件。

**分部分项工程类型选择：** 新增**下拉多选菜单**（必填，至少选 1 项），同实体项目，支持勾选 12 类标准工程类型，存储至 biz_project 表的 subsection_types 字段。

**安全施工险预警等级：** 新增**下拉单选字段**（选填），虚拟项目初期可设为默认"无特殊风险"，转实体项目前需要调整为准确等级。

**项目风险预评级：** 系统自动展示（只读，彩色标签），根据虚拟合同金额、预计工程类型、招标预算自动评估等级。

**审批流程：** 采购员提交 → 财务审批 → 总经理审批。

**拟投入金额限额控制：** 虚拟项目下签订支出合同时，若"累计金额 + 本次合同金额"超过限额，系统拦截并提示"超出虚拟项目投入限额"。

#### 3.10.5 虚拟项目转实体项目

**触发条件：** 取得中标通知书后，由采购员发起转换申请。

**操作流程：** 采购员在虚拟项目详情页点击"转为实体项目"按钮；上传中标通知书（必填）、填写中标日期、中标金额，完善实体项目信息；提交审批：采购员 → 财务 → 总经理。

**数据归集规则：** 审批通过后：虚拟项目状态变为"已转实体"；所有支出合同自动关联新实体项目编号；文档、成本记录自动归集；审批记录保留可追溯；待办/通知自动关联。

#### 3.10.6 虚拟项目中止

采购员发起中止申请，填写中止原因和成本下挂目标（从进行中的实体项目或"其他综合成本"中选择），提交审批：采购员 → 财务 → 总经理。中止后项目冻结为只读，成本归集到所选目标项目。

#### 3.10.7 虚拟项目业务特殊规则

虚拟项目下可签订支出合同，但不触发自动超量校验（由预算员人工审核）。价格预警规则与实体项目一致。零星采购超金额预警阈值为批量采购总额的 1.5%。所有支出合同累计金额不得超过拟投入金额限额。

#### 3.10.8 项目暂停与关闭

**项目暂停：** 项目经理发起 → 总经理审批。暂停期间禁止新建合同、入库单、付款申请，但允许查看历史数据和上传文档。已发起但未完成的审批流程保持原状，审批人仍可正常审批。恢复操作由项目经理发起 → 总经理审批，通过后系统自动检查暂停期间是否有逾期的甘特图任务节点。

**项目关闭：** 竣工结算审批全部完成后系统自动关闭，也支持总经理手动关闭。关闭后所有功能冻结为只读。

#### 3.10.9 项目数据表(biz_project)

id INT 主键自增；project_no VARCHAR(20) 非空唯一索引；project_name VARCHAR(200) 非空；project_alias VARCHAR(100) 可空；project_type TINYINT 非空（1 虚拟/2 实体）；contract_type VARCHAR(50) 非空；location VARCHAR(200)；amount_with_tax DECIMAL(14,2)；amount_without_tax DECIMAL(14,2)；tax_amount DECIMAL(14,2)；tax_rate DECIMAL(5,2)；client_name VARCHAR(200)；plan_start_date DATE；plan_end_date DATE；warranty_date DATE；subsection_types VARCHAR(500) 可空（JSON数组格式存储选中的分部分项工程类型代码）；safety_insurance_level TINYINT 可空（安全施工险预警等级：1=无特殊风险/2=一般风险/3=中等风险/4=高风险）；risk_grade VARCHAR(10) 可空（项目风险预评级：A/B/C/D）；status VARCHAR(20) 非空；manager_id INT 可空外键；invest_limit DECIMAL(14,2) 可空（拟投入金额限额，单位元）；bid_time DATE 可空；source_project_id INT 可空；cost_target_project_id INT 可空；creator_id INT 非空；remark TEXT；created_at、updated_at DATETIME。索引：project_no 唯一索引，project_type+status 组合索引，manager_id 普通索引，creator_id 普通索引，risk_grade 普通索引（用于风险看板）。

#### 3.10.10 接口设计

项目列表：GET /api/v1/project/list。项目详情：GET /api/v1/project/{id}。实体项目立项：POST /api/v1/project/entity。虚拟项目立项：POST /api/v1/project/virtual。虚拟转实体：POST /api/v1/project/{id}/convert。虚拟中止：POST /api/v1/project/{id}/terminate。项目暂停：POST /api/v1/project/{id}/suspend。项目恢复：POST /api/v1/project/{id}/resume。

#### 3.10.11 项目状态流转规则

**虚拟项目状态流转：** 跟踪中（初始态）→ 已转实体（终态）或已中止（终态）。跟踪中 → 已转实体需"虚拟转实体"审批通过；跟踪中 → 已中止需"虚拟项目中止"审批通过。

**实体项目状态流转：** 进行中 → 已暂停（暂停审批通过）→ 进行中（恢复审批通过）。进行中 → 已完工验收（完工验收审批通过，前置校验：所有入库单须审批完成）→ 已竣工验收 → 已完成审计 → 已关闭（终态）。

#### 3.10.12 项目编号生成异常兜底方案

**Redis 故障兜底：** Redis 不可用时切换为数据库兜底方案——使用 SELECT ... FOR UPDATE 对编号种子表加行锁。种子表结构为 biz_no_seed（prefix VARCHAR(10), date_part VARCHAR(10), current_seq INT, PRIMARY KEY(prefix, date_part)）。

**编号重复兜底：** 数据库所有编号字段均建有唯一索引。极端并发导致重复时捕获 DuplicateKeyException 后自动重试（最多 3 次），3 次仍失败则返回错误提示并记录 ERROR 日志。

### 3.11 合同管理模块

#### 3.11.1 功能概述

合同管理模块涵盖收入合同和支出合同的签订、审批、校验、关联等全流程管理。

#### 3.11.2 收入合同签订

**操作角色：** 采购员发起。**合同编号：** IC+YYMMDD+2 位顺序号（如 IC24031501），每日重置。

**签订字段：** 合同名称（必填）、合同编号（自动生成）、关联项目（必填，"进行中"状态的实体项目）、合同模板（选填）、合同类型（必填）、甲方信息（必填）、合同含税金额（必填）、不含税金额（必填）、税率（必填）、税金（自动计算）、签订日期（必填）、开始日期（必填）、结束日期（必填）、保修期（选填）、付款计划（子表，至少一条）、合同文件（必填）、备注。

**审批流程：** 采购员提交 → 财务审批 → 法务审批 → 总经理审批。驳回至发起人。

#### 3.11.3 支出合同签订

**操作角色：** 采购员发起。**合同编号：** EC+YYMMDD+2 位顺序号，每日重置。

**签订字段：** 合同名称（必填）、合同编号（自动）、关联项目（必填）、关联收入合同（实体项目下必填，虚拟项目下非必填）、合同模板（必填）、合同类型（必填，联动模板列表）、运输方式（必填）、接货人（选填）、交货正负差（选填）、交货期限（选填）、货款结算方式（选填）、验收负责人（选填）、质保期（选填）、供应商/分包商（必填）、合同含税金额（必填）、税率（必填）、不含税金额（自动）、税金（自动）、签订日期（必填）、开始日期（必填）、结束日期（必填）、关联采购清单（实体项目下必填）、物资明细（子表，至少一条）、合同文件（必填）、备注。

**审批流程（常规）：** 采购员提交 → 财务审批 → 法务审批 → 总经理审批。
**审批流程（超量采购）：** 采购员提交 → 预算员审批 → 财务审批 → 法务审批 → 总经理审批。
**驳回策略：** 驳回至发起人，保留审批意见记录，不清除已填数据。

#### 3.11.4 支出合同校验规则

**超量校验（实体项目）：** 提交审批时自动触发，遍历物资明细中每条物资匹配采购清单对应条目，若任一物资超量或无对应条目则标记超量并插入预算员审批节点，采购员须填写超量原因（不少于 10 字符）。虚拟项目不触发。

**价格预警（全局）：** 保存或提交时实时比对合同物资单价与材料基准价。超基准价 1% 以内为黄色预警（不阻止），超 1% 以上为红色预警（须确认知悉并填写价格说明，不少于 10 字符）。虚拟项目同样触发。

**数据校验：** 设备类合同必填物资名称、单位、数量；材料类合同必填物资名称、规格型号、单位、数量。

#### 3.11.5 零星采购超金额校验

签订零星采购类支出合同提交时自动触发。校验累计零星采购金额 + 本次金额是否超过批量采购总额的 1.5%。超过则审批流程变为：采购员 → 预算员 → 总经理。

#### 3.11.6 合同补充协议

支持在已签订合同上签订补充协议，关联原合同编号。补充协议编号：BC+YYMMDD+2 位顺序号。包含补充内容（富文本）、金额变更、工期变更（天数）、附件。审批通过后原合同金额自动累加。审批流程与对应合同类型一致。

#### 3.11.7 合同数据表(biz_contract)

id INT 主键自增；contract_no VARCHAR(20) 非空唯一索引；contract_name VARCHAR(200) 非空；contract_category TINYINT 非空（1 收入/2 支出）；contract_type VARCHAR(50) 非空；project_id INT 非空外键；income_contract_id INT 可空外键；purchase_list_id INT 可空外键；template_id INT 可空；supplier_id INT 可空外键；amount_with_tax DECIMAL(14,2)；amount_without_tax DECIMAL(14,2)；tax_rate DECIMAL(5,2)；tax_amount DECIMAL(14,2)；sign_date DATE；start_date DATE；end_date DATE；warranty_date DATE 可空；transport_method VARCHAR(50) 可空；receiver VARCHAR(50) 可空；delivery_tolerance VARCHAR(50) 可空；delivery_deadline DATE 可空；settlement_method VARCHAR(100) 可空；acceptance_person VARCHAR(50) 可空；quality_period VARCHAR(50) 可空；parent_contract_id INT 可空；status VARCHAR(20) 默认 'draft'；creator_id INT 非空；remark TEXT；created_at、updated_at DATETIME。索引：contract_no 唯一索引，project_id 普通索引，supplier_id 普通索引，contract_category+status 组合索引，project_id+contract_category 联合索引，status+sign_date 联合索引。

#### 3.11.8 接口设计

合同列表：GET /api/v1/contract/list。合同详情：GET /api/v1/contract/{id}。签订收入合同：POST /api/v1/contract/income。签订支出合同：POST /api/v1/contract/expense。超量校验预检：POST /api/v1/contract/expense/check-overquantity。价格预警预检：POST /api/v1/contract/expense/check-price。合同补充协议：POST /api/v1/contract/{id}/supplement。

#### 3.11.9 合同全生命周期状态流转规则

合同包含 5 个状态：draft（草稿）→ pending（待审批）→ approved（已审批）或 rejected（已驳回）→ terminated（已终止）。

- **draft → pending：** 所有必填字段已填写，附件已上传，业务校验通过。
- **pending → approved：** 审批全部通过。执行：支出合同自动累加采购清单已采购数量；自动生成入库单草稿；自动更新材料基准价。
- **pending → rejected：** 任一审批节点不同意。状态回到 draft，审批意见保留。
- **approved → terminated：** 总经理手动终止。前置校验：需确认无未完成的入库单或付款申请。终止后只读。

#### 3.11.10 补充协议对原合同影响规则

**金额变更：** 审批通过后原合同 amount_with_tax 自动累加补充协议金额变更值。系统自动重新校验付款累计金额和采购清单超量基准。

**工期变更：** 审批通过后自动更新原合同 end_date 字段，推送通知给项目经理提示检查甘特图。甘特图不自动调整。

**版本管理：** 每次补充协议自动保存合同历史版本快照（biz_contract_version 表），支持版本间关键字段对比。

#### 3.11.11 合同模板导入与智能签订操作

**采购员合同签订流程优化**：
- 进入合同签订录入页面时，选择合同类型后系统自动匹配并加载对应的导入模板
- 模板内容以**富文本只读 + 可编辑混合**模式展示
- 下划线标记区域显示为**白色背景**，允许输入编辑其他区域显示为**灰色背景**，禁止修改和删除

**供应商自动填充**：
- 采购员在表单中选择供应商后，系统自动从 biz_supplier 表回填：供应商全称、联系人、联系电话、公司地址、开户银行、银行账号
- 自动填充的内容支持在当次合同中进行修改（仅影响本合同，不改变供应商主数据）

**表单与合同正文联动**：
- 金额、税率、工期、项目信息等关键字段输入后实时同步到合同正文的对应位置
- 采用双向数据绑定，确保表单与合同显示一致性

#### 3.11.12 合同签订防篡改机制

**操作限制规则**：
- 非下划线正文区域：禁止选中、禁止修改、禁止删除、禁止粘贴
- 复制操作时仅复制可编辑区域内容，不复制受保护的固定文本
- 用户尝试修改受保护区域时，系统立即给出提示："此区域为合同固定条款，禁止修改"

**系统校验机制**：
- 提交审批前后端同时进行防篡改校验
- 检测是否存在非授权修改，若发现异常立即拦截提交
- 审批通过后将合同内容标记为"已锁定"，之后任何修改操作严格校验

**审计与追踪**：
- 每次编辑操作记入 sys_audit_log 
- 防篡改校验流程和结果存入 biz_contract_tamper_check 表
- 生成的水印和打印记录均可追溯

#### 3.11.13 合同自动水印与打印功能

**自动生成 PDF 水印**：
- 合同审批全部通过后，系统自动生成合同的 PDF 版本
- **自动叠加 MOCHU 水印**：
  - 水印内容：MOCHU-OA 正式合同
  - 位置：居中斜铺（45 度角）
  - 透明度：30%-50%
  - 水印集成于 PDF 文档层，无法被移除或覆盖

**一键打印功能**：
- 位置：合同详情页右上角，审批完成后显示"打印"按钮
- 打印内容：合同完整正文和所有编辑内容
- 打印方式：去掉水印后打印（打印版本为可打印格式），自动 A4 分页
- 输出选项：直接打印到连接的打印机、或导出为 PDF 文件
- 打印前预览：用户可查看打印效果，确认无误后执行打印

**打印日志**：打印操作记入 sys_audit_log，记录打印人、打印时间、IP 地址

### 3.12 供应商管理模块

> **与第 3C 章联动：** 询价信息归集、产品价格双库与自动比价报告、供应商价格水平等级（设备/材料/劳务分榜）见 **§3C.1～§3C.5**、**§3C.8**。

#### 3.12.1 功能概述

供应商管理模块维护供应商基础信息和银行信息，供合同签订时选择引用。采购员可直接创建和管理供应商，无需审批。

#### 3.12.2 供应商字段

供应商名称（VARCHAR(200)，必填）、联系人（VARCHAR(50)，必填）、联系电话（VARCHAR(20)，必填）、邮箱（VARCHAR(100)，选填）、地址（VARCHAR(300)，选填）、联系人负责区域（VARCHAR(200)，选填）、纳税人识别号（VARCHAR(50)，选填）、开户银行（VARCHAR(100)，选填）、银行账号（VARCHAR(50)，选填）、状态（TINYINT，1 正常/0 停用，默认 1）。

#### 3.12.3 数据表(biz_supplier)

id INT 主键自增；supplier_name VARCHAR(200) 非空；contact_name VARCHAR(50)；contact_phone VARCHAR(20)；email VARCHAR(100)；address VARCHAR(300)；region VARCHAR(200)；tax_no VARCHAR(50)；bank_name VARCHAR(100)；bank_account VARCHAR(50)；status TINYINT 默认 1；creator_id INT；created_at、updated_at DATETIME。

#### 3.12.4 接口设计

供应商列表：GET /api/v1/supplier/list。新增：POST /api/v1/supplier。编辑：PUT /api/v1/supplier/{id}。变更状态：PATCH /api/v1/supplier/{id}/status。

#### 3.12.5 供应商信息收集扩展

**新增信息维护字段**：在供应商新增/编辑页面补充以下可维护信息：

| 字段 | 类型 | 说明 | 必填 |
|------|------|------|------|
| supply_content | VARCHAR(500) | 供应产品主要内容（主营材料/设备/服务范围）| 选填 |
| payment_term | VARCHAR(50) | 合作账期（现款、月结30/60/90天、季度结等）| 选填 |
| legal_person | VARCHAR(100) | 法人代表名称 | 选填 |
| credit_code | VARCHAR(50) | 统一社会信用代码 | 选填 |
| qualification | VARCHAR(500) | 公司资质范围（ICP/CCC/专营等）| 选填 |
| cooperation_status | TINYINT | 合作状态（1-正常/2-暂停/3-黑名单）| 必填（默认1） |
| price_level | TINYINT | 价格水平（1-优于行业/2-普通/3-较差）| 系统自动 |
| score | DECIMAL(5,2) | 综合评分（0-100）| 系统自动计算 |
| remark | TEXT | 备注信息 | 选填 |

#### 3.12.6 供应统计数据模型

**供应统计展示**：供应商详情页增加"供应统计"标签页，自动统计和展示以下核心指标：

**供应概览数据**：
- 累计合作项目数（distinct 项目数）
- 累计合同金额（合同含税金额求和）
- 累计供货次数（入库单数量）
- 最近供货时间（最新入库时间）
- 合作时长（首笔合同至今天数）

**历史供应产品清单**：按材料名称+规格型号分类统计，展示：
- 材料/设备名称、规格型号、单位
- 供应总量、最小供应单位
- 最新合同价格、历史价格最低值
- 最近供货日期、供货周期

**历史价格趋势**：
- 图表展示过去 12 个月价格变化趋势
- 支持按材料/设备分类查看
- 显示价格最高/最低/平均值

**验收质量统计**：
- 入库合格率（合格入库数/总入库数）
- 历史退货/退库情况
- 质量投诉记录

#### 3.12.7 价格水平自动评价

**评价规则**：系统根据历史采购数据自动评价供应商价格水平，每日凌晨 03:00 执行一次定时任务更新评价结果。

**评价来源**：
- 取样范围：过去 6 个月已审批通过的支出合同
- 比较维度：**同一材料名称 + 同一规格型号 + 同一时间段**（月份）
- 最小样本量：同一类物资至少 2 个供应商才触发对比评价

**评价等级定义**：

| 等级 | 判定标准 | 颜色标签 | 说明 |
|------|---------|---------|------|
| 优于行业 | 价格 < 同期均价 - 5% | 绿色 | 价格具有竞争力，优先采购 |
| 普通 | 价格在均价 ±5% 以内 | 灰色 | 价格水平中等 |
| 较差 | 价格 > 同期均价 + 5% | 红色 | 价格偏高，谨慎采购 |

**评价计算逻辑**：
1. 按材料名称+规格型号分组
2. 按采购月份分组统计
3. 同组同月的所有供应商平均价格 = 同期均价
4. 比较该供应商该月平均价与同期均价的差值
5. 根据差值比例判定等级
6. 多个物资等级汇总取"最优"等级（若有优于、普通、较差三个，取较差）

**人工修正机制**：
- 采购员可对系统评价进行质疑和手动修正
- 修正时必须填写"修正原因"（文本框，不少于 20 字符）
- 修正记录存入 biz_supplier_price_adjustment 表
- 系统下次计算时将采用修正后的数据

**触发规则**：
- 系统每日 03:00 定时执行评价更新任务
- 新增合同审批通过时实时更新该供应商评价
- 手动修正后立即更新

#### 3.12.8 供应商综合评价

**评价指标体系**：系统自动收集和计算以下 7 项指标，汇总生成综合评分和评级。

| 指标 | 权重 | 数据来源 | 计算方式 |
|------|------|---------|---------|
| 价格水平 | 30% | 历史合同数据 | 优于=100分、普通=80分、较差=60分 |
| 供货及时性 | 20% | 入库单/收货时间 | (按时交货数/总交货数)×100 |
| 质量合格率 | 20% | 入库验收/退库记录 | (合格入库数/总入库数)×100 |
| 服务响应 | 10% | 合同变更/投诉记录 | 无投诉100分、每次投诉-5分 |
| 账期优势 | 10% | 供应商账期设置 | 现款=100、30天=90、60天=80、≥90天=70 |
| 合作年限 | 5% | 首笔合同时间 | ≥5年=100、3-5年=80、1-3年=60、<1年=40 |
| 纠纷记录 | 5% | 退货/质量投诉/纠纷争议 | 无纠纷=100、每次纠纷-10分（最低0分） |

**综合评分计算**：
```
综合评分 = Σ(指标分数 × 权重系数)
```

**评级划分**：
- A 级：综合评分 ≥ 90 分（优秀，优先采购）
- B 级：综合评分 80-89 分（良好，正常采购）
- C 级：综合评分 70-79 分（一般，需审慎采购）
- D 级：综合评分 < 70 分（较差，限制采购）

**自动更新**：供应商评级每日凌晨 03:00 定时更新一次，或当发生以下事件时实时更新：
- 新增合同并审批通过
- 入库单审批完成
- 退库单确认
- 质量投诉提交
- 付款申请完成

#### 3.12.9 数据表扩展

**扩展 biz_supplier 表字段**：

```sql
ALTER TABLE biz_supplier ADD COLUMN (
    supply_content VARCHAR(500),           -- 供应产品内容
    payment_term VARCHAR(50),              -- 合作账期（现款/月结30等）
    legal_person VARCHAR(100),             -- 法人代表
    credit_code VARCHAR(50),               -- 统一社会信用代码
    qualification VARCHAR(500),            -- 公司资质
    cooperation_status TINYINT DEFAULT 1,  -- 合作状态(1-正常/2-暂停/3-黑名单)
    price_level TINYINT,                   -- 价格水平(1-优于/2-普通/3-较差)
    score DECIMAL(5,2),                    -- 综合评分
    grade VARCHAR(1),                      -- 综合评级(A/B/C/D)
    supply_count INT DEFAULT 0,            -- 累计供货次数
    total_amount DECIMAL(14,2),            -- 累计采购金额
    last_supply_time DATETIME,             -- 最后供货时间
    first_contract_time DATETIME,          -- 首笔合同时间
    price_level_update_time DATETIME,      -- 价格水平最后更新时间
    score_update_time DATETIME,            -- 评分最后更新时间
    INDEX idx_price_level (price_level),
    INDEX idx_grade (grade),
    INDEX idx_cooperation_status (cooperation_status)
);
```

**新增评价调整记录表**：

```sql
CREATE TABLE biz_supplier_price_adjustment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    material_id INT,                       -- 涉及物资ID（为空表示全部物资）
    original_price_level TINYINT,          -- 原始评价等级
    adjusted_price_level TINYINT,          -- 调整后等级
    adjustment_reason VARCHAR(500),        -- 调整原因
    adjusted_by INT,                       -- 调整人ID
    adjusted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    remark TEXT,
    FOREIGN KEY (supplier_id) REFERENCES biz_supplier(id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_adjusted_at (adjusted_at)
);
```

**新增供应统计快照表**（用于历史查询和趋势分析）：

```sql
CREATE TABLE biz_supplier_supply_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    stat_month VARCHAR(7),                 -- 统计月份(YYYY-MM)
    contract_amount DECIMAL(14,2),         -- 该月合同总额
    contract_count INT,                    -- 该月合同数
    inbound_qty DECIMAL(10,2),             -- 该月入库总量
    inbound_count INT,                     -- 该月入库次数
    qualified_qty DECIMAL(10,2),           -- 该月合格入库量
    reject_qty DECIMAL(10,2),              -- 该月退货量
    complaint_count INT,                   -- 该月投诉次数
    on_time_delivery_rate DECIMAL(5,2),    -- 按时交货率
    quality_pass_rate DECIMAL(5,2),        -- 质量合格率
    price_level TINYINT,                   -- 该月价格等级
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES biz_supplier(id),
    UNIQUE INDEX uq_supplier_month (supplier_id, stat_month),
    INDEX idx_stat_month (stat_month)
);
```

#### 3.12.10 供应商列表与搜索增强

**列表显示增强**：在供应商列表页面新增以下功能：

**新增展示列**：
- 价格水平（绿色/灰色/红色标签）
- 综合评级（A/B/C/D 标签，不同颜色）
- 合作账期
- 合作状态

**高级筛选**：
- 按价格水平筛选（优于/普通/较差）
- 按评级筛选（A/B/C/D 多选）
- 按账期筛选（现款/月结30/月结60等）
- 按合作状态筛选（正常/暂停/黑名单）
- 按合作时长筛选（<3个月/3-12个月/1-3年/>3年）
- 按供应产品内容关键词搜索

**操作类按钮**：
- 查看供应统计详情
- 查看评价调整历史
- 价格等级人工修正（采购员权限）
- 评级申诉（供应商方）

#### 3.12.11 合同签订时供应商推荐

**合同录入页面优化**：当采购员在合同签订表单中选择"合同类型"后：

1. **供应商推荐列表**：根据合同类型自动展示该类型供应商，排序规则：
   - 优先展示 A 级供应商（评级排序：A→B→C→D）
   - 同等级内按"价格水平"排序（优于行业 → 普通 → 较差）
   - 同级别内按"最近供货时间"排序（最近优先）

2. **价格预警**：
   - 供应商等级为 D 级时高亮预警（背景为黄色）
   - 价格水平为"较差"时显示红色警告标签
   - 采购员选择此类供应商时须勾选确认框："我已知悉该供应商价格水平较差，确认采购"

3. **账期自动提示**：
   - 供应商详情卡片右侧显示其设置的"合作账期"
   - 点击"建议账期"按钮自动回填到合同的付款条件字段

#### 3.12.12 接口扩展设计

**供应统计查询接口**：GET /api/v1/supplier/{id}/supply-stats
- 请求参数：stat_month（可选，不填则查询最近 12 个月）
- 响应：供应概览、产品清单、价格趋势、质量统计

**供应商评价接口**：GET /api/v1/supplier/{id}/evaluation
- 响应：7 项指标得分、综合评分、评级

**供应商比价接口**：POST /api/v1/supplier/compare
- 请求参数：material_id（物资ID）、date_range（选填日期范围）
- 响应：该物资所有供应商的价格聚合数据、均价、价格等级

**价格等级修正接口**：POST /api/v1/supplier/{id}/adjust-price-level
- 请求参数：material_id（选填）、adjusted_level、adjustment_reason
- 权限：采购员或预算员

**供应商推荐接口**：GET /api/v1/supplier/recommend?contract_type=xxx&material_type=xxx
- 响应：按推荐度排序的供应商列表（含评级、价格水平、账期）

**定时任务**：
- 任务名称：供应商价格与评分自动评估
- Cron 表达式：0 0 3 * * ?（每日凌晨 03:00）
- 执行内容：重新计算全部供应商价格水平、综合评分、评级
- 记录月度供应统计快照到 biz_supplier_supply_stats 表
- 超时时间：600 秒

#### 3.12.13 供应商信息导出与报表

**支持导出功能**：
- 导出供应商评价报表（Excel）：包含所有指标、评分、评级、合作情况
- 导出供应商比价表（Excel）：对比多个供应商的相同物资价格
- 导出供应统计表（Excel）：按月展示供应商合作数据趋势

**报表字段**：供应商名称、合作状态、价格水平、综合评分、评级、合作账期、累计合同金额、累计供货次数、质量合格率、最近供货时间、联系方式

### 3.13 采购管理模块

> **与第 3C 章联动：** 采购清单二级价格匹配与「价格待询价」、基准价新增与支出合同价格确认入库、零星采购超限额（1.5%）审批切换见 **§3C.3**、**§3C.5**、**§3C.9**（与 **§3A.1** 广联达导入可并存，实现时统一优先级）。

#### 3.13.1 采购清单

采购清单为实体项目的物资计划汇总表，由预算员编制。清单编号：PL+YYMMDD+2 位顺序号，每日重置。

**清单字段：** 清单编号（自动）、关联项目（必填，实体项目）、关联收入合同（必填）、编制人（自动）、编制日期（自动）、清单状态（待审批/已审批/已完成）、物资明细（子表）、预算总金额（自动汇总）、备注。

**物资明细——设备类：** 物资名称（必填）、品牌（必填）、单位（必填）、计划数量（必填）、预算单价（自动代入）、预算合价（自动）。品牌为设备类物资的必填字段，不填写时系统禁止保存/提交清单，提示「设备类物资必须填写品牌」。

**物资明细——材料类：** 物资名称（必填）、规格型号（必填）、品牌（选填）、单位（必填）、计划数量（必填）、预算单价（自动代入）、预算合价（自动）。

**价格自动代入规则：** 预算单价由系统根据"物资名称 + 规格型号"自动匹配材料基准价表(biz_material_base_price)中的生效基准价代入，无需人工录入。若基准价不存在，系统提示「当前物资无生效基准价，请先维护基准价」；预算合价由系统自动计算：预算合价 = 预算单价 × 计划数量。基准价更新后，已保存采购清单支持"重新同步价格"功能（需预算员或总经理权限），更新时记录修改日志。

**无基准价处理与颜色标注规则：** 系统根据"物资名称 + 规格型号"精确匹配材料基准价表（effective_status=1）：匹配成功则自动代入基准价，否则判定为**无基准价**。无基准价物资的处理规则如下——（1）预算单价自动赋值 0.00，预算合价自动计算为 0.00（= 0.00 × 计划数量）。（2）该行在采购清单表格中以**浅黄色背景(#FFF2CC)高亮标注**，醒目提示预算员该材料无基准价。（3）鼠标悬浮地提示「该材料无基准价，预算单价按0记取」，便于快速识别。（4）无基准价**不阻止清单保存/提交**，但提交审批时生成弱提示「存在无基准价材料(共 X 行)，价格按0记取，请确认」。

**价格字段权限控制：** 预算单价、预算合价为敏感信息字段，权限控制规则如下——（1）拥有以下权限的账户可见完整价格：总经理(GM)、预算员(BUDGET)、拥有采购价格查看权限(purchase:price-view)的用户。（2）无上述权限的账户（采购员、项目经理、资料员等）在采购清单列表、详情页、打印、导出时，预算单价列显示"***"，预算合价列显示"***"。（3）前端根据用户权限动态渲染价格列，后端接口对无权限用户返回脱敏数据，确保价格信息不被泄露。

**审批流程：** 预算员提交 → 财务审批 → 总经理审批。驳回至发起人。同一项目下仅允许一份有效采购清单。审批通过后需通过"变更"方式调整。

#### 3.13.1.1 物资明细表扩展(biz_purchase_list_item)

在采购清单物资明细表中新增字段：brand VARCHAR(100)（品牌字段，存储物资品牌信息）。

#### 3.13.1.2 权限点新增

新增权限点：purchase:price-view（查看采购清单预算价格）。用户配置此权限后可查看预算单价、预算合价等敏感价格字段。

#### 3.13.2 批量采购

批量采购通过支出合同签订流程执行，实体项目下受采购清单约束，超量时触发预算员审批。虚拟项目下无自动校验。

#### 3.13.3 零星采购

零星采购编号：LP+YYMMDD+3 位顺序号。字段包含关联项目（必填）、物资名称（必填）、规格型号（选填）、单位（必填）、数量（必填）、单价含税（必填）、合价（自动）、税率（必填）、税票类型（必填，无/普票/专票）、供应商（选填）、附件、备注。

**审批流程：** 采购员提交 → 财务审批 → 总经理审批。超金额预警时变更为：采购员 → 预算员 → 总经理。

#### 3.13.4 材料基准价管理

**材料基础信息表(biz_material_base)：** id INT 主键自增；material_code VARCHAR(20) 非空唯一（格式 M+6 位顺序号）；material_name VARCHAR(200) 非空；spec_model VARCHAR(200) 可空；unit VARCHAR(20) 非空；category VARCHAR(50) 非空（主材/辅材/设备/劳务）；status TINYINT 默认 1；created_at、updated_at。

**材料价格基准表(biz_material_base_price)：** id INT 主键自增；material_id INT 非空外键；base_price DECIMAL(14,2) 非空（含税）；tax_rate DECIMAL(5,2) 默认 13.00；price_source VARCHAR(200)；last_contract_id INT 可空；last_update_time DATETIME；update_type TINYINT（1 系统自动/2 人工修改）；effective_status TINYINT 默认 1。

**材料历史价格表(biz_material_price_history)：** id INT 主键自增；material_id INT 非空；price DECIMAL(14,2) 非空；contract_id INT 可空；supplier_id INT 可空；purchase_time DATETIME；remark VARCHAR(255)。

**人工修改基准价：** 预算员发起修改申请 → 总经理审批。审批通过后更新基准价，update_type=2。

#### 3.13.5 接口设计

采购清单列表：GET /api/v1/purchase/list。创建采购清单：POST /api/v1/purchase/list。零星采购列表：GET /api/v1/purchase/spot。创建零星采购：POST /api/v1/purchase/spot。材料基准价列表：GET /api/v1/material/base-price/list。基准价修改申请：POST /api/v1/material/base-price/change-apply。

### 3.14 物资管理模块

> **与第 3C 章联动：** 现场出库单须关联合同或入库单、跨项目调拨须先完成调拨单审批，见 **§3C.6**。

#### 3.14.1 物资入库

**入库单编号：** RK+YYMMDD+3 位顺序号，每日重置。

**入库触发：** 支出合同审批通过后系统自动生成入库单草稿（含关联项目、合同、供应商信息、物资明细），采购员在实际到货后填写入库数量并提交。

**入库单字段：** 入库单号（自动）、关联项目（只读）、关联合同（只读）、入库日期（必填，默认当天）、供应商（只读）、到货时间（必填）、物流单号/司机信息（必填）、入库明细（子表）、备注。

**入库明细表格：** 物资名称（只读）、规格型号（只读）、单位（只读）、合同数量（只读）、已入库数量（只读）、本次入库数量（必填，校验不超过合同数量-已入库数量）、累计入库数量（自动）、小计金额（自动=本次入库数量×合同单价）。表尾展示合计金额。

**审批流程：** 采购员提交 → 财务审批。驳回至采购员。

**到货通知：** 入库审批完成后通过企业微信推送收货通知。

#### 3.14.2 物资出库

**项目现场出库：** 出库单号 CK+YYMMDD+3 位顺序号。项目经理发起，字段含出库日期、领用人、用途说明、出库明细（从已入库物资选择，校验不超过库存）、附件。审批：项目经理 → 采购员确认 → 财务审批 → 总经理审批。

**采购员出库：** 适用于软件许可、技术服务等非实物类物资。审批：采购员 → 财务审批。

#### 3.14.3 物资退库

退库单号 TK+YYMMDD+3 位顺序号。字段含关联项目、退库明细（物资名称、规格型号、数量、原入库单号）、处置方式（现场处理/退回厂家/入公司库/项目间调拨）、目标项目（调拨时必填）、原因说明（必填）。

**审批流程：** 项目经理 → 采购员确认 → 财务审批 → 总经理审批。

**处置方式执行：** 现场处理——库存扣减，成本核销。退回厂家——库存扣减，应收账款处理。入公司库——从项目库存转移到公司总库（project_id=0）。项目间调拨——当前项目扣减、目标项目增加，成本台账同步调整。

#### 3.14.4 库存盘点

盘点单号 PD+YYMMDD+3 位顺序号。流程：创建盘点单 → 录入实际盘点数量 → 系统自动计算差异 → 差异审批 → 确认执行后调整库存。

#### 3.14.5 库存数据表(biz_inventory)

id INT 主键自增；project_id INT 非空（0 表示公司总库）；material_id INT 非空；material_name VARCHAR(200)；spec_model VARCHAR(200)；unit VARCHAR(20)；quantity DECIMAL(10,2) 非空默认 0；avg_price DECIMAL(14,2)（加权平均单价）；total_amount DECIMAL(14,2)。联合唯一索引：project_id+material_id。

#### 3.14.6 接口设计

入库单列表：GET /api/v1/material/inbound/list。入库单详情：GET /api/v1/material/inbound/{id}。提交入库：POST /api/v1/material/inbound。出库单列表：GET /api/v1/material/outbound/list。提交出库：POST /api/v1/material/outbound。退库单列表：GET /api/v1/material/return/list。提交退库：POST /api/v1/material/return。库存查询：GET /api/v1/material/inventory。

#### 3.14.7 库存数量计算规则

**加权平均单价计算：** 新加权平均单价 = (当前库存金额 + 本次入库金额) / (当前库存数量 + 本次入库数量)。其中当前库存金额 = 当前库存数量 × 当前加权平均单价；本次入库金额 = 本次入库数量 × 合同单价。

**入库更新：** 入库单审批通过后在同一事务中执行：quantity 累加；重新计算 avg_price；更新 total_amount = quantity × avg_price。

**出库更新：** quantity 扣减；avg_price 不变；更新 total_amount；出库成本 = 出库数量 × avg_price 计入项目成本台账。

**退库更新：** 根据处置方式——现场处理扣减库存计入损耗；退回厂家扣减库存不计入项目成本；入公司库从项目库存转移到公司总库，单价取原加权平均单价；项目间调拨 A 项目扣减、B 项目增加，单价取 A 项目的加权平均单价。

#### 3.14.8 盘盈盘亏处理流程

**盘盈处理：** 审批通过后增加库存数量。入库单价按最近一次入库的合同单价计算。盘盈金额计入"营业外收入-盘盈利得"。

**盘亏处理：** 审批通过后扣减库存数量。盘亏成本按加权平均单价计算，计入项目损耗成本。盘亏金额超过物资总值 10% 的，审批流程增加总经理审批节点。

### 3.15 施工管理模块

> **与第 3C 章联动：** 里程碑甘特与收入合同拆分明细双向映射、子任务锁控与审批链、与 **§3A.3/§3A.4** 分部分项及隐蔽工程字段协同，见 **§3C.7**。

#### 3.15.1 里程碑管理

里程碑是项目施工的关键节点，由收入合同拆分明细表中的项目名称和里程碑名称自动映射生成。

**里程碑字段：** 关联项目（自动）、里程碑名称（自动映射，只读）、计划完成时间（DATE，必填）、实际完成时间（DATE，选填）、进度百分比（DECIMAL(5,2)，0~100）、交付成果描述（TEXT，选填）、责任人（INT，必填）、备注。

**审批流程：** 项目经理提交 → 总经理审批（预算员、采购员、资料员、财务为阅办角色）。驳回至项目经理。

#### 3.15.2 任务甘特图

**功能需求：** 采用 dhtmlxGantt 或同类前端甘特图组件。支持可视化任务时间线展示（按日/周/月切换），多里程碑并行展示，任务依赖关系（FS/SS/FF/SF），任务负责人指派，进度百分比实时更新。

**进度计划字段：** 节点 ID（自动）、节点名称（VARCHAR(200)，必填）、节点类型（里程碑/任务）、父节点 ID（可空）、计划开始/完成日期（DATE，必填）、实际开始/完成日期（选填）、进度百分比（DECIMAL(5,2)，默认 0）、责任人 ID（选填）、前置节点 ID（可空）、前置关系类型（VARCHAR(10)，默认 FS）、备注。

**任务进度描述：** 一个任务节点下可添加多条进度描述记录。每次填写不覆盖历史记录，提交后自动打上时间戳，打上时间戳的记录不可修改只能查看。

**甘特图审批：** 项目经理 → 总经理。审批通过后里程碑和任务名称自动写入收入合同拆分表。结构锁定不可修改，但进度百分比和实际日期可持续更新。修改结构须重新提交审批。

#### 3.15.3 进度填报

**操作角色：** 项目经理。在甘特图中选择节点点击"填报进度"，支持批量填报。

**填报字段：** 节点 ID（自动）、实际开始日期（首次填报时必填）、实际完成日期（完成时必填）、进度百分比（必填，0~100）、当期形象进度描述（TEXT，必填）、当期产值（选填）、累计产值（自动）、附件。

**节点状态自动判定：** 未开始——实际开始日期为空。进行中——实际开始日期非空、实际完成日期为空、进度百分比 < 100%。已完成——实际完成日期非空或进度百分比 = 100%。延期——已完成但实际完成日期 > 计划完成日期。预警中——当前日期 > 计划完成日期但仍为进行中或未开始。

**进度报告自动生成：** 系统根据甘特图所有节点最新进度数据自动汇总生成项目进度报告。

#### 3.15.4 偏差预警

**预警规则：** 系统每日 00:00 定时任务扫描所有"进行中"状态项目的甘特图节点。里程碑节点当前日期 > 计划完成日期且未完成时触发预警。细化任务节点可设置偏差阈值（默认 3 天）。

**通知机制：** 预警触发后推送给项目经理并通过企业微信通知项目团队全体成员。通知模板："【进度预警】项目【{项目名称}】里程碑【{里程碑名称}】计划完成日期为【{计划完成日期}】，当前状态为【{状态}】，已滞后【{X}】天。"

#### 3.15.5 进度-产值联动

**弱相关预警（对账单）：** 产值比例 = 对账单累计完成产值/收入合同总金额。进度比例 = 甘特图综合进度百分比。预警阈值默认 5%（可配置），不阻止对账单流程。

**强相关控制（劳务提报工程量）：** 若劳务分包商申请的产值比例 > 进度比例 + 允许偏差（默认 2%），系统拦截提交。特殊放行需填写超进度原因，审批流程为预算员 → 总经理。

#### 3.15.6 甘特图任务数据表(biz_gantt_task)

id INT 主键自增；project_id INT 非空外键；task_name VARCHAR(200) 非空；task_type TINYINT（1 里程碑/2 任务）；parent_id INT 可空；plan_start_date DATE；plan_end_date DATE；actual_start_date DATE 可空；actual_end_date DATE 可空；progress DECIMAL(5,2) 默认 0；assignee_id INT 可空；predecessor_id INT 可空；predecessor_type VARCHAR(10) 默认 'FS'；sort_order INT 默认 0；status VARCHAR(20) 默认 'pending'；created_at、updated_at DATETIME。索引：project_id 普通索引，parent_id 普通索引，project_id+parent_id 联合索引。

#### 3.15.7 接口设计

获取甘特图：GET /api/v1/progress/gantt/{projectId}。保存甘特图：POST /api/v1/progress/gantt/{projectId}。进度填报：POST /api/v1/progress/report。批量填报：POST /api/v1/progress/report/batch。进度报告：GET /api/v1/progress/report/{projectId}。

### 3.16 变更管理模块

#### 3.16.1 现场签证

签证编号 VS+YYMMDD+3 位顺序号。项目经理发起。字段含关联项目、关联合同、签证事由（必填）、签证内容（必填）、工程量清单（子表）、预估费用（自动汇总）、对工期影响（选填）、甲方确认状态、附件（必填）。审批：项目经理 → 预算员审批（采购员阅办，资料员阅知）→ 总经理审批。驳回至发起人。

#### 3.16.2 甲方需求变更

变更编号 CH+YYMMDD+3 位顺序号。项目经理发起。字段含关联项目、关联合同、变更来源（甲方书面指令/口头要求/设计变更通知单/其他）、变更内容（必填）、变更影响分析（必填）、成本影响（正值增加/负值减少）、工期影响（选填）、甲方确认状态、附件（必填）。审批：项目经理 → 预算员 → 总经理。

#### 3.16.3 超量采购变更

项目经理发起。字段含关联项目、超量物资清单（子表含物资名称、规格型号、单位、计划数量自动读取、已采购数量自动计算、本次需求数量手填、超量数量自动计算）、超量原因（必填）、事由类型（预算员勾选）、附件。审批：项目经理 → 预算员 → 采购员阅知 → 总经理。

#### 3.16.4 劳务签证

项目经理发起。字段含关联项目、签证类别（必填）、签证内容（必填）、签证原因（必填）、金额（DECIMAL(14,2)，必填）、附件。审批：项目经理 → 预算员 → 总经理。

#### 3.16.5 变更台账

展示字段：项目名称、变更类型、变更编号、发起日期、发起人、变更事由、预估成本影响、实际成本影响、工期影响、审批状态、甲方确认状态。支持组合查询、导出 Excel、按项目汇总、穿透查看详情。

#### 3.16.6 变更数据表(biz_change_order)

id INT 主键自增；change_no VARCHAR(20) 非空唯一；change_type VARCHAR(20) 非空；project_id INT 非空；contract_id INT 可空；change_source VARCHAR(50) 可空；change_content TEXT 非空；change_reason TEXT；cost_impact DECIMAL(14,2)；actual_cost_impact DECIMAL(14,2) 可空；schedule_impact VARCHAR(200)；client_confirm_status VARCHAR(20) 默认 'pending'；status VARCHAR(20)；applicant_id INT 非空；created_at、updated_at DATETIME。

#### 3.16.7 附件管理与在线预览

**附件上传规则：** 签证单、设计变更单等变更单据支持多文件批量上传。支持文件格式：.doc/.docx/.xls/.xlsx/.pdf/.jpg/.png/.dwg/.zip/.rar；单文件大小限制≤50MB，总附件数量限制≤20份，前端校验格式和大小，超限时弹出提示"文件格式不支持/单文件超过50MB/附件数量超过20个"。上传过程显示进度条，上传完成后自动关联当前业务单据(签证单/变更单)，并在数据库 biz_change_attachment 表中记录文件信息（文件名、格式、大小、上传人、上传时间、存储路径）。

**在线预览功能：** 附件列表展示文件名、格式图标、上传时间、操作（预览/下载/删除）。点击预览支持在线打开，不需下载本地：PDF 支持分页浏览及缩放、Office 文档（Word/Excel）在线查看并支持分页、图片（JPG/PNG）支持缩放/旋转、CAD 图纸（DWG）支持基础图层查看、ZIP/RAR 支持文件列表展示，预览弹窗自适应屏幕，支持关闭/全屏切换。预览加载失败时弹出 Toast 提示"文件加载失败，请重试或下载本地打开"。

**权限与操作：** 上传、预览权限按系统现有角色权限控制（项目经理/预算员/总经理可上传预览）；附件删除仅上传人和系统管理员可操作，删除前需二次确认；无权限用户预览按钮隐藏并提示"无查看权限"；支持导出（Excel/PDF）时保持权限控制，无查看权限的用户导出内容脱敏隐藏附件。

**附件数据表(biz_change_attachment)：** id INT 主键自增；change_order_id INT 非空外键；file_name VARCHAR(255) 非空；file_type VARCHAR(50) 非空；file_size BIGINT；file_path VARCHAR(500) 非空；uploader_id INT 非空；upload_time DATETIME 非空；remark VARCHAR(255) 可空；created_at DATETIME。索引：change_order_id。

#### 3.16.8 接口设计

变更列表：GET /api/v1/change/list。创建现场签证：POST /api/v1/change/visa。创建甲方变更：POST /api/v1/change/owner-change。创建超量变更：POST /api/v1/change/overage。创建劳务签证：POST /api/v1/change/labor-visa。变更台账查询：GET /api/v1/change/ledger。变更台账导出：GET /api/v1/change/ledger/export。上传附件：POST /api/v1/change/attachment/upload。附件列表：GET /api/v1/change/{id}/attachments。附件预览地址：GET /api/v1/change/attachment/{id}/preview。删除附件：DELETE /api/v1/change/attachment/{id}。


### 3.17 财务管理模块

> **与第 3C 章联动：** 收入合同拆分明细与甘特图节点名称双向映射、子任务增删与金额闭环校验、拆分审批链，见 **§3C.7**。

#### 3.17.1 收入合同拆分

收入合同签订后由预算员编制拆分明细表，将合同总金额按甘特图任务节点分拆，关联进度计算产值。

**拆分明细子表：** 序号（自动）、里程碑名称（自动映射甘特图）、任务节点名称（自动映射）、任务合同金额（DECIMAL(14,2)，必填）、进度百分比（只读，自动关联甘特图）、当期完成产值（只读，自动=任务合同金额×进度百分比）、当期完成产值总额（只读）、累计完成产值（只读）、进度比例（只读，=累计完成产值/合同含税金额×100%）。

**校验规则：** 所有任务合同金额之和须等于收入合同含税金额（允许误差 0.01 元）。

**审批流程：** 预算员提交 → 项目经理审批 → 总经理审批。驳回至预算员。审批通过后锁定。

#### 3.17.2 收入对账单

##### 3.17.2.1 自动生成与定时配置

**项目独立定时配置：** 系统为每个项目单独配置对账生成周期，支持四种模式：按月生成（固定日期，如每月26日6:00）、按季度生成（固定周期）、自定义周期生成、手动触发生成。管理员在后台可编辑、修改、关闭单个项目的对账规则，下次定时任务执行时按更新后的配置生成。

**定时任务执行与异常处理：** 系统按配置规则自动生成对账单，若生成失败（如数据缺失、连接中断），后台自动重试最多 3 次；若重试失败，记录详细日志并推送系统提醒给财务管理员。

**对账单编号与基础字段：** 对账单编号 DZ+YYMMDD+2位顺序号（每月重置）。对账单字段含对账单编号（自动）、关联项目（自动）、所属期间（自动）、对账单状态、创建时间、本期开始日期、本期结束日期、备注。

**对账单详情展示字段：** 关联收入合同、合同含税金额（只读）、进度比例（只读）、当期产值（只读）、累计产值（只读）、当期回款（项目经理填写）、累计回款（自动计算）、应收余额（只读）、未回款金额（只读）、附件。

**审批流程：** 项目经理确认提交 → 采购员审批 → 预算员审批 → 财务审批 → 总经理审批。驳回至项目经理。

##### 3.17.2.2 附件上传与在线预览

**业务绑定规则：** 附件与单张对账单唯一绑定，通过对账单ID关联溯源，支持单对账单多文件附件管理。

**上传规则：** 支持多文件批量上传，兼容格式 `.doc/.docx/.xls/.xlsx/.pdf/.jpg/.png/.zip/.rar`（共8种），前端校验文件格式与大小；单文件≤50MB，单对账单最多 20 个附件，超限时弹出提示"文件超过50MB/附件数量超过20个"。上传过程显示进度条，上传完成后记录文件名、格式、大小、上传人、上传时间、存储路径等信息入库（表 biz_statement_attachment）。

**在线预览与操作：** 附件列表展示文件名、格式图标、上传时间、操作按钮（预览/下载/删除）。点击预览在弹窗中在线打开，无需本地下载：PDF 格式支持分页浏览及缩放、Office 文档（Word/Excel）在线查看并支持分页、图片（JPG/PNG）支持缩放/旋转、压缩包（ZIP/RAR）显示文件列表可逐项查看。预览弹窗自适应屏幕大小，支持关闭和全屏切换。预览加载失败时弹出 Toast 提示"文件加载失败，请重试或下载本地打开"。

**权限与删除：** 附件上传/预览权限按系统统一权限控制；附件删除仅上传人和系统管理员可操作，删除前需二次确认。

##### 3.17.2.3 上期与本期对账单差异分析（核心功能）

**期次自动匹配与选择：** 系统自动识别同一项目下**连续对账期次**，默认关联「上期对账单 ↔ 本期对账单」进行对比；若项目无历史对账数据，提示"无上期对账单，暂无法进行差异分析"。支持**手动自定义选择**该项目任意两期对账单进行跨期次差异对比，下拉菜单列表所有历史对账单供选择。

**多维度自动差异计算：** 后端实时对比两期对账单数据，计算并标记差异，核心差异维度包括：
- **汇总差异**：对账总金额差额、应收余额差额、已回款金额差额、未回款金额差额，自动计算差异率（%）。
- **明细差异**：逐行对比合作方/客户、合同信息、上期发生额、本期发生额、上期期末余额、本期期末余额，自动计算差异金额与差异占比。

**差异数据标记与展示：** 系统自动识别存在差异的数据行，采用**红色高亮 / 标红字体**显著标记，清晰区分无差异项与差异项，便于财务人员快速核查。

**差异明细展示与筛选：** 采用「对比表格」格式展示：左列为上期数据、右列为本期数据、中列为差异值、末列为差异原因备注。支持两种筛选模式：**仅显示差异项**（隐藏无差异行）/ **显示全部对账明细**（展示所有行）。大数据量差异明细支持分页加载，单页显示 50-100 行，确保页面流畅不卡顿。

**差异原因备注与留存：** 财务人员可对**单条差异明细**或**整单差异**添加备注说明。备注支持选择常用原因模板快捷填充（"回款延迟"、"合同变更"、"计费口径调整"、"数据更正"、"其他"），也支持手动输入文字说明。备注信息与对账单、差异记录绑定保存，后续查看时可追溯差异原因，支持按时间倒序显示备注历史。

**差异分析导出与共享：** 支持将完整差异分析报告导出为 **Excel / PDF** 两种格式。导出文件包含：项目信息、对账期次选择、汇总差异表、明细对比表、差异备注等完整内容。同时支持差异分析页面直接打印，适配财务对账归档需求。

**可视化辅助展示（优化）：** 柱状图/折线图形式直观展示两期对账单的关键金额变化趋势（如回款金额、应收余额变化），图表旁标注差异金额与差异占比，辅助财务快速判断波动原因。

##### 3.17.2.4 数据表设计

**biz_statement_attachment（对账单附件表）：** id INT 主键自增；statement_id INT 非空外键关联 biz_reconciliation_statement；file_name VARCHAR(255) 非空；file_type VARCHAR(50) 非空；file_size BIGINT；file_path VARCHAR(500) 非空；uploader_id INT 非空；upload_time DATETIME 非空；remark VARCHAR(255) 可空；created_at DATETIME。索引：statement_id。

**biz_statement_difference（对账单差异记录表）：** id INT 主键自增；statement_id INT 非空；compare_statement_id INT 非空（对比的上期对账单）；difference_type VARCHAR(50)（summary 汇总差异 / detail 明细差异）；detail_key VARCHAR(200)（明细差异的关键字段，如客户名称+合同号）；prev_value TEXT（上期值）；curr_value TEXT（本期值）；difference_amount DECIMAL(14,2)（差异金额）；difference_ratio DECIMAL(5,2)（差异占比%）；remark TEXT（差异原因备注）；created_at DATETIME。索引：statement_id、compare_statement_id。

**biz_reconciliation_setting（对账定时配置表）：** id INT 主键自增；project_id INT 非空唯一；cycle_type VARCHAR(20)（monthly 月度 / quarterly 季度 / custom 自定义 / manual 手动）；cycle_day INT（月度日期，如26）；cycle_days INT（自定义周期天数）；last_generate_time DATETIME（最后生成时间）；next_generate_time DATETIME（下次生成时间）；is_enabled TINYINT 默认 1（1 启用 / 0 禁用）；created_at、updated_at DATETIME。索引：project_id。

#### 3.17.3 回款督办管理

##### 3.17.3.1 功能定位

工程回款督办自动化管理：**依据合同付款条款、项目甘特图工程节点、竣工资料关键验收信息，智能校验回款进度是否匹配工程节点要求，自动生成回款督办工作计划；经总经理审批通过后，正式转办至项目经理执行回款督办跟踪。**

##### 3.17.3.2 核心数据关联与数据源集成

**数据来源对接规范：**
- **合同管理模块**：获取合同付款条款（付款节点名称、计划付款比例/金额、约定付款时间、节点触发条件），关联表 biz_contract、biz_contract_payment_term；
- **项目甘特图模块**：获取工程关键节点（节点名称、计划完成时间、实际完成进度%、节点验收状态），关联表 biz_gantt_task；
- **竣工资料模块**：获取竣工关键信息（分部/分项验收结果、竣工报审状态、合格证明文件），关联表 biz_completion_docs、biz_completion_inspection；
- **收入对账单/回款记录模块**：获取项目实际回款数据（已回款金额累计、最近回款到账时间、未回款金额、回款凭证），关联表 biz_receipt、biz_reconciliation_statement。

**数据绑定规则：** 以**项目ID+合同ID**为唯一关联维度，自动匹配对应工程节点、付款条款、回款数据，禁止跨项目/跨合同数据混用。

##### 3.17.3.3 回款进度智能校验逻辑（核心算法）

**① 节点匹配校验**

系统自动将**工程甘特图节点**与**合同付款节点**一一映射，匹配规则如下：
1. 按节点名称精确匹配："基础验收完成" → 合同条款对应节点；
2. 若名称不匹配，按节点时间范围（计划完成时间）模糊匹配；
3. 若无对应甘特图节点，标记该付款节点为"工程节点未找到"。

判断当前工程节点是否已达到合同约定的付款触发条件：
- 工程进度≥触发条件进度 → 节点达成，可收款；
- 工程进度<触发条件进度 → 节点未达成，标记为"前置条件不满足"。

**② 回款达标判定**

后端实时计算回款缺口与完成率，核心指标包括：
- **合同约定本节点应付金额**：根据合同付款条款中该节点的付款比例或固定金额计算；
- **实际已回款金额**：查询该项目该合同对应的所有收入记录累计金额；
- **回款缺口金额**：应付 - 已回 = 缺口（若缺口>0为逾期）；
- **回款完成率**：已回 / 应付 × 100%；
- **逾期天数**：若约定付款日期已过但未收款，计算(当前日期 - 约定日期)。

根据计算结果自动标记状态：
- ✅ **正常**：回款进度≥约定要求（≥95%），无缺口，工程节点达成；
- ⚠️ **预警**：回款未达标（<95%），但未超过约定付款时限，工程节点达成；
- ❌ **逾期**：回款缺口+超过约定付款时限（超期>0），重点督办。

**③ 竣工资料联动校验**

若对应工程节点需竣工/验收资料作为付款依据（如"竣工验收通过"节点需竣工报告），系统自动校验：
- 资料是否存在且提交状态≥已提交；
- 检验结果是否为"合格"；
- 必输字段是否齐全。

若资料缺失或不合格，标记为**回款前置条件不满足**，纳入督办重点，提示"待提交竣工资料"。

##### 3.17.3.4 回款督办工作计划生成

**自动生成规则：**

系统根据校验结果，一键生成《回款督办工作计划》，包含以下字段：
- **项目基础信息**：项目编号、项目名称、客户方、关联合同、合同金额；
- **工程节点信息**：节点名称、计划完成时间、实际完成进度、验收状态、关键资料状态；
- **回款条款信息**：约定付款节点、应付比例/金额、约定付款时间；
- **回款情况统计**：应付金额、已回款金额、回款缺口、回款完成率、逾期天数；
- **督办策略**：督办优先级（高/中/低，根据缺口金额与超期天数自动评级）、建议督办时限、建议下阶段行动（如"催款""资料追证"）；
- **责任关联**：责任项目经理、经办人（提交人）、审批人（预填为总经理）；
- **数据溯源**：校验依据来源、关联数据快照、生成时间。

**手动编辑调整：**

支持经办人在规划后修改以下内容：
- 督办优先级调整、完成时限自定义、补充督办说明或特殊说明；
- 支持手动调整应付金额、已回款金额（需审核），一经修改自动重新计算完成率；
- 可批量导入对账数据、手动批量生成多个项目的督办计划；
- 支持预览后提交审批。

**模板固化与格式规范：**

督办计划采用系统固定模板（可配置题头/页脚），保证格式统一。模板包含表格版式（项目信息、节点明细、回款对比、督办建议四大板块）、状态标签（优先级、完成率进度条）、审批区（审批人、审批意见、审批时间签名栏）。

##### 3.17.3.5 审批流程与审批操作

**流程节点：** 经办人提交 → 总经理审批。

**审批操作选项：**
1. **同意**：审批通过，系统自动进入转办环节（见下节），审批记录中标记"已同意"，转办通知自动推送；
2. **驳回**：退回经办人修改，需填写驳回原因（下拉选项"数据有误""优先级不合理""建议调整方案"等，或自由文本），驳回通知推送经办人账号；
3. **加签/转批（可选扩展）**：支持总经理指定其他审批人同步审批或转交审批权，被指定人收到消息提醒；
4. **预审意见**：在同意前可填写非正式审批意见，提醒经办人注意细节，经办人可基于意见调整后重新提交。

**审批留痕机制：**

所有审批操作完整记录，包括审批人、审批角色、审批时间、审批意见、操作类型（同意/驳回/加签），记录不可删除、不可篡改，支持查看完整审批历史。

##### 3.17.3.6 自动转办与任务推送

**审批通过后自动转办：**

1. 系统自动检测审批状态变为"已同意"，触发转办流程（无需人工干预）；
2. 生成《回款督办任务单》，与督办计划绑定，自动指派给项目经理；
3. 任务状态初始化为"待接收"。

**消息推送与转办通知：**

转办时自动推送多渠道通知：
- **站内信**：详细列表，展示任务信息摘要、跳转链接；
- **系统弹窗**：登录后弹出新任务提醒，包含项目名、督办重点、建议行动；
- **可选通知**：可配置是否推送短信或企业微信（整合现有消息通知服务）。

**督办执行反馈机制：**

项目经理在任务页定期填报回款跟进反馈：
- **回款跟进进度**：描述已采取行动、沟通对象、对方反馈（文字+多行输入框）；
- **沟通记录**：客户类上门、电话、邮件沟通内容摘要，可上传沟通音频、截图；
- **预计回款时间**：基于沟通反馈的预计到账日期；
- **存在问题**：若回款困难，说明原因（合同纠纷、资料缺失、资金困难等）；
- **佐证附件**：上传回款承诺函、对账资料、客户确认函等佐证（文件类型限制同系统规范）。

所有反馈自动记录时间戳、填报人、填报内容版本，支持编辑历史追溯。

**任务状态管理与生命周期：**

任务状态流转：待接收 → 督办中 → 已回款完成 / 督办终止。
- **待接收**：转办后自动推送，项目经理可点击"接收"进入督办中状态；
- **督办中**：项目经理定期填报反馈，督办任务保持活跃；
- **已回款完成**：回款金额累计≥应付金额，由系统自动判定或项目经理手动标记；
- **督办终止**：由项目经理或经办人标记（如客户已破产、合同变更等），终止前需填写终止原因。

支持状态手动更新（项目经理/经办人有权）与系统自动同步（基于回款数据更新）。

##### 3.17.3.7 列表、详情与数据展示

**督办计划列表页面：**

展示字段：项目名称、关联合同、工程节点、回款缺口、优先级（标签：高/中/低 + 配色）、审批状态（图标；如已同意、待审批、已驳回）、督办状态（待转办/督办中/已完成/已终止）、创建人、创建时间。

支持功能：
- **筛选**：项目多选、时间范围、优先级、状态组合筛选，实时动态刷新；
- **搜索**：按项目名称/合同号/客户名称模糊搜索；
- **分页**：默认 20 行/页，支持切换；
- **排序**：按优先级（高→中→低）、缺口金额（大→小）、超期天数（长→短）默认排序，支持列头排序切换；
- **批量操作（可选）**：选中多条记录，批量导出、批量转办（审批通过后）。

**详情页面展示：**

采用分块展示，完整展现督办单的全生命周期信息：

1. **基础信息块**：项目/合同/客户信息、创建时间、创建人；
2. **校验依据块**：
   - 合同条款摘要（约定付款比例、金额、时间、触发条件）；
   - 甘特图关键节点进度条（可视化展示计划vs实际完成度）；
   - 竣工资料状态列表（资料名称、提交状态、检验结果、必输字段核查状态）；
3. **回款数据对比块**：
   - 表格展示应付金额、已回款金额、缺口金额、回款完成率（百分比+进度条），缺口字段若≠0 则标红；
   - 柱状图可视化展示：蓝柱（应付）vs橙柱（已回），一目了然；
   - 回款明细表：列举项目所有收入记录（到账时间、收款金额、收款方式、关联凭证）；
4. **督办计划块**：
   - 完整展示生成的督办计划内容；
   - 优先级标签、建议时限、建议行动；
5. **审批记录块**：
   - 时间线展示审批历史（提交时间、审批人、意见、操作）；
   - 若有驳回，展示驳回原因与修改痕迹；
6. **执行反馈块**：
   - 表格展示项目经理所有反馈记录（时间、内容摘要、附件数、状态）；
   - 支持展开查看各条反馈详情、下载附件；
7. **附件查看块**：
   - 列表展示所有上传附件（名称、格式、大小、上传者、上传时间）；
   - 支持预览、下载、删除操作（权限控制）。

**督办台账导出功能：**

支持导出为 Excel / PDF 两种格式：
- **Excel 导出**：包含多个工作表（概览表、项目明细表、回款对比表、审批记录表、执行反馈表）；
- **PDF 导出**：排版精美，包含完整信息、数据可视化图表、打印友好排版；
- 导出文件命名规范：《督办计划_项目名_导出时间.xlsx/pdf》。

##### 3.17.3.8 附件管理与在线预览

**附件支持规范：**

支持上传合同扫描件、甘特图截图、竣工资料扫描件、回款凭证、沟通记录等附件，支持格式：`.doc/.docx/.xls/.xlsx/.pdf/.jpg/.png/.zip/.rar`（共8种）。

**上传限制与批量上传：**

单文件≤50MB，批量上传支持最多 10 文件/次。前端校验格式与大小，超限时弹出友好提示"文件超过50MB/格式不支持"。支持拖拽上传与文件夹选择上传，显示上传进度条。

**附件与督办单绑定：**

上传成功后自动记录文件元数据（名称、格式、大小、上传者、上传时间），与对应督办计划/反馈记录绑定。

**预览与操作：**

附件列表支持点击在线预览（弹窗打开）、下载、删除操作。预览规则沿用系统现有规范（PDF 支持分页/缩放、Office 文档在线查看、图片缩放/旋转、压缩包文件列表展示）。删除附件前需二次确认，删除后无法恢复。

##### 3.17.3.9 权限控制与角色定义

**角色权限划分：**

| 角色 | 权限范围 | 可操作功能 |
|-----|--------|---------|
| 经办人（DATA/ADMIN） | 自己创建的计划 | 创建、编辑、提交督办计划；查看审批反馈；修改驳回后的计划并重新提交 |
| 总经理（GM） | 全部计划 | 审批全部计划（同意/驳回/加签）；查看全项目督办数据；查看执行反馈 |
| 项目经理（PROJ_MGR） | 本人负责项目 | 查看本项目督办计划；接收督办任务；填报执行反馈；标记任务完成/终止 |
| 财务（BUDGET/FINANCE） | 全部计划（只读） | 查看督办计划、回款数据、审批记录、执行反馈；导出督办台账 |
| 采购员（PURCHASE） | 全部计划（只读） | 同财务权限 |
| 管理员（ADMIN） | 全部计划 | 所有权限+数据维护（修改配置、删除记录、权限管理） |

**权限详细规则：**

- 无权限用户隐藏创建、编辑、审批、转办按钮；
- 列表与详情页根据权限显示/隐藏敏感数据（如客户欠款金额、预计回款时间等）；
- 禁止越权查看其他项目经理负责项目的反馈；
- 审批记录、执行反馈均拥有查看权限的用户无法编辑或删除（只读）。

##### 3.17.3.10 预警、提醒与异常处理

**逾期回款预警：**

- 系统每日执行定时任务，检查所有"督办中"任务，若回款缺口+超期天数>0，标记为逾期，界面自动标红高亮显示；
- 根据超期天数分级显示：超期 1-7 天（黄色）、超期 8-14 天（橙色）、超期>14 天（红色）；
- 逾期任务优先在列表置顶展示。

**定时推送提醒：**

- 每日上午 09:00 推送"待审批（当日新增）"任务给总经理，包含待审批计划数、重点项目名；
- 每日下午 17:00 推送"待接收督办任务"给对应项目经理，包含新任务数、客户方欠款总额；
- 每周一 09:00 推送"逾期督办汇总"给总经理，列举所有逾期任务的项目名、缺口金额、超期天数。

**数据异常处理与错误提示：**

| 异常类型 | 提示信息 | 处理方案 |
|---------|--------|--------|
| 甘特图节点未找到 | "关键里程碑未配置甘特图，无法对标工程进度；请补充甘特图数据后重试" | 提示经办人补全数据后重新生成 |
| 竣工资料链接失败 | "竣工资料数据获取异常，建议稍后重试；若问题持续，请联系系统管理员" | 后台记录错误日志，自动重试 3 次 |
| 回款数据异常（如出现负数） | "回款数据异常（已回款金额<0），请检查收入记录；系统已记录该异常，管理员将稍后核验" | 后台告警+邮件通知管理员 |
| 审批流程异常 | "提交审批失败，请稍后重试；若问题持续，请刷新页面重新操作" | 后台自动重试审批流转，失败 3 次后告警管理员 |

**操作日志完整记录：**

所有创建、编辑、提交、审批、转办、反馈、附件上传操作均完整记录至 sys_audit_log（操作人、操作时间、操作类型、修改内容、IP 地址等），管理员可追溯全流程数据变更。

##### 3.17.3.11 数据表与接口设计

**核心数据表：**

**biz_payment_supervision_plan（回款督办计划表）:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| plan_no | VARCHAR(20) | 计划编号（GS+YYMMDD+3位序号）|
| project_id | INT | 非空外键 → biz_project |
| contract_id | INT | 非空外键 → biz_contract |
| gantt_task_id | INT | 非空外键 → biz_gantt_task（关联工程节点） |
| payment_term_id | INT | 非空外键 → biz_contract_payment_term（关联付款条款） |
| required_amount | DECIMAL(14,2) | 约定付款金额 |
| received_amount | DECIMAL(14,2) | 已回款金额 |
| gap_amount | DECIMAL(14,2) | 回款缺口 |
| completion_rate | DECIMAL(5,2) | 回款完成率(%) |
| overdue_days | INT | 超期天数 |
| priority | VARCHAR(10) | 优先级（HIGH/MEDIUM/LOW） |
| expected_date | DATE | 建议回款时限 |
| supervision_status | VARCHAR(20) | 督办状态（PENDING/IN_PROGRESS/COMPLETED/TERMINATED） |
| approval_status | VARCHAR(20) | 审批状态（DRAFT/SUBMITTED/APPROVED/REJECTED） |
| assigned_pm_id | INT | 责任项目经理ID |
| created_by | INT | 创建人 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 修改时间 |
| remark | TEXT | 备注说明 |

索引：project_id + contract_id、supervision_status、created_at。

**biz_payment_supervision_approval（审批记录表）:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| plan_id | INT | 非空外键 → biz_payment_supervision_plan |
| approver_id | INT | 审批人ID |
| action | VARCHAR(20) | 操作（SUBMIT/APPROVE/REJECT） |
| opinion | TEXT | 审批意见 |
| created_at | DATETIME | 审批时间 |

**biz_payment_supervision_feedback（执行反馈表）:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| plan_id | INT | 非空外键 → biz_payment_supervision_plan（关联督办计划） |
| progress_desc | TEXT | 跟进进度描述 |
| communication_record | TEXT | 沟通记录 |
| expected_receipt_date | DATE | 预计回款时间 |
| issues | TEXT | 存在问题 |
| submitted_by | INT | 反馈提交者ID |
| submitted_at | DATETIME | 提交时间 |
| updated_at | DATETIME | 更新时间 |

索引：plan_id、submitted_at。

**biz_payment_supervision_feedback_attachment（反馈附件表，新增）:**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| feedback_id | INT | 非空外键 → biz_payment_supervision_feedback |
| file_name | VARCHAR(255) | 文件名称 |
| file_type | VARCHAR(50) | 文件类型 |
| file_size | BIGINT | 文件大小 |
| file_path | VARCHAR(500) | 存储路径 |
| uploaded_by | INT | 上传者ID |
| uploaded_at | DATETIME | 上传时间 |

索引：feedback_id。

**接口清单（12 个核心 API）：**

获取数据源并校验：GET /api/v1/payment-supervision/precheck/{projectId}。生成回款督办计划：POST /api/v1/payment-supervision/generate。获取计划详情：GET /api/v1/payment-supervision/{planId}。更新计划：PUT /api/v1/payment-supervision/{planId}。获取计划列表：GET /api/v1/payment-supervision/list。提交审批：POST /api/v1/payment-supervision/{planId}/submit。审批操作（同意/驳回）：POST /api/v1/payment-supervision/{planId}/approve。自动转办：POST /api/v1/payment-supervision/{planId}/assign。接收转办任务：POST /api/v1/payment-supervision/{planId}/accept。提交执行反馈：POST /api/v1/payment-supervision/{planId}/feedback。更新反馈：PUT /api/v1/payment-supervision/feedback/{feedbackId}。导出督办台账：POST /api/v1/payment-supervision/export。

财务人员录入，无需审批。编号 SK+YYMMDD+3 位顺序号。字段含关联项目、关联收入合同、收款金额、发票金额、发票号码、收款日期、收款方式（银行转账/支票/现金/其他）、备注。保存后合同"累计收款金额"自动更新。

#### 3.17.4 付款管理

**人工费/软件费付款申请编号** PA+YYMMDD+3 位顺序号。**材料款/设备款付款申请编号** MP+YYMMDD+3 位顺序号。

**人工费付款字段：** 付款编号（自动）、关联项目、关联支出合同、关联对账单（必填）、付款金额（校验不超过对账单可付余额）、收款方（自动回填）、收款银行和账号（自动回填）、付款方式、发票信息、备注。

**材料款付款字段：** 付款编号（自动）、关联项目、关联支出合同、关联入库单（必填，可多选）、付款金额（校验不超过合同累计可付余额）、收款方、银行和账号、付款方式、发票信息、备注。

**审批流程（统一）：** 项目经理 → 采购员审批 → 财务审批 → 总经理审批。驳回至项目经理。

**成本归集：** 付款审批通过且财务确认打款后，系统自动将付款金额按合同类型归集到项目成本台账。

#### 3.17.5 支出合同付款计划表管理

##### 3.17.5.1 功能定位与流转规则

支出合同付款计划表作为支出合同的附属配套表格，独立于合同核心条款但强制关联，随支出合同同步走"创建→编辑→审批→生效"全流程。该表仅用于系统内付款数据统计与监管，不具备合同法律效力，不提供打印、电子文件下载功能，数据仅作为后续月度付款计划、已付/应付核算的核心数据源。

合同审批通过后，付款计划表数据自动入库冻结，仅支持管理员/财务人员按权限修改（需留痕记录），确保数据完整可追溯。

##### 3.17.5.2 计划表创建与关联

**触发时机：** 用户在系统创建"支出合同"时，需同步创建"支出合同付款计划表"（支持一键自动生成、手动编辑）。

**关联维度：** 以合同ID为唯一关联键，计划表与对应支出合同强制绑定，不可跨合同关联。

**操作入口：** 支出合同详情页新增"付款计划管理"标签页，仅支持查看、编辑操作（无打印/下载入口）。

##### 3.17.5.3 计划表核心字段定义

| 字段名称 | 字段说明 | 必填 |
|---------|--------|-----|
| 合同编号/名称 | 自动同步支出合同信息，不可修改 | 是 |
| 条款编号 | 关联合同内对应付款条款（下拉选择） | 是 |
| 合同付款条款原文 | 自动同步对应条款内容，作为付款依据 | 是 |
| 计划付款时间 | 按合同约定填写具体付款日期（精确到日） | 是 |
| 计划付款金额 | 按条款约定填写应付金额；支持按比例自动计算（如合同总金额×30%） | 是 |
| 付款节点说明 | 补充该笔付款对应的业务节点（如"材料到货验收后"） | 否 |
| 备注 | 其他监管所需信息（如"需提供发票后付款"） | 否 |

##### 3.17.5.4 流程联动与审批

**编辑权限：** 合同创建人、管理员可编辑，编辑后需随合同一同提交审批。

**审批同步：** 付款计划表与支出合同合并展示在审批页面，审批人可查看计划表完整性与合理性；审批通过则计划表生效，驳回则需同步修改计划表。

**数据同步：** 合同生效后，计划表数据自动同步至"合同付款条款数据源"，供后续月度付款计划统计调用。

##### 3.17.5.5 数据表设计

**biz_contract_payment_plan（支出合同付款计划表）：** id INT 主键自增；contract_id INT 非空外键；payment_term_id INT 非空外键；planned_payment_date DATE 非空；planned_amount DECIMAL(14,2) 非空；payment_node_desc VARCHAR(255) 可空；remark VARCHAR(500) 可空；created_at、updated_at DATETIME。索引：contract_id、payment_term_id。

#### 3.17.6 月度付款计划管理

##### 3.17.6.1 核心数据来源集成

**支出合同数据：** 合同编号、合同名称、合作方、项目、合同类型（材料采购/设备采购/劳务人工）、合同总金额。

**付款计划表数据：** 合同ID、条款编号、付款计划表原文、计划付款时间、计划付款金额、付款节点说明。

**历史已付款数据：** 对应合同/条款的累计已付款金额、付款批次、付款时间、付款凭证、支付方式。关联表：biz_payment_apply（付款申请表中status='PAID'的记录）。

**数据绑定规则：** 以「合同ID + 条款编号」为唯一关联维度，自动匹配付款计划表 + 已付款记录 + 待付余额。系统按财务确认打款时间统计已付款，未确认打款状态的付款申请不计入已付款数据。

##### 3.17.6.2 自动统计逻辑

**月度筛选规则：** 系统按自然月为统计周期，用户选择指定月份后自动匹配"支出合同付款计划表"中「计划付款时间在当月」且「达到付款触发条件」的记录。

**款项分类统计：** 按合同类型自动归类（材料类应付款、设备类应付款、人工类应付款）。

**精准金额核算：** 每条付款数据自动计算：
- 计划表约定付款金额；
- 对应条款已完成付款金额（累计历史已支付）；
- 本次应付金额 = 计划表约定 - 已完成付款（扣减已付部分，避免重复支付）；
- 合同总金额、剩余未付金额。

**汇总统计：** 后端自动统计材料款/设备款/人工款各类的已付小计、本次应付小计，汇总月度总已付、总应付金额。

##### 3.17.6.3 月度计划生成与编辑

**一键自动生成：** 选择统计月份后，系统一键抓取当月到期数据 + 历史已付 + 计划表数据，生成完整月度付款计划。

**手动调整维护：** 支持手动新增、删除、修改本次应付金额（需标注调整原因），修改后自动更新剩余未付金额。

**状态管理：** 草稿→已确认→审批中→已审批→部分支付→已完成支付，状态变更时同步联动已付金额统计。

##### 3.17.6.4 列表展示与筛选

**计划表列表：** 展示合同编号、名称、项目、合作方、计划付款总金额、计划笔数、合同状态、创建时间；支持按项目、合同类型、状态筛选，关键词搜索。

**付款计划列表：** 展示统计月份、项目、总已付、总应付、计划状态、创建人、创建时间；支持按月份、项目、款项类型、付款状态筛选。

**详情页：** 分块展示支出合同付款计划表摘要、回款数据对比表（含已付/应付）、条款依据明细、汇总统计，支持全屏查看。

##### 3.17.6.5 导出与权限

**支出合同付款计划表：** 不支持任何电子文件导出（Excel/PDF）及打印功能，仅可系统内在线查看。

**月度付款计划导出：** 支持导出Excel/PDF格式，包含封面、汇总表、条款依据明细。

**权限控制：** 合同创建人编辑付款计划表；经办人创建/编辑月度计划；财务审核；总经理审批。阻止所有用户对计划表的打印/导出操作（前端隐藏按钮+后端拦截）。

##### 3.17.6.6 校验规则与异常提示

**金额校验：** 计划付款总和>合同总金额时拦截；同一条款重复添加时标红提示；本次应付为负时拦截；已付>应付时自动修正。

**数据校验：** 计划时间/金额为空时禁止提交，提示"请完善核心信息"；历史已付数据与计划表不匹配时自动同步更新；无结果时提示"本月无应付款项"。

##### 3.17.6.7 接口设计

查询支出合同付款计划表：GET /api/v1/payment-plan/contracts/{contractId}。创建/编辑计划表：POST/PUT /api/v1/payment-plan/plan-table。查询月度付款计划：GET /api/v1/payment-plan/monthly/{month}。一键生成月度计划：POST /api/v1/payment-plan/generate-monthly。更新计划状态：PUT /api/v1/payment-plan/{planId}/status。导出月度计划：POST /api/v1/payment-plan/export。

#### 3.17.7 日常报销管理

##### 3.17.7.1 功能定位与数据联动

日常报销管理为财务系统的重要组成部分，与现有月度付款计划、合同付款计划表数据同源，共享部门/项目/合同基础数据字典，确保成本归集维度一致。核心功能：支持合规单据填报→多格式附件上传→模块联动→预算实时扣减→权限隔离→合规校验→审批流转→支付执行的全闭环流程。

数据关联维度：报销单强制绑定「部门ID」「项目ID」（可选绑定「合同ID」），与现有系统共用ID，避免数据重复录入。

##### 3.17.7.2 预算占用实时管控

**预算关联与占用计算：**

报销单提交时自动关联对应部门/项目的年度/月度预算，按费用类型维度（差旅费预算、业务招待费预算等）明确预算额度。

报销审批通过后，实时扣减对应预算金额，自动计算并同步更新：
- 预算已用金额 = Σ(已审批通过的该类报销金额)
- 剩余预算金额 = 总预算 - 已用金额
- 预算占用比例 = 已用金额 ÷ 总预算 × 100%

**预警与拦截规则：**

- ⚠️ **预警触发**：预算占用比例≥80%，系统弹出黄色预警 "当前部门/项目该类费用预算已使用80%，请合理安排报销"
- ❌ **超预算拦截**：预算占用比例≥100%，自动拦截提交，提示 "该部门/项目对应费用预算已耗尽，需提交预算调整申请后再报销"

预算调整申请获批后，按调整后总预算重新计算占用比例。

##### 3.17.7.3 角色权限隔离与数据可见范围

| 角色 | 核心操作权限 | 数据可见范围 | 禁止权限 |
|-----|-----------|-----------|--------|
| 报销人 | 提交本人报销单、上传附件、查看本人记录（含审批状态）、修改未提交/驳回的单据 | 仅本人创建的报销单 | 查看他人数据、审核、修改已提交单 |
| 部门负责人 | 审核本部门员工报销单、查看本部门所有报销记录 + 预算使用情况 | 本部门所有报销数据 | 审核其他部门、修改核心信息 |
| 财务审核岗 | 审核全部报销单（合规性+金额准确性）、查验发票、查看所有明细 | 全部门报销数据、预算数据、合同数据 | 提交报销单、修改报销人填报的核心信息 |
| 财务主管 | 复核大额报销（单笔>5000元）、特殊费用报销、查看汇总数据 | 全公司报销汇总+明细、预算数据 | 直接修改金额、删除已提交单 |
| 管理层 | 查看分管部门/全公司汇总数据、审批大额/特殊报销 | 分管范围/全公司汇总数据 | 查看个人明细、编辑报销单 |
| 出纳 | 执行支付操作、查看待支付报销单、生成支付凭证、对账 | 已审批通过的报销数据 | 修改金额、删除单据、更改审批结果 |
| 管理员 | 系统配置、数据维护、查看全量数据 | 全系统数据 | 替代其他角色提交/审核（仅维护） |

##### 3.17.7.4 单据填报与附件规范

**核心必填字段：**
- 报销人（自动带出登录人）、部门ID/名称（自动关联登录人所属部门）、项目ID/名称（可选）、合同ID/名称（可选，仅合同相关报销）
- 费用类型（下拉：差旅费、业务招待费、办公费、通讯费等）、报销金额（含税/不含税区分）、费用发生日期、费用用途（≥10字详细描述）
- 非必填字段：备注（补充说明）

**明细支持：** 同一报销单可填报多笔同类型/不同类型费用，支持批量添加/删除明细，自动计算总金额。

**附件要求：**
- **核心凭证**：增值税专用发票（完整抬头、税号、专用章）、增值税普通发票（抬头、税号、专用章）、非发票凭证（财政监制章/机票/火车票）
- **辅助证明**：行程单、消费明细、验收单、会议通知、项目任务单
- **上传规则**：支持.pdf/.jpg/.png/.doc/.docx/.xls/.xlsx/.zip/.rar（共8种），单文件≤50MB；附件与报销单唯一绑定；自动命名 "报销单编号-附件类型-序号"

**预览与操作：** 支持在线预览（无需下载），仅未提交/驳回状态可删除/替换（二次确认，删除不可恢复）。

##### 3.17.7.5 合规校验规则与异常拦截

**报销时效校验：**
- 规则：费用发生日期距提交日期≤30天，超出需上传"跨期报销说明"附件
- 拦截：超时效未说明，提示 "费用已超1个月，需上传跨期说明并经部门负责人审批"

**业务招待费限额校验：**
- 规则：单笔/累计业务招待费（绑定合同时）≤合同金额×5‰；不绑定合同则按部门月度预算限额控制
- 计算：系统自动获取合同总金额，计算最大可报销限额，对比本次+累计招待费
- 拦截：超限时，提示 "该合同招待费限额为XX元，已报销XX元，本次报销后将超出限额"

**其他合规校验：**
- 发票真伪：对接税务部门发票查验接口，虚假发票拦截 "发票核验失败"
- 金额一致性：报销单总额≠附件凭证总额（允许±1元误差），提示 "报销金额与凭证不符"
- 必填与完整性：必填字段未填/无附件，禁止提交，提示 "请完善必填字段并上传附件"
- 重复报销：自动比对发票号码+金额，已审批通过的相同记录拦截 "该发票已报销，不可重复提交"

##### 3.17.7.6 审批流程与操作留痕

**流程节点：**
- **普通报销**（≤5000元）：报销人提交 → 部门负责人审核 → 财务审核岗审核 → 财务主管复核（可选）→ 出纳支付
- **大额/特殊报销**（>5000元/业务招待费/跨期）：报销人提交 → 部门负责人审核 → 财务审核岗审核 → 财务主管复核 → 管理层审批 → 出纳支付

**审批操作：** 同意、驳回（选择驳回原因：附件不全/超时效/超限额/发票不合规/其他，支持补充意见）。

**留痕机制：** 记录审批人、审批时间、审批意见、操作IP，全程不可篡改，支持导出审批轨迹。

**驳回处理：** 报销人收到驳回通知后可修改/补充附件，重新提交按原流程流转。

##### 3.17.7.7 列表展示与查询筛选

**报销单列表字段：** 报销单编号、报销人、部门、项目、合同编号、费用类型、总金额、提交时间、审批状态、预算占用比例、操作栏。

**筛选条件：** 按部门、项目、合同、费用类型、审批状态、报销时间范围、金额区间筛选；关键词搜索（报销单编号、报销人姓名、发票号码）；大数据量分页加载（默认20条/页）。

**详情页展示：** 报销单完整信息、费用明细列表、附件列表（支持预览）、审批轨迹、预算占用明细。高亮合规校验结果，异常状态标红提示。

##### 3.17.7.8 导出与归档

**导出功能：**
- 单张报销单导出（Excel/PDF），包含单据信息、费用明细、附件清单、审批记录
- 批量导出报销记录（按筛选条件），格式Excel，适配财务做账与归档需求

**归档规则：** 审批通过并完成支付后自动归档至"财务报销归档库"，归档后不可修改，保留期限≥10年，支持按年度/部门/项目查询。

##### 3.17.7.9 数据表设计

**biz_expense_report（报销单主表）：** id INT 主键自增；report_no VARCHAR(20) 唯一（格式：BX+YYMMDD+3位序号）；reporter_id INT 非空；department_id INT 非空；project_id INT 可空外键；contract_id INT 可空外键；expense_type VARCHAR(50) 非空；total_amount DECIMAL(14,2) 非空；report_date DATE 非空；expense_date DATE 非空；expense_desc TEXT 非空；status VARCHAR(20)（DRAFT/SUBMITTED/APPROVED/REJECTED/PAID）；approval_status VARCHAR(20)；budget_used_ratio DECIMAL(5,2)；created_at、updated_at DATETIME。索引：reporter_id、department_id、project_id。

**biz_expense_report_detail（报销明细表）：** id INT 主键自增；report_id INT 非空外键；expense_category VARCHAR(50)；amount DECIMAL(14,2)；description VARCHAR(255)；created_at。

**biz_expense_report_attachment（报销附件表）：** id INT 主键自增；report_id INT 非空外键；file_name VARCHAR(255)；file_type VARCHAR(50)；file_size BIGINT；file_path VARCHAR(500)；uploader_id INT；upload_time DATETIME；created_at。索引：report_id。

**biz_expense_report_approval（报销审批记录表）：** id INT 主键自增；report_id INT 非空外键；approver_id INT；action VARCHAR(20)（SUBMIT/APPROVE/REJECT）；opinion TEXT；created_at。

##### 3.17.7.10 接口设计

创建报销单：POST /api/v1/expense/report。查询报销单列表：GET /api/v1/expense/report/list。获取报销单详情：GET /api/v1/expense/report/{reportId}。编辑报销单：PUT /api/v1/expense/report/{reportId}。提交报销单：POST /api/v1/expense/report/{reportId}/submit。审批报销单：POST /api/v1/expense/report/{reportId}/approve。上传报销附件：POST /api/v1/expense/report/{reportId}/attachments。查询报销附件列表：GET /api/v1/expense/report/{reportId}/attachments。删除报销附件：DELETE /api/v1/expense/attachment/{attachmentId}。校验预算占用：GET /api/v1/expense/budget-check。查询发票真伪：POST /api/v1/expense/invoice-verify。导出报销单：POST /api/v1/expense/report/export。查询审批轨迹：GET /api/v1/expense/report/{reportId}/approval-trace。

#### 3.17.8 接口设计

收入拆分列表：GET /api/v1/statement/split/list。创建收入拆分：POST /api/v1/statement/split。对账单列表：GET /api/v1/statement/list。对账单详情：GET /api/v1/statement/{id}。提交对账单：POST /api/v1/statement/{id}/submit。上传对账单附件：POST /api/v1/statement/{id}/attachments。对账单附件列表：GET /api/v1/statement/{id}/attachments。删除对账单附件：DELETE /api/v1/statement/attachment/{attachmentId}。查询对账定时配置：GET /api/v1/statement/config/{projectId}。修改对账定时配置：PUT /api/v1/statement/config/{projectId}。手动触发对账单生成：POST /api/v1/statement/generate-manual。获取差异分析期次列表：GET /api/v1/statement/difference/periods/{projectId}。查询对账单差异分析：GET /api/v1/statement/difference/analysis。添加差异备注：POST /api/v1/statement/difference/remark。导出差异分析报告：POST /api/v1/statement/difference/export。回款督办计划列表：GET /api/v1/payment-supervision/list。生成回款督办计划：POST /api/v1/payment-supervision/generate。审批回款督办：POST /api/v1/payment-supervision/{planId}/approve。转办督办任务：POST /api/v1/payment-supervision/{planId}/assign。付款计划表查询：GET /api/v1/payment-plan/contracts/{contractId}。付款计划表管理：POST/PUT /api/v1/payment-plan/plan-table。月度付款计划查询：GET /api/v1/payment-plan/monthly/{month}。月度计划生成：POST /api/v1/payment-plan/generate-monthly。收款列表：GET /api/v1/payment/receipt/list。创建收款：POST /api/v1/payment/receipt。付款申请列表：GET /api/v1/payment/apply/list。人工费付款：POST /api/v1/payment/apply/labor。材料款付款：POST /api/v1/payment/apply/material。

#### 3.17.9 成本归集细目

| 成本一级科目 | 成本二级科目 | 归集来源 | 归集规则 |
|-------------|-------------|---------|---------|
| 材料费 | 主材费 | 材料类支出合同付款 | 合同类型='材料类'且物资分类='主材' |
| 材料费 | 辅材费 | 材料类支出合同付款 | 合同类型='材料类'且物资分类='辅材' |
| 设备费 | 设备采购费 | 设备类支出合同付款 | 合同类型='设备类' |
| 设备费 | 设备租赁费 | 设备租赁类支出合同付款 | 合同备注标注为'租赁' |
| 人工费 | 劳务工资 | 劳务合同付款 | 合同类型='劳务施工' |
| 人工费 | 劳务签证费 | 劳务签证审批通过的金额 | 变更类型='labor_visa' |
| 分包费 | 专业分包费 | 分包合同付款 | 合同类型='专业工程分包' |
| 软件费 | 软件采购费 | 软件类支出合同付款 | 合同类型='软件工程' |
| 其他费 | 零星采购费 | 零星采购审批通过的金额 | 零星采购记录 |
| 其他费 | 变更增量费 | 现场签证/甲方变更审批的金额 | 变更审批记录 |

#### 3.17.10 发票管理功能

**发票录入：** 财务人员在收款登记或付款审批时录入。字段含发票号码（VARCHAR(50)，必填）、发票类型（增值税专票/增值税普票/其他，必填）、发票金额（DECIMAL(14,2)，必填）、开票日期（DATE，必填）、开票方名称（自动回填）、税率、税额（自动）、发票附件（选填）。

**发票与业务关联：** 通过 biz_invoice 表关联收款记录或付款申请记录。一个付款申请可关联多张发票，发票总金额应等于付款金额（允许误差 0.01 元）。

**发票过期预警：** 增值税专用发票认证期限为 360 天。系统每日检查所有未认证专票，距开票日不足 30 天时生成预警。

### 3.18 竣工管理模块

#### 3.18.1 完工管理

项目经理发起。字段含关联项目、申请标题、计划完工日期、完工内容说明（必填）、自检结论（必填）、遗留问题（选填）、附件。审批：项目经理 → 预算员 → 采购员 → 总经理（资料员阅知）。验收通过后项目状态变为"已完工验收"。

#### 3.18.2 竣工图纸

支持分类管理、批量上传（最多 10 文件/次）、关联项目、版本管理。审批：预算员 → 项目经理 → 采购员 → 总经理（资料员阅知）。

#### 3.18.3 竣工文档归档

**文档分类：** 签证和设计变更、图纸会审记录、工地会议纪要、工程档案及技术资料、合同文件、对账单、付款申请。支持按类别分标签页展示、批量上传、系统自动归集已有文件。

#### 3.18.4 劳务结算

结算编号 LS+YYMMDD+3 位顺序号。字段含关联项目、关联劳务合同、结算金额（DECIMAL(14,2)，必填）、已支付金额（自动）、本次申请付款金额（选填）、备注。审批：项目经理 → 预算员 → 采购员 → 财务 → 总经理。

#### 3.18.5 接口设计

完工申请：POST /api/v1/completion/finish-apply。竣工图纸列表：GET /api/v1/completion/drawings。上传竣工图纸：POST /api/v1/completion/drawings。竣工文档列表：GET /api/v1/completion/docs。劳务结算列表：GET /api/v1/completion/labor-settlement/list。创建劳务结算：POST /api/v1/completion/labor-settlement。

### 3.19 合同模板管理模块

#### 3.19.1 功能概述

合同模板采用变量占位符机制——模板文件中以 {{变量名}} 标记可填写区域，签订合同时系统解析模板将变量渲染为输入框，非变量区域只读展示。

#### 3.19.2 收入合同模板

字段含模板名称、合同类型、模板来源（甲方拟定/非甲方拟定）、甲方信息、模板文件、条款说明、生效状态、版本号、审批状态。甲方拟定模板审批：采购员 → 总经理。非甲方拟定：采购员 → 财务 → 法务 → 总经理。

#### 3.19.3 支出合同模板

**录入/修改时间窗口：** 每月 25 日 00:00 至 30 日 23:59。窗口期外新增和编辑按钮禁用。审批：采购员 → 财务 → 法务 → 总经理。模板分类：设备类、材料类、劳务类、施工类、软件类。每个合同类型可设置一个默认模板。

**常用变量：** {{合同编号}}、{{合同名称}}、{{甲方}}、{{乙方}}、{{合同金额}}、{{金额大写}}、{{签订日期}}、{{开始日期}}、{{结束日期}}、{{项目名称}}、{{项目编号}}。

#### 3.19.4 接口设计

模板列表：GET /api/v1/contract/template/list。创建模板：POST /api/v1/contract/template。编辑模板：PUT /api/v1/contract/template/{id}。检查维护窗口：GET /api/v1/contract/template/check-window。

#### 3.19.5 合同模板导入与智能签订设计

##### 3.19.5.1 模板导入规则

**支持格式**：.docx、.doc 格式合同模板

**模板标识方式**：模板中可编辑区域使用 **下划线______** 标记，系统在导入时自动解析这些下划线区域作为"智能填充字段"

**导入后自动解析**：
- 自动提取模板中所有下划线标记的可填充区域
- 自动识别固定文本、段落格式、表格结构
- 建立字段与合同表单的映射关系

**按合同类型分类管理**：
- 收入类：IC（收入合同）
- 支出类：EC（支出合同）、LP（零星采购）、BC（补充协议）
- 分类存储：materials（材料类）、equipment（设备类）、labor（劳务类）、other（其他）

##### 3.19.5.2 合同签订页面自动加载流程

**操作流程**：
1. 采购员进入合同签订页面
2. 选择项目名称 → 选择合同类型
3. 系统**自动匹配对应的已导入模板**
4. **自动加载格式化合同正文**到签订表单
5. 页面展示为**富文本只读 + 可填区域编辑**双模态：
   - 下划线区域：白色背景，可输入内容、可编辑、可修改
   - 非下划线区域：灰色背景、不可选中、不可修改、不可删除、不可粘贴覆盖

**编辑权限控制细节**：
- 仅下划线标记的区域允许编辑，系统对每个编辑操作进行权限校验
- 对非下划线正文任何修改：禁止修改、禁止删除、禁止粘贴覆盖、禁止复制
- 复制操作时仅复制可填写内容，不复制固定文本
- 防止通过 DOM 操作或浏览器开发工具进行篡改

##### 3.19.5.3 智能自动填充规则

**选择供应商后自动回填**：采购员在合同表单中选择供应商名称后，系统从 biz_supplier 表自动回填：
1. 供应商全称
2. 供应商联系人
3. 联系电话
4. 公司地址
5. 开户银行
6. 银行账号

**项目信息自动填充**：
1. 项目名称（关联项目自动读取）
2. 项目编号（自动读取）
3. 工程地点（自动读取）
4. 签订日期（系统当前日期，可手动修改）

**表单字段实时同步到合同正文**：
- 金额、税率、工期等表单字段输入后实时同步到合同正文对应位置
- 采用双向数据绑定，支持从表单修改和从合同正文直接修改（仅编辑区）

##### 3.19.5.4 防篡改机制

**前端防护**：
- 非下划线区域在 DOM 结构中被标记为"只读"属性，禁用右键菜单
- 键盘输入被拦截，Delete/Backspace/Ctrl+X 等操作不生效
- 粘贴操作触发智能过滤，仅粘贴到可编辑区域，自动过滤进入非编辑区域的内容

**后端防护**：
- 提交审批时系统对比原模板与当前合同内容
- 检查是否存在非授权修改（非下划线区域被篡改），违规拦截提交
- 审批通过后合同内容写入数据库时标记为"已锁定"，任何再次编辑严格校验

**日志与追踪**：
- 所有编辑操作记入 sys_audit_log，包括内容、操作人、操作时间
- 防篡改校验结果记入 biz_contract_tamper_check 表，作为法律证据

##### 3.19.5.5 审批通过后自动水印与打印

**水印自动生成**：
- 审批全部通过后，系统自动生成合同的 PDF 版本
- PDF 每页**自动添加半透明 MOCHU 水印**
- 水印内容：MOCHU-OA 正式合同
- 水印位置：居中斜铺（45 度角）
- 水印透明度：30%-50%（不影响文本阅读）
- **水印不可删除、不可覆盖**（PDF 中集成为文档层，无法移除）

**一键打印功能**：
- 审批完成的合同预览页提供**一键打印按钮**（位置：合同详情右上角）
- 打印资源：
  - 打印时自动去掉水印（可配置），输出为可打印版本
  - 打印格式：自动分页、A4 标准格式（210mm × 297mm）
  - 打印输出：支持直接连接打印机输出、支持导出为 PDF 文件
- 打印前预览：用户可查看打印效果，确认后再执行打印

##### 3.19.5.6 业务流程完整链路

1. **管理员导入合同模板**
   - 模板上传、解析、下划线字段识别
   - 系统自动建立字段与表单的映射表

2. **采购员合同签订操作**
   - 选择项目 → 选择合同类型 → 自动加载模板
   - 填写下划线内容 → 选择供应商 → 自动填充信息
   - 实时预览合同效果

3. **提交审批**
   - 系统自动校验防篡改结果
   - 检查必填字段完整性
   - 提交到审批流程：采购员 → 财务 → 法务 → 总经理

4. **审批通过处理**
   - 自动生成 PDF 并添加 MOCHU 水印
   - 生成打印版本（无水印可打印版）
   - 合同进入"已生效"状态

5. **打印与归档**
   - 打印用户点击一键打印按钮
   - 系统推送到打印机或导出 PDF
   - 打印记录存入审计日志
   - 完成合同归档

##### 3.19.5.7 新增字段与表结构

**新增表：biz_contract_template_field（模板字段映射表）**

```sql
CREATE TABLE biz_contract_template_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id INT NOT NULL,
    field_name VARCHAR(100) NOT NULL,        -- 字段名称（如"供应商名称"）
    field_placeholder VARCHAR(100) NOT NULL,  -- 模板占位符（如{{supplier_name}}）
    field_type VARCHAR(50),                  -- 字段类型（text/date/select等）
    is_editable TINYINT DEFAULT 1,          -- 是否允许编辑（0-固定，1-可编辑）
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uq_template_field (template_id, field_placeholder),
    FOREIGN KEY (template_id) REFERENCES biz_contract_template(id)
);
```

**新增表：biz_contract_watermark_log（水印生成日志表）**

```sql
CREATE TABLE biz_contract_watermark_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id INT NOT NULL,
    watermark_content VARCHAR(200),         -- 水印内容
    watermark_position VARCHAR(50),         -- 水印位置（居中/左上/右下等）
    watermark_opacity DECIMAL(3,2),         -- 透明度（0.3-0.5）
    pdf_url VARCHAR(500),                   -- 生成的 PDF URL
    created_by INT,                         -- 生成人 ID
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES biz_contract(id),
    INDEX idx_contract_id (contract_id)
);
```

**新增表：biz_contract_tamper_check（防篡改校验日志表）**

```sql
CREATE TABLE biz_contract_tamper_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_id INT NOT NULL,
    check_type VARCHAR(50),                 -- 校验类型（content/field/structure）
    check_result VARCHAR(50),               -- 校验结果（pass/fail）
    detection_details TEXT,                 -- 检测详情（JSON 格式记录异常信息）
    checked_by INT,                         -- 校验人 ID
    checked_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES biz_contract(id),
    INDEX idx_contract_check_result (contract_id, check_result)
);
```

**扩展 biz_contract 表字段**：
```sql
ALTER TABLE biz_contract ADD COLUMN (
    template_source_id INT COMMENT '来源模板ID',
    pdf_url VARCHAR(500) COMMENT 'PDF文件URL',
    watermark_applied TINYINT DEFAULT 0 COMMENT '是否已添加水印(0-否,1-是)',
    is_locked TINYINT DEFAULT 0 COMMENT '是否锁定禁止修改(0-否,1-是)',
    tamper_checked TINYINT DEFAULT 0 COMMENT '是否已通过防篡改校验(0-否,1-是)'
);
```

##### 3.19.5.8 接口补充设计

**模板导入接口**：POST /api/v1/contract/template/import-docx
- 请求参数：template_file（上传文件）、template_type（IC/EC/LP/BC）、template_category（material/equipment/labor）
- 响应：template_id、parsed_fields（解析出的字段列表）、field_count（字段数）

**获取合同签订页面**：GET /api/v1/contract/draft/{projectId}/{contractType}
- 响应：template_content（格式化合同正文）、editable_fields（可编辑字段）、form_fields（表单数据）

**水印生成接口**：POST /api/v1/contract/{id}/generate-watermark
- 触发条件：合同审批全部通过后系统自动调用
- 响应：pdf_url、watermark_status、created_at

**一键打印接口**：GET /api/v1/contract/{id}/print
- 响应：可打印版 PDF 文件流
- 同时生成打印记录到 sys_audit_log

##### 3.19.5.9 UI/UX 设计

**合同签订页面展示**：
- 左侧：表单区域（项目选择、合同类型、供应商选择、价格信息等）
- 右侧：合同正文预览区（富文本展示，下划线区灰底，非下划线区白底）
- 底部：提交/保存草稿/返回按钮

**权限控制视觉反馈**：
- 可编辑区域：鼠标进入显示绿色边框，光标变成编辑状态（输入光标）
- 禁止编辑区域：鼠标进入显示禁止符号（禁行标志），光标变成禁止状态

**打印按钮**：
- 审批完成后在合同详情页右上角显示"一键打印"按钮
- 点击后弹出打印预览对话框，展示打印效果，确认后打印

### 3.20 人力资源管理模块

#### 3.20.1 工资/社保管理

**工资表自动生成：** 每月 25 日 00:00 定时任务自动生成下月工资表草稿。生成逻辑为从薪资配置表和社保配置表读取数据，按个税税率表计算个税，实发工资 = 基本工资 + 岗位工资 + 绩效 + 补贴 - 社保个人扣款 - 个税。

**财务调整：** 工资表生成后财务可逐人调整奖金、临时扣款等。审批：财务 → 总经理 → 标记"可打款"。

#### 3.20.2 日常报销

报销类型：交通费、餐费、住宿费、办公用品、通讯费、其他。编号 BX+YYMMDD+3 位顺序号。审批：普通员工 → 直接主管 → 财务 → 出纳确认。部门主管 → 上级领导 → 财务 → 出纳确认。

#### 3.20.3 资产移交

资产分类：办公资产和工程资产。办公资产审批：部门主管 → HR 确认。工程资产审批：项目经理 → 采购员确认。

#### 3.20.4 入职/离职流程

**入职流程：** 新员工填写入职申请表 → HR 审核 → 业务部门主管审批 → HR 确认。通过后自动创建用户记录、分配角色、生成密码和邮箱、刷新通讯录。

**离职流程：** 员工填写离职申请表 → 部门主管审批 → HR 审批（检查离职条件）。通过后自动禁用用户、邮箱，清除权限和 Token。

#### 3.20.5 人员合同管理

合同类型：固定期限、无固定期限、实习协议、劳务协议、其他。状态：生效中、已到期、已终止、已续签。每日 00:30 定时检查距到期 ≤ 30 天的合同生成提醒。

#### 3.20.6 资质管理

**公司资质：** 距有效期 ≤ 90 天首次预警，之后每月 1 日重复提醒。

**人员资质：** 距有效期 ≤ 30 天首次预警。HR 可手动"结束预警"。

#### 3.20.7 接口设计

工资表列表：GET /api/v1/hr/salary/list。工资调整：PUT /api/v1/hr/salary/{id}。提交报销：POST /api/v1/hr/reimburse。资产移交：POST /api/v1/hr/asset-transfer。入职申请：POST /api/v1/hr/entry。离职申请：POST /api/v1/hr/resign。人员合同列表：GET /api/v1/hr/contract/list。公司资质列表：GET /api/v1/hr/certificate/company。人员资质列表：GET /api/v1/hr/certificate/personal。

### 3.21 通知公告模块

#### 3.21.1 公告类型

通知（日常事务）、公告（重要决定或制度发布）、制度文件（规章制度文件发布）。

#### 3.21.2 发布功能

字段含标题（必填）、类型（必填）、内容（富文本，必填）、发布范围（全员/指定部门多选）、发布时间（选填，不填则立即发布）、附件。状态：草稿、已发布、已下线。GM 和 HR 可发布、编辑、删除公告。

#### 3.21.3 接口设计

公告列表：GET /api/v1/announcement/list。发布：POST /api/v1/announcement。编辑：PUT /api/v1/announcement/{id}。下线：PATCH /api/v1/announcement/{id}/offline。

### 3.22 报表管理模块

#### 3.22.1 核心报表定义

**项目成本汇总报表：** 数据来源为付款申请和成本归集表。展示项目名称、合同含税金额、累计各费用科目、成本合计、利润、利润率。支持按项目/部门/时间段筛选。

**项目收支对比报表：** 展示合同金额（收入）、累计收款、累计支出、收支差额、收款进度百分比。

**采购执行进度报表：** 展示采购清单总项数、已采购项数、采购完成率、已入库数量占比。

**物资库存报表：** 展示物资名称、规格型号、库存数量、库存金额。

#### 3.22.2 报表订阅与推送

订阅配置含报表名称、接收人、推送频率（每日/每周/每月）、推送时间、推送格式（邮件/PDF/企业微信卡片）。定时任务：日报 06:30、周报周一 06:30、月报每月 1 日 06:30。所有报表支持导出 Excel 和 PDF。

**报表预计算：** 核心报表采用定时任务每日凌晨 03:00 预聚合，存入 biz_report_cache 中间表，查询时直接读取。

#### 3.22.3 接口设计

项目成本：GET /api/v1/report/project-cost。收支对比：GET /api/v1/report/income-expense。采购进度：GET /api/v1/report/purchase-progress。库存：GET /api/v1/report/inventory。导出：GET /api/v1/report/export。订阅管理：POST/GET/DELETE /api/v1/report/subscribe。

### 3.23 审计日志模块

#### 3.23.1 记录内容

每条日志含操作人 ID 和姓名、操作时间（精确到秒）、操作类型（登录/登出/新增/修改/删除/审批/导出/下载）、操作模块、操作详情（TEXT）、操作结果（成功/失败）、IP 地址、User-Agent。

#### 3.23.2 日志记录方式

通过 Spring AOP 在 Service 层使用自定义注解 @AuditLog。日志数据异步写入（消息队列或异步线程池），不影响主业务性能。

#### 3.23.3 查询与导出

查询条件：时间范围（必选，默认最近 7 天）、操作人（模糊搜索）、操作类型（多选）、操作模块（多选）、关键词。支持导出 Excel 和 JSON 格式，单次最大 10 万行。

#### 3.23.4 日志保留策略

在线保留 12 个月，超过 12 个月的由定时任务（每月 1 日 02:00）自动归档至冷存储，归档数据保留 3 年后可删除。

#### 3.23.5 数据表(sys_audit_log)

id BIGINT 主键自增；operator_id INT；operator_name VARCHAR(50)；operate_time DATETIME 非空；operate_type VARCHAR(20) 非空；module VARCHAR(50)；detail TEXT；result VARCHAR(10)；ip VARCHAR(50)；user_agent VARCHAR(500)；created_at DATETIME。索引：operate_time 普通索引，operator_id 普通索引，operate_type 普通索引，operate_time+operate_type 联合索引。按月分表（sys_audit_log_YYYYMM）。

#### 3.23.6 接口设计

日志查询：GET /api/v1/system/audit-log/list。日志导出：GET /api/v1/system/audit-log/export。

### 3.24 工程成果展示模块

#### 3.24.1 功能概述

工程成果展示模块用于发布和展示公司工程项目案例，支持图文视频多媒体内容。

#### 3.24.2 案例字段

案例名称（必填）、案例别名（选填）、项目类型（必填）、项目地点（必填）、合同金额（选填）、项目规模（选填）、开工/竣工日期（选填）、建设单位（选填）、合作单位（选填）、项目简介（富文本，必填）、项目亮点（选填）、封面图片（必填，16:9 比例）、展示图片（多张）、展示视频链接（选填）、全景图链接（选填）、附件、关键词、置顶状态、可见范围（全部访客/仅内部/指定人员）、浏览次数（默认 0）、发布状态（草稿/待审核/已发布/已下架）。

#### 3.24.3 审核流程

采购员（或指定案例管理员）提交 → 部门主管审批 → 总经理审批。

#### 3.24.4 接口设计

案例列表：GET /api/v1/case/list。案例详情：GET /api/v1/case/{id}。创建案例：POST /api/v1/case。编辑：PUT /api/v1/case/{id}。上下架：PATCH /api/v1/case/{id}/status。


## 第 3A 章 新增功能需求（V3.0 补充）

> 以下为 V3.0 版本新增的 10 项功能需求，涵盖广联达造价文件导入、竣工照片水印校验、分部分项工程勾选、隐蔽工程联动、竣工验收文档目录、DWG图纸管理、设计变更联动、材料采购提醒等，可直接并入对应模块开发。


新增功能一：

请基于当前 MOCHU-OA 施工管理系统需求规格说明书
V2.0，新增【广联达造价文件导入自动生成采购清单】功能模块，要求如下：

1\. 业务目标

支持直接导入广联达造价软件导出的标准 Excel/GBQ/GCCP
格式文件，系统自动解析工程量、清单项目、材料名称、规格型号、单位、数量、单价，一键生成【采购清单草稿】，进入原有审批流程。

2\. 功能范围

\- 支持上传：广联达导出的造价清单文件（.xlsx、.xls、.gbq、.gccp）

\-
自动解析字段：项目名称、清单编号、清单名称、材料名称、规格型号、单位、工程量、单价、合价、专业分类

\- 自动匹配：系统现有材料库（材料基准价、物资编码）

\- 自动生成：采购清单草稿，可编辑、可删除、可补充、可提交审批

\- 冲突处理：材料名称不一致、规格缺失、单位不统一时给出智能提示

\- 日志记录：导入记录、导入人、导入时间、解析结果、成功/失败条数

3\. 字段映射规则（必须明确）

广联达字段 → 系统采购清单字段

\- 清单编号 → 物资编码（自动生成或匹配）

\- 项目名称/清单名称 → 物资名称

\- 规格型号 → 规格型号

\- 单位 → 单位

\- 工程量 → 计划数量

\- 单价 → 预算单价

\- 合价 → 预算合价

\- 专业/分部 → 物资分类（主材/辅材/设备/劳务）

4\. 业务流程

1\. 预算员进入【采购管理】→【广联达导入】

2\. 选择项目 → 上传广联达文件

3\. 系统解析 → 展示预览界面（可修改）

4\. 确认导入 → 生成采购清单草稿

5\. 进入原有采购清单审批流程（预算员→财务→总经理）

5\. 异常与校验规则

\- 数量为0自动过滤

\- 单价异常偏高/偏低给出预警

\- 材料名称重复自动合并或提示

\- 导入失败行支持单独导出、重新编辑导入（系统给出导入失败信息提醒）

6\. 接口与表结构

\- 新增接口：/api/v1/purchase/import-glodon

\- 新增导入记录表：biz_glodon_import_log

\- 采购清单原有结构不变，仅扩展导入来源字段

7\. UI/UE

\- 导入按钮放在采购清单列表页

\- 导入预览页用表格展示，支持编辑、删除、匹配材料

\- 导入结果展示：成功N条、失败M条

请按需求规格说明书的正式格式，把这段功能写成标准的【3.9
采购管理模块】补充章节，保持与原文档风格、术语、结构完全一致，可直接并入V2.0完整版。

功能二：

请基于 MOCHU-OA 施工管理系统 V2.0 需求规格说明书，在【3.14
竣工管理模块】中补充以下\*\*竣工资料照片水印解析与甘特图进度智能校验\*\*功能，要求格式、术语、结构与原文档完全一致，可直接插入正文。

需求内容：

1\. 功能名称

竣工资料照片水印解析与甘特图进度自动校验

2\. 功能概述

系统支持对项目经理在竣工资料/施工过程中上传的\*\*施工现场照片\*\*进行\*\*水印文字自动解析\*\*，提取拍摄时间、拍摄地点、项目编号、水印备注等信息；

同时与项目\*\*甘特图任务节点进度\*\*进行联动校验：

当\*\*任意甘特图任务节点完成进度超过
60%\*\*，但该节点\*\*未上传5张带有效水印的施工现场照片\*\*时，系统自动生成待办事项推送给对应项目经理，提醒补充现场照片，确保竣工资料完整性、真实性、可追溯。

3\. 核心规则

3.1 照片水印解析规则

\-
支持解析图片内置文字水印，提取内容包括：拍摄时间、项目编号、任务节点名称、现场位置、拍摄人；

\- 水印必须包含：项目编号 +
任务节点名称+拍摄时间，否则判定为\*\*无效照片\*\*；

\- 系统自动识别照片归属：归属到对应甘特图任务节点/施工部位。

3.2 甘特图进度校验规则

\- 校验触发：每日 00:10 由 XXL-JOB 定时任务执行全量扫描；

\- 触发条件：甘特图任务节点 \*\*进度百分比 \> 60%\*\*；

\- 校验条件：该节点下 \*\*上传有效水印施工现场照片低于5张\*\*；

\- 满足条件 → 系统自动生成待办。

3.3 待办自动生成规则

\- 待办类型：竣工资料照片缺失提醒

\- 接收人：当前项目的项目经理（PROJ_MGR）

\- 待办标题：【资料缺失提醒】项目 {projectName} 任务 {taskName}
进度超60%，请上传现场照片

\- 待办内容：

任务【{taskName}】当前进度
{progress}%，已超过60%，但上传带有效水印的施工现场照片低于5张，请尽快上传，否则影响竣工归档。

\- 待办优先级：中

\- 待办来源：系统自动生成

\- 待办关闭条件：该任务节点下上传≥1张有效水印照片后，系统自动关闭待办

3.4 异常与边界

\- 暂停/中止/已关闭项目不触发；

\- 已上传照片但水印无效的，仍判定为"未上传"；

\- 同一个任务节点只生成一次待办，不重复生成。

4\. 与现有模块联动

\- 依赖：甘特图任务模块、竣工资料上传模块、待办模块、定时任务模块；

\- 复用：现有待办推送机制、企业微信消息推送；

\- 不新增表结构，复用 biz_gantt_task、biz_attachment、sys_audit_log。

5\. 接口与定时任务

\- 新增 XXL-JOB 定时任务：照片缺失校验任务

\- Cron：0 10 0 \* \* ?

\- 新增图片水印解析工具类：ImageWatermarkUtil

\- 不新增业务接口，逻辑在后台自动执行

请按照原需求文档正式格式，将以上内容补充写入【3.14
竣工管理模块】，保持术语统一、结构一致、可直接合并进 V2.0 完整版。

新增功能三：

请基于 MOCHU-OA 施工管理系统 V2.0 需求规格说明书，在【3.6 项目管理模块 →
3.6.3
实体项目立项】中补充\*\*项目立项时分部分项工程类型勾选功能\*\*，要求格式、结构、术语与原文档完全一致，可直接插入正文。

需求内容：

1\. 功能名称

项目立项 - 分部分项工程类型多选勾选

2\. 功能概述

在实体项目立项与虚拟项目立项表单中，增加\*\*分部分项工程类型\*\*选择项，采用\*\*下拉多选菜单\*\*展示，支持预算员/采购员在项目立项阶段明确本项目包含的分部分项工程范围，用于后续竣工资料自动校验、验收节点自动生成、资料缺项提醒。

3\. 展示方式

\- 控件类型：下拉多选复选框（支持全选、反选、搜索）

\- 必选规则：至少选择 1 项，否则无法提交立项

\- 展示位置：实体项目立项 / 虚拟项目立项表单中部

\- 数据来源：系统固定标准分部分项工程分类列表

4\. 分部分项工程标准选项（固定下拉项）

1\. 地基与基础工程

2\. 主体结构工程

3\. 建筑装饰装修工程

4\. 屋面及防水工程

5\. 建筑给水排水及供暖工程

6\. 建筑电气工程

7\. 通风与空调工程

8\. 智能建筑工程

9\. 市政道路工程

10\. 市政管网工程

11\. 园林绿化工程

12\. 其他专业工程

5\. 业务规则

\- 项目立项保存后，分部分项类型同步写入项目表 biz_project
对应字段，以逗号分隔存储

\-
项目状态为进行中后，仍允许项目经理/预算员在项目设置中\*\*重新编辑修改\*\*

\- 所选内容将用于：

\- 竣工资料自动校验（哪些资料必须上传）

\- 验收节点自动生成

\- 资料缺失自动提醒

\- 报表分类统计

\- 已进入竣工阶段的项目不允许再修改分部分项范围

6\. 字段与表结构

\- 新增字段：biz_project 表增加 subsection_types VARCHAR(500)

\- 存储格式：以逗号分隔字符串存储，如：1,2,3,6

\- 不影响原有字段与逻辑

7\. 前端交互

\- 下拉支持搜索关键词快速定位

\- 已选项标签化展示

\- 未选择时提交弹出提示："请至少选择一项分部分项工程类型"

\- 编辑时自动回显已勾选内容

请按照原需求文档正式格式，将以上内容补充到【3.6.3
实体项目立项】和【3.6.4
虚拟项目立项】中，保持结构、术语、排版完全统一，可直接合并进 V2.0
完整版。

新增功能四：

请基于 MOCHU-OA 施工管理系统 V2.0 需求规格说明书，在【3.11 施工管理模块
→ 3.11.2 任务甘特图】中补充\*\*甘特图任务节点隐蔽事项设置 +
联动竣工资料自动校验\*\*功能，要求格式、术语、结构、排版与原文档完全一致，可直接插入正文。

需求内容：

1\. 功能名称

甘特图任务节点隐蔽事项设置与竣工资料联动

2\. 功能概述

在甘特图新增/编辑任务节点时，增加\*\*【是否包含隐蔽事项】单选菜单\*\*，用于标记该施工节点是否涉及隐蔽工程；

系统根据勾选结果，自动联动竣工管理模块，强制要求上传\*\*隐蔽工程验收资料\*\*与\*\*带水印隐蔽工程现场照片\*\*，并在节点进度≥60%时进行资料完整性校验，未上传则自动生成待办提醒项目经理。

3\. 界面与控件

\- 控件位置：甘特图任务节点编辑弹窗中

\- 控件类型：单选框组

□ 否（默认）

□ 是（包含隐蔽工程）

\- 约束条件：单选必选，不能为空

4\. 业务联动规则（核心）

4.1 勾选【是：包含隐蔽事项】时

\- 该任务节点标记为"隐蔽工程节点"

\- 竣工资料模块自动生成\*\*必传资料项\*\*：

1\. 隐蔽工程验收记录

2\. 隐蔽工程现场照片（带时间、项目编号水印）

3\. 隐蔽工程验收签证单

4\. 材料/工序检验报告

\-
系统自动将上述资料设为\*\*强制必传\*\*，未完整上传不允许节点标记为100%完成

4.2 勾选【否：无隐蔽事项】时

\- 不触发隐蔽资料强制校验

\- 仅按普通节点要求上传常规施工照片即可

4.3 自动校验与待办规则

\- 触发时机：每日00:10 XXL-JOB定时扫描

\- 触发条件：

1\. 任务节点标记为【含隐蔽事项】

2\. 节点进度百分比 ≥ 60%

3\. 未上传任何隐蔽工程验收资料或照片

\- 执行动作：系统自动生成待办推送给项目经理

\- 待办标题：【隐蔽资料缺失】任务 {taskName}
含隐蔽事项且进度≥60%，请上传验收资料

\- 关闭条件：完整上传必传隐蔽资料后自动关闭

5\. 数据与表结构

\- 新增字段：biz_gantt_task 表增加 hidden_work TINYINT(1)

1 = 包含隐蔽事项，0 = 无（默认）

\- 存储方式：任务节点保存时写入，编辑可修改

\- 联动字段：与竣工资料 biz_attachment、验收记录 biz_acceptance 关联

6\. 前端交互

\- 甘特图上对"含隐蔽事项"的节点用\*\*特殊图标/底色\*\*标识

\- 节点 hover 时提示：本节点含隐蔽工程，需上传隐蔽验收资料

\- 节点提交100%完成时，校验资料是否齐全，不齐全则拦截并提示

7\. 与现有模块联动

\- 施工管理模块（甘特图）

\- 竣工管理模块（资料上传、验收记录）

\- 待办推送模块（自动待办、企业微信通知）

\- 定时任务模块（XXL-JOB）

请按照原需求文档正式标准格式，将以上内容补充写入【3.11.2
任务甘特图】章节，保持术语统一、结构一致、可直接合并进 V2.0 完整版。

新增功能五：

请基于 MOCHU-OA 施工管理系统 V2.0 需求规格说明书，在【3.14
竣工管理模块】中补充 \*\*竣工验收自动生成验收文档目录\*\* 功能，并实现与
\*\*立项分部分项、甘特图隐蔽工程、检验批、验收记录\*\*
全联动，要求格式、结构、术语、排版与原文档完全一致，可直接插入正文。

需求内容：

1\. 功能名称

竣工验收---按立项分部分项自动生成验收文档目录

2\. 功能概述

在项目进入 \*\*竣工验收阶段\*\* 时，系统根据
\*\*项目立项时勾选的分部分项工程类型\*\*，自动生成该项目专属的
\*\*完整竣工验收文档目录\*\*，形成标准化资料清单，供资料员按目录逐一上传；

同时将 \*\*隐蔽工程、隐蔽工程验收记录、检验批质量验收记录\*\* 与
\*\*甘特图任务节点进度\*\*
强关联校验，未满足条件的资料项标红预警、禁止归档，确保竣工资料完整、合规、可追溯。

3\. 生成逻辑

3.1 依据来源

读取项目表 biz_project 中的 subsection_types
字段（立项时勾选的分部分项工程）。

3.2 自动生成规则

\- 地基与基础 → 生成地基与基础分部全套验收目录

\- 主体结构 → 生成主体结构分部全套验收目录

\- 建筑装饰装修 → 生成装饰装修分部全套验收目录

\- 屋面及防水 → 生成防水分部目录

\- 建筑给水排水及供暖 → 生成给排水采暖目录

\- 建筑电气 → 生成建筑电气目录

\- 通风与空调 → 生成通风空调目录

\- 智能建筑 → 生成智能建筑目录

\- 市政道路/管网/园林 → 生成对应市政类目录

系统自动合并、去重，形成一份 \*\*该项目唯一的竣工验收文档总目录\*\*。

4\. 验收文档目录组成（自动生成）

4\. 通用必备目录（所有项目必选）

\- 工程竣工验收申请

\- 竣工验收方案

\- 竣工验收报告

\- 单位工程质量竣工验收记录

\- 质量控制资料核查记录

\- 安全和功能检验资料核查记录

\- 观感质量验收记录

\- 竣工图

\- 工程竣工结算书

4.2 按分部分项自动生成的目录（动态生成）

\- 分部工程质量验收记录

\- 分项工程质量验收记录

\- 检验批质量验收记录

\- 隐蔽工程验收记录

\- 施工试验及检测报告

\- 材料/设备合格证及进场验收记录

\- 施工记录、施工日志

\- 现场照片、影像资料

\- 分部分项工程验收汇总表

5\. 关键联动规则（核心）

5.1 检验批质量验收记录 ↔ 甘特图任务节点

\- 每个甘特图任务节点对应至少 1 份检验批

\- 节点进度 ≥ 100% 时，必须上传对应检验批，否则标红预警

\- 未上传检验批的节点，不允许进入竣工验收通过状态

5.2 隐蔽工程 ↔ 甘特图隐蔽标记

\- 甘特图标记为"含隐蔽事项"的节点

\- 必须上传：隐蔽工程验收记录 + 带水印隐蔽工程照片

\- 节点进度 ≥ 60% 未上传 → 系统标红并自动生成待办给项目经理

\- 节点进度 ≥ 100% 仍未上传 → 拦截竣工验收流程，禁止归档

5.3 资料完整性校验

\- 系统自动校验目录完成率

\- 未上传/不合格的资料项标红显示

\- 竣工验收提交时，必须满足：

1\. 所有检验批齐全

2\. 所有隐蔽节点资料齐全

3\. 所有分部分项验收记录齐全

4\. 竣工图完整

6\. 资料员操作流程

1\. 进入竣工验收模块 → 自动生成文档目录

2\. 按目录逐项上传资料

3\. 系统自动校验、自动匹配甘特图节点

4\. 预警项处理完成后，方可提交竣工验收

7\. 字段与表结构

\- 复用：biz_project.subsection_types、biz_gantt_task.hidden_work

\- 新增：biz_completion_document_catalog（竣工文档目录表）

\- 新增：biz_completion_document_item（文档目录项）

\- 复用：biz_attachment 存储上传资料

8\. UI/UE

\- 目录按"分部---分项---检验批"树形展示

\- 已上传：绿色√

\- 未上传：灰色○

\- 缺失/预警：红色⚠

\- 点击可直接上传，自动关联甘特图节点

请按照原需求文档正式格式，将以上内容补充写入【3.14
竣工管理模块】，保持结构、术语、排版完全统一，可直接合并进 V2.0 完整版。

新增功能六：

请基于 MOCHU-OA 施工管理系统 V2.0 需求规格说明书，在【3.6
项目管理模块】或【3.14 竣工管理模块】中补充
\*\*招标DWG图纸导入、版本管理、自动对比识图\*\*
功能，要求格式、术语、结构、排版与原文档完全一致，可直接插入正文。

需求内容：

1\. 功能名称

招标DWG图纸导入与版本对比自动识图功能

2\. 功能概述

支持项目经理/资料员在项目立项或图纸管理环节直接上传、保存\*\*招标原版DWG格式图纸\*\*；

后续上传\*\*修订版、变更版、竣工版DWG图纸\*\*时，系统自动进行\*\*图纸版本对比、自动读图解析\*\*，智能识别并提示前后版本之间的\*\*主要修改内容\*\*（如图纸尺寸、构件位置、材料标注、图层变更、文字说明、轴线、标高、户型/结构修改等），形成图纸变更对比报告，用于变更签证、成本核算、竣工资料归档。

3\. 核心功能

3.1 DWG图纸上传与存储

\- 支持上传格式：.dwg（AutoCAD 2010\~2024 版本兼容）

\- 上传位置：项目详情 → 图纸管理 / 招标资料模块

\- 每版图纸自动标记版本号：V1.0（原版）、V1.1、V1.2、V2.0......

\- 图纸存储在MinIO，保留原始文件与解析后的轻量化文件

3.2 自动识图与解析内容

系统可自动解析DWG图纸并提取以下信息：

\- 图层信息（建筑、结构、水电、暖通等）

\- 轴线、标高、尺寸、坐标

\- 构件、墙体、门窗、设备位置

\- 图纸文字说明、图例、注释

\- 材料标注、做法说明

\- 图纸图框、图号、版本号

3.3 版本对比与自动识别修改内容

上传新版图纸时，系统自动与\*\*上一版本图纸\*\*对比，并标记差异：

\- 新增/删除的构件、墙体、管线

\- 尺寸、标高、位置变更

\- 文字说明、图例修改

\- 图层开关、颜色变更

\- 材料、做法标注变更

\- 轴线、边界范围调整

3.4 差异展示方式

\- 差异区域高亮显示（红色=删除，绿色=新增，黄色=修改）

\- 自动生成\*\*图纸修改说明清单\*\*

\- 支持一键查看差异、一键导出对比报告

\- 支持在线预览CAD图纸（无需安装AutoCAD）

4\. 业务联动规则

4.1 与变更管理联动

\- 图纸自动识别的修改内容，可\*\*一键生成变更签证单/设计变更单\*\*

\- 自动带入变更位置、修改内容、影响范围

\- 用于预算员核算成本、项目经理确认签证

4.2 与竣工资料联动

\- 所有版本图纸自动进入竣工图纸目录

\- 版本对比报告作为\*\*设计变更资料\*\*归档

\- 支持按版本追溯：招标版 → 施工图版 → 变更版 → 竣工版

4.3 与甘特图/验收联动

\- 重大图纸变更自动提醒项目经理更新进度计划

\- 涉及隐蔽工程、结构变更的图纸，自动触发验收资料补全提醒

5\. 权限与操作

\- 上传权限：项目经理、资料员、采购员

\- 对比权限：所有项目相关人员

\- 覆盖/替换权限：仅项目经理、总经理

\- 查看权限：按项目数据权限控制

6\. 表结构与接口

\- 新增表：biz_project_drawing（项目图纸表）

\-
字段：id、project_id、version、file_path、original_name、draw_type、status、compare_info、creator、created_at

\- 新增接口：

\- 上传DWG图纸：/api/v1/drawing/upload

\- 获取图纸列表：/api/v1/drawing/list

\- 图纸版本对比：/api/v1/drawing/compare

\- 导出对比报告：/api/v1/drawing/export-report

7\. UI/UE

\- 图纸列表展示版本、上传时间、解析状态

\- 在线预览采用CAD轻量化控件

\- 对比模式：双图同步滚动 + 差异叠加显示

\- 差异清单以条目形式展示，可点击定位到图纸位置

请按照原需求文档正式标准格式，将以上内容补充到合适章节（项目管理/竣工管理/图纸管理均可），保持术语统一、结构一致、可直接合并进
V2.0 完整版。

新增功能八：

我给你一套\*\*可直接落地开发、贴合MOCHU-OA施工管理系统\*\*的\*\*DWG图纸导入+自动读图+版本对比提示修改\*\*的完整实现方案，不讲虚的，全是工程化可执行的步骤，技术选型用施工行业最成熟的方案，兼容你现有系统架构。

\# 一、功能实现总逻辑（5步闭环）

1\. \*\*前端上传\*\*：支持DWG格式图纸拖拽/选择上传，自动记录版本

2\. \*\*后端存储\*\*：原版+修改版图纸永久保存，关联项目

3\. \*\*自动解析\*\*：调用CAD解析引擎，提取图纸核心元素

4\. \*\*智能对比\*\*：新版vs原版图纸自动比对，识别修改内容

5\. \*\*前端提示\*\*：高亮显示修改位置+文字清单，推送系统提醒

\# 二、分步具体实现（开发直接照做）

\## 1. 第一步：DWG图纸导入与版本存储（基础）

\### 实现方式

\- \*\*前端\*\*：用支持\*\*CAD文件上传\*\*的控件（无插件，纯网页）

\- 支持格式：\`.dwg\`（兼容AutoCAD 2010\~2024全版本）

\- 上传规则：单文件≤50MB，同一图纸自动按「招标版→修改版→竣工版」升版

\- 版本命名：自动生成\`V1.0（招标原版）、V1.1、V1.2\...\`

\- \*\*后端\*\*：Spring
Boot接收文件，存入\*\*MinIO对象存储\*\*（你系统现有存储）

\- \*\*数据库记录\*\*（必加字段）：

表\`biz_project_drawing\`

\| 字段 \| 含义 \|

\|\-\--\|\-\--\|

\| project_id \| 关联项目ID \|

\| draw_name \| 图纸名称（如：建筑施工图-主楼） \|

\| version \| 版本号（V1.0/V1.1） \|

\| is_original \| 是否招标原版（1=是，0=否） \|

\| file_path \| MinIO存储路径 \|

\| parse_status \| 解析状态（0=未解析，1=已解析，2=解析失败） \|

\| compare_version \| 对比的基准版本（如V1.0） \|

\### 关键逻辑

\- 项目首次上传→自动标记为\*\*招标原版（V1.0）\*\*

\- 后续上传→自动识别为\*\*修改版\*\*，绑定上一版本为对比基准

\## 2. 第二步：自动读图（DWG解析）（核心能力）

\### 技术选型：用成熟CAD解析引擎（不重复造轮子）

\*\*施工行业标准方案\*\*：ODA Teigha SDK / MxDraw
SDK（支持Java/后端调用，纯服务端解析）

\### 自动解析内容（系统提取，无需人工操作）

1\. 基础信息：图号、图名、图纸类型（建筑/结构/水电）

2\. 几何元素：墙体、轴线、标高、尺寸、门窗、管线位置

3\. 图层信息：建筑图层、结构图层、暖通图层、标注图层

4\. 文字信息：材料说明、做法标注、设计说明

5\. 关键属性：构件尺寸、坐标、范围边界

\### 实现逻辑

\- 后端\*\*异步解析\*\*（上传后后台自动跑，不阻塞前端）

\- 解析结果存入数据库：\`biz_drawing_parse\`（存储图纸元素数据）

\- 解析失败→自动标记，提示管理员重新上传

\## 3. 第三步：自动对比识别修改内容（核心功能）

\### 对比触发时机

上传\*\*修改版图纸\*\*→保存后\*\*自动触发\*\*对比（无需人工点击）

\### 对比逻辑（精准识别主要修改，忽略无关细节）

系统按「元素类型+空间位置+属性」三维度对比原版vs修改版：

1\. \*\*构件变更\*\*：墙体/管线/门窗的新增、删除、位置移动

2\. \*\*尺寸变更\*\*：轴线尺寸、标高、构件大小修改

3\. \*\*图层变更\*\*：图层新增/删除/隐藏

4\. \*\*文字标注变更\*\*：材料、做法、设计说明修改

5\. \*\*边界范围变更\*\*：建筑轮廓、施工范围调整

\### 自动判定修改类型

\- 新增：原版无，新版有 → 标记「新增」

\- 删除：原版有，新版无 → 标记「删除」

\- 修改：位置/尺寸/文字变化 → 标记「内容修改」

\### 过滤无效差异

忽略图框、标题栏、无关标注等非业务修改，\*\*只提示施工关键变更\*\*

\## 4. 第四步：前端展示修改内容（直观提示）

\### 1）可视化提示（核心）

\- 双图同步预览：左侧原版，右侧修改版

\- 差异高亮标记：

\- 红色=删除内容

\- 绿色=新增内容

\- 黄色=修改内容

\- 点击差异项→自动定位到图纸对应位置

\### 2）文字清单提示（清晰明了）

自动生成《图纸修改对比清单》，条目化展示：

\`\`\`

1\. 主楼3层墙体位置修改（X轴偏移200mm）

2\. 新增地下室给排水管线2条

3\. 标高3.6m修改为3.9m

4\. 材料标注：混凝土C30改为C35

\`\`\`

\### 3）系统主动提醒

\- 弹出全局提示：「本图纸相比招标原版有4处主要修改」

\- 项目经理/资料员收到系统待办：「项目XX图纸已变更，请查看修改内容」

\## 5. 第五步：与现有系统联动（贴合你的OA需求）

1\. \*\*联动变更签证\*\*：修改内容一键生成「设计变更单」

2\. \*\*联动竣工验收\*\*：修改版图纸自动归入竣工资料目录

3\. \*\*联动甘特图\*\*：重大修改自动提醒项目经理调整进度计划

4\. \*\*联动隐蔽工程\*\*：涉及隐蔽部位修改→触发重新验收

\# 三、整体技术栈（完全兼容你现有V2.0系统）

\- 前端：Vue3 + CAD在线预览控件（无需安装AutoCAD）

\- 后端：Spring Boot 3.x

\- 存储：MinIO（图纸文件）+ MySQL（解析/对比数据）

\- 解析引擎：ODA Teigha（服务端DWG解析）

\- 定时任务：XXL-JOB（异步解析/对比）

\# 四、关键注意事项（避坑）

1\. \*\*大图纸处理\*\*：超过20MB的DWG，后台异步解析，不影响系统性能

2\. \*\*版本管理\*\*：禁止覆盖原版，所有修改版单独保存，可追溯

3\. \*\*兼容性\*\*：支持所有设计院导出的标准DWG格式

4\. \*\*权限控制\*\*：只有项目经理/资料员可上传修改版，其他人仅查看

\# 总结

这套方案\*\*完全落地\*\*：

1\. 招标DWG直接上传→自动存为原版

2\. 修改版上传→自动保存+自动解析+自动对比

3\. 系统\*\*可视化+文字清单\*\*双提示修改内容

4\. 无缝对接你现有竣工、变更、甘特图模块

开发团队按这个步骤做，就能实现你要的「图纸导入→自动读图→提示修改」全功能。

新增功能九：

请基于 MOCHU-OA 施工管理系统 V2.0 需求规格说明书，在【3.12
变更管理模块】中补充 \*\*设计变更与设计图纸变更双向联动\*\*
功能，要求格式、术语、结构、排版与原文档完全一致，可直接插入正文。

需求内容：

1\. 功能名称

设计变更与设计图纸变更自动联动与一致性校验

2\. 功能概述

当用户在系统中发起\*\*设计变更单（现场签证/甲方变更/设计变更）\*\*
并填写变更内容后，系统自动提醒用户\*\*上传对应版本的 DWG
设计变更图纸\*\*；

图纸上传后，系统自动对\*\*新版图纸与基准图纸\*\*进行对比解析，识别图纸实际变更内容，并与\*\*设计变更单中填写的变更内容\*\*进行智能比对，自动判断两者是否一致，最终生成\*\*变更一致性对比说明\*\*，供预算员、项目经理、总经理审批时参考，确保"变更单内容"与"实际图纸变更"一致，避免错漏、虚报、不一致。

3\. 触发流程

1\. 项目经理/采购员发起【设计变更】并保存变更内容；

2\. 系统立即弹出提醒：

"请上传对应版本的 DWG 设计变更图纸，以便系统自动校验变更一致性。"

3\. 用户上传新版 DWG 图纸；

4\. 系统自动执行：

\- 图纸版本对比（新版 vs 基准版）

\- 提取图纸实际变更内容

\- 与设计变更单填写内容比对

5\. 系统输出\*\*一致性结论\*\*与\*\*对比说明\*\*；

6\. 审批页面展示对比结果，不一致则标红预警。

4\. 自动校验逻辑（核心）

4.1 系统对比范围

\- 位置/范围变更

\- 尺寸/标高/轴线变更

\- 构件新增/删除/移动

\- 材料/做法/说明变更

\- 管线/设备布局变更

\- 结构/建筑轮廓变更

4.2 一致性判断规则

\- 一致：图纸变更内容 与 变更单描述 完全匹配

\- 部分一致：图纸有变更，但变更单描述不全/多写

\- 不一致：图纸变更与变更单描述不符

\- 无变更：图纸未发生实际变更

4.3 系统自动输出对比说明

示例：

1\. 变更单描述：主楼 3 层墙体位置右移 200mm

图纸识别结果：墙体右移 200mm → 一致

2\. 变更单描述：新增给排水管 2 条

图纸识别结果：未发现新增管线 → 不一致

3\. 变更单描述：未提及标高修改

图纸识别结果：3.6m → 3.9m → 变更单漏填

5\. 业务约束

\- 未上传变更图纸，不允许提交设计变更审批；

\- 系统判定"不一致/部分一致"时，审批页面标红预警；

\- 总经理可强制通过，但必须填写强制通过原因；

\- 所有对比记录自动归档至竣工资料。

6\. 与现有模块联动

\- 变更管理模块（设计变更单）

\- 图纸管理模块（DWG 导入、版本对比）

\- 审批流程模块

\- 竣工资料归档模块

7\. 表结构（不破坏原有结构）

\- 变更表 biz_change_order 增加：

\- drawing_status TINYINT 图纸是否上传

\- compare_status TINYINT 对比状态

\- compare_result TEXT 对比结果说明

\- 复用图纸表 biz_project_drawing

8\. UI 展示

\- 变更详情页增加【图纸变更对比】区域

\- 展示：一致性结果、差异清单、图纸预览

\- 红色 = 不一致，黄色 = 部分一致，绿色 = 一致

请按照原需求文档正式格式，将以上内容补充写入【3.12
变更管理模块】，保持术语统一、结构一致、可直接合并进 V2.0 完整版。

新增功能十：

请基于 MOCHU-OA 施工管理系统 V2.0 需求规格说明书，在【3.9
采购管理模块】中补充
\*\*材料采购自动提醒功能\*\*，要求格式、术语、结构、排版与原文档完全一致，可直接插入正文。

需求内容：

1\. 功能名称

基于采购清单、材料入库单、甘特图进度的材料采购自动提醒

2\. 功能概述

系统根据
\*\*项目采购清单（计划量）、材料入库单（已到货量）、甘特图任务节点进度（施工节奏）\*\*
三者实时数据进行智能计算，自动判断\*\*即将需要使用但尚未采购/尚未到货的材料\*\*，并自动生成采购提醒消息推送给采购员，提醒及时开展采购、下单、催货工作，避免因材料滞后影响施工进度。

3\. 自动提醒触发逻辑（核心三要素联动）

系统每日凌晨通过 XXL-JOB
定时任务执行计算，满足以下条件自动触发采购提醒：

1\. \*\*甘特图任务节点即将开始\*\*（当前进度 \< 10%，计划开始时间在未来
7 天内）

2\. \*\*该任务节点所需材料已在采购清单中定义\*\*

3\. \*\*已入库数量 \< 计划数量 × 预警系数（默认 80%）\*\*

→ 判定为：\*\*即将使用但材料不足，需要采购\*\*

→ 自动生成采购提醒推送给对应采购员

4\. 计算规则

材料缺口数量 = 采购清单计划数量 − 已入库数量

提醒触发条件：

\- 甘特图对应工序即将开工

\- 材料缺口数量 \> 0

\- 无正在审批中的采购合同 / 无待入库单据

5\. 提醒内容与接收人

\- 提醒类型：材料采购提醒

\- 接收人：项目对应采购员（PURCHASE）

\- 提醒标题：【采购提醒】项目 {projectName} 即将开工，材料
{materialName} 缺口 {num}，请及时采购

\- 提醒内容：

项目：{projectName}

工序：{taskName}（甘特图节点）

计划开工时间：{planStartDate}

材料名称：{materialName}

规格型号：{spec}

计划数量：{planQty}

已入库数量：{inQty}

缺口数量：{gapQty}

请尽快完成采购、下单、催料。

6\. 与现有模块联动

\- 采购清单（biz_purchase_list）：取计划量、材料信息

\- 材料入库单（biz_inbound_order）：取已入库量、已到货量

\- 甘特图任务（biz_gantt_task）：取工序计划时间、进度

\- 消息推送模块：自动发系统消息 + 企业微信提醒

\- 合同模块：判断是否已有未完成采购合同

7\. 业务规则

\- 每个材料、每个工序只提醒一次

\- 已生成采购合同/已入库则自动取消提醒

\- 暂停项目、中止项目不触发提醒

\- 提醒记录存入消息表，可查看、可标记已处理

\- 支持在系统配置中设置预警天数（默认7天）、预警系数（默认80%）

8\. 接口与定时任务

\- 新增 XXL-JOB 任务：材料采购缺口扫描

\- Cron：0 0 1 \* \* ?（每日凌晨1点执行）

\- 不新增表结构，复用现有业务数据

请按照原需求文档正式格式，将以上内容补充到【3.9
采购管理模块】，保持术语统一、结构一致，可直接合并进 V2.0 完整版。


---


## 第 3B 章 通用设计模式与基础能力


### 3B.1 编号生成器设计

**应用场景**：
- 项目编号（PRJ_20260411_001）
- 合同编号（CTR_20260411_001）
- 待办编号（TODO_20260411_001）
- 采购单号、入库单号等

**编号规则**：
```
{前缀}_{日期YYYYMMDD}_{序列号}
```

**编号生成策略**：
- 使用 Redis INCR 原子自增
- Key 格式：`biz_no:{前缀}:{日期}`
- 每天 00:00 自动重置计数器
- 支持并发调用

**实现要点**：
```java
public interface INoGenerator {
    String generate(String prefix);
}

public class RedisNoGenerator implements INoGenerator {
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    public String generate(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String key = "biz_no:" + prefix + ":" + date;
        long seq = redisTemplate.opsForValue().increment(key);
        
        // 设置 TTL：明天过期
        redisTemplate.expire(key, 2, TimeUnit.DAYS);
        
        return String.format("%s_%s_%06d", prefix, date, seq);
    }
}
```

### 3B.2 乐观锁与版本控制

**适用场景**：
- 高并发修改场景（库存、支付金额等）
- 防止脏写

**实现方式**：
- 在关键表添加 `version` 字段（INT 类型，默认 0）
- 更新时校验版本号不变，同时 version + 1

**应用表**：
- biz_inventory（库存）
- biz_project（项目）
- biz_contract（合同）
- biz_payment_apply（付款申请）
- biz_todo（待办）
- sys_announcement（公告）

**SQL 更新示例**：
```sql
UPDATE biz_project 
SET status = 'PROCESSING', version = version + 1 
WHERE id = ? AND version = ?;
```

**MyBatis-Plus 配置**：
```java
@Version
private Integer version;
```

### 3B.3 审批流引擎详细设计

#### 3.7.3.1 核心职责

为项目、合同、采购、财务、公告等审批场景提供统一能力：
- 发起审批
- 条件路由
- 多级审批
- 驳回/退回修改
- 加签/转办
- 阅办/阅知
- 超时提醒
- 自动转办
- 待办同步

#### 3.7.3.2 核心数据模型

**审批流定义表 biz_approval_def**：
```sql
CREATE TABLE biz_approval_def (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type VARCHAR(50) NOT NULL,  -- 业务类型
    biz_name VARCHAR(100) NOT NULL,  -- 业务名称
    flow_json LONGTEXT NOT NULL,    -- 流程定义 JSON
    status TINYINT DEFAULT 1,
    version INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_biz_type (biz_type)
);
```

**审批实例表 biz_approval_instance**：
```sql
CREATE TABLE biz_approval_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type VARCHAR(50) NOT NULL,
    biz_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    applicant_name VARCHAR(100),
    status ENUM('PENDING','APPROVING','APPROVED','REJECTED','WITHDRAWN'),
    current_node_order INT,
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_biz_type_id (biz_type, biz_id),
    INDEX idx_status (status)
);
```

**审批记录表 biz_approval_record**：
```sql
CREATE TABLE biz_approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    instance_id BIGINT NOT NULL,
    node_order INT NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    approver_id BIGINT NOT NULL,
    approver_name VARCHAR(100),
    action ENUM('APPROVE','REJECT','BACK_TO_EDIT','TRANSFER','ADD_SIGN'),
    opinion TEXT,
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    delegated_from_id BIGINT,  -- 转办来源人
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_instance_id (instance_id)
);
```

#### 3.7.3.3 流程定义 JSON 示例

```json
{
  "bizType": "ANNOUNCEMENT",
  "bizName": "公告发布审批",
  "nodes": [
    {
      "order": 1,
      "type": "APPROVE",
      "name": "部门主管审批",
      "role": "DEPT_MANAGER",
      "conditionalApprovers": [],
      "timeout": 86400,
      "autoTransferUsers": []
    },
    {
      "order": 2,
      "type": "APPROVE",
      "name": "HR审批",
      "role": "HR",
      "timeout": 86400,
      "autoTransferUsers": []
    },
    {
      "order": 3,
      "type": "APPROVE",
      "name": "GM审批",
      "role": "GM",
      "timeout": 86400,
      "autoTransferUsers": []
    }
  ]
}
```

#### 3.7.3.4 审批回调接口

```java
public interface ApprovalCallback {
    /**
     * 审批通过时回调
     */
    void onApproved(Long instanceId, String bizType, Long bizId);
    
    /**
     * 审批驳回时回调
     */
    void onRejected(Long instanceId, String bizType, Long bizId, String opinion);
    
    /**
     * 审批超时时回调
     */
    void onTimeout(Long instanceId, String bizType, Long bizId);
}
```

**实现类示例**：
```java
@Component
public class AnnouncementApprovalCallback implements ApprovalCallback {
    @Override
    public void onApproved(Long instanceId, String bizType, Long bizId) {
        // 公告审批通过，自动发布或生成发布待办
        announcementService.autoPublish(bizId);
    }
    
    @Override
    public void onRejected(Long instanceId, String bizType, Long bizId, String opinion) {
        // 公告审批驳回，回到草稿状态，保存驳回意见
        announcementService.rejectAndSavOpinion(bizId, opinion);
    }
}
```

### 3B.4 富文本编辑器统一设计

#### 3.7.4.1 编辑器选型

推荐使用 **TinyMCE 或 Quill**，建议采用 TinyMCE：
- 功能完整，支持图片/表格/链接等
- XSS 防护较好
- 插件生态完整

#### 3.7.4.2 统一组件封装

**Vue 3 组件**：
```vue
<template>
  <div class="rich-text-editor">
    <editor
      v-model="content"
      api-key="YOUR_API_KEY"
      :init="editorInit"
      @onChange="handleContentChange"
      @onImageUpload="handleImageUpload"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const content = ref('');

const editorInit = {
  height: 400,
  plugins: 'paste link image table code help',
  toolbar: 'undo redo | formatselect | bold italic | alignleft aligncenter alignright | bullist numlist | image link table',
  images_upload_handler: customImageUploadHandler,
  paste_filter_drop: true,
  paste_as_text: true,
  valid_elements: '+a[href|title|target],+b,+strong,+i,+em,+u,+p,+br,+ul,+li,+ol,+img[src|alt]',
};

const handleContentChange = (e: any) => {
  emit('contentChange', e.target.getContent());
};

const handleImageUpload = async (blobInfo: any) => {
  // 调用 /api/v1/attachment/upload
  // 返回图片 URL
};
</script>
```

#### 3.7.4.3 图片上传与管理

**图片上传流程**：
1. 用户在编辑器中粘贴或插入图片
2. 触发 `images_upload_handler` 回调
3. 调用 `/api/v1/attachment/upload` 上传到 MinIO
4. 返回 URL 给编辑器
5. 同时记录图片与业务实体的关联关系

**富文本关键表设计**（可选）：
```sql
CREATE TABLE biz_richtext_image_rel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type VARCHAR(50) NOT NULL,
    biz_id BIGINT NOT NULL,
    attachment_id BIGINT NOT NULL,
    image_type ENUM('cover','content','other'),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_biz_type_id (biz_type, biz_id)
);
```

---


## 第 4 章 非功能需求设计

### 4.1 性能需求

系统支持 500 并发用户同时在线。普通页面加载时间不超过 3 秒。列表查询接口响应不超过 1 秒（单页不超过 50 条）。报表查询不超过 10 秒。文件上传/下载吞吐不低于 10MB/s。单表数据量控制在 500 万行以内。

#### 4.1.1 性能优化具体技术方案

**前端性能优化：** Vite 构建 + Gzip 压缩 + CDN 加速 + 路由懒加载；搜索框输入防抖（300ms）；首页数据本地缓存（5 分钟）；大表格虚拟滚动。

**后端性能优化：** 通讯录、部门树等低变动接口结果缓存到 Redis；所有列表查询必须分页；报表聚合使用预计算中间表（每日 03:00 更新）；复杂 JOIN 控制在 3 表以内。

**SQL 优化规范：** 禁止 SELECT *；WHERE 条件字段必须有索引；禁止在 WHERE 中对字段使用函数；大批量 INSERT 每批 500 条。

**性能测试方案**

| 测试场景 | 并发数 | 持续时间 | 达标指标 |
|---------|--------|---------|---------|
| 登录接口 | 500 | 5 分钟 | 平均响应 ≤ 500ms，错误率 ≤ 0.1% |
| 项目列表查询 | 200 | 5 分钟 | 平均响应 ≤ 1s，P99 ≤ 3s |
| 合同签订（含审批） | 50 | 5 分钟 | 平均响应 ≤ 2s |
| 报表查询 | 50 | 5 分钟 | 平均响应 ≤ 10s |
| 文件上传（10MB） | 20 | 5 分钟 | 吞吐量 ≥ 10MB/s |
| 首页加载 | N/A | N/A | LCP ≤ 3s，FID ≤ 100ms |

### 4.2 安全需求

**数据加密：** 用户密码 BCrypt 加盐哈希（strength=10）；敏感字段（身份证号、银行账号）AES-256 加密；数据库连接 SSL 加密。

**接口防护：** 所有 API 强制 HTTPS；防重放攻击（nonce+timestamp+sign）；SQL 注入防护（MyBatis 参数绑定）；XSS 防护；CSRF 防护。

**登录安全：** 密码错误 5 次锁定 30 分钟；验证码 60 秒间隔；Token 30 天有效期；异常 IP 安全提醒。

#### 4.2.1 敏感数据分级保护规则

| 安全等级 | 数据类型 | 存储方式 | 访问控制 | 传输方式 |
|---------|---------|---------|---------|---------|
| 一级（最高） | 身份证号、银行账号、密码 | AES-256 加密 | 仅本人和 GM/HR 可查看原文 | HTTPS + 字段级加密 |
| 二级 | 合同金额、工资数据、成本数据 | 明文存储 | 按数据权限控制 | HTTPS |
| 三级 | 手机号、邮箱、地址 | 明文存储 | 列表默认脱敏，详情页按权限展示 | HTTPS |
| 四级（普通） | 项目名称、物资信息等 | 明文存储 | 按功能权限控制 | HTTPS |

### 4.3 可用性需求

SLA ≥ 99.9%。Nginx 负载均衡 + 双应用节点。MySQL 主从架构（一主一从），配合 MHA 自动切换，RPO ≤ 1 秒。Redis 哨兵模式（一主两从三哨兵）。MinIO 初期单节点 + 定期备份。数据库每日 03:00 全量备份，binlog 实时增量备份，保留 30 天。计划内维护提前 24 小时通知，窗口安排在凌晨 02:00-06:00。错误提示使用友好中文描述。

### 4.4 可扩展性需求

模块化设计，Service 接口解耦。审计日志按月分表，其他表超 500 万行按项目 ID 或时间分表（ShardingSphere）。API 版本管理（/api/v1/...），旧版本保留至少 6 个月。第三方集成通过抽象接口实现。

### 4.5 移动端适配需求

核心功能（审批、待办、通讯录、报表查看、进度查看）支持移动端 H5 或微信小程序。适配 320px~428px 宽度。移动端特色：拍照上传、手写签名、企业微信消息推送。移动端与 PC 端共用同一套后端 API。

#### 4.5.1 移动端功能裁剪规则

| 功能模块 | PC 端 | 移动端 | 说明 |
|---------|------|--------|------|
| 待办审批 | 完整 | 完整 | 核心功能 |
| 通讯录 | 完整 | 完整 | 支持搜索和查看 |
| 项目列表 | 完整 | 简化 | 仅展示名称、状态、进度 |
| 合同签订 | 完整 | 不支持 | 复杂表单建议 PC 端 |
| 甘特图 | 完整 | 简化 | 仅列表视图 |
| 进度填报 | 完整 | 简化 | 仅单节点快捷填报 |
| 报表查看 | 完整 | 简化 | 关键指标和简易图表 |
| 系统管理 | 完整 | 不支持 | 仅 PC 端操作 |

**移动端兼容性：** iOS 12.0+，Android 7.0+；微信内置浏览器；微信小程序基础库 2.20.0+；Chrome Mobile 80+，Safari Mobile 12+。

---


### 补充：非功能实现设计（V2.1 增强）


### 9.1 性能设计

#### 9.1.1 首页聚合优化

- **并发查询**：使用 CompletableFuture 并行查询快捷入口、待办、公告、最近访问
- **缓存策略**：待办数和公告列表缓存 5 分钟
- **分页加载**：待办默认仅加载 10 条，支持"查看全部"异步加载

#### 9.1.2 待办列表优化

- **分页查询**：默认每页 20 条，最大 100 条
- **索引覆盖**：`idx_handler_category_time(handler_id, todo_category, action_time DESC)`
- **异步详情**：详情在抽屉打开时加载，不在列表展示

#### 9.1.3 数据库查询优化

- **慢 SQL 监控**：MySQL 配置 slow_query_log，阈值 1 秒
- **常见 N+1 示例**：
  - 避免：遍历项目列表时逐条查询金额合计
  - 优化：使用 SQL 窗口函数或 GROUP BY 一次查询

#### 9.1.4 大附件处理

- **分片上传**：超过 10MB 的文件采用 5MB 分片上传
- **秒传检查**：上传前检查 MD5 是否已存在于 MinIO
- **异步合并**：分片上传完成后异步合并

### 9.2 可用性设计

#### 9.2.1 首页容错

- **子模块独立失败隔离**：待办、公告轮播任一失败不影响整体首页
- **组件错误隔离**：前端使用 ErrorBoundary 捕获子组件错误
- **优雅降级**：待办加载失败显示"暂无待处理事项"，公告轮播失败显示默认兜底图

#### 9.2.2 公告图片兜底

- **图片加载失败**：显示预设的兜底图
- **轮播组件加载失败**：整个轮播组件不渲染，不影响页面其他部分

#### 9.2.3 富文本异常处理

- **内容解析失败**：返回纯文本备选显示
- **XSS 过滤触发**：记录审计日志，但不影响用户编辑

### 9.3 安全性设计

#### 9.3.1 富文本安全

```java
public String sanitizeHtml(String html) {
    // 白名单过滤器配置
    Safelist safelist = Safelist.basicWithImages()
        .addTags("table", "tr", "td", "thead", "tbody", "thad")
        .addAttributes("table", "border", "cellpadding", "cellspacing");
    
    // 使用 Jsoup 清理 HTML
    Document clean = Jsoup.clean(html, safelist);
    return clean;
}
```

#### 9.3.2 图片上传安全

- **类型限制**：仅允许 jpg、jpeg、png、gif、webp
- **大小限制**：单文件最大 50MB，分片最大 5MB
- **病毒扫描**：选配第三方病毒扫描服务（可选）
- **上传隔离**：图片存储在独立的 MinIO bucket，不与重要数据混合

#### 9.3.3 公告审批前防护

- **审批前不可访问**：公告在审批结束前不可被非审批人看到
- **待办中的公告内容**：在待办详情中可预览公告内容供审批人查看

---


## 第 5 章 公共规范设计

### 5.1 审批流程通用规范

**审批对话框标准格式：** 点击"提交审批"时弹出确认对话框，显示审批流程节点列表。审批操作区域包含同意按钮（展开审批意见输入框，不少于 2 字符）、不同意按钮（展开原因输入框，必填，不少于 5 字符）。

**审批意见要求：** 无论同意或不同意均必须填写审批意见。审批意见与结果、时间、审批人一起记录在审批历史表中，不可修改。

**驳回规则（全局默认）：** 所有审批流程驳回至发起人。驳回后发起人可查看所有审批意见，修改表单后重新提交（从第一个节点重新开始）。驳回不清除已填写的表单数据。

**加签/转办：** 加签——审批人可临时增加额外审批人。转办——审批人可将任务转交其他用户处理。

**待办提醒：** 新待办产生时即时在系统待办列表中显示，同时通过企业微信推送。

**阅办与阅知定义：** 阅办——需点击"已阅"确认，可选填意见，不阻塞主流程。阅知——仅收到通知，无需操作，不阻塞主流程。

**审批流程引擎：** 流程定义采用 JSON 格式配置，支持条件分支。流程实例数据存储在 biz_approval_instance 表和 biz_approval_record 表。

#### 5.1.1 审批超时处理规则

**超时提醒：** 24 小时未处理发送第一次提醒。48 小时未处理发送第二次提醒并抄送直接上级。

**自动转办：** 72 小时未处理自动转办至直接上级（sys_dept.leader_id）。转办操作记录在审批历史中。

**超时配置：** 超时时间可按业务类型配置（sys_config，key 格式 approval.timeout.{bizType}，默认 72 小时）。节假日不计入超时时间。

### 5.2 附件上传通用规范

**支持的文件类型：** 图片类 jpg、png、gif、webp；文档类 pdf、doc、docx、xls、xlsx；其他类 txt、zip、rar。系统校验文件扩展名和 MIME 类型双重验证。

**大小限制：** 单文件最大 50MB，批量上传每批最多 10 个文件。

**存储方式：** MinIO 对象存储，路径格式为 /业务类型/年月/原始文件名_时间戳.扩展名。文件去重策略为计算 MD5，相同 MD5 不重复存储。

**附件关联实体(biz_attachment)：** id、file_name、file_path、file_size、file_type、md5、biz_type、biz_id、uploader_id、upload_time。biz_type 枚举值含 visa、owner_change、overage、contract、project、bid_notice、inbound、outbound、completion、reimburse、template。索引：biz_type+biz_id 联合索引。

**访问鉴权：** 附件下载接口校验当前用户是否有权访问对应业务实体。MinIO 访问通过后端签发预签名 URL（有效期 1 小时）。

#### 5.2.1 附件预览功能

**图片预览：** 支持 jpg/png/gif/webp 在线预览，支持缩放、旋转、全屏。**PDF 预览：** 使用 PDF.js 组件。**Office 文档预览：** 通过后端转换为 PDF 或集成第三方预览服务（OnlyOffice 或 kkFileView）。预览权限与下载权限一致。

### 5.3 编号规则通用规范

所有业务编号遵循统一格式：字母前缀 + 日期部分 + 顺序号。

| 业务实体 | 前缀 | 日期格式 | 顺序号位数 | 完整示例 | 重置机制 |
|---------|------|---------|-----------|---------|---------|
| 虚拟项目 | V | YYMM | 3 位 | V2403001 | 每月重置 |
| 实体项目 | P | YYMMDD | 3 位 | P240315001 | 每日重置 |
| 收入合同 | IC | YYMMDD | 2 位 | IC24031501 | 每日重置 |
| 支出合同 | EC | YYMMDD | 2 位 | EC24031501 | 每日重置 |
| 补充协议 | BC | YYMMDD | 2 位 | BC24031501 | 每日重置 |
| 采购清单 | PL | YYMMDD | 2 位 | PL24031501 | 每日重置 |
| 零星采购 | LP | YYMMDD | 3 位 | LP240315001 | 每日重置 |
| 入库单 | RK | YYMMDD | 3 位 | RK240315001 | 每日重置 |
| 出库单 | CK | YYMMDD | 3 位 | CK240315001 | 每日重置 |
| 退库单 | TK | YYMMDD | 3 位 | TK240315001 | 每日重置 |
| 盘点单 | PD | YYMMDD | 3 位 | PD240315001 | 每日重置 |
| 对账单 | DZ | YYMM | 2 位 | DZ240301 | 每月重置 |
| 人工费付款 | PA | YYMMDD | 3 位 | PA240315001 | 每日重置 |
| 材料款付款 | MP | YYMMDD | 3 位 | MP240315001 | 每日重置 |
| 收款登记 | SK | YYMMDD | 3 位 | SK240315001 | 每日重置 |
| 现场签证 | VS | YYMMDD | 3 位 | VS240315001 | 每日重置 |
| 甲方变更 | CH | YYMMDD | 3 位 | CH240315001 | 每日重置 |
| 劳务结算 | LS | YYMMDD | 3 位 | LS240315001 | 每日重置 |
| 报销单 | BX | YYMMDD | 3 位 | BX240315001 | 每日重置 |
| 材料编码 | M | 无 | 6 位全局递增 | M000001 | 不重置 |

**唯一性保障：** 编号生成使用 Redis 原子自增（INCR），key 格式 biz_no:{前缀}:{日期部分}，TTL 设置为重置周期 +1 天。数据库字段加唯一索引兜底。Redis 故障时切换为数据库种子表兜底方案。

### 5.4 通知提醒通用规范

**提醒方式：** 系统内待办/消息列表 + 企业微信应用消息推送。

**提醒对象：** 审批待办推送给下一审批节点审批人；进度预警推送给项目团队；资质到期推送给 HR；工资审批推送给总经理；公告推送给范围内所有用户。

**通知模板：** 审批待办——"【待办提醒】您有一个{业务类型}需要审批，申请人：{申请人}，申请时间：{申请时间}"。进度预警——"【进度预警】项目【{项目名称}】里程碑【{节点名称}】计划完成日期为【{计划日期}】，已滞后【{X}】天"。资质到期——"【资质预警】{资质类型}【{证书名称}】将于{到期日期}到期，距到期还有{X}天"。入库到货——"【到货通知】项目【{项目名称}】的物资已到货，到货时间：{到货时间}，物流单号：{单号}"。

**企业微信推送实现：** 抽象 MessagePushService 接口。推送失败时记录日志不重试。sys_user 表的 wx_userid 字段用于匹配。

### 5.5 通知提醒优先级规则

**通知去重：** 同一业务单据在同一审批节点 5 分钟内不重复推送。

**渠道优先级：** 企业微信和系统内待办同时触发。用户可在个人设置中关闭企业微信推送，但不可关闭系统内待办。

**免打扰时段：** 企业微信推送在 22:00-08:00 之间不发送（延迟至 08:00 批量发送），系统内待办不受限制。

---


## 第 6 章 接口总览

### 6.1 接口设计规范

**统一前缀：** /api/v1/

**请求数据格式：** application/json（文件上传使用 multipart/form-data）

**统一响应格式：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710489600000
}
```

**返回码规范：** 200 成功、400 参数错误、401 未认证/Token 过期、403 无权限、404 资源不存在、423 账号锁定、429 请求过于频繁、500 服务器内部错误。

**异常响应格式：**

```json
{
  "code": 400,
  "message": "合同金额不能为空",
  "errors": [
    {"field": "amountWithTax", "message": "不能为空"}
  ],
  "timestamp": 1710489600000
}
```

**分页请求参数：** page（从 1 开始，默认 1）、size（默认 20，最大 100）。

**分页响应：** data 对象包含 records（数据列表）、total、page、size、pages。

### 6.2 全模块接口清单

**登录认证（/api/v1/auth/）：** POST check-account、POST send-sms、POST login-by-password、POST login-by-sms、POST logout、POST forgot-password、POST reset-password、POST retrieve-account。

**首页（/api/v1/home/）：** GET /、GET /todo-count、GET /todo-list。

**通讯录（/api/v1/contact/）：** GET /depts、GET /employees、GET /search。

**用户管理（/api/v1/admin/users/）：** GET /、POST /、PUT /{id}、PATCH /{id}/status、POST /{id}/reset-password。

**角色管理（/api/v1/admin/roles/）：** GET /、POST /、PUT /{id}、GET /{id}/permissions、PUT /{id}/permissions。

**权限管理（/api/v1/admin/permissions/）：** GET /。

**部门管理（/api/v1/admin/depts/）：** GET /tree、POST /、PUT /{id}、PATCH /{id}/status。

**项目管理（/api/v1/project/）：** GET /list、GET /{id}、POST /entity、POST /virtual、POST /{id}/convert、POST /{id}/terminate、POST /{id}/suspend、POST /{id}/resume。

**合同管理（/api/v1/contract/）：** GET /list、GET /{id}、POST /income、POST /expense、POST /expense/check-overquantity、POST /expense/check-price、POST /{id}/supplement。

**合同模板（/api/v1/contract/template/）：** GET /list、POST /、PUT /{id}、GET /check-window。

**供应商（/api/v1/supplier/）：** GET /list、POST /、PUT /{id}、PATCH /{id}/status。

**采购管理（/api/v1/purchase/）：** GET /list、POST /list、GET /spot、POST /spot。

**材料基准价（/api/v1/material/base-price/）：** GET /list、POST /change-apply。

**物资管理（/api/v1/material/）：** GET /inbound/list、GET /inbound/{id}、POST /inbound、GET /outbound/list、POST /outbound、GET /return/list、POST /return、GET /inventory。

**施工进度（/api/v1/progress/）：** GET /gantt/{projectId}、POST /gantt/{projectId}、POST /report、POST /report/batch、GET /report/{projectId}。

**变更管理（/api/v1/change/）：** GET /list、POST /visa、POST /owner-change、POST /overage、POST /labor-visa、GET /ledger、GET /ledger/export。

**对账单（/api/v1/statement/）：** GET /split/list、POST /split、GET /list、GET /{id}、POST /{id}/submit。

**付款（/api/v1/payment/）：** GET /receipt/list、POST /receipt、GET /apply/list、POST /apply/labor、POST /apply/material。

**竣工管理（/api/v1/completion/）：** POST /finish-apply、GET /drawings、POST /drawings、GET /docs、GET /labor-settlement/list、POST /labor-settlement。

**人事管理（/api/v1/hr/）：** GET /salary/list、PUT /salary/{id}、POST /reimburse、POST /asset-transfer、POST /entry、POST /resign、GET /contract/list、GET /certificate/company、GET /certificate/personal。

**通知公告（/api/v1/announcement/）：** GET /list、POST /、PUT /{id}、PATCH /{id}/offline。

**报表（/api/v1/report/）：** GET /project-cost、GET /income-expense、GET /purchase-progress、GET /inventory、GET /export、POST /subscribe、GET /subscribe/list、DELETE /subscribe/{id}。

**案例展示（/api/v1/case/）：** GET /list、GET /{id}、POST /、PUT /{id}、PATCH /{id}/status。

**审计日志（/api/v1/system/audit-log/）：** GET /list、GET /export。

**审批流程（/api/v1/approval/）：** GET /todo、GET /done、POST /{instanceId}/approve、POST /{instanceId}/delegate、POST /{instanceId}/add-sign、GET /{instanceId}/history。

**通用附件（/api/v1/attachment/）：** POST /upload、GET /{id}/download、DELETE /{id}、GET /list。

### 6.3 业务异常返回码明细

| 模块 | 错误码 | 错误描述 | 触发场景 |
|------|--------|---------|---------|
| 项目(10) | 10001 | 项目状态不允许此操作 | 暂停/中止/关闭状态执行新建操作 |
| 项目(10) | 10002 | 超出虚拟项目投入限额 | 虚拟项目下签订支出合同超限额 |
| 合同(20) | 20001 | 合同金额与税率计算不一致 | 含税金额 ≠ 不含税×(1+税率) |
| 合同(20) | 20002 | 物资超量需预算员审批 | 超量校验触发 |
| 合同(20) | 20003 | 付款批次比例超 100% | 比例之和超限 |
| 物资(30) | 30001 | 入库数量超合同约定 | 入库数量+已入库 > 合同数量 |
| 物资(30) | 30002 | 出库数量超库存可用量 | 出库数量 > 当前库存 |
| 物资(30) | 30003 | 库存不足 | 库存为 0 时出库 |
| 财务(40) | 40001 | 付款金额超合同可付余额 | 累计付款 > 合同含税金额 |
| 财务(40) | 40002 | 无可关联对账单 | 人工费付款无已审批对账单 |
| 编号(50) | 50001 | 编号生成失败 | Redis 和数据库兜底均失败 |
| 审批(60) | 60001 | 审批流程已结束 | 重复审批已完成流程 |
| 审批(60) | 60002 | 非当前审批人 | 非当前节点审批人尝试审批 |

### 6.4 接口限流熔断规则

**全局限流：** 基于 Redis 令牌桶算法，默认 QPS 上限 1000，单用户每秒 50。限流后返回 429。

**敏感接口限流：** 登录——同一 IP 每分钟最多 30 次。验证码——同一手机号每小时最多 10 次。文件上传——同一用户每分钟最多 20 次。导出——同一用户每小时最多 10 次。

**熔断规则：** 对第三方服务调用（企业微信、短信、邮箱）配置 Resilience4j 熔断器：失败率阈值 50%，统计窗口 10 次调用，熔断持续 30 秒。熔断期间直接返回降级结果。

---


## 第 7 章 项目开发保障要求

### 7.1 开发规范

**代码命名规范：** Java 类名大驼峰，方法名和变量名小驼峰，常量全大写下划线分隔。数据库表名前缀 + 下划线（sys_ 系统表，biz_ 业务表），字段名小写下划线。前端组件文件名大驼峰，CSS 类名短横线分隔。

**注释规范：** Java 类必须有类级别注释。Service 层公开方法必须有 JavaDoc 注释。复杂业务逻辑必须有行内注释。前端 Vue 组件 script 标签开头注释组件用途。

**版本控制规范：** Git Flow 分支模型。每次提交关联任务编号。代码合并到 develop 前必须通过 Code Review。

#### 7.1.1 代码规范细则

**后端分层规范：** Controller 层仅负责参数接收、校验和响应封装。Service 层负责业务逻辑编排，单个方法不超过 80 行。DAO 层（Mapper）仅负责数据库操作，复杂 SQL 使用 XML 映射文件。

**事务规范：** @Transactional 只加在 Service 层。多表写操作必须开启事务。只读操作使用 @Transactional(readOnly=true)。事务中禁止调用第三方服务，通过事件机制异步执行。

**前端组件规范：** 页面级组件在 views/ 目录，公共组件在 components/ 目录。使用 `<script setup>` 语法。Props 必须声明类型和默认值。全局状态使用 Pinia。

### 7.2 测试要求

**单元测试：** Service 层核心方法覆盖率 ≥ 60%，重点覆盖金额计算、编号生成、权限校验、审批路由。JUnit 5 + Mockito。

**接口测试：** 所有 API 接口编写自动化测试，覆盖正常和主要异常场景。Postman 或 Spring MockMvc。

**集成测试：** 核心业务流程（立项 → 签约 → 入库 → 对账 → 付款）编写端到端测试。

**UAT 测试：** 每个 Phase 交付前由业务代表验收。

**验收标准：** 功能验收——所有需求点实现并通过测试。性能验收——满足 §4.1 指标。安全验收——通过 OWASP Top 10 检查。文档验收——接口文档、部署文档、用户手册完整。

#### 7.2.1 Bug 管理流程

| Bug 等级 | 定义 | 响应时间 | 修复时间 |
|---------|------|---------|---------|
| 致命(P0) | 系统崩溃/数据丢失/安全漏洞 | 30 分钟内 | 4 小时内 |
| 严重(P1) | 核心功能不可用/数据计算错误 | 2 小时内 | 1 个工作日 |
| 一般(P2) | 非核心功能异常/界面显示错误 | 1 个工作日 | 3 个工作日 |
| 建议(P3) | 体验优化/文案修改 | 3 个工作日 | 下个迭代 |

Bug 状态流程：新建 → 已确认 → 修复中 → 已修复 → 已验证 → 已关闭。

### 7.3 交付物清单

**Phase 0：** 登录/组织/权限模块源代码、数据库初始化脚本、接口文档、单元测试报告、部署说明。

**Phase 1：** 项目/合同/模板/供应商/通讯录模块源代码、数据库变更脚本、接口文档更新、测试报告（含集成测试）、用户手册（项目和合同部分）。

**Phase 2A/2B：** 采购/物资/进度/变更/财务/人事模块源代码、数据库变更脚本、接口文档更新、核心业务流程端到端测试报告、用户操作手册（全模块）。

**Phase 3：** 竣工/公告/案例模块源代码、数据库变更脚本、接口文档更新、测试报告。

**Phase 4：** 报表/审计日志模块源代码、数据库变更脚本、完整接口文档、完整测试报告（含性能和安全扫描）、系统部署包（Docker 镜像）、运维手册、完整用户手册、数据库设计文档。

**最终交付物汇总：** 全部模块源代码、数据库设计文档及全量 DDL 脚本、OpenAPI 3.0 接口文档、系统部署包及部署手册、运维监控手册、用户操作手册、测试报告（单元+接口+集成+性能+安全）、需求追溯矩阵。

### 7.4 系统上线与运维规范

#### 7.4.1 部署架构

**生产环境最小部署：** Nginx 1 台 → 应用服务器 2 台（Spring Boot JAR，JDK 17）→ MySQL 1 主 1 从 → Redis 1 主 2 从 3 哨兵 → MinIO 1 台 → XXL-JOB-Admin 1 台。

**部署方式：** Docker Compose 编排。应用服务器容器化部署（Eclipse Temurin JDK 17 镜像）。Nginx 配置前端静态资源和 API 反向代理。

#### 7.4.2 灰度发布策略

发布流程：1）镜像构建推送；2）灰度阶段：1 台更新为新版本，Nginx 分配 10% 流量，观察 30 分钟；3）全量更新；4）异常时回滚。保留最近 3 个版本镜像。数据库变更通过 Flyway 管理。

#### 7.4.3 监控指标

| 监控维度 | 指标 | 告警阈值 | 告警方式 |
|---------|------|---------|---------|
| 应用层 | 接口响应时间 P99 | > 5 秒 | 企业微信 |
| 应用层 | 接口错误率 | > 1% | 企业微信 |
| 应用层 | JVM 堆内存使用率 | > 85% | 企业微信 |
| 数据库 | 慢查询数量/小时 | > 50 | 企业微信 |
| 数据库 | 主从同步延迟 | > 5 秒 | 企业微信 + 短信 |
| Redis | 内存使用率 | > 80% | 企业微信 |
| Redis | 命中率 | < 90% | 企业微信 |
| 服务器 | CPU 使用率 | > 80% 持续 5 分钟 | 企业微信 |
| 服务器 | 磁盘使用率 | > 85% | 企业微信 |

---


## 第 7A 章 风险与技术约束

### 10.1 主要风险分析

| 风险 | 影响 | 防控措施 |
|------|------|---------|
| 待办中心若未统一建模 | 后续不同模块易重复实现，维护成本高 | 所有待办必须统一入 biz_todo，禁止另起炉灶 |
| 公告审批与原逻辑切换 | 历史数据兼容问题，可能出现发布状态混乱 | 做数据迁移脚本，审查历史数据　|
| 富文本图片与附件绑定不规范 | 孤儿文件堆积，浪费存储空间 | 定时任务定期清理，富文本上传统一走 /api/v1/attachment/upload |
| 公司信息自动填充无主数据治理 | 出现重复公司、错误匹配 | 建立公司基础库维护规范，定期数据清理 |
| 首页聚合过多导致首屏性能变差 | 用户体验下降，可能出现白屏 | 缓存策略、并发查询、分页、组件容错 |
| 价格联动计算的浮点数精度问题 | 金额错误，影响财务准确性 | 后端必须做严格的三值一致性校验，允许误差 0.01 |
| 编号生成部署后并发冲突 | 编号重复、丢失 | 使用 Redis INCR，Redis 配置为哨兵模式保证可用性 |
| 审批流超时提醒失败 | 用户无法及时跟进 | 多渠道提醒（企业微信 + 系统待办 + 邮件） |

### 10.2 技术约束

#### 10.2.1 开发约束

- **待办统一模型**：所有待办必须统一入 biz_todo，禁止在其他表存储待办信息
- **公告审批引擎**：公告必须接入审批引擎，禁止绕过流程直发
- **富文本上传路径**：富文本中的所有图片必须走 `/api/v1/attachment/upload`，不允许直接指向本地路径或外部 URL
- **公司信息回填**：自动填充后的字段可手动修改，不强制是否反向更新
- **首页模块隔离**：首页的每个模块（待办、公告、快捷入口）必须独立容错

#### 10.2.2 部署约束

- **富文本编辑器**：前端依赖 TinyMCE/Quill，需确保 CDN/npm 包正常加载
- **MinIO 可用性**：确保 MinIO 部署为哨兵模式或集群，保证图片上传不中断
- **Redis 集群**：编号生成、缓存等功能依赖 Redis，须部署为哨兵模式（1 主 2 从 3 哨兵）
- **定时任务服务**：XXL-JOB admin 和 executor 可用性必须 ≥ 99%

---


## 第 8 章 技术架构与公共能力详细设计


### 4.1 后端技术选型

**核心框架**：
- Spring Boot 3.x（JDK 17+）
- Spring Security 6.x
- Spring Cloud（可选，微服务阶段）

**ORM 框架**：
- MyBatis-Plus 3.x
- Hibernate Validator（参数校验）

**缓存**：
- Redisson（Redis 客户端）
- Spring Cache 注解

**文件存储**：
- MinIO SDK

**定时任务**：
- XXL-JOB 2.4.0+

**API 文档**：
- Knife4j（OpenAPI 3.0 可视化）

### 4.2 前端技术选型

**核心框架**：
- Vue 3 + Vite
- TypeScript
- Element Plus 2.x（企业级 UI 组件库）

**状态管理**：
- pinia

**路由**：
- vue-router 4

**HTTP 客户端**：
- axios + 统一请求拦截

**代码工具库**：
- lodash-es
- day.js（时间处理）
- hex2canvas（前端截图）

**富文本编辑**：
- 推荐 TinyMCE 或 Quill

### 4.3 前端目录结构建议

```
src/
├─ api/                  # API 调用模块
│  ├─ modules/
│  ├─ request.ts        # axios 统一配置
│  └─ interceptor.ts    # 请求拦截器
├─ assets/              # 静态资源
├─ components/          # 通用组件
│  ├─ common/           # 公用组件（分页、表格、表单等）
│  └─ form/             # 表单相关组件
├─ directives/          # 自定义指令
├─ hooks/               # 可复用逻辑
│  └─ useTaxAmountLinkage.ts
├─ layout/              # 布局组件
├─ router/              # 路由配置
├─ store/               # 状态管理（pinia）
├─ styles/              # 全局样式
├─ utils/               # 工具函数
│  ├─ format.ts         # 格式化工具
│  ├─ validate.ts       # 校验工具
│  └─ permission.ts     # 权限工具
├─ views/               # 业务页面
│  ├─ home/
│  ├─ todo/
│  ├─ announcement/
│  ├─ project/
│  └─ common/
├─ App.vue
├─ main.ts
└─ permission.ts        # 路由权限控制
```

### 4.4 后端技术选型架构

```
Controller 层（XxxController）
    ↓ 参数校验、鉴权、响应封装
Application/Facade 层（XxxAppService）
    ↓ 业务编排、DTO 装配、多个 Domain Service 编排
Domain Service 层（XxxDomainService）
    ↓ 领域规则实现、事务边界、状态流转
Repository/Mapper 层
    ├─ Repository（XxxRepository）- 单表操作
    └─ Mapper（XxxMapper）- 复杂查询、统计
Infrastructure 层
    ├─ Redis 缓存服务
    ├─ 文件存储服务（MinIO）
    ├─ 编号生成器
    ├─ 邮件服务
    ├─ 短信服务
    └─ 企业微信集成
```

### 4.5 回首页聚合架构

**首页采用聚合查询模型**，由 HomeQueryService 统一汇总：
- 快捷入口
- 待办总数
- 最近待办列表
- 已办/已阅快捷入口
- 公告及轮播图
- 最近访问

**性能优化**：
- CompletableFuture 并行查询
- Redis 缓存热点数据
- 单接口统一返回，减少首页请求数

**GET /api/v1/home**：
```json
{
    "shortcuts": [],         // 常用入口
    "todoList": [],          // 最近 10 条待办
    "todoCount": 5,          // 待办总数
    "announcements": [],     // 最新 5 条公告
    "recentVisits": []       // 最近访问
}
```

### 4.6 统一返回体设计

**成功响应**：
```json
{
    "code": 200,
    "message": "success",
    "data": { /* 业务数据 */ },
    "timestamp": 1680000000000
}
```

**错误响应**：
```json
{
    "code": 400,
    "message": "参数校验失败",
    "errors": [
        {
            "field": "amount",
            "message": "金额不能为空"
        }
    ],
    "timestamp": 1680000000000
}
```

### 4.7 统一异常体系

**异常类层次**：
```
RuntimeException
├─ BizException (业务异常)
├─ ValidationException (参数校验异常)
├─ UnauthorizedException (认证异常)
├─ ForbiddenException (权限异常)
├─ ResourceNotFoundException (资源不存在)
├─ RateLimitException (限流异常)
├─ ThirdPartyServiceException (第三方服务异常)
├─ FileUploadException (文件上传异常)
├─ RichTextEditorException (富文本异常)
└─ NoGeneratorException (编号生成异常)
```

### 4.8 DTO/VO/Entity 命名规范

| 对象类型 | 后缀 | 用途 | 示例 |
|----------|------|------|------|
| 请求参数对象 | Req | Controller 接收请求参数 | ProjectCreateReq |
| 响应对象 | Resp / VO | 返回给前端的对象 | ProjectResp、ProjectVO |
| 持久化对象 | Entity / DO | 与数据库表一一对应 | ProjectEntity、ProjectDO |
| 转换器 | Convertor | MapStruct 数据转换 | ProjectConvertor |

### 4.9 核心公共 Hooks（前端）

**useTaxAmountLinkage**：
```typescript
export function useTaxAmountLinkage(options?: {
    onAmountWithTaxChange?: (value: number) => void;
    onAmountWithoutTaxChange?: (value: number) => void;
    onTaxRateChange?: (value: number) => void;
}) {
    const amountWithTax = ref<number>();
    const amountWithoutTax = ref<number>();
    const taxRate = ref<number>();
    const taxAmount = ref<number>();
    
    // 含税价变化时自动计算不含税价和税额
    const handleAmountWithTaxChange = (value: number) => {
        if (!taxRate.value) {
            message.warning('请先选择税率');
            return;
        }
        amountWithoutTax.value = Number((value / (1 + taxRate.value)).toFixed(2));
        taxAmount.value = Number((value - amountWithoutTax.value).toFixed(2));
        options?.onAmountWithTaxChange?.(value);
    };
    
    // 不含税价变化时自动计算含税价和税额
    const handleAmountWithoutTaxChange = (value: number) => {
        if (!taxRate.value) {
            message.warning('请先选择税率');
            return;
        }
        amountWithTax.value = Number((value * (1 + taxRate.value)).toFixed(2));
        taxAmount.value = Number((amountWithTax.value - value).toFixed(2));
        options?.onAmountWithoutTaxChange?.(value);
    };
    
    return {
        amountWithTax,
        amountWithoutTax,
        taxRate,
        taxAmount,
        handleAmountWithTaxChange,
        handleAmountWithoutTaxChange
    };
}
```

**useCompanyAutoFill**：
```typescript
export function useCompanyAutoFill() {
    const companies = ref<CompanyInfo[]>([]);
    const loading = ref(false);
    
    const searchCompanies = debounce(async (keyword: string) => {
        if (keyword.length < 2) return;
        loading.value = true;
        try {
            const res = await api.searchCompanies(keyword);
            companies.value = res.data;
        } catch (e) {
            message.error('公司搜索失败');
        } finally {
            loading.value = false;
        }
    }, 300);
    
    const selectCompany = (company: CompanyInfo) => {
        return {
            creditCode: company.creditCode,
            address: company.address,
            contactName: company.contactName,
            contactPhone: company.contactPhone,
            bankName: company.bankName,
            bankAccount: company.bankAccount,
            taxNo: company.taxNo,
            email: company.email
        };
    };
    
    return {
        companies,
        loading,
        searchCompanies,
        selectCompany
    };
}
```

---


## 第 9 章 缓存、审计与定时任务设计

### 7.1 完整缓存设计

#### 7.1.1 缓存分类

| 缓存类型 | 场景 | Key 格式 | 值类型 | TTL | 更新策略 |
|---------|------|---------|--------|-----|---------|
| **认证缓存** | 用户 Token | auth:token:{userId}:{clientType} | JWT 字符串 | 30 天 | 登录时写，登出时 DEL |
| | 用户权限 | user:permissions:{userId} | JSON 权限列表 | 与 Token 同步 | 角色变更时 DEL |
| | 登录失败计数 | auth:login_fail:{username} | Integer | 30 分钟 | INCR 自增，成功登录 DEL |
| **短信缓存** | 验证码 | sms:phone:{phone} | 6 位数字 | 5 分钟 | 发送时 SET，验证后 DEL |
| **编号缓存** | 业务编号 | biz_no:{前缀}:{日期} | Integer | 1 天 | INCR 自增，日期变更自动重置 |
| **首页缓存** | 待办数量 | home:todo_count:{userId} | Integer | 5 分钟 | 新待办产生时 DEL |
| | 公告列表 | home:announcements:{userId} | JSON 公告列表 | 5 分钟 | 发布/离线时 DEL |
| **通讯录缓存** | 部门员工 | contact:list:{deptId} | JSON 员工列表 | 30 分钟 | 人事变动时 DEL |
| **防重放缓存** | 防重放 nonce | api:nonce:{nonce} | 1 | 5 分钟 | SETNX 检查，TTL 过期自动清理 |
| **公司信息缓存** | 公司搜索建议 | company:search:{keyword} | JSON 列表 | 10 分钟 | 公司信息修改时 DEL |
| **审批缓存** | 审批流定义 | approval_def:{bizType} | JSON 流程定义 | 1 小时 | 流程配置修改时 DEL |

#### 7.1.2 缓存一致性策略

- **主动删除**：发起删除缓存操作时立即删除
- **被动过期**：依赖 TTL 自动过期
- **Pub/Sub 联动**：角色变更时通过 Redis Pub/Sub 通知所有节点清理权限缓存

#### 7.1.3 缓存穿透/击穿/雪崩防护

- **穿透**：使用布隆过滤器（Bloom Filter）或取 null 值并短期缓存
- **击穿**：关键缓存添加互斥锁（分布式锁）
- **雪崩**：使用随机 TTL，避免同时过期

### 7.2 审计日志与留痕设计

#### 7.2.1 审计记录范围

| 业务模块 | 记录操作 | 记录字段 |
|---------|---------|---------|
| 待办中心 | 待办创建、处理、已阅确认 | todo_id、action、operator、operate_time |
| 公告管理 | 编辑、图片上传/删除、提交审批、审批通过/驳回、发布、下线 | announcement_id、action、operator、operate_time |
| 富文本 | 编辑异常（如 XSS 过滤触发、内容丢失等） | biz_type、biz_id、error_msg、operate_time |
| 附件上传 | 上传成功/失败、文件大小异常 | file_name、file_size、status、operate_time |
| 公司信息 | 自动填充命中/失败、手工修改 | company_id、action、operator、operate_time |
| 价格计算 | 后端校验失败（如税率非法、金额超范围） | biz_type、biz_id、error_msg、operate_time |

#### 7.2.2 AOP 注解实现

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String module() default "";
    String operateType() default "";
    String description() default "";
}

@Aspect
@Component
public class AuditLogAspect {
    @Around("@annotation(auditLog)")
    public Object auditLog(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        String module = auditLog.module();
        String operateType = auditLog.operateType();
        
        long startTime = System.currentTimeMillis();
        try {
            Object result = point.proceed();
            
            // 记录成功日志
            logAuditInfo(module, operateType, "SUCCESS", 
                System.currentTimeMillis() - startTime);
            
            return result;
        } catch (Exception e) {
            // 记录失败日志
            logAuditInfo(module, operateType, "FAIL", 
                System.currentTimeMillis() - startTime, e.getMessage());
            throw e;
        }
    }
}
```

**使用示例**：
```java
@AuditLog(module = "ANNOUNCEMENT", operateType = "SUBMIT_APPROVAL", 
    description = "提交公告审批")
public void submitForApproval(Long announcementId) {
    // ...
}
```

### 7.3 定时任务详细设计

#### 7.3.1 原有任务补充

在 XXL-JOB 基础上补充新任务：

| 任务名称 | Cron 表达式 | 作用 | 超时 | 失败策略 |
|---------|------------|------|------|---------|
| 首页公告缓存刷新 | 0 */10 * * * ? | 刷新首页轮播/公告缓存 | 120 秒 | 重试 1 次 |
| 待办超时提醒 | 0 0 * * * ? | 扫描超时待办并推送企业微信提醒 | 300 秒 | 失败告警 |
| 公告审批超时提醒 | 0 30 * * * ? | 审批超时 30 分钟后提醒 | 120 秒 | 失败告警 |
| 富文本孤儿附件清理 | 0 0 5 * * ? | 清理未关联公告正文的图片（7 天前上传） | 600 秒 | 失败告警 |

#### 7.3.2 任务实现示例

```java
@Component
public class CacheRefreshJobHandler {
    @XxlJob("homeCarouselCacheRefresh")
    public void refreshCarouselCache() {
        try {
            // 查询最新已发布公告
            List<AnnouncementVO> announcements = announcementService
                .getLatestCarriage(5);
            
            // 更新 Redis 缓存
            redisTemplate.opsForValue().set(
                "home:announcements:carousel",
                JsonUtils.toJson(announcements),
                Duration.ofMinutes(5)
            );
            
            XxlJobHelper.handleSuccess("首页轮播缓存刷新成功");
        } catch (Exception e) {
            XxlJobHelper.handleFail("首页轮播缓存刷新失败: " + e.getMessage());
        }
    }
    
    @XxlJob("todoTimeoutRemind")
    public void remindTimeoutTodos() {
        // 扫描审批超过 24 小时未处理的待办
        List<BizTodo> timeoutTodos = todoService.getTimeoutTodos(24);
        for (BizTodo todo : timeoutTodos) {
            // 发送企业微信提醒
            wechatService.sendReminder(todo.getHandlerId(), todo.getTitle());
            
            // 记录审计日志
            auditLogService.log("TODO", "TIMEOUT_REMIND", todo.getId());
        }
        
        XxlJobHelper.handleSuccess("超时待办提醒完成");
    }
}
```

---


## 第 10 章 业务模块补充设计

### 8.1 价格联动在各模块的应用

#### 8.1.1 项目立项模块

**补充字段**：
- biz_project.invest_limit（投资预算）：支持税率联动输入
- biz_project.company_name：支持自动填充甲方信息

#### 8.1.2 合同管理模块

**支出合同签订页面**：
- 合同总价（含税金额、不含税金额、税率、税额）：采用统一联动计算组件
- 供应商选择：支持自动回填联系人、联系电话、开户地址、银行账号

#### 8.1.3 采购管理模块

**采购明细**：
- 单价（含税、不含税）：支持税率联动
- 材料基准价更新规则：
  - 若当前价 < 历史价，写入价格更新记录
  - 若当前价 > 历史价，仅预警不自动覆盖

#### 8.1.4 财务管理模块

**付款申请表单**：
- 金额字段统一支持税率联动
- 支付到供应商时自动回填银行账号

### 8.2 公司信息自动填充的应用

#### 8.2.1 合同管理

- **甲方选择**：支持名称模糊搜索 → 自动回填信用代码、地址、联系方式
- **乙方选择**：支持供应商号或名称搜索 → 自动回填银行账号、纳税人识别号

#### 8.2.2 供应商管理

- **供应商登记**：输入公司名称 → 自动建议公司基础库匹配
- **确认后自动回填**：地址、联系人、联系电话、邮箱等

#### 8.2.3 施工进度管理

- **人工费支付对象**：支持公司名称自动匹配回填对方账户信息

### 8.3 业务模块的权限补充

**待办与公告相关权限字段补充**：

| 权限编码 | 描述 | 对应操作 |
|---------|------|---------|
| todo:view | 查看待办 | 待办列表、详情 |
| todo:handle | 处理待办 | 审批通过/驳回 |
| todo:read | 确认已阅 | 已阅待确认 |
| announcement:create | 创建公告 | 新建草稿 |
| announcement:submit | 提交审批 | 草稿提交审批 |
| announcement:approve | 审批公告 | 审批通过/驳回 |
| announcement:publish | 发布公告 | 发布/下线 |
| announcement:offline | 下线公告 | 手动下线 |

---


## 第 11 章 测试与交付规范

### 11.1 新增重点测试场景

#### 11.1.1 待办中心测试

- [ ] 首页展示待办、已办、已阅切换正常、数据不混乱
- [ ] 支持名称关键词搜索、时间范围筛选、排序
- [ ] 点击待办标题打开详情，显示完整信息
- [ ] 详情中的附件支持预览
- [ ] 流程时间线完整、节点顺序正确
- [ ] 审批/阅办操作在待办详情有权限控制
- [ ] 待办数量缓存失效时自动更新

#### 11.1.2 公告修复测试

- [ ] 图片上传成功，在富文本中可见
- [ ] 富文本文本编辑正常、中文不乱码、可删除
- [ ] 首页轮播正常显示已发布公告及其图片
- [ ] 工作待办显示公告审批任务且不对首页造成影响

#### 11.1.3 公告审批流程测试

- [ ] 草稿新建后不可直接发布
- [ ] 提交审批后在待办中心生成审批任务
- [ ] 审批通过后可发布
- [ ] 驳回后回到草稿状态，保留驳回意见
- [ ] 公告日志记录完整（编辑、提交、审批、发布）

#### 11.1.4 价格联动计算测试

- [ ] 输入含税价 + 选择税率 → 自动计算不含税价和税额
- [ ] 输入不含税价 + 选择税率 → 自动计算含税价和税额
- [ ] 未选择税率时提示"请先选择税率"且禁止联动
- [ ] 联动结果保留两位小数
- [ ] 避免循环触发（最后编辑的字段锁定）
- [ ] 后端校验：三值计算一致，允许误差 0.01
- [ ] 税率只允许固定下拉值（0/1/3/6/9/13%）

#### 11.1.5 公司信息自动填充测试

- [ ] 输入公司名称 >= 2 字符触发搜索
- [ ] 下拉列表显示候选公司（模糊匹配）
- [ ] 选中后自动回填字段（信用代码、地址、联系方式等）
- [ ] 自动回填后的字段可手动修改
- [ ] 修改后再提交，允许用户选择是否反向更新公司基础库

### 11.2 交付物清单

**代码交付**：
- [ ] 完整源代码（前后端）
- [ ] 数据库 DDL 脚本（含新表 biz_todo、biz_announcement_log 等）
- [ ] 数据迁移脚本（如有）

**文档交付**：
- [ ] 《首页待办模块前端设计说明》
- [ ] 《公告审批流程配置清单》
- [ ] 《公司主数据字典说明》
- [ ] 《富文本与图片上传联调说明》
- [ ] 《审批流引擎使用手册》
- [ ] 《编号生成器配置指南》
- [ ] 《部署运维手册》

**测试交付**：
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试用例完整
- [ ] 性能压测报告
- [ ] UAT 测试报告

**部署交付**：
- [ ] Docker 镜像
- [ ] Kubernetes 部署清单
- [ ] 回滚方案

---


## 附录（原 V2.0 文档）


## 附录

### 附录 A：术语解释

| 术语 | 英文/缩写 | 定义 |
|------|----------|------|
| 虚拟项目 | Virtual Project | 用于前期投标跟踪阶段的项目记录，未签订正式合同，可转为实体项目或中止 |
| 实体项目 | Entity Project | 已签订正式合同的实际执行项目，具有完整的全生命周期管理 |
| 采购清单 | Purchase List | 实体项目的物资计划汇总表，作为支出合同签订的超量校验基准 |
| 超量采购 | Over-quantity Purchase | 支出合同中物资采购数量超过采购清单计划数量的情况，需预算员审批 |
| 材料基准价 | Material Base Price | 系统维护的物资参考价格，用于支出合同签订时的价格预警比对 |
| 收入合同拆分 | Income Split | 将收入合同总金额按甘特图任务节点拆分，关联进度计算产值 |
| 对账单 | Statement | 按月汇总项目产值和回款情况的财务单据 |
| 成本归集 | Cost Collection | 将付款金额按合同类型归集到项目成本台账对应科目的过程 |
| 阅办 | Read & Acknowledge | 审批流程中的非阻塞性操作，指定角色需确认已阅读但不影响审批流转 |
| 阅知 | Read Only | 审批流程中的通知性操作，指定角色仅收到通知无需任何确认 |
| RBAC | Role-Based Access Control | 基于角色的访问控制模型 |
| JWT | JSON Web Token | 用于身份认证的令牌格式 |
| SLA | Service Level Agreement | 服务等级协议 |
| RPO | Recovery Point Objective | 恢复点目标，数据丢失的最大可接受时间 |
| 付款申请 | Payment Apply | 项目经理发起的资金支付审批流程，关联合同和业务单据 |
| 物资明细 | Material Detail | 合同、入库单、出库单中的物资条目列表 |
| 项目编号 | Project Number | 系统自动生成的项目唯一标识号 |
| 审批意见 | Approval Opinion | 审批人在审批流程中填写的文字说明 |
| 进度百分比 | Progress Percentage | 甘特图任务级别的完成程度（0~100%） |
| 进度比例 | Progress Ratio | 产值级别的完成程度（累计产值/合同含税金额×100%） |
| 合同含税金额 | Contract Amount (Tax Included) | 合同约定的含增值税总金额 |

### 附录 B：数据字典补充索引设计

以下为核心业务表推荐的索引设计（除正文已定义的索引外）：

| 表名 | 索引名 | 索引字段 | 索引类型 | 设计目的 |
|------|--------|---------|---------|---------|
| biz_project | idx_status_type | status, project_type | 联合索引 | 项目列表按类型+状态筛选 |
| biz_project | idx_creator | creator_id | 普通索引 | 按创建人查询项目 |
| biz_contract | idx_project_category | project_id, contract_category | 联合索引 | 按项目+类型查询合同 |
| biz_contract | idx_status_date | status, sign_date | 联合索引 | 合同列表按状态+时间筛选 |
| biz_inventory | uk_project_material | project_id, material_id | 唯一索引 | 库存唯一性保证 |
| biz_gantt_task | idx_project_parent | project_id, parent_id | 联合索引 | 甘特图树形查询 |
| biz_payment_apply | idx_contract_status | contract_id, status | 联合索引 | 按合同查询付款申请 |
| biz_approval_record | idx_instance | instance_id, node_order | 联合索引 | 审批历史查询 |
| biz_attachment | idx_biz | biz_type, biz_id | 联合索引 | 按业务查询附件 |
| sys_audit_log_YYYYMM | idx_time_type | operate_time, operate_type | 联合索引 | 日志按时间+类型查询 |

### 附录 C：数据归档与清理策略

| 数据类型 | 在线保留周期 | 归档方式 | 归档后存储 | 清理规则 |
|---------|------------|---------|-----------|---------|
| 审计日志 | 12 个月 | 按月归档到归档表 | 归档表保留 3 年 | 3 年后可删除 |
| 短信验证码记录 | 30 天 | 直接删除 | 不归档 | 定时任务每日清理 |
| 审批流程历史 | 永久在线 | 不归档 | N/A | 不清理 |
| 已关闭项目数据 | 永久在线 | 不归档 | N/A | 不清理 |
| 临时上传文件 | 7 天 | 定时清理 MinIO 临时桶 | 不归档 | 未关联业务的附件 7 天后删除 |
| 异步导出文件 | 7 天 | 定时清理 MinIO 导出桶 | 不归档 | 7 天后删除 |
| Redis 缓存 | 按 TTL 自动过期 | 不归档 | N/A | 自动过期 |

### 附录 D：团队协作规范

#### D.1 职责分工

| 角色 | 职责范围 | 沟通对象 |
|------|---------|---------|
| 产品经理 | 需求定义、优先级排序、验收标准制定、UAT 组织 | 业务方、开发、测试 |
| 后端开发 | 接口开发、数据库设计、业务逻辑实现、单元测试 | 前端开发、测试 |
| 前端开发 | 页面开发、交互实现、接口对接、UI 适配 | 后端开发、UI 设计 |
| 测试工程师 | 测试用例设计、功能/接口/性能测试、Bug 跟踪 | 开发、产品 |
| 业务方 | 需求确认、UAT 验收、上线验证 | 产品经理 |

#### D.2 需求变更流程

需求变更遵循以下流程：1）变更发起方提交变更申请（描述变更内容、变更原因、期望时间）；2）产品经理评估变更影响（涉及模块、开发工作量、对现有进度的影响）；3）项目经理审批（工作量 ≤ 3 人天直接审批，> 3 人天需与业务方协商优先级和上线时间）；4）审批通过后更新需求文档和任务看板，通知相关开发/测试人员；5）变更完成后在版本发布说明中注明。严禁口头变更需求，所有变更必须有书面记录。

---


## 补充附录（V2.1/V3.0 新增）


## 附录 A：建议的核心类列表

### A.1 待办模块

| 类名 | 层级 | 职责 |
|------|------|------|
| TodoController | Controller | 待办列表/详情接口 |
| TodoAppService | Facade | 待办数据装配、关联附件/审批记录加载 |
| TodoQueryService | Service | 待办查询逻辑 |
| TodoDomainService | DomainService | 待办状态转换（创建→处理→已办）|
| TodoRepository | Repository | 待办表单表查询操作 |
| TodoConvertor | Utils | Todo 与 TodoResp/TodoVO 转换 |

### A.2 公告模块

| 类名 | 层级 | 职责 |
|------|------|------|
| AnnouncementController | Controller | 公告 CRUD、发布、审批接口 |
| AnnouncementAppService | Facade | 公告业务编排、图片处理 |
| AnnouncementDomainService | DomainService | 公告生命周期管理 |
| AnnouncementApprovalCallback | Callback | 公告审批回调 |
| AnnouncementLogService | Service | 公告审计日志 |
| RichTextEditorService | Service | 富文本编辑器统一服务 |

### A.3 公司信息模块

| 类名 | 层级 | 职责 |
|------|------|------|
| CompanyController | Controller | 公司查询、创建接口 |
| CompanyQueryService | Service | 公司搜索、自动填充 |
| CompanyAutoFillService | Service | 自动填充逻辑实现 |

### A.4 前端 Hooks

| Hook 名称 | 职责 |
|---------|------|
| useTodoList | 待办列表查询、分页、筛选 |
| useTodoDetail | 待办详情、附件、审批记录加载 |
| useTaxAmountLinkage | 税率联动计算 |
| useCompanyAutoFill | 公司信息搜索与自动回填 |

---

## 附录 B：建议的状态枚举

### B.1 待办状态

```java
public enum TodoStatus {
    TODO("待办"),
    DONE("已办"),
    READ("已阅");
}
```

### B.2 公告状态

```java
public enum AnnouncementStatus {
    DRAFT("草稿"),
    PENDING("待审批"),
    APPROVING("审批中"),
    APPROVED("审批通过"),
    REJECTED("审批驳回"),
    PUBLISHED("已发布"),
    OFFLINE("已下线");
}
```

### B.3 审批动作

```java
public enum ApprovalAction {
    APPROVE("通过"),
    REJECT("驳回"),
    BACK_TO_EDIT("退回修改"),
    TRANSFER("转办"),
    ADD_SIGN("加签");
}
```

---

## 附录 C：常见问题（FAQ）

### C.1 待办相关

**Q：为什么要建立统一的 biz_todo 表？**

A：避免各模块重复实现待办逻辑。统一模型便于权限控制、查询优化、后续维护。

**Q：待办数据与原有审批流程的关系？**

A：审批实例 + 记录表是流程数据；待办表是用户的工作视图，用于首页展示和快速操作。

### C.2 公告相关

**Q：公告已发布后能否编辑？**

A：已发布的公告不允许编辑，如需修改只能下线后重新编辑和审批。

**Q：公告审批驳回后的恢复流程？**

A：驳回后自动回到草稿状态，用户可编辑后重新提交审批。

### C.3 公司信息相关

**Q：自动填充失败如何处理？**

A：自动填充仅作为辅助，用户可手工填写。不阻挡业务流程，仅提示"未匹配到公司信息"。

**Q：公司基础库的维护责任？**

A：由数据管理员或 HR 定期维护，建议建立公司信息审核工作流。

### C.4 编号生成相关

**Q：编号生成为什么用 Redis INCR？**

A：INCR 是原子操作，天然支持并发，避免锁竞争。

**Q：Redis 宕机如何应急？**

A：建议 Redis 部署为哨兵模式保证可用性。Redis 宕机时系统无法生成编号，属于 P0 故障。

---

## 附录 D：版本发布清单

发布前必须完成：
- [ ] 所有 P0/P1 Bug 已修复
- [ ] 所有新增功能的单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过率 ≥ 95%
- [ ] UAT 测试完全通过
- [ ] 代码检视完毕，无严重问题
- [ ] 发布说明（Release Notes）已编写
- [ ] 数据库变更脚本已准备并验证
- [ ] 数据迁移脚本已准备（如有）
- [ ] 配置变更清单已准备
- [ ] 回滚方案已准备并演练
- [ ] 性能压测报告已评审
- [ ] 安全检查完毕（SQL 注入、XSS 等）

---

## 附录 E：联系方式与支持

**技术问题**：
- 邮件：tech-support@mochu.com
- 钉钉群：MOCHU-OA 技术支持

**需求反馈**：
- 邮件：product@mochu.com

**部署/运维支持**：
- 邮件：ops@mochu.com
- 电话：400-xxx-xxxx

---

## 文档最终确认

本文档为 MOCHU-OA 施工管理系统的**完整合并版综合开发需求文档（V3.0）**，已整合以下全部内容：

- ✅ V2.0 核心需求规格说明书（20个业务模块完整详细设计）
- ✅ 补充设计说明书（待办中心、公告审批、价格联动、公司信息填充）
- ✅ 软件详细设计说明书（系统架构、分层设计、编码规范）
- ✅ 补充开发需求（10项新增功能：广联达导入、水印校验、分部分项、隐蔽工程、DWG图纸、设计变更联动、材料采购提醒等）

*文档结束*

# CRM System

基于 Spring Boot 3、Spring Cloud Alibaba、MyBatis-Plus、Sa-Token、Vue 3、Element Plus 构建的 CRM 微服务系统。项目包含认证、客户、销售、营销、服务、线索、工作流、网关和前端应用，覆盖客户全生命周期管理、销售自动化、营销自动化、工单服务、线索评分和流程自动化。

## 项目结构

```text
crm-system/
├── crm-auth          # 认证、用户、角色、菜单、通知、操作日志
├── crm-common        # 通用实体、结果封装、异常处理、通知服务
├── crm-customer      # 客户联系人、客户洞察、互动记录
├── crm-sales         # 商机管理、销售漏斗、赢单率
├── crm-marketing     # 营销活动、活动匹配、效果报表
├── crm-service       # 服务工单、自动分配、SLA、知识库
├── crm-leads         # 线索管理、评分、转化商机
├── crm-workflow      # 工作流、节点设计、实例与轨迹
├── crm-gateway       # Spring Cloud Gateway 网关
├── crm-frontend      # Vue 3 前端
├── crm-starter       # 批量启动后端服务
└── sql               # 初始化与增量升级脚本
```

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 后端 | Java 17, Spring Boot 3.2.4, Spring Cloud 2023, Spring Cloud Alibaba |
| 网关 | Spring Cloud Gateway |
| 服务治理 | Nacos Discovery / Config, Sentinel Gateway |
| 权限认证 | Sa-Token |
| ORM | MyBatis-Plus, ShardingSphere-JDBC 可选方案 |
| 数据库 | MySQL 8 |
| 前端 | Vue 3, Vite, Pinia, Vue Router, Element Plus, ECharts |
| 通信 | OpenFeign, SSE |

## 功能模块

- 仪表盘：经营工作台、核心指标、销售漏斗、赢单率、工单趋势、关键待办。
- 客户管理：客户 CRUD、客户详情、客户画像、互动时间线、转化归因。
- 客户信息管理：客户全量信息模块化管理，覆盖客户主档、脂肪档案、身份信息、住址信息、关系图谱、联系记录、网站博客等。
- 产品管理：产品列表、产品新增、上下架状态、库存与价格维护。
- 销售自动化：商机 CRUD、销售阶段、漏斗统计、赢单率统计。
- 销售管理：销售订单台账、Excel 导入、订单详情、平台配置、平台订单/退款抽取。
- 支付管理：统一下单、支付订单、退款、渠道配置、通知状态、对账状态。
- 库存进销存：当前库存、上期库存、库存流水、采购销售库存报表、低库存识别。
- 营销自动化：活动 CRUD、活动匹配、营销效果报表、效果明细录入。
- 增值服务：客户成功、数据迁移、营销顾问等服务台账。
- 客户服务：工单 CRUD、自动分配、SLA 看板、知识库搜索与文章维护。
- 线索管理：线索 CRUD、自动评分、线索转商机。
- 跟进记录：销售/客服跟进时间线、下一步动作记录。
- 数据统计：销售趋势、回款趋势、业务构成与核心指标。
- 工作流自动化：工作流 CRUD、节点设计、触发执行、实例追踪、轨迹记录。
- 系统管理：用户、角色、菜单、部门、操作日志、通知管理、SSE 消息列表、外部渠道推送。

## 新增增强能力

### 客户洞察

- 页面：`/customer/:id`
- 能力：客户画像、互动时间线、转化归因、渠道贡献、画像评分、转化概率。
- 写入：支持新增互动记录。

相关接口：

```text
GET  /customer/insight/{id}
GET  /customer/interaction/list/{contactId}
POST /customer/interaction
```

相关表：

```text
crm_customer_interaction
```

### 客户信息管理

- 页面：`/fat-management`
- 命名：历史路由仍为 `/fat-management`，页面名称已改为“客户信息管理”。
- 展示：卡片式展示客户主档、脂肪档案、身份信息、住址信息、联系记录、网站博客；关系图谱使用 ECharts 力导向图。
- 新建：右上角“新建客户信息”支持一次创建客户主档、脂肪档案、身份、住址、关系、联系记录、网站博客。
- 单项维护：各模块支持单独新增、编辑、更新；关系图谱支持选中节点/连线后新增、编辑、删除、刷新。
- 说明：客户主档自动生成的默认关系需要先编辑保存为模块明细后才能删除。

相关接口：

```text
GET    /customer/fat-management/overview
GET    /customer/fat-management/page
POST   /customer/fat-management
PUT    /customer/fat-management/{id}
POST   /customer/fat-management/module/{moduleType}
PUT    /customer/fat-management/module/{moduleType}/{id}
DELETE /customer/fat-management/module/{moduleType}/{id}
POST   /customer/contact
```

相关表：

```text
crm_contact
crm_fat_management_record
crm_customer_info_item
```

### SLA 与知识库

- 页面：`/service/knowledge`
- 能力：SLA 响应等级、知识库搜索、处理方案、处理步骤。
- 写入：支持新增知识库文章。

相关接口：

```text
GET  /service/knowledge/overview
GET  /service/knowledge/articles
POST /service/knowledge/articles
```

相关表：

```text
crm_knowledge_article
```

### 营销效果报表

- 页面：`/marketing/report`
- 能力：触达、打开、点击、转化、ROI、渠道趋势、活动排行。
- 写入：支持录入营销效果明细，并用于报表聚合。

相关接口：

```text
GET  /marketing/campaign/report
POST /marketing/campaign/performance
```

相关表：

```text
crm_campaign_performance
```

### 工作流实例追踪

- 页面：`/workflow/instances`
- 能力：实例列表、当前节点、执行状态、执行结果、实例轨迹。
- 写入：触发工作流时自动写入轨迹。

相关接口：

```text
GET /workflow/workflow/instances
GET /workflow/workflow/instances/{id}
GET /workflow/workflow/instances/{id}/trace
```

相关表：

```text
crm_workflow_trace
```

### 产品、销售、跟进和统计补全

新增页面：

```text
/product            产品管理
/value-service      增值服务
/sales-management   销售管理
/followup           跟进记录
/statistics         数据统计
```

新增接口：

```text
GET  /sales/product/page
POST /sales/product
GET  /sales/order/page
POST /sales/order
GET  /sales/stats/report
GET  /service/value-added/page
GET  /service/value-added/summary
POST /service/value-added
GET  /customer/followup/page
POST /customer/followup
```

新增数据表：

```text
crm_product
crm_sales_order
crm_value_added_service
crm_followup_record
```

### 销售订单导入与平台数据抽取

- 页面：`/sales-management`
- 能力：销售订单 Excel 导入、订单详情抽屉、平台配置、同步日志、退款记录。
- 平台：淘宝、天猫、京东、抖音、亚马逊、拼多多。
- 抽取：配置平台后支持抽取订单、退款等数据；当前内置演示抽取逻辑，方便联调页面和台账。

相关接口：

```text
GET  /sales/order/page
GET  /sales/order/{id}
POST /sales/order
POST /sales/order/import

GET  /sales/platform/configs
POST /sales/platform/configs
PUT  /sales/platform/configs/{id}
POST /sales/platform/sync
GET  /sales/platform/refunds
GET  /sales/platform/logs
```

相关表：

```text
crm_sales_order
crm_sales_platform_config
crm_sales_platform_refund
crm_sales_platform_sync_log
```

### 通知与消息中心

- 实时协议：WebSocket 已替换为 SSE。
- 顶部铃铛：点击右上角铃铛可查看消息列表，支持未读数量、实时消息插入、刷新和跳转通知管理。
- 通知管理：支持站内 SSE、邮件、钉钉、飞书、短信、微信推送。
- 接收人：发送表单可填写本次接收人；不填写时使用渠道配置中的默认接收人。
- 渠道配置：支持启用状态、Webhook/API、密钥、SMTP、发件人、默认接收人、消息模板。
- 推送日志：每个渠道记录 `SUCCESS / SKIPPED / FAILED`、目标、响应或错误。

相关接口：

```text
GET  /sse/notification/subscribe
POST /system/notification/send
GET  /system/notification/page
GET  /system/notification/channels
PUT  /system/notification/channels/{id}
GET  /system/notification/{id}/push-logs
```

相关表：

```text
sys_notification
sys_notification_channel_config
sys_notification_push_log
```

## 环境要求

- Java 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8+
- Redis 7+
- Nacos 2.x

如果 macOS 上 Maven 提示 `JAVA_HOME` 不正确，可临时指定 JDK 17：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
```

## 数据库

默认本地数据库配置：

```text
MySQL: 127.0.0.1:3306
用户名: root
密码: 12345678
Redis: 127.0.0.1:6379
Nacos: 127.0.0.1:8848
Nacos namespace: dev
```

新环境初始化：

```bash
mysql -u root -p < sql/crm_init.sql
mysql -u root -p < sql/crm_upgrade_insights.sql
mysql -u root -p < sql/crm_upgrade_business_ops.sql
mysql -u root -p < sql/crm_upgrade_sales_platform_import.sql
mysql -u root -p < sql/crm_upgrade_customer_info_modules.sql
mysql -u root -p < sql/crm_upgrade_customer_info_items.sql
mysql -u root -p < sql/crm_upgrade_notification_channels.sql
mysql -u root -p < sql/crm_upgrade_sentinel_console.sql
```

已初始化过的环境增量升级：

```bash
mysql -u root -p < sql/crm_upgrade_insights.sql
mysql -u root -p < sql/crm_upgrade_business_ops.sql
mysql -u root -p < sql/crm_upgrade_sales_platform_import.sql
mysql -u root -p < sql/crm_upgrade_customer_info_modules.sql
mysql -u root -p < sql/crm_upgrade_customer_info_items.sql
mysql -u root -p < sql/crm_upgrade_notification_channels.sql
mysql -u root -p < sql/crm_upgrade_sentinel_console.sql
```

`crm_upgrade_insights.sql` 使用 `CREATE TABLE IF NOT EXISTS` 和 `INSERT IGNORE`，用于补充客户互动、知识库、营销效果、工作流轨迹相关表和示例数据。

`crm_upgrade_business_ops.sql` 用于补充客户信息管理、支付引擎、库存、上期库存和进销存相关表、菜单权限和示例数据。

`crm_upgrade_sales_platform_import.sql` 用于补充销售订单导入、平台配置、平台退款、同步日志相关表。

`crm_upgrade_customer_info_modules.sql` 用于将历史“脂肪管理”菜单升级为“客户信息管理”等菜单数据。

`crm_upgrade_customer_info_items.sql` 用于补充客户信息模块明细表，支持身份、住址、关系、联系、网站博客等模块独立新增、编辑、删除。

`crm_upgrade_notification_channels.sql` 用于补充通知渠道配置和推送日志，支持邮件、钉钉、飞书、短信、微信配置后推送。

`crm_upgrade_sentinel_console.sql` 用于补充系统管理下的 Sentinel 控制台菜单，前端通过 iframe 嵌套访问控制台。

## 公共配置

本地公共配置位于：

```text
crm-common/src/main/resources/application-common.yml
```

各微服务通过下面配置引入公共默认值：

```yaml
spring:
  config:
    import:
      - optional:classpath:application-common.yml
```

当前公共配置主要收敛 Sa-Token 默认项，避免每个服务重复维护：

```yaml
sa-token:
  token-name: Authorization
  timeout: 2592000
  active-timeout: -1
  is-concurrent: true
  is-share: true
  token-style: tik
  is-read-header: true
  is-read-body: false
  is-log: false
```

`crm-auth` 和 `crm-gateway` 只保留差异项，例如 `is-log` 和 `alone-redis`；其他业务服务不再单独声明 `sa-token`。

如果使用 Nacos 配置中心，建议新增公共 dataId：

```text
crm-common.yaml
```

内容与 `application-common.yml` 保持一致。`crm-starter` 启动时会按下面顺序导入配置：

```text
optional:classpath:application-common.yml
optional:nacos:crm-common.yaml
optional:nacos:<服务名>.yaml
```

## 业务运营扩展

新增页面：

```text
/fat-management    客户信息管理
/payment           支付管理
/inventory         库存进销存
```

新增接口：

```text
GET  /customer/fat-management/overview
GET  /customer/fat-management/page
POST /customer/fat-management
PUT  /customer/fat-management/{id}
POST /customer/fat-management/module/{moduleType}
PUT  /customer/fat-management/module/{moduleType}/{id}
DELETE /customer/fat-management/module/{moduleType}/{id}

GET  /sales/payment/overview
GET  /sales/payment/orders
GET  /sales/payment/refunds
GET  /sales/payment/channels
POST /sales/payment/unified-order
POST /sales/payment/refund

GET  /sales/inventory/overview
GET  /sales/inventory/stocks
GET  /sales/inventory/previous
GET  /sales/inventory/movements
GET  /sales/inventory/purchasing-sales-stock
POST /sales/inventory/movement
```

新增数据表：

```text
crm_fat_management_record
crm_customer_info_item
crm_payment_order
crm_payment_refund
crm_payment_channel
crm_inventory_stock
crm_inventory_snapshot
crm_inventory_movement
```

本轮已补齐的能力：

```text
客户信息管理：客户全量建档、体脂指标、目标指标、阶段、复测、风险等级、身份、住址、关系图谱、联系记录、网站博客，支持卡片化新增、编辑、删除和更新。
支付引擎：统一下单、支付订单、退款、渠道配置、通知状态、对账状态。
库存：当前库存、可用库存、锁定库存、安全库存、库存金额。
上期库存：期间快照、期初/入库/出库/期末。
进销存：采购入库、销售出库、库存流水和进销存汇总报表。
```

仍建议继续补充的能力：

```text
客户信息管理：智能饮食打卡、设备数据接入、复测提醒自动化、隐私授权管理、统一客户ID合并。
支付引擎：渠道签名验签、异步通知幂等表、分账/转账实付落库、渠道账单文件解析、风控限额。
库存进销存：采购订单审批、供应商管理、批次/效期、多仓调拨、盘点差异处理、成本计价策略。
```

## 后端构建

编译核心模块：

```bash
mvn -DskipTests compile
```

只编译本次增强相关模块：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-customer,crm-service,crm-marketing,crm-workflow -am -DskipTests compile
```

## 后端启动

先启动 MySQL、Redis、Nacos，再启动网关和业务服务。所有命令都在项目根目录执行：

```bash
cd /Users/xiong/Desktop/openclaw/project/crm-system
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
```

端口规划：

```text
sentinel-dashboard  8858
crm-gateway         8090
crm-customer        8081
crm-sales           8082
crm-marketing       8083
crm-service         8084
crm-leads           8085
crm-workflow        8086
crm-auth            8087
crm-frontend        3000（默认，也可指定 3007）
```

按下面顺序分别打开多个终端启动：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-auth spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-customer spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-sales spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-marketing spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-service spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-leads spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-workflow spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-gateway spring-boot:run
```

默认网关地址：

```text
http://localhost:8090
```

启动成功后可以先检查：

```bash
curl -X POST http://127.0.0.1:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

如果只调试某个业务服务，也可以直接访问服务端口；前端正常使用建议统一走网关 `8090`。

### 后端一键启动器

推荐使用 `crm-starter` 一键启动 Sentinel 控制台和后端 8 个服务：

```bash
cd /Users/xiong/Desktop/openclaw/project/crm-system
java -jar crm-starter/target/crm-starter-1.0.0-SNAPSHOT.jar
```

如需重新打包启动器：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-starter -am -DskipTests package
```

`crm-starter` 当前行为：

```text
1. 检查 Java、MySQL、Redis、Nacos。
2. 启动前扫描并清理服务端口：8858、8081、8082、8083、8084、8085、8086、8087、8090。
3. 端口占用时先 kill PID，仍未释放时再 kill -9 PID。
4. 启动子进程默认强制使用 JDK 17：
   /Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
5. 如需覆盖 JDK，可设置：
   CRM_STARTER_JAVA_HOME=/path/to/jdk17
6. 自动准备并启动 Sentinel 控制台，默认访问地址为 http://localhost:8858，默认账号/密码为 sentinel/sentinel。
7. 启动每个业务服务时显式启用 Nacos 配置中心和注册中心。
8. Nacos 默认 server-addr 为 127.0.0.1:8848，namespace 为 dev。
9. Nacos 配置读取 dataId 为：
   crm-common.yaml、crm-auth.yaml、crm-customer.yaml、crm-sales.yaml、crm-marketing.yaml、crm-service.yaml、crm-leads.yaml、crm-workflow.yaml、crm-gateway.yaml
10. Nacos 配置使用 optional:nacos:<服务名>.yaml；Nacos 无配置时继续使用本地 application.yml。
11. 端口开始监听只作为过程日志，必须等 Spring Boot 输出 Started XxxApplication 且进程保持运行，才判定服务启动成功。
12. 启动失败会输出 ERROR、Exception、端口占用、Nacos 配置/注册相关日志，并打印最近启动日志。
13. Sentinel 控制台 jar 固定从 crm-starter/src/main/resources/sentinel-dashboard-1.8.9.jar 读取，不再联网下载。
14. 如需覆盖 Sentinel 控制台账号密码，可设置 SENTINEL_DASHBOARD_USERNAME、SENTINEL_DASHBOARD_PASSWORD。
```

`crm-starter/start.sh` 也支持相同行为：

```bash
cd /Users/xiong/Desktop/openclaw/project/crm-system/crm-starter
./start.sh
```

使用 `screen` 后台启动示例：

```bash
screen -dmS crm-auth zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-auth spring-boot:run'
screen -dmS crm-customer zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-customer spring-boot:run'
screen -dmS crm-sales zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-sales spring-boot:run'
screen -dmS crm-marketing zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-marketing spring-boot:run'
screen -dmS crm-service zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-service spring-boot:run'
screen -dmS crm-leads zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-leads spring-boot:run'
screen -dmS crm-workflow zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-workflow spring-boot:run'
screen -dmS crm-gateway zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-gateway spring-boot:run'
```

后台启动后查看服务：

```bash
screen -ls
lsof -nP -iTCP:8080,8081,8082,8083,8084,8085,8086,8087,8090 -sTCP:LISTEN
```

停止后端服务：

```bash
for s in crm-gateway crm-auth crm-customer crm-sales crm-marketing crm-service crm-leads crm-workflow; do
  screen -S "$s" -X quit 2>/dev/null || true
done

lsof -tiTCP:8080,8081,8082,8083,8084,8085,8086,8087,8090 -sTCP:LISTEN | xargs -r kill
```

只停止前端：

```bash
screen -S crm-frontend -X quit 2>/dev/null || true
lsof -tiTCP:3000,3007 -sTCP:LISTEN | xargs -r kill
```

## 前端

安装依赖：

```bash
cd crm-frontend
npm install
```

开发启动：

```bash
npm run dev
```

默认地址：

```text
http://localhost:3000
```

从项目根目录一条命令启动前端：

```bash
cd /Users/xiong/Desktop/openclaw/project/crm-system/crm-frontend
npm run dev -- --host 0.0.0.0
```

如果 3000 端口被占用，可指定 3007：

```bash
npm run dev -- --host 0.0.0.0 --port 3007
```

使用 `screen` 后台启动前端 3007：

```bash
screen -dmS crm-frontend zsh -lc 'cd /Users/xiong/Desktop/openclaw/project/crm-system/crm-frontend && npm run dev -- --host 0.0.0.0 --port 3007'
```

生产构建：

```bash
npm run build
```

前端接口默认代理到：

```text
http://127.0.0.1:8090
```

配置位置：

```text
crm-frontend/vite.config.js
```

## 一次性启动清单

1. 启动基础设施：

```bash
mysql.server start
redis-server
```

Nacos 按本机安装目录启动，常见命令：

```bash
cd /path/to/nacos/bin
sh startup.sh -m standalone
```

项目配置使用 Nacos namespace `dev`。如果本地 Nacos 没有 `dev` 命名空间，需要先在 Nacos 控制台创建，或把各模块 `application.yml` 中的 `namespace: dev` 调整为本机已有 namespace。

2. 初始化数据库：

```bash
cd /Users/xiong/Desktop/openclaw/project/crm-system
mysql -u root -p < sql/crm_init.sql
mysql -u root -p < sql/crm_upgrade_insights.sql
mysql -u root -p < sql/crm_upgrade_business_ops.sql
mysql -u root -p < sql/crm_upgrade_sales_platform_import.sql
mysql -u root -p < sql/crm_upgrade_customer_info_modules.sql
mysql -u root -p < sql/crm_upgrade_customer_info_items.sql
mysql -u root -p < sql/crm_upgrade_notification_channels.sql
mysql -u root -p < sql/crm_upgrade_sentinel_console.sql
```

3. 启动 Sentinel 控制台和后端 8 个服务：

推荐一键启动：

```bash
cd /Users/xiong/Desktop/openclaw/project/crm-system
java -jar crm-starter/target/crm-starter-1.0.0-SNAPSHOT.jar
```

或使用启动脚本：

```bash
cd /Users/xiong/Desktop/openclaw/project/crm-system/crm-starter
./start.sh
```

也可以手动启动：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-auth spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-customer spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-sales spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-marketing spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-service spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-leads spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-workflow spring-boot:run
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl crm-gateway spring-boot:run
```

也可以在项目根目录使用 `screen` 一次性后台启动后端：

```bash
screen -dmS crm-auth zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-auth spring-boot:run'
screen -dmS crm-customer zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-customer spring-boot:run'
screen -dmS crm-sales zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-sales spring-boot:run'
screen -dmS crm-marketing zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-marketing spring-boot:run'
screen -dmS crm-service zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-service spring-boot:run'
screen -dmS crm-leads zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-leads spring-boot:run'
screen -dmS crm-workflow zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-workflow spring-boot:run'
screen -dmS crm-gateway zsh -lc 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home; cd /Users/xiong/Desktop/openclaw/project/crm-system && mvn -pl crm-gateway spring-boot:run'
```

4. 启动前端：

```bash
cd /Users/xiong/Desktop/openclaw/project/crm-system/crm-frontend
npm run dev
```

5. 浏览器访问：

```text
http://localhost:3000
```

本地如果使用 3007 启动前端，则访问：

```text
http://localhost:3007
```

## 登录账号

初始化脚本内置管理员：

```text
用户名：admin
密码：admin123
```

## 网关路由

网关配置位于：

```text
crm-gateway/src/main/resources/application.yml
```

主要路由：

```text
/api/auth/**       -> crm-auth
/api/customer/**   -> crm-customer
/api/sales/**      -> crm-sales
/api/marketing/**  -> crm-marketing
/api/service/**    -> crm-service
/api/leads/**      -> crm-leads
/api/workflow/**   -> crm-workflow
/api/system/**     -> crm-auth
/api/sse/**        -> crm-auth
```

SSE 消息订阅由前端自动建立，代理路径为：

```text
GET /api/sse/notification/subscribe
```

## 流量降级

已补充网关级 Sentinel 限流降级，配置位于：

```text
crm-gateway/src/main/java/com/crm/gateway/config/SentinelGatewayRuleConfig.java
crm-gateway/src/main/java/com/crm/gateway/config/CrmGatewaySentinelProperties.java
crm-gateway/src/main/resources/application.yml
```

当前默认按路由 ID 加载本地规则，超限后返回 HTTP 429 JSON 响应：

```text
crm-auth       100 QPS
crm-customer   200 QPS
crm-sales      150 QPS
crm-marketing  120 QPS
crm-service    150 QPS
crm-leads      150 QPS
crm-workflow    80 QPS
crm-system      80 QPS
```

同时支持按具体接口加载 API 级限流规则，默认包含：

```text
auth-login                  /api/auth/login                         20 QPS
sales-stats-report          /api/sales/sales/stats/report           30 QPS
marketing-campaign-report   /api/marketing/marketing/campaign/report 30 QPS
sales-order-import          /api/sales/sales/order/import            5 QPS
sales-platform-sync         /api/sales/sales/platform/config/.*/sync 10 QPS
```

可通过 `crm.gateway.sentinel.route-rules` 调整各路由阈值，通过 `crm.gateway.sentinel.api-rules` 调整具体接口阈值和突发流量 `burst`。`api-rules.match-strategy` 支持 `exact`、`prefix`、`regex`。Sentinel Dashboard 地址沿用：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8858
      eager: true
```

Sentinel 控制台服务由 `crm-starter` 管理：

```text
服务名：sentinel-dashboard
端口：8858
控制台地址：http://localhost:8858
前端嵌套页面：/system/sentinel
默认账号：sentinel
默认密码：sentinel
账号覆盖：SENTINEL_DASHBOARD_USERNAME
密码覆盖：SENTINEL_DASHBOARD_PASSWORD
本地 jar：crm-starter/src/main/resources/sentinel-dashboard-1.8.9.jar
```

Sentinel 控制台由项目内置 jar 启动，路径为：

```text
crm-starter/src/main/resources/sentinel-dashboard-1.8.9.jar
```

销售统计和营销报表也补充了服务级 Sentinel 资源降级：

```text
salesStatsReport        -> GET /sales/stats/report
marketingCampaignReport -> GET /marketing/campaign/report
```

当这些资源被 Sentinel 规则拦截时，会返回带 `degraded=true` 的轻量报表数据。

## 分库分表

已补充高增长表的 ShardingSphere-JDBC 方案样例，默认不启用，避免本地单库开发环境缺少物理分片库时启动失败。

新增文件：

```text
config/sharding/application-sharding.yml
config/sharding/crm-sharding.yaml
sql/crm_sharding_tables.sql
```

建议优先分片的表：

```text
crm_auth.sys_oper_log                 操作日志，高写入高留存
crm_customer.crm_customer_interaction 客户互动时间线
crm_customer.crm_followup_record      跟进记录
crm_service.crm_ticket                服务工单
crm_marketing.crm_campaign_performance 营销效果明细
```

启用步骤：

```bash
mysql -u root -p < sql/crm_sharding_tables.sql
```

将 `config/sharding/application-sharding.yml` 放入对应服务 `resources` 目录，将 `config/sharding/crm-sharding.yaml` 放入对应服务 `resources/sharding/` 目录，并在需要启用分片的服务中增加：

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
</dependency>
```

启动时追加：

```bash
--spring.profiles.active=sharding
```

当前样例采用 2 库多表的取模分片：

```text
sys_oper_log                 2 库 x 16 表，按 id 分片
crm_customer_interaction     2 库 x 8 表，按 contact_id 分片
crm_followup_record          2 库 x 8 表，按 contact_id 分片
crm_ticket                   2 库 x 8 表，按 contact_id 分片
crm_campaign_performance     2 库 x 8 表，按 campaign_id 分片
```

## 验证记录

已验证：

```bash
npm run build
```

已验证增强相关后端模块：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-customer -am -DskipTests compile

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-service -am -DskipTests compile

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-marketing -am -DskipTests compile

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-workflow -am -DskipTests compile

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-gateway -am -DskipTests compile

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-sales,crm-marketing -am -DskipTests compile

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-customer,crm-sales -am -DskipTests compile

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home \
mvn -pl crm-starter -am -DskipTests package
```

前端构建仍会提示 Element Plus 和 ECharts vendor chunk 较大，这是依赖体积导致，不影响运行。当前已通过 Vite manualChunks 将 Vue、Element Plus、ECharts、Axios 拆分为独立 vendor 包。

## 后续建议

- 将操作日志内部采集接口增加服务间签名或内网访问控制。
- 增加后端单元测试和前端页面级 E2E 测试。

## 最新开发补充

- 新增通用 `@OperationLog` 注解和 AOP，新增写入类接口已记录结构化操作日志，并异步上报到 `crm_auth.sys_oper_log`。
- 新增 auth 内部采集接口：`POST /system/oper-log/internal`。默认上报地址：`http://127.0.0.1:8087/system/oper-log/internal`。
- 新增写入接口权限控制：产品、销售订单、增值服务、跟进记录、客户互动、营销效果、知识库、SLA、工作流触发和审批。
- SLA 规则升级为数据库可配置：`crm_sla_rule`，接口 `GET/POST /service/knowledge/sla-rules`。
- 营销效果明细的 `cost_amount`、`revenue_amount` 改为 `DECIMAL(15,2)`，报表 ROI 按收入/成本计算。
- 工作流实例支持节点级动作：审批通过、驳回、转交、重试。
- 网关新增 Sentinel 路由级限流降级，超限统一返回 429 JSON。
- 销售统计和营销报表新增 Sentinel 服务级降级资源，触发后返回轻量报表数据。
- 新增分库分表可选配置样例和物理分片表 SQL，覆盖操作日志、客户互动、跟进、工单和营销效果明细。
- 新增客户信息管理、支付管理、库存进销存页面、接口、权限菜单和升级脚本。
- 客户信息管理已由单一脂肪档案扩展为模块化客户资料，支持客户主档、脂肪档案、身份信息、住址信息、关系图谱、联系记录、网站博客的新增、编辑、删除、更新。
- 关系图谱改为 ECharts 力导向图，支持选中节点/关系后维护用户自建资料。
- 通知中心支持 SSE 实时消息、消息列表、系统广播通知、接收人覆盖，以及邮件、钉钉、飞书、短信、微信渠道配置后推送。
- 销售管理支持 Excel 导入销售订单、订单详情查看、淘宝、天猫、京东、抖音、亚马逊、拼多多平台配置，以及订单和退款数据抽取。
- Sa-Token 公共配置收敛到 `crm-common/src/main/resources/application-common.yml`，Nacos 对应公共 dataId 为 `crm-common.yaml`；各业务服务删除重复 `sa-token` 块，`crm-auth` 和 `crm-gateway` 仅保留差异项。
- `crm-starter` 一键启动器支持启动前清理后端端口、强制 JDK 17、显式启用 Nacos 配置中心/注册中心、输出 Nacos 读取和注册日志，并以 Spring Boot `Started XxxApplication` 作为真正启动成功条件。
- 新增 Sentinel 控制台服务和前端嵌套访问页：`sentinel-dashboard` 运行在 `8858`，前端路由为 `/system/sentinel`，系统管理菜单通过 `crm_upgrade_sentinel_console.sql` 补充。

工作流动作接口：

```text
POST /workflow/workflow/instances/{id}/approve
POST /workflow/workflow/instances/{id}/reject
POST /workflow/workflow/instances/{id}/transfer
POST /workflow/workflow/instances/{id}/retry
```

# 黑马记账

一款运行在 Windows / Mac 上的个人桌面记账应用，支持收支记录、二级分类管理、统计报表和数据导出。

## 功能

- **记账**：记录每笔支出或收入，支持二级分类联动选择
- **记录查看**：按日期倒序列表，收入绿色、支出红色，支持多维度筛选搜索
- **统计报表**：月度收支总览、分类占比饼图、每日收支明细、日历视图
- **分类管理**：预置 10 个支出大类 + 7 个收入大类共 87 个子分类，支持用户新增自定义分类
- **贪吃蛇**：内置经典贪吃蛇小游戏，支持经典/穿墙/迷宫/限时四种模式和三档难度
- **数据管理**：数据存储在本机，支持 Excel 导出

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot + Spring Data JPA |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3 + Vite + Element Plus |
| 图表 | ECharts |

## 本地运行

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0
- Node.js 16+

### 1. 启动数据库

确保 MySQL 服务已启动，创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS heima_accounting DEFAULT CHARACTER SET utf8mb4;
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 http://localhost:3000

### 4. 访问应用

浏览器打开 http://localhost:3000 ，首次启动会自动初始化预置分类数据。

## 项目结构

```
heima-vibe-accounting-book/
├── backend/                 # Spring Boot 后端
│   └── src/main/java/com/heima/accounting/
│       ├── controller/      # API 控制器
│       ├── service/         # 业务逻辑
│       ├── repository/      # 数据访问层
│       ├── entity/          # 数据实体
│       ├── dto/             # 数据传输对象
│       └── config/          # 配置类
├── frontend/                # Vue 3 前端
│   └── src/
│       ├── views/           # 页面组件
│       ├── components/      # 通用组件
│       ├── api/             # API 封装
│       └── router/          # 路由配置
└── CLAUDE.md                # 完整产品文档
```

# 自动排课系统

基于 Spring Boot 3 + Vue 3 的自动排课系统，支持自动排课、冲突检测、可视化调课与课表导出。

## 功能特性

- **基础数据管理**：教师、班级、课程、教室、时间段的全功能 CRUD
- **自动排课**：基于回溯 + 贪心算法，自动生成满足约束的课表
  - 教师 / 班级 / 教室三类时间冲突检测
  - 教室容量、教师可教课程、连排约束等规则
- **手动调课**：班级 / 教师 / 教室三视角课表，拖拽单元格调课，实时冲突提示
- **课表导出**：一键导出 Excel 课表
- **角色权限**：JWT 认证，管理员 / 教师 / 学生三种角色

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3、Element Plus、Vite、Pinia、Vue Router、Axios、xlsx |
| 后端 | Java 17、Spring Boot 3.3、Spring Data JPA、Spring Security (crypto)、JWT (jjwt) |
| 数据库 | MySQL 8.0 |
| 构建 | Maven、npm |

## 快速开始

### 环境要求

- JDK 17
- Node.js 18+
- MySQL 8.0（已创建 `scheduling` 数据库，或首次启动自动建表）
- Maven（首次运行需用于构建后端 jar）

### 一键启动

双击根目录的 `start-all.bat`（或 PowerShell 中执行 `start-all.ps1`），脚本会：

1. 自动构建后端 jar（首次运行）
2. 启动后端（端口 8080）并等待就绪
3. 启动前端（端口 5173）
4. 自动打开浏览器

默认管理员账号：`admin / admin123`

> 脚本内的 `DB_PASSWORD`、`JWT_SECRET` 等为本地开发默认值，部署时请通过环境变量覆盖。

### 环境变量

| 变量 | 说明 | 默认值 |
|---|---|---|
| `DB_URL` | 数据库连接串 | `jdbc:mysql://localhost:3306/scheduling` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | 无 |
| `JWT_SECRET` | JWT 签名密钥 | 无 |
| `DEFAULT_ADMIN_PASSWORD` | 首次启动时的管理员密码 | 无 |

## 项目结构

```
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 3 前端
├── scripts/          # 构建辅助脚本
├── tests/            # 测试脚本
├── docs/             # 设计文档
│   ├── PRODUCT.md    # 产品设计文档
│   └── TECH.md       # 技术方案
├── start-all.bat     # 一键启动（Windows）
└── start-all.ps1     # 一键启动（PowerShell）
```

## License

[MIT](LICENSE)

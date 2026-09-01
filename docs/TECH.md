# 排课系统 — 技术方案

> 基于 Spring Boot + Vue 3 的自动排课系统技术实现方案
> 文档版本：v1.0  |  最后更新：2026-09-01

---

## 1. 方案概述

### 1.1 目标

本文档为《排课系统》产品提供完整的技术实现方案，明确：
- 前后端技术选型与理由
- 系统架构与模块划分
- 数据库与核心算法设计
- 接口规范与开发计划

### 1.2 核心设计原则

| 原则 | 说明 |
|---|---|
| 前后端分离 | 前端负责界面交互，后端负责业务逻辑与数据持久化 |
| 数据一致性 | 排课结果涉及多表关联，使用数据库事务保证一致性 |
| 可扩展性 | 教师/学生角色、排课规则预留扩展接口 |
| 易维护性 | 代码分层清晰，业务逻辑与数据访问解耦 |

---

## 2. 总体架构

```
┌─────────────────────────────────────────────────────────────┐
│                         前端层 (Vue 3)                       │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐ │
│  │   登录页     │ │  基础数据管理 │ │   排课 / 课表展示    │ │
│  └──────────────┘ └──────────────┘ └──────────────────────┘ │
│                         Axios + RESTful                     │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                      后端层 (Spring Boot)                    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────────┐ │
│  │ Controller│ │  Service │ │Repository│ │  Scheduling    │ │
│  │   接口层  │ │  业务层  │ │  数据层  │ │  排课引擎      │ │
│  └──────────┘ └──────────┘ └──────────┘ └────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                       数据层 (MySQL 8.0)                     │
│         教师 / 班级 / 课程 / 教室 / 时间段 / 排课结果         │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 前端技术方案

### 3.1 技术栈

| 技术 | 版本 | 用途 |
|---|---|---|
| Vue 3 | 3.4+ | 渐进式前端框架，Composition API 组织复杂课表状态 |
| Element Plus | 2.7+ | UI 组件库，快速搭建管理后台 |
| Vite | 5.0+ | 构建工具，支持热更新与快速启动 |
| Vue Router | 4.0+ | 单页应用路由管理 |
| Pinia | 2.0+ | 全局状态管理 |
| Axios | 1.7+ | HTTP 请求封装 |
| xlsx | 0.18+ | 前端导出 Excel 课表 |

### 3.2 目录结构

```
frontend/
├── public/
├── src/
│   ├── api/              # 接口请求封装
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia 状态管理
│   ├── views/            # 页面视图
│   │   ├── LoginView.vue
│   │   ├── DashboardView.vue
│   │   ├── TeacherView.vue
│   │   ├── ClassView.vue
│   │   ├── CourseView.vue
│   │   ├── ClassroomView.vue
│   │   ├── TimeSlotView.vue
│   │   ├── ScheduleView.vue
│   │   └── TimetableView.vue
│   ├── App.vue
│   └── main.js
├── package.json
└── vite.config.js
```

### 3.3 关键技术点

- **课表组件**：使用 Element Plus 的 `el-table` 自定义二维表格，行=节次，列=星期
- **拖拽调课**：利用 HTML5 Drag and Drop API，交换两个单元格的排课记录
- **权限控制**：路由守卫根据角色动态渲染菜单

---

## 4. 后端技术方案

### 4.1 技术栈

| 技术 | 版本 | 用途 |
|---|---|---|
| Spring Boot | 3.3+ | 后端主框架 |
| Spring Data JPA | 3.3+ | ORM 数据访问 |
| MySQL | 8.0+ | 关系型数据库 |
| Maven | 3.9+ | 项目构建与依赖管理 |
| JDK | 17+ | Java 运行环境 |
| Lombok | 1.18+ | 减少样板代码 |
| Jakarta Validation | 3.0+ | 请求参数校验 |

### 4.2 目录结构

```
backend/
├── src/main/java/com/example/scheduling/
│   ├── config/           # 配置类
│   ├── controller/       # RESTful 接口
│   ├── service/          # 业务逻辑
│   │   ├── impl/         # 实现类
│   ├── repository/       # JPA 数据访问
│   ├── entity/           # 实体类
│   ├── dto/              # 数据传输对象
│   ├── exception/        # 全局异常处理
│   └── scheduling/       # 排课算法引擎
├── src/main/resources/
│   ├── application.yml
│   └── application-dev.yml
├── pom.xml
└── mvnw
```

### 4.3 分层职责

| 层次 | 职责 |
|---|---|
| Controller | 接收 HTTP 请求，参数校验，返回统一响应 |
| Service | 业务逻辑编排，事务管理 |
| Repository | 数据库 CRUD，复杂查询使用 `@Query` |
| Entity | 与数据库表一一对应 |
| DTO | 前后端数据交互对象 |

---

## 5. 数据库设计

### 5.1 实体关系

```
教师(Teacher) 1:N 可教课程(TeacherCourse)
课程(Course)  1:N 可教课程(TeacherCourse)

班级(Class)   1:N 排课结果(Schedule)
教师(Teacher) 1:N 排课结果(Schedule)
课程(Course)  1:N 排课结果(Schedule)
教室(Classroom) 1:N 排课结果(Schedule)
时间段(TimeSlot) 1:N 排课结果(Schedule)
```

### 5.2 核心表结构

#### 5.2.1 教师表 `teacher`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键 |
| code | VARCHAR(32) | 教师编号，唯一 |
| name | VARCHAR(64) | 姓名 |
| gender | TINYINT | 性别：0-女，1-男 |
| phone | VARCHAR(20) | 电话 |
| title | VARCHAR(32) | 职称 |

#### 5.2.2 班级表 `class`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键 |
| code | VARCHAR(32) | 班级编号，唯一 |
| name | VARCHAR(64) | 班级名称 |
| grade | VARCHAR(32) | 年级 |
| student_count | INT | 人数 |

#### 5.2.3 课程表 `course`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键 |
| code | VARCHAR(32) | 课程编号，唯一 |
| name | VARCHAR(64) | 课程名称 |
| credit | DECIMAL(3,1) | 学分 |
| weekly_hours | INT | 周学时 |
| type | TINYINT | 类型：0-必修，1-选修 |

#### 5.2.4 教室表 `classroom`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键 |
| code | VARCHAR(32) | 教室编号，唯一 |
| name | VARCHAR(64) | 教室名称 |
| capacity | INT | 容量 |
| type | TINYINT | 类型：0-普通，1-实验室，2-多媒体 |

#### 5.2.5 时间段表 `time_slot`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键 |
| week_day | TINYINT | 星期：1-7 |
| section | TINYINT | 节次：1-8 |

#### 5.2.6 教师可教课程表 `teacher_course`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键 |
| teacher_id | BIGINT FK | 教师 |
| course_id | BIGINT FK | 课程 |

#### 5.2.7 排课结果表 `schedule`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 主键 |
| class_id | BIGINT FK | 班级 |
| teacher_id | BIGINT FK | 教师 |
| course_id | BIGINT FK | 课程 |
| classroom_id | BIGINT FK | 教室 |
| time_slot_id | BIGINT FK | 时间段 |
| week_number | INT | 第几周 |
| created_at | DATETIME | 创建时间 |

### 5.3 本地数据设计

#### 5.3.1 初始化方式

- 使用 Spring Boot `data.sql`（或 `CommandLineRunner`）在 **dev 环境**自动执行初始化
- 通过 `spring.sql.init.mode` 控制执行：dev 为 `always`，prod 为 `never`
- 排课结果表 `schedule` **不预置数据**，由排课算法产生

#### 5.3.2 预置数据清单

| 数据 | 数量 | 说明 |
|---|---|---|
| 时间段 | 40 | 周一~周五 × 8 节，系统预置，禁止删除 |
| 管理员账号 | 1 | `admin / 123456`，密码 BCrypt 加密存储 |
| 教师 | 5 | 含职称、可教课程关联 |
| 班级 | 4 | 不同年级，人数有差异 |
| 课程 | 6 | 含必修/选修，周学时 2~4 |
| 教室 | 6 | 普通 4 + 实验室 1 + 多媒体 1 |

#### 5.3.3 示例数据设计原则

- 数据规模可完整跑通自动排课（不存在资源不足导致无解）
- 故意构造紧约束（如某课程仅 1 名教师可教），用于验证算法优先级排序
- 教室容量覆盖班级人数区间，用于验证容量约束
- 演示数据可一键重置，方便答辩演示

---

## 6. 排课算法设计

### 6.1 算法思路

采用 **贪心 + 回溯** 策略：

1. **按约束优先级排序待排任务**：周学时高、可选教师少、可选教室少的课程优先
2. **生成候选位置**：根据教师、班级、教室可用时间段生成候选
3. **选择最优候选**：优先选择连排位置（同一天连续节次）
4. **冲突检测**：每次放置前检查教师/班级/教室/教师可教课程/容量约束
5. **回溯**：若后续无解，回退重新选择

### 6.2 冲突检测规则

| 规则 | 检测逻辑 |
|---|---|
| 教师不冲突 | 同一 `time_slot_id`，`teacher_id` 不重复 |
| 班级不冲突 | 同一 `time_slot_id`，`class_id` 不重复 |
| 教室不冲突 | 同一 `time_slot_id`，`classroom_id` 不重复 |
| 教室容量 | `class.student_count <= classroom.capacity` |
| 教师可教课程 | `teacher_id` 必须在 `teacher_course` 中存在 |
| 课程周学时 | 一门课在 `schedule` 中的记录数等于 `course.weekly_hours` |
| 连排约束 | 尽量将同一课程的节次安排在同一天连续时间段 |

### 6.3 算法伪代码

```text
function autoSchedule(tasks, timeSlots, classrooms):
    sort tasks by constraint tightness desc
    result = []
    if backtrack(0, tasks, result):
        return result
    else:
        return "无可行解"

function backtrack(index, tasks, result):
    if index == tasks.length:
        return true
    task = tasks[index]
    candidates = generateCandidates(task)
    sort candidates by continuity score desc
    for candidate in candidates:
        if noConflict(candidate, result):
            result.add(candidate)
            if backtrack(index + 1, tasks, result):
                return true
            result.remove(candidate)
    return false
```

---

## 7. 接口设计规范

### 7.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 7.2 核心接口列表

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/auth/login` | POST | 登录 |
| `/api/teachers` | GET/POST/PUT/DELETE | 教师 CRUD |
| `/api/classes` | GET/POST/PUT/DELETE | 班级 CRUD |
| `/api/courses` | GET/POST/PUT/DELETE | 课程 CRUD |
| `/api/classrooms` | GET/POST/PUT/DELETE | 教室 CRUD |
| `/api/time-slots` | GET/POST/PUT/DELETE | 时间段 CRUD |
| `/api/schedules/auto` | POST | 触发自动排课 |
| `/api/schedules` | GET | 查询排课结果 |
| `/api/schedules/{id}` | PUT | 手动调课 |
| `/api/timetables/class/{id}` | GET | 班级课表 |
| `/api/timetables/teacher/{id}` | GET | 教师课表 |
| `/api/timetables/classroom/{id}` | GET | 教室课表 |
| `/api/timetables/export` | GET | 导出 Excel 课表 |

---

## 8. 核心流程设计

### 8.1 登录流程

```
登录页提交账号密码 → POST /api/auth/login
→ 后端校验（BCrypt 比对密码）
→ 签发 JWT → 前端存入 localStorage
→ 后续请求携带 Authorization: Bearer <token>
→ 收到 401 时前端清除 token 并跳回登录页
```

### 8.2 基础数据 CRUD 流程

```
页面操作 → Axios 请求 → Controller 参数校验（Jakarta Validation）
→ Service 业务校验（编号唯一、删除保护）
→ Repository 持久化 → 统一响应（code/message/data）
→ 前端刷新列表或提示错误
```

### 8.3 自动排课流程

```
管理员点击「开始排课」→ POST /api/schedules/auto
→ 后端校验基础数据完备性（周学时、教师可教、教室容量等）
→ 事务内清空旧排课结果
→ 加载全部数据构建排课任务 → 执行 贪心+回溯 算法
→ 成功：批量写入 schedule 表，返回排课结果
→ 失败：回滚事务，返回无解/冲突报告
```

### 8.4 手动调课流程

```
课表页拖拽单元格（源 → 目标）
→ 前端预检冲突（教师/班级/教室占用），冲突则提示并取消
→ 预检通过 → PUT /api/schedules/{id}
→ 后端二次校验全部冲突规则 → 更新记录 → 返回最新课表
```

### 8.5 课表展示与导出流程

```
选择视角（班级/教师/教室）→ GET /api/timetables/{视角}/{id}
→ 后端查询 schedule 关联数据 → 组装二维课表（行=节次，列=星期）
→ 前端渲染；导出时前端 xlsx 库生成 Excel 下载
```

---

## 9. 安全设计

### 9.1 安全措施

- **登录鉴权**：使用 JWT Token，存储于前端 `localStorage`
- **密码存储**：数据库中密码使用 BCrypt 加密
- **接口权限**：MVP 阶段仅管理员角色，后续扩展 Spring Security 角色控制
- **SQL 注入**：使用 JPA 参数化查询，避免拼接 SQL
- **跨域处理**：Spring Boot 配置 CORS，允许前端开发服务器访问

### 9.2 安全边界

| 边界 | 规则 | 实现方式 |
|---|---|---|
| 认证边界 | 仅 `POST /api/auth/login` 公开，其余接口必须携带有效 JWT | 拦截器统一校验，失败返回 401 |
| 授权边界 | MVP 所有需登录接口仅 `ADMIN` 可访问；教师/学生只读课表接口（预留） | 角色校验，越权返回 403 |
| 输入边界 | 所有写接口参数必须通过 Jakarta Validation 校验（非空、长度、范围、格式） | `@Valid` + 实体注解 |
| 数据完整性边界 | 编号唯一；基础数据被排课结果引用时禁止删除 | 唯一索引 + 外键 + Service 删除前检查 |
| 操作边界 | 自动排课整体在一个事务内；手动调课前强制冲突检测 | `@Transactional` + 冲突校验服务 |

---

## 10. 开发环境与部署

### 10.1 开发环境

| 工具 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | 后端运行环境 |
| Node.js | 20+ | 前端运行环境 |
| MySQL | 8.0 | 数据库 |
| IntelliJ IDEA / VS Code | - | 开发 IDE |

### 10.2 本地启动

```bash
# 后端
cd backend
mvn spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

### 10.3 部署方案

- **后端**：打包为 `jar`，使用 `java -jar` 或 Docker 部署
- **前端**：`npm run build` 生成静态资源，使用 Nginx 托管
- **数据库**：MySQL 生产环境独立部署

---

## 11. 开发计划

| 阶段 | 周期 | 主要任务 |
|---|---|---|
| M1 | 0.5 周 | 项目骨架搭建，前后端跑通，登录页可访问 |
| M2 | 1 周 | 登录 + 教师/班级/课程/教室/时间段 CRUD |
| M3 | 1.5 周 | 排课规则 + 自动排课 + 课表展示 |
| M4 | 1 周 | 手动调课 + Excel 导出 + 测试 + 答辩文档 |

---

## 12. 测试与验收

### 12.1 测试策略

| 层次 | 工具 | 覆盖点 |
|---|---|---|
| 单元测试 | JUnit 5 + Mockito | 冲突检测规则、排课算法、Service 业务逻辑 |
| 数据层测试 | Spring Data JPA + 测试库 | Repository CRUD、关联查询、唯一约束 |
| 接口测试 | MockMvc / Postman | 登录、各 CRUD 接口、排课与调课接口 |
| 前端验收 | 手工测试为主 | 页面流程、表单校验、课表交互、Excel 导出 |

### 12.2 关键测试用例（排课核心）

| 用例 | 前置条件 | 期望结果 |
|---|---|---|
| 教师冲突 | 同一教师已占用同一时间段 | 拒绝排入 |
| 班级冲突 | 同一班级已占用同一时间段 | 拒绝排入 |
| 教室冲突 | 同一教室已占用同一时间段 | 拒绝排入 |
| 教室容量不足 | 班级人数 > 教室容量 | 拒绝分配该教室 |
| 教师不可教 | 教师不在该课程可教列表 | 拒绝分配该教师 |
| 周学时守恒 | 课程周学时 = N | 排课后该课记录数恰好为 N |
| 连排优先 | 周学时 ≥ 2 | 优先安排同日连续节次 |
| 无解场景 | 资源不足 | 返回明确的无解/冲突报告，事务回滚 |

### 12.3 验收标准

| 阶段 | 验收标准 |
|---|---|
| M1 | 后端启动无报错，前端登录页可访问并完成首次前后端联调 |
| M2 | 管理员可登录；5 类基础数据 CRUD 全部可用且校验生效 |
| M3 | 示例数据可一键自动排课，三个视角课表展示正确，无硬性冲突 |
| M4 | 拖拽调课生效且冲突被拦截；课表可导出 Excel；全部测试通过 |

### 12.4 实施顺序

1. 环境验证（JDK / Node / MySQL / Git 可用）
2. 项目骨架与登录（M1）
3. 基础数据 CRUD（M2）——每个模块按 实体 → Repository → Service → Controller → 接口测试 推进
4. 冲突检测规则单元测试先行，再实现排课算法（M3）
5. 课表展示 → 手动调课 → Excel 导出（M4）
6. 回归测试与答辩文档收尾

---

## 13. 风险与应对

| 风险 | 应对措施 |
|---|---|
| 自动排课无解 | 提供冲突提示，允许管理员调整基础数据后重试 |
| 数据量大时性能下降 | 算法加缓存，复杂查询加索引 |
| 前端课表交互复杂 | 拆分为独立组件，先实现静态展示再添加拖拽 |

---

## 14. 参考文档

- [PRODUCT.md](./PRODUCT.md) — 产品设计文档
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Vue 3 官方文档](https://vuejs.org/)
- [Element Plus 官方文档](https://element-plus.org/)

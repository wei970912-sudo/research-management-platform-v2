# 架构设计说明

## 一、目标

将旧版“多静态页面 + localStorage 本地存储”的科研管理工具升级为：

- 前端单页应用
- 后端统一 API 服务
- 数据库存储主业务数据
- 文件存储统一管理
- 角色权限与日志审计可扩展

## 二、目标架构

```text
Frontend (Vue 3 + Vite)
    |
    | HTTP / JSON
    v
Backend (Spring Boot)
    |
    +-- MySQL / PostgreSQL
    |
    +-- File Storage (Local / MinIO)
```

## 三、后端模块划分

- `foundation`: 统一数据底座与跨模块聚合源
- `project`: 项目主数据
- `schedule`: 阶段、控制线、里程碑、考核项
- `meeting`: 会议记录与纪要
- `contract`: 任务书预算、合同金额、签订状态
- `document`: 节点文档与附件归档
- `calendar`: 日程与提醒
- `dashboard`: 聚合统计看板
- `assistant`: OpenAI 兼容科研助手接口
- `common`: 通用返回结构、异常、基础配置

## 四、前端模块划分

- `views/DashboardView.vue`: 首页总览
- `views/ProjectListView.vue`: 项目列表
- `views/ProjectDetailView.vue`: 项目详情
- `views/ScheduleView.vue`: 进度与里程碑
- `views/MeetingView.vue`: 会议管理

后续可继续增加：

- 合同管理页
- 文档归档页
- 日历提醒页
- 系统管理页

## 五、数据层原则

- 旧版 `localStorage` 只作为迁移来源，不再作为主数据源
- 所有统计聚合由后端输出
- 前端只负责展示、表单交互和调用接口
- 主键统一为后端生成
- 项目主键作为跨模块关联锚点
- AI 助手通过统一数据底座获取项目上下文

## 六、科研助手接入原则

- 对外暴露平台内部接口：`POST /api/assistant/chat`
- 对模型侧使用 OpenAI 兼容协议
- 默认配置指向 `Ollama`
- 项目上下文在后端注入，避免前端重复拼装业务上下文
- 后续可扩展为“项目资料 + 文档附件 + 会议纪要”的检索增强助手

## 七、演进路线

1. 搭建新前后端骨架
2. 固化接口与数据库模型
3. 开发真实 CRUD
4. 编写旧数据迁移脚本
5. 分批替换旧模块

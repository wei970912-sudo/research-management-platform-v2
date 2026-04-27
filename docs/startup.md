# 启动说明

## 前端

目录：`frontend/`

```bash
npm install
npm run dev
```

默认地址：

- [http://localhost:5174](http://localhost:5174)

## 后端

目录：`backend/`

```bash
mvn spring-boot:run
```

默认地址：

- [http://localhost:8080/api/dashboard/summary](http://localhost:8080/api/dashboard/summary)

## 当前说明

- 目前后端返回的是占位数据，用于验证前后端边界和页面结构
- 当前已内置统一数据底座服务，便于后续替换成数据库实现
- 当前已内置 OpenAI 兼容助手接口，默认面向 Ollama 配置
- 下一步建议接入数据库、实体、Service、Repository 和真实 CRUD

## Ollama 科研助手

如需使用本地模型，请先准备：

```bash
ollama serve
ollama pull qwen2.5:7b
```

如模型或地址不同，可通过环境变量覆盖：

```bash
ASSISTANT_BASE_URL=http://localhost:11434/v1
ASSISTANT_MODEL=qwen2.5:7b
ASSISTANT_API_KEY=ollama
```

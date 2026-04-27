# 科研助手与 Ollama 配置说明

## 一、目标

将平台内科研助手统一收口到后端，由后端通过 OpenAI 兼容接口调用本地 Ollama 模型。

这样做的好处：

- 前端不直接暴露模型地址
- 平台可以自动注入项目上下文
- 后续方便增加权限、审计、对话日志和知识库增强

## 二、当前接口

平台内部接口：

- `POST /api/assistant/chat`

请求体：

```json
{
  "message": "请结合当前项目进度给出阶段材料准备建议",
  "projectId": "P-1003"
}
```

## 三、默认配置

后端配置位于：

- [application.yml](D:/03study/work%20ai/research-management-platform-v2/backend/src/main/resources/application.yml)

当前默认值：

- `assistant.openai-compatible.base-url=http://localhost:11434/v1`
- `assistant.openai-compatible.api-key=ollama`
- `assistant.openai-compatible.model=qwen2.5:7b`

## 四、Ollama 建议准备

典型步骤：

1. 启动 Ollama 服务
2. 拉取本地模型
3. 保证 OpenAI 兼容接口可用
4. 启动平台后端

示例命令：

```bash
ollama serve
ollama pull qwen2.5:7b
```

## 五、后续增强建议

- 增加流式输出
- 增加会话历史
- 增加资料检索增强
- 增加提示词模板管理
- 增加按项目隔离的知识库


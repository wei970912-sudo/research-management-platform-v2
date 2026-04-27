<template>
  <section class="view">
    <div class="section-title">
      <div>
        <h2>科研助手</h2>
        <p class="muted">通过后端 OpenAI 兼容接口连接本地 Ollama，并结合统一数据底座给出中文建议。</p>
      </div>
    </div>

    <div class="grid-2">
      <article class="page-card">
        <div class="section-title">
          <div>
            <h3>提问面板</h3>
            <p class="muted">接口地址：`POST /api/assistant/chat`</p>
          </div>
        </div>

        <label class="field">
          <span>项目上下文</span>
          <select v-model="projectId" class="select">
            <option value="">全局上下文</option>
            <option v-for="project in projects" :key="project.id" :value="project.id">
              {{ project.name }}
            </option>
          </select>
        </label>

        <label class="field">
          <span>问题</span>
          <textarea
            v-model="message"
            class="textarea"
            rows="8"
            placeholder="例如：根据当前项目进展，建议下一步工作安排和风险应对措施。"
          />
        </label>

        <button class="btn-primary" :disabled="loading || !message.trim()" @click="submitQuestion">
          {{ loading ? "生成中，请稍候..." : "提交问题" }}
        </button>
        <p v-if="loading" class="muted hint">本地模型推理需要一点时间，请耐心等待。</p>
        <p v-if="error" class="error-msg">{{ error }}</p>
      </article>

      <article class="page-card">
        <div class="section-title">
          <div>
            <h3>助手回复</h3>
            <p class="muted">以分段、列表和标题方式渲染回答，不再直接展示原始 Markdown。</p>
          </div>
        </div>

        <div class="answer-box">
          <p v-if="!answer" class="muted">暂未提问。</p>
          <template v-else>
            <div class="meta">
              <span class="tag">{{ answer.provider }}</span>
              <span class="tag">{{ answer.model }}</span>
            </div>
            <div class="rendered-answer" v-html="renderedAnswer"></div>
            <div class="references" v-if="answer.references?.length">
              <h4>注入的上下文</h4>
              <ul>
                <li v-for="item in answer.references" :key="`${item.type}-${item.id}-${item.title}`">
                  {{ item.type }} / {{ item.title }}
                </li>
              </ul>
            </div>
          </template>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { askAssistant, fetchProjects } from "../api/modules";
import { renderAssistantContent } from "../utils/richText";

const projects = ref([]);
const projectId = ref("");
const message = ref("根据当前统一数据底座中的项目进展情况，帮我总结本阶段的重点工作和潜在风险。");
const answer = ref(null);
const loading = ref(false);
const error = ref("");

const renderedAnswer = computed(() => renderAssistantContent(answer.value?.content || ""));

onMounted(async () => {
  projects.value = await fetchProjects();
});

const submitQuestion = async () => {
  loading.value = true;
  error.value = "";
  try {
    answer.value = await askAssistant({
      message: message.value,
      projectId: projectId.value || null
    });
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || "请求失败，请检查 Ollama 是否正在运行。";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.view {
  display: grid;
  gap: 20px;
}

.field {
  display: grid;
  gap: 8px;
  margin-bottom: 16px;
}

.field span {
  font-weight: 700;
}

.answer-box {
  display: grid;
  gap: 14px;
}

.meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.rendered-answer {
  padding: 16px;
  border-radius: 14px;
  background: #f6faf8;
  border: 1px solid #d7e1de;
  line-height: 1.75;
  color: #21302a;
}

.rendered-answer :deep(h2),
.rendered-answer :deep(h3),
.rendered-answer :deep(p),
.rendered-answer :deep(ul),
.rendered-answer :deep(ol) {
  margin: 0 0 12px;
}

.rendered-answer :deep(ul),
.rendered-answer :deep(ol) {
  padding-left: 20px;
}

.rendered-answer :deep(code) {
  padding: 2px 6px;
  border-radius: 6px;
  background: #e7f1ed;
}

.references h4 {
  margin: 0 0 8px;
}

.references ul {
  margin: 0;
  padding-left: 18px;
}

.hint {
  margin-top: 8px;
  font-size: 13px;
}

.error-msg {
  margin-top: 8px;
  padding: 12px;
  border-radius: 8px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  font-size: 13px;
}
</style>

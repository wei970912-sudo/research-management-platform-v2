<template>
  <section class="stakeholder-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">协同关系</p>
        <h1>干系人管理</h1>
        <p class="subtext">独立维护项目干系人、关联工作和主要协同内容，支持从入口直接进入管理。</p>
      </div>
      <div class="header-actions">
        <RouterLink class="secondary nav-link" to="/portal">返回入口</RouterLink>
        <button class="secondary" type="button" @click="loadAll">刷新数据</button>
        <button class="primary" type="button" :disabled="!canEdit" @click="openDialog()">新增干系人</button>
      </div>
    </header>

    <section class="toolbar-card">
      <label class="field compact">
        <span>项目筛选</span>
        <select v-model="selectedProjectId">
          <option value="">全部项目</option>
          <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
        </select>
      </label>
      <label class="field search-field">
        <span>关键词</span>
        <input v-model.trim="keyword" type="search" placeholder="搜索干系人、工作或关联内容" />
      </label>
    </section>

    <section class="stat-grid">
      <article class="stat-card">
        <span>干系人总数</span>
        <strong>{{ filteredStakeholders.length }}</strong>
      </article>
      <article class="stat-card">
        <span>涉及项目</span>
        <strong>{{ relatedProjectCount }}</strong>
      </article>
      <article class="stat-card">
        <span>按工作关联</span>
        <strong>{{ phaseLinkedCount }}</strong>
      </article>
      <article class="stat-card">
        <span>手工 / 其他</span>
        <strong>{{ manualLinkedCount }}</strong>
      </article>
    </section>

    <p v-if="message" class="page-message">{{ message }}</p>

    <section class="panel">
      <div v-if="!filteredStakeholders.length" class="empty-state">当前筛选条件下暂无干系人记录。</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>项目</th>
            <th>干系人</th>
            <th>关联方式</th>
            <th>涉及工作 / 事项</th>
            <th>主要关联内容</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredStakeholders" :key="item.id">
            <td>{{ item.projectName }}</td>
            <td>{{ item.stakeholderName }}</td>
            <td>{{ relatedWorkTypeLabel(item.relatedWorkType) }}</td>
            <td>{{ item.relatedWorkValue }}</td>
            <td class="content-cell">{{ item.mainContent }}</td>
            <td>
              <button class="link-btn" type="button" :disabled="!canEdit" @click="openDialog(item)">编辑</button>
              <button class="link-btn danger" type="button" :disabled="!canEdit" @click="removeStakeholderRecord(item)">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <div v-if="dialogOpen" class="modal-overlay" @click.self="closeDialog">
      <div class="modal-card">
        <form class="record-form" @submit.prevent="saveStakeholderRecord">
          <div class="form-head">
            <div>
              <p class="eyebrow">干系人</p>
              <h2>{{ editingId ? "编辑干系人" : "新增干系人" }}</h2>
            </div>
            <button class="ghost" type="button" @click="closeDialog">取消</button>
          </div>

          <label class="field">
            <span>所属项目</span>
            <select v-model="form.projectId" required>
              <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
            </select>
          </label>

          <label class="field">
            <span>干系人</span>
            <input v-model.trim="form.stakeholderName" type="text" required />
          </label>

          <label class="field">
            <span>关联方式</span>
            <select v-model="form.relatedWorkType" required>
              <option value="PHASE">按进度工作选择</option>
              <option value="MANUAL">手工填写</option>
              <option value="OTHER">其他</option>
            </select>
          </label>

          <label v-if="form.relatedWorkType === 'PHASE'" class="field">
            <span>涉及工作</span>
            <select v-model="form.relatedWorkValue" required>
              <option v-for="phase in phaseOptions" :key="phase.id" :value="phase.name">{{ phase.name }}</option>
            </select>
          </label>

          <label v-else class="field">
            <span>{{ form.relatedWorkType === "MANUAL" ? "涉及工作" : "其他关联项" }}</span>
            <input v-model.trim="form.relatedWorkValue" type="text" required />
          </label>

          <label class="field">
            <span>主要关联内容</span>
            <textarea v-model.trim="form.mainContent" rows="5" required></textarea>
          </label>

          <p v-if="formError" class="form-error">{{ formError }}</p>
          <button class="primary full" type="submit" :disabled="saving || !canEdit">
            {{ saving ? "保存中..." : "保存" }}
          </button>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { RouterLink } from "vue-router";
import {
  createStakeholder,
  deleteStakeholder,
  fetchPhases,
  fetchProjects,
  fetchStakeholders,
  updateStakeholder
} from "../api/modules";
import { isAdmin } from "../auth";

const projects = ref([]);
const phases = ref([]);
const stakeholders = ref([]);
const selectedProjectId = ref("");
const keyword = ref("");
const dialogOpen = ref(false);
const editingId = ref("");
const saving = ref(false);
const formError = ref("");
const message = ref("");

const form = reactive({
  projectId: "",
  stakeholderName: "",
  relatedWorkType: "PHASE",
  relatedWorkValue: "",
  mainContent: ""
});

const canEdit = computed(() => isAdmin());

const filteredStakeholders = computed(() =>
  stakeholders.value.filter((item) => {
    const projectMatched = !selectedProjectId.value || item.projectId === selectedProjectId.value;
    const keywordMatched =
      !keyword.value ||
      [item.projectName, item.stakeholderName, item.relatedWorkValue, item.mainContent].join(" ").includes(keyword.value);
    return projectMatched && keywordMatched;
  })
);

const phaseOptions = computed(() => phases.value.filter((phase) => phase.projectId === form.projectId));
const relatedProjectCount = computed(() => new Set(filteredStakeholders.value.map((item) => item.projectId)).size);
const phaseLinkedCount = computed(() => filteredStakeholders.value.filter((item) => item.relatedWorkType === "PHASE").length);
const manualLinkedCount = computed(() => filteredStakeholders.value.filter((item) => item.relatedWorkType !== "PHASE").length);

const loadAll = async () => {
  try {
    const [projectData, phaseData, stakeholderData] = await Promise.all([fetchProjects(), fetchPhases(), fetchStakeholders()]);
    projects.value = projectData;
    phases.value = phaseData;
    stakeholders.value = stakeholderData;
    if (selectedProjectId.value && !projects.value.some((item) => item.id === selectedProjectId.value)) {
      selectedProjectId.value = "";
    }
    message.value = "";
  } catch (error) {
    message.value = error?.response?.data?.message || "加载干系人数据失败，请稍后重试。";
  }
};

const openDialog = (item = null) => {
  dialogOpen.value = true;
  editingId.value = item?.id || "";
  form.projectId = item?.projectId || selectedProjectId.value || projects.value[0]?.id || "";
  form.stakeholderName = item?.stakeholderName || "";
  form.relatedWorkType = item?.relatedWorkType || "PHASE";
  form.relatedWorkValue = item?.relatedWorkValue || "";
  form.mainContent = item?.mainContent || "";
  formError.value = "";
  if (form.relatedWorkType === "PHASE" && !form.relatedWorkValue) {
    form.relatedWorkValue = phaseOptions.value[0]?.name || "";
  }
};

const closeDialog = () => {
  dialogOpen.value = false;
  editingId.value = "";
  formError.value = "";
};

const saveStakeholderRecord = async () => {
  saving.value = true;
  formError.value = "";
  try {
    const payload = {
      ...form,
      relatedWorkValue:
        form.relatedWorkType === "PHASE" ? form.relatedWorkValue || phaseOptions.value[0]?.name || "" : form.relatedWorkValue
    };
    if (editingId.value) {
      await updateStakeholder(editingId.value, payload);
    } else {
      await createStakeholder(payload);
    }
    closeDialog();
    await loadAll();
  } catch (error) {
    formError.value = error?.response?.data?.message || "保存失败，请检查输入后重试。";
  } finally {
    saving.value = false;
  }
};

const removeStakeholderRecord = async (item) => {
  if (!window.confirm(`确认删除干系人“${item.stakeholderName}”吗？`)) return;
  await deleteStakeholder(item.id);
  await loadAll();
};

const relatedWorkTypeLabel = (type) => {
  if (type === "PHASE") return "按进度工作";
  if (type === "MANUAL") return "手工填写";
  return "其他";
};

onMounted(loadAll);
</script>

<style scoped>
.stakeholder-page{min-height:100vh;padding:28px;background:#f5f7fb;color:#18201d}
.page-header,.header-actions,.toolbar-card,.grid-two,.form-head{display:flex;gap:12px}
.page-header,.form-head{justify-content:space-between;align-items:flex-start}
.header-actions,.toolbar-card{align-items:end;flex-wrap:wrap}
.page-header{margin-bottom:20px}
.eyebrow{margin:0 0 6px;color:#1f7a5a;font-size:12px;font-weight:700;text-transform:uppercase}
h1,h2,p{margin:0}
h1{font-size:30px}
h2{font-size:22px}
.subtext{margin-top:8px;color:#63706a}
.toolbar-card,.panel,.stat-card{background:#fff;border:1px solid #dfe6e2;border-radius:12px;box-shadow:0 12px 28px rgba(31,55,48,.08)}
.toolbar-card{padding:18px;margin-bottom:18px}
.field{display:grid;gap:8px;color:#63706a;font-size:13px;font-weight:700}
.field input,.field select,.field textarea{width:100%;border:1px solid #dfe6e2;border-radius:10px;padding:11px 12px;background:#fff}
.compact{width:240px}
.search-field{min-width:280px}
.stat-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px;margin-bottom:18px}
.stat-card{display:grid;gap:8px;padding:18px}
.stat-card span{color:#63706a;font-size:13px;font-weight:700}
.stat-card strong{font-size:24px}
.panel{padding:20px}
.data-table{width:100%;border-collapse:collapse}
.data-table th,.data-table td{padding:14px 12px;border-bottom:1px solid #e7ece9;text-align:left;vertical-align:top}
.data-table th{color:#63706a;font-size:13px;font-weight:700}
.content-cell{min-width:280px;white-space:pre-wrap}
.primary,.secondary,.ghost{border-radius:10px;padding:10px 14px;font-weight:700}
.primary{border:0;background:#1f7a5a;color:#fff}
.secondary,.ghost{border:1px solid #dfe6e2;background:#fff;color:#18201d}
.nav-link{text-decoration:none}
.link-btn{border:0;background:transparent;padding:0;color:#1f7a5a;font-weight:700}
.link-btn.danger{color:#b53232}
.page-message{margin:0 0 16px;color:#b53232;font-weight:700}
.empty-state{display:grid;place-items:center;padding:28px;border:1px dashed #dfe6e2;border-radius:12px;color:#63706a;background:#fafbfc}
.modal-overlay{position:fixed;inset:0;display:grid;place-items:center;padding:24px;background:rgba(24,32,29,.34);z-index:999}
.modal-card{width:min(560px,calc(100vw - 36px));max-height:calc(100vh - 48px);overflow:auto;background:#fff;border:1px solid #dfe6e2;border-radius:16px;box-shadow:0 20px 48px rgba(31,55,48,.24);padding:22px}
.record-form{display:grid;gap:14px}
.form-error{margin:0;color:#b53232;font-size:13px}
.full{width:100%}
@media (max-width:960px){
  .stat-grid{grid-template-columns:repeat(2,minmax(0,1fr))}
}
@media (max-width:760px){
  .stakeholder-page{padding:18px}
  .page-header,.toolbar-card,.form-head,.grid-two{flex-direction:column}
  .stat-grid{grid-template-columns:1fr}
  .compact,.search-field{width:100%;min-width:unset}
}
</style>

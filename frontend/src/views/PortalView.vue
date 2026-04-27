<template>
  <section class="dashboard-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">科研管理平台</p>
        <h1>科研管理工具</h1>
      </div>
      <div class="top-actions">
        <button class="secondary" type="button" @click="openProjectForm">新增项目</button>
        <button class="primary" type="button" @click="openEventForm(selectedDate)">新增提醒</button>
      </div>
    </header>

    <main class="dashboard">
      <section class="module-view">
        <div class="section-head">
          <div>
            <h2>程序入口</h2>
            <p>选择需要处理的科研管理事项</p>
          </div>
          <span class="status-pill">可扩展模块</span>
        </div>

        <div class="module-grid">
          <RouterLink v-for="item in modules" :key="item.to" :to="item.to" class="module-card">
            <div class="module-icon">{{ item.icon }}</div>
            <div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </div>
            <div class="module-footer">
              <span class="module-path">{{ item.to }}</span>
              <span class="badge">{{ item.status }}</span>
            </div>
          </RouterLink>
        </div>

        <div class="assistant-strip">
          <div class="assistant-copy">
            <p class="eyebrow">智能助手</p>
            <h3>科研助手</h3>
            <p>
              基于 OpenAI 兼容接口连接本地 Ollama 模型，并自动注入统一数据库中的项目、会议、
              合同、节点与里程碑上下文。
            </p>
          </div>
          <div class="assistant-entry">
            <label class="assistant-field">
              <span>项目上下文</span>
              <select v-model="assistantProjectId">
                <option value="">全局上下文</option>
                <option v-for="item in projects" :key="item.id" :value="item.id">{{ item.name }}</option>
              </select>
            </label>
            <label class="assistant-field">
              <span>问题</span>
              <textarea
                v-model.trim="assistantMessage"
                rows="3"
                placeholder="例如：根据当前项目进展，帮我总结本周重点工作和风险。"
              ></textarea>
            </label>
            <div class="assistant-actions">
              <button class="primary" type="button" :disabled="assistantLoading || !assistantMessage" @click="askNow">
                {{ assistantLoading ? "正在生成" : "立即提问" }}
              </button>
              <RouterLink class="secondary assistant-link" to="/assistant">打开完整助手页</RouterLink>
            </div>
            <div v-if="assistantAnswer" class="assistant-answer" v-html="renderedAssistantAnswer"></div>
          </div>
        </div>
      </section>

      <aside class="side-view">
        <section class="panel">
          <div class="panel-head">
            <div>
              <p class="eyebrow">进行中项目</p>
              <h2>在执行项目</h2>
            </div>
          </div>
          <div class="project-list">
            <article v-for="project in projects" :key="project.id" class="project-item">
              <header>
                <div>
                  <h3>{{ project.name }}</h3>
                  <p class="project-meta">{{ project.owner || "未填写负责人" }} / {{ project.currentStage || project.status }}</p>
                </div>
                <span class="badge">{{ project.status }}</span>
              </header>
              <p>{{ project.description || "暂无备注" }}</p>
              <div class="project-actions">
                <RouterLink class="link-btn" :to="`/projects/${project.id}`">详情</RouterLink>
                <button class="link-btn danger-link" type="button" @click="removeProject(project.id)">删除</button>
              </div>
            </article>
            <p v-if="!projects.length" class="empty">暂无在执行项目。</p>
          </div>
        </section>

        <section class="panel">
          <div class="panel-head">
            <div>
              <p class="eyebrow">日历提醒</p>
              <h2>重要日历</h2>
            </div>
            <div class="calendar-actions">
              <button class="ghost square" type="button" @click="shiftMonth(-1)">‹</button>
              <button class="ghost square" type="button" @click="shiftMonth(1)">›</button>
            </div>
          </div>

          <div class="month-title">{{ monthTitle }}</div>
          <div class="weekday-row" aria-hidden="true">
            <span>一</span>
            <span>二</span>
            <span>三</span>
            <span>四</span>
            <span>五</span>
            <span>六</span>
            <span>日</span>
          </div>
          <div class="calendar-grid">
            <button
              v-for="date in calendarDays"
              :key="date.value"
              class="day-cell"
              :class="{
                muted: !date.inCurrentMonth,
                today: date.value === today,
                active: date.value === selectedDate
              }"
              type="button"
              @click="selectDate(date)"
            >
              <span>{{ date.day }}</span>
              <span v-if="hasEvent(date.value)" class="event-dot"></span>
            </button>
          </div>

          <div class="event-panel">
            <div class="event-panel-head">
              <h3>{{ selectedDateTitle }}</h3>
              <button class="link-btn" type="button" @click="openEventForm(selectedDate)">添加</button>
            </div>
            <div class="event-list">
              <article v-for="item in selectedDateEvents" :key="item.id" class="event-item">
                <header>
                  <div>
                    <h3>{{ item.title }}</h3>
                    <p class="event-meta">{{ item.type }} / {{ item.time || "全天" }}</p>
                  </div>
                  <span class="badge">{{ item.projectName }}</span>
                </header>
                <p>{{ item.note || "暂无说明" }}</p>
              </article>
              <p v-if="!selectedDateEvents.length" class="empty">当天暂无事项。</p>
            </div>
          </div>
        </section>
      </aside>
    </main>

    <Teleport to="body">
      <div v-if="projectDialog" class="modal-overlay" @click.self="closeProjectForm">
        <div class="modal-card">
          <form class="record-form" @submit.prevent="saveProject">
            <div class="form-head">
              <div>
                <p class="eyebrow">项目</p>
                <h2>项目记录</h2>
              </div>
              <button class="ghost" type="button" @click="closeProjectForm">取消</button>
            </div>
            <label>
              <span>项目名称</span>
              <input v-model.trim="projectForm.name" type="text" required />
            </label>
            <label>
              <span>项目阶段</span>
              <input v-model.trim="projectForm.currentStage" type="text" required />
            </label>
            <label>
              <span>负责人</span>
              <input v-model.trim="projectForm.owner" type="text" required />
            </label>
            <label>
              <span>备注</span>
              <textarea v-model.trim="projectForm.description" rows="4" required></textarea>
            </label>
            <p v-if="projectSaveError" class="form-error">{{ projectSaveError }}</p>
            <button class="primary full" type="submit" :disabled="projectSaving">
              {{ projectSaving ? "保存中..." : "保存项目" }}
            </button>
          </form>
        </div>
      </div>

      <div v-if="eventDialog" class="modal-overlay" @click.self="closeEventForm">
        <div class="modal-card">
          <form class="record-form" @submit.prevent="saveEvent">
            <div class="form-head">
              <div>
                <p class="eyebrow">提醒</p>
                <h2>日历提醒</h2>
              </div>
              <button class="ghost" type="button" @click="closeEventForm">取消</button>
            </div>
            <label>
              <span>所属项目</span>
              <select v-model="eventForm.projectId" required>
                <option v-for="item in projects" :key="item.id" :value="item.id">{{ item.name }}</option>
              </select>
            </label>
            <label>
              <span>日期</span>
              <input v-model="eventForm.date" type="date" required />
            </label>
            <label>
              <span>时间</span>
              <input v-model="eventForm.time" type="time" />
            </label>
            <label>
              <span>事项类型</span>
              <select v-model="eventForm.type" required>
                <option value="会议提醒">会议提醒</option>
                <option value="材料提交">材料提交</option>
                <option value="合同节点">合同节点</option>
                <option value="关键节点">关键节点</option>
                <option value="其他事项">其他事项</option>
              </select>
            </label>
            <label>
              <span>事项内容</span>
              <input v-model.trim="eventForm.title" type="text" required />
            </label>
            <label>
              <span>说明</span>
              <textarea v-model.trim="eventForm.note" rows="3"></textarea>
            </label>
            <label>
              <span>提前提醒</span>
              <select v-model.number="eventForm.remindBefore">
                <option :value="0">当天提醒</option>
                <option :value="1">提前 1 天</option>
                <option :value="3">提前 3 天</option>
                <option :value="7">提前 7 天</option>
              </select>
            </label>
            <p v-if="eventSaveError" class="form-error">{{ eventSaveError }}</p>
            <button class="primary full" type="submit" :disabled="eventSaving">
              {{ eventSaving ? "保存中..." : "保存提醒" }}
            </button>
          </form>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  askAssistant,
  createCalendarEvent,
  createProject,
  deleteProject,
  fetchCalendarEvents,
  fetchDocuments,
  fetchProjects
} from "../api/modules";
import { renderAssistantContent } from "../utils/richText";

const modules = [
  { to: "/dashboard", title: "科研管理总览", description: "汇聚各模块数据，展示核心任务指标和提醒。", icon: "总", status: "已接入" },
  { to: "/meetings", title: "会议记录", description: "记录会议纪要、参会人员和过程说明。", icon: "会", status: "已接入" },
  { to: "/contracts", title: "合同金额管理", description: "管理任务书金额、实际合同和签订状态。", icon: "合", status: "已接入" },
  { to: "/schedule", title: "进度里程碑", description: "跟踪阶段、控制线、里程碑节点和版本。", icon: "进", status: "已接入" },
  { to: "/stakeholders", title: "干系人管理", description: "维护项目干系人、关联工作和主要关联内容。", icon: "干", status: "已接入" },
  { to: "/documents", title: "节点文件管理", description: "归档节点材料、附件清单和归档备注。", icon: "档", status: "已接入" }
];

const projects = ref([]);
const events = ref([]);
const documents = ref([]);
const currentMonth = ref(startOfMonth(new Date()));
const today = toDateInputValue(new Date());
const selectedDate = ref(today);
const assistantProjectId = ref("");
const assistantMessage = ref("根据当前统一数据库，帮我总结本周重点工作和风险。");
const assistantAnswer = ref(null);
const assistantLoading = ref(false);

const projectDialog = ref(false);
const projectSaving = ref(false);
const projectSaveError = ref("");
const eventDialog = ref(false);
const eventSaving = ref(false);
const eventSaveError = ref("");

const projectForm = reactive({
  name: "",
  owner: "",
  status: "进行中",
  currentStage: "执行中",
  milestoneCompletionRate: 0,
  description: ""
});

const eventForm = reactive({
  projectId: "",
  title: "",
  date: today,
  time: "",
  type: "会议提醒",
  remindBefore: 0,
  note: ""
});

const load = async () => {
  const [projectData, eventData, documentData] = await Promise.all([
    fetchProjects(),
    fetchCalendarEvents(),
    fetchDocuments()
  ]);
  projects.value = projectData;
  events.value = eventData;
  documents.value = documentData;
  assistantProjectId.value = assistantProjectId.value || projects.value[0]?.id || "";
  eventForm.projectId = eventForm.projectId || projects.value[0]?.id || "";
};

const allCalendarEntries = computed(() => [
  ...events.value,
  ...documents.value.map((item) => ({
    id: `doc-${item.id}`,
    title: item.nodeName,
    date: item.targetDate,
    time: "",
    type: "归档节点",
    projectName: item.projectName,
    note: item.notes
  }))
]);

const renderedAssistantAnswer = computed(() => renderAssistantContent(assistantAnswer.value?.content || ""));
const monthTitle = computed(() =>
  currentMonth.value.toLocaleDateString("zh-CN", { year: "numeric", month: "long" })
);

const calendarDays = computed(() => {
  const firstDay = startOfMonth(currentMonth.value);
  const start = new Date(firstDay);
  const mondayIndex = (firstDay.getDay() + 6) % 7;
  start.setDate(firstDay.getDate() - mondayIndex);
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start);
    date.setDate(start.getDate() + index);
    return {
      value: toDateInputValue(date),
      day: date.getDate(),
      inCurrentMonth: date.getMonth() === currentMonth.value.getMonth()
    };
  });
});

const selectedDateEvents = computed(() =>
  allCalendarEntries.value
    .filter((item) => item.date === selectedDate.value)
    .sort((a, b) => (a.time || "99:99").localeCompare(b.time || "99:99"))
);

const selectedDateTitle = computed(() => `${formatDate(selectedDate.value)} 事项`);

const openProjectForm = () => {
  projectSaveError.value = "";
  projectDialog.value = true;
};

const closeProjectForm = () => {
  projectDialog.value = false;
  projectSaveError.value = "";
};

const saveProject = async () => {
  projectSaving.value = true;
  projectSaveError.value = "";
  try {
    await createProject({ ...projectForm });
    projectDialog.value = false;
    projectForm.name = "";
    projectForm.owner = "";
    projectForm.status = "进行中";
    projectForm.currentStage = "执行中";
    projectForm.milestoneCompletionRate = 0;
    projectForm.description = "";
    await load();
  } catch (error) {
    projectSaveError.value = error?.response?.data?.message || "项目保存失败，请检查必填项后重试。";
  } finally {
    projectSaving.value = false;
  }
};

const removeProject = async (projectId) => {
  await deleteProject(projectId);
  await load();
};

const openEventForm = (dateText) => {
  eventSaveError.value = "";
  eventForm.projectId = eventForm.projectId || projects.value[0]?.id || "";
  eventForm.date = dateText || today;
  eventDialog.value = true;
};

const closeEventForm = () => {
  eventDialog.value = false;
  eventSaveError.value = "";
};

const saveEvent = async () => {
  eventSaving.value = true;
  eventSaveError.value = "";
  try {
    await createCalendarEvent({ ...eventForm });
    eventDialog.value = false;
    eventForm.projectId = projects.value[0]?.id || "";
    eventForm.title = "";
    eventForm.date = today;
    eventForm.time = "";
    eventForm.type = "会议提醒";
    eventForm.remindBefore = 0;
    eventForm.note = "";
    await load();
  } catch (error) {
    eventSaveError.value = error?.response?.data?.message || "提醒保存失败，请检查必填项后重试。";
  } finally {
    eventSaving.value = false;
  }
};

const askNow = async () => {
  assistantLoading.value = true;
  try {
    assistantAnswer.value = await askAssistant({
      message: assistantMessage.value,
      projectId: assistantProjectId.value || null
    });
  } catch (error) {
    assistantAnswer.value = {
      content: `请求失败，请检查 Ollama 是否正在运行。\n\n错误信息：${error?.message || "未知错误"}`
    };
  } finally {
    assistantLoading.value = false;
  }
};

const shiftMonth = (offset) => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + offset, 1);
};

const selectDate = (date) => {
  selectedDate.value = date.value;
  if (!date.inCurrentMonth) {
    currentMonth.value = startOfMonth(new Date(`${date.value}T00:00:00`));
  }
};

const hasEvent = (dateText) => allCalendarEntries.value.some((item) => item.date === dateText);

onMounted(load);

function startOfMonth(date) {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function toDateInputValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function formatDate(dateText) {
  const date = new Date(`${dateText}T00:00:00`);
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    weekday: "short"
  });
}
</script>

<style scoped>
.dashboard-page{min-height:100vh;background:#f7f8fb}
.topbar,.section-head,.panel-head,.form-head{display:flex;align-items:flex-start;justify-content:space-between;gap:18px}
.topbar{padding:28px 40px 20px;background:#fff;border-bottom:1px solid #dfe6e2}
.top-actions,.calendar-actions,.event-panel-head,.project-actions,.assistant-actions{display:flex;align-items:center;gap:10px;flex-wrap:wrap}
.dashboard{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(360px,.85fr);gap:24px;padding:24px 40px 40px}
.module-view,.panel{background:#fff;border:1px solid #dfe6e2;border-radius:8px;box-shadow:0 16px 36px rgba(31,55,48,.12)}
.module-view{min-height:calc(100vh - 132px);overflow:hidden}
.section-head{align-items:end;padding:22px;border-bottom:1px solid #dfe6e2}
.section-head p,.project-item p,.event-item p,.empty,.module-card p{color:#63706a}
.status-pill,.badge{display:inline-flex;align-items:center;min-height:28px;border-radius:8px;padding:4px 9px;background:#e8f1ff;color:#22518f;font-weight:700;font-size:13px}
.module-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px;padding:22px}
.module-card{display:grid;gap:14px;min-height:210px;padding:20px;border:1px solid #dfe6e2;border-radius:8px;background:#fff;transition:border .18s ease,background .18s ease,transform .18s ease;text-decoration:none;color:inherit}
.module-card:hover{border-color:#1f7a5a;background:#eef6f2;transform:translateY(-2px)}
.module-icon{display:grid;place-items:center;width:46px;height:46px;border-radius:8px;background:#f4f7f5;color:#155d43;font-size:24px;font-weight:800}
.module-footer{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-top:auto}
.module-path{color:#63706a;font-size:13px}
.assistant-strip{display:grid;grid-template-columns:minmax(0,.85fr) minmax(360px,1.15fr);gap:18px;padding:0 22px 22px}
.assistant-copy,.assistant-entry{padding:18px;border:1px solid #dfe6e2;border-radius:8px;background:#fff}
.assistant-copy p:last-child{margin-top:10px;color:#63706a;line-height:1.7}
.assistant-field{display:grid;gap:8px;margin-bottom:12px;color:#63706a;font-size:13px;font-weight:700}
.assistant-field select,.assistant-field textarea,.modal-card input,.modal-card select,.modal-card textarea{width:100%;border:1px solid #dfe6e2;border-radius:8px;padding:11px 12px;background:#fff}
.assistant-link{display:inline-flex;align-items:center;justify-content:center;text-decoration:none}
.assistant-answer{margin-top:12px;padding:12px;border-radius:8px;background:#f4f7f5;color:#2b3732;line-height:1.7}
.assistant-answer :deep(h2),.assistant-answer :deep(h3),.assistant-answer :deep(p),.assistant-answer :deep(ul),.assistant-answer :deep(ol){margin:0 0 12px}
.assistant-answer :deep(ul),.assistant-answer :deep(ol){padding-left:18px}
.side-view{display:grid;gap:24px;align-content:start}
.panel{padding:22px}
.project-list,.event-list{display:grid;gap:10px;margin-top:18px}
.project-item,.event-item{display:grid;gap:8px;padding:13px;border:1px solid #dfe6e2;border-radius:8px;background:#fff}
.project-item header,.event-item header{display:flex;align-items:flex-start;justify-content:space-between;gap:12px}
.project-meta,.event-meta{color:#63706a;font-size:13px}
.month-title{margin:18px 0 12px;font-size:18px;font-weight:800}
.weekday-row,.calendar-grid{display:grid;grid-template-columns:repeat(7,minmax(0,1fr));gap:6px}
.weekday-row{margin-bottom:6px;color:#63706a;font-size:13px;font-weight:700;text-align:center}
.day-cell{position:relative;display:grid;place-items:start center;min-height:48px;border:1px solid #dfe6e2;border-radius:8px;padding:7px 4px;background:#fff;color:#18201d}
.day-cell:hover,.day-cell.active{border-color:#1f7a5a;background:#eef6f2}
.day-cell.muted{color:#a1aaa5;background:#fafbfc}
.day-cell.today{color:#fff;background:#1f7a5a;border-color:#1f7a5a;font-weight:800}
.event-dot{width:7px;height:7px;margin-top:5px;border-radius:999px;background:#9c6428}
.event-panel{margin-top:18px;padding-top:18px;border-top:1px solid #dfe6e2}
.primary,.secondary,.ghost{border-radius:8px;padding:10px 15px;font-weight:700}
.primary{border:0;background:#1f7a5a;color:#fff}
.secondary,.ghost{border:1px solid #dfe6e2;background:#fff;color:#18201d}
.ghost.square{display:grid;place-items:center;width:34px;height:34px;padding:0;font-size:22px}
.link-btn{padding:0;background:transparent;color:#1f7a5a;font-weight:800;border:0;text-decoration:none}
.danger-link{color:#b53232}
.modal-overlay{position:fixed;inset:0;display:flex;align-items:center;justify-content:center;padding:24px;background:rgba(24,32,29,.34);z-index:9999}
.modal-card{width:min(560px,calc(100vw - 36px));max-height:calc(100vh - 48px);overflow:auto;background:#fff;border:1px solid #dfe6e2;border-radius:12px;box-shadow:0 20px 48px rgba(31,55,48,.24);padding:22px}
.record-form{display:grid;gap:15px}
.record-form label{display:grid;gap:8px;color:#63706a;font-size:13px;font-weight:700}
.form-error{margin:0;color:#b53232;font-size:13px;line-height:1.6}
.full{width:100%}
h1,h2,h3,p{margin:0}
h1{margin-top:4px;font-size:30px}
h2{font-size:22px}
h3{font-size:16px}
.eyebrow{color:#1f7a5a;font-size:12px;font-weight:700;text-transform:uppercase}
@media (max-width:1100px){.dashboard{grid-template-columns:1fr;padding:18px}.topbar{padding:22px 18px 16px}.module-view{min-height:auto}}
@media (max-width:720px){.topbar,.section-head,.panel-head,.form-head,.top-actions,.assistant-actions{align-items:stretch;flex-direction:column}.module-grid,.assistant-strip{grid-template-columns:1fr}h1{font-size:26px}}
</style>

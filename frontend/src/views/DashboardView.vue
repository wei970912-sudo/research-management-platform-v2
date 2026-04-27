<template>
  <section class="overview-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">Research Overview</p>
        <h1>科研管理总览</h1>
      </div>
      <div class="top-actions">
        <RouterLink class="secondary nav-link" to="/portal">返回工具入口</RouterLink>
        <button class="secondary" type="button" @click="load">刷新数据</button>
      </div>
    </header>

    <main class="overview-layout">
      <section class="summary-view">
        <div class="section-head">
          <div>
            <h2>项目总览</h2>
            <p>{{ snapshotMeta }}</p>
          </div>
          <label class="search-box compact">
            <span>查看项目</span>
            <select v-model="selectedProjectId">
              <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
            </select>
          </label>
        </div>

        <div class="stat-grid core-stat-grid">
          <article class="stat-card">
            <span>当前项目进度</span>
            <strong>{{ overallProgress.percent }}%</strong>
            <em>{{ overallProgress.done }}/{{ overallProgress.total }} 个指标成果已完成</em>
          </article>
          <article class="stat-card">
            <span>控制线数量</span>
            <strong>{{ selectedControlLines.length }}</strong>
            <em>{{ selectedPhases.length }} 项工作，{{ selectedMilestones.length }} 个里程碑</em>
          </article>
          <article class="stat-card">
            <span>合同执行金额</span>
            <strong>{{ formatMoney(contractScope.usedAmount) }}</strong>
            <em>总金额 {{ formatMoney(contractScope.totalAmount) }}</em>
          </article>
          <article class="stat-card">
            <span>提醒与归档</span>
            <strong>{{ selectedCalendarEvents.length + selectedDocuments.length }}</strong>
            <em>{{ selectedCalendarEvents.length }} 条提醒，{{ selectedDocuments.length }} 个节点</em>
          </article>
        </div>

        <section class="block">
          <div class="block-head">
            <div>
              <p class="eyebrow">Progress Control</p>
              <h2>进度与控制线</h2>
            </div>
            <RouterLink class="link-btn" to="/schedule">进入进度管理</RouterLink>
          </div>

          <div class="overview-progress">
            <div>
              <span>整体控制线进度</span>
              <strong>{{ overallProgress.percent }}%</strong>
              <p>总览页与进度页使用同一套控制线和指标成果数据源，按已完成成果 / 总成果计算。</p>
            </div>
            <div class="progress-track"><i :style="{ width: `${overallProgress.percent}%` }"></i></div>
          </div>

          <div class="indicator-grid">
            <article v-for="line in selectedControlLines" :key="line.id" class="indicator-card">
              <header>
                <div>
                  <h3>{{ line.name }}</h3>
                  <p class="meta-line">{{ line.owner || "未填写负责人" }} / {{ formatDate(line.targetDate) }}</p>
                </div>
                <span class="badge" :class="line.progressPercent === 100 ? 'done' : 'pending'">{{ line.progressPercent }}%</span>
              </header>
              <p>{{ line.note || "暂无说明" }}</p>
              <div class="progress-track"><i :style="{ width: `${line.progressPercent}%` }"></i></div>
              <p>{{ line.completedControlPoints }}/{{ line.totalControlPoints }} 个指标成果已完成</p>
            </article>
            <p v-if="!selectedControlLines.length" class="empty">当前项目暂无控制线数据。</p>
          </div>

          <details class="detail-collapse">
            <summary>展开工作与里程碑明细（{{ selectedPhases.length }} 项工作 / {{ selectedMilestones.length }} 个里程碑）</summary>
            <div class="detail-stack">
              <div class="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>工作</th>
                      <th>负责人</th>
                      <th>开始</th>
                      <th>结束</th>
                      <th>状态</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="phase in selectedPhases" :key="phase.id">
                      <td class="main-cell"><strong>{{ phase.name }}</strong><span>{{ selectedProject?.name }}</span></td>
                      <td>{{ phase.owner || "未填写" }}</td>
                      <td>{{ formatDate(phase.startDate) }}</td>
                      <td>{{ formatDate(phase.endDate) }}</td>
                      <td><span class="badge" :class="getStatusClass(phase.status)">{{ phase.status }}</span></td>
                    </tr>
                    <tr v-if="!selectedPhases.length">
                      <td colspan="5" class="empty">暂无工作数据</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>里程碑</th>
                      <th>日期</th>
                      <th>状态</th>
                      <th>关联工作</th>
                      <th>类型</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in selectedMilestones" :key="item.id">
                      <td class="main-cell"><strong>{{ item.name }}</strong><span>{{ selectedProject?.name }}</span></td>
                      <td>{{ formatDate(item.date) }}</td>
                      <td>{{ item.status }}</td>
                      <td>{{ phaseName(item.phaseId) }}</td>
                      <td>{{ item.critical ? "关键节点" : "普通节点" }}</td>
                    </tr>
                    <tr v-if="!selectedMilestones.length">
                      <td colspan="5" class="empty">暂无里程碑数据</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </details>
        </section>

        <section class="block">
          <div class="block-head">
            <div>
              <p class="eyebrow">Contract Budget</p>
              <h2>合同金额与签订情况</h2>
            </div>
            <RouterLink class="link-btn" to="/contracts">进入合同金额管理</RouterLink>
          </div>

          <div class="stat-grid compact-grid">
            <article class="stat-card">
              <span>总金额</span>
              <strong>{{ formatMoney(contractScope.totalAmount) }}</strong>
            </article>
            <article class="stat-card">
              <span>执行金额</span>
              <strong>{{ formatMoney(contractScope.usedAmount) }}</strong>
            </article>
            <article class="stat-card">
              <span>已签订</span>
              <strong>{{ signedCount }}/{{ selectedContracts.length }}</strong>
            </article>
            <article class="stat-card">
              <span>待推进</span>
              <strong>{{ unsignedCount }} 份</strong>
            </article>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>合同名称</th>
                  <th>任务书项目</th>
                  <th>状态</th>
                  <th>金额</th>
                  <th>签订单位</th>
                  <th>日期</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="contract in selectedContracts" :key="contract.id">
                  <td class="main-cell"><strong>{{ contract.name }}</strong><span>{{ contract.content || "暂无内容" }}</span></td>
                  <td>{{ contract.taskName || "未绑定任务书项目" }}</td>
                  <td><span class="badge" :class="getContractStatusClass(contract.status)">{{ contract.status }}</span></td>
                  <td>{{ formatMoney(contract.amount) }}</td>
                  <td>{{ contract.signingUnit || "未填写" }}</td>
                  <td>{{ formatDate(contract.signingDate) }}</td>
                </tr>
                <tr v-if="!selectedContracts.length">
                  <td colspan="6" class="empty">暂无合同记录</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <aside class="side-view">
        <section class="panel">
          <div class="panel-head">
            <div>
              <p class="eyebrow">Data Foundation</p>
              <h2>模块数据底座</h2>
            </div>
            <span class="status-pill">自动读取</span>
          </div>
          <details class="detail-collapse side-collapse" open>
            <summary>展开模块同步状态（{{ moduleSources.length }} 项）</summary>
            <div class="module-source-list">
              <article v-for="item in moduleSources" :key="item.name" class="source-item">
                <header>
                  <div>
                    <h3>{{ item.name }}</h3>
                    <span>{{ item.storageKey }}</span>
                  </div>
                  <span class="badge" :class="item.ready ? 'done' : 'pending'">{{ item.ready ? "已读取" : "暂无数据" }}</span>
                </header>
                <p>{{ item.summary }}</p>
                <RouterLink :to="item.href">打开模块</RouterLink>
              </article>
            </div>
          </details>
        </section>

        <section class="panel">
          <div class="panel-head">
            <div>
              <p class="eyebrow">Recent Signals</p>
              <h2>近期信息</h2>
            </div>
          </div>
          <details class="detail-collapse side-collapse" open>
            <summary>展开近期信息（{{ recentSignals.length }} 条）</summary>
            <div class="signal-list">
              <article v-for="signal in recentSignals" :key="signal.key" class="signal-item">
                <header>
                  <div>
                    <h3>{{ signal.title }}</h3>
                    <span>{{ signal.module }} / {{ signal.time }}</span>
                  </div>
                  <RouterLink :to="signal.href">查看</RouterLink>
                </header>
                <p>{{ signal.text }}</p>
              </article>
              <p v-if="!recentSignals.length" class="empty">暂无近期信息。</p>
            </div>
          </details>
        </section>
      </aside>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import {
  fetchCalendarEvents,
  fetchContracts,
  fetchControlLines,
  fetchDocuments,
  fetchMeetings,
  fetchMilestones,
  fetchPhases,
  fetchProjects
} from "../api/modules";

const projects = ref([]);
const phases = ref([]);
const milestones = ref([]);
const meetings = ref([]);
const contracts = ref([]);
const documents = ref([]);
const calendarEvents = ref([]);
const controlLines = ref([]);
const selectedProjectId = ref("");
const snapshotMeta = ref("正在读取各模块数据");

const load = async () => {
  const [
    projectData,
    phaseData,
    milestoneData,
    meetingData,
    contractData,
    documentData,
    calendarEventData,
    controlLineData
  ] = await Promise.all([
    fetchProjects(),
    fetchPhases(),
    fetchMilestones(),
    fetchMeetings(),
    fetchContracts(),
    fetchDocuments(),
    fetchCalendarEvents(),
    fetchControlLines()
  ]);
  projects.value = projectData;
  phases.value = phaseData;
  milestones.value = milestoneData;
  meetings.value = meetingData;
  contracts.value = contractData;
  documents.value = documentData;
  calendarEvents.value = calendarEventData;
  controlLines.value = controlLineData;
  selectedProjectId.value = selectedProjectId.value || projects.value[0]?.id || "";
  snapshotMeta.value = `数据读取时间：${new Date().toLocaleString("zh-CN")}`;
};

const selectedProject = computed(() => projects.value.find((project) => project.id === selectedProjectId.value) || null);
const selectedPhases = computed(() => phases.value.filter((item) => item.projectId === selectedProjectId.value));
const selectedMilestones = computed(() => milestones.value.filter((item) => item.projectId === selectedProjectId.value));
const selectedMeetings = computed(() => meetings.value.filter((item) => item.projectId === selectedProjectId.value));
const selectedDocuments = computed(() => documents.value.filter((item) => item.projectId === selectedProjectId.value));
const selectedCalendarEvents = computed(() => calendarEvents.value.filter((item) => item.projectId === selectedProjectId.value));
const selectedContracts = computed(() =>
  contracts.value.filter((item) => item.projectId === selectedProjectId.value)
);
const selectedControlLines = computed(() => controlLines.value.filter((item) => item.projectId === selectedProjectId.value));

const contractScope = computed(() => ({
  totalAmount: selectedContracts.value.reduce((sum, item) => sum + Number(item.taskBudget || 0), 0),
  usedAmount: selectedContracts.value.reduce((sum, item) => sum + Number(item.amount || 0), 0)
}));

const signedCount = computed(() => selectedContracts.value.filter((item) => isSigned(item.status)).length);
const unsignedCount = computed(() => selectedContracts.value.filter((item) => !isSigned(item.status)).length);

const overallProgress = computed(() => {
  const total = selectedControlLines.value.reduce((sum, item) => sum + Number(item.totalControlPoints || 0), 0);
  const done = selectedControlLines.value.reduce((sum, item) => sum + Number(item.completedControlPoints || 0), 0);
  return {
    total,
    done,
    percent: total ? Math.round((done / total) * 100) : selectedProject.value?.milestoneCompletionRate || 0
  };
});

const moduleSources = computed(() => [
  {
    name: "进度管理",
    storageKey: "统一数据底座 / control-lines + phases + milestones",
    href: "/schedule",
    ready: controlLines.value.length + phases.value.length + milestones.value.length > 0,
    summary: `${controlLines.value.length} 条控制线，${phases.value.length} 项工作，${milestones.value.length} 个里程碑`
  },
  {
    name: "合同金额管理",
    storageKey: "统一数据底座 / contracts",
    href: "/contracts",
    ready: contracts.value.length > 0,
    summary: `${contracts.value.length} 份合同`
  },
  {
    name: "会议记录",
    storageKey: "统一数据底座 / meetings",
    href: "/meetings",
    ready: meetings.value.length > 0,
    summary: `${meetings.value.length} 条会议记录`
  },
  {
    name: "节点文件管理",
    storageKey: "统一数据底座 / documents",
    href: "/documents",
    ready: documents.value.length > 0,
    summary: `${documents.value.length} 个文档节点`
  },
  {
    name: "提醒事项",
    storageKey: "统一数据底座 / calendarEvents",
    href: "/portal",
    ready: calendarEvents.value.length > 0,
    summary: `${calendarEvents.value.length} 条提醒`
  }
]);

const recentSignals = computed(() => {
  const signals = [
    ...selectedControlLines.value.map((item) => ({
      key: `control-line-${item.id}`,
      module: "进度管理",
      title: item.name,
      text: `${item.completedControlPoints}/${item.totalControlPoints} 个指标成果已完成`,
      time: formatDateTime(item.targetDate),
      sortValue: toTimestamp(item.targetDate),
      href: "/schedule"
    })),
    ...selectedMilestones.value.map((item) => ({
      key: `milestone-${item.id}`,
      module: "关键里程碑",
      title: item.name,
      text: `${phaseName(item.phaseId)} / ${item.status}`,
      time: formatDateTime(item.date),
      sortValue: toTimestamp(item.date),
      href: "/schedule"
    })),
    ...selectedMeetings.value.map((item) => ({
      key: `meeting-${item.id}`,
      module: "会议记录",
      title: item.topic,
      text: `${item.type} / ${formatDate(item.date)}`,
      time: formatDateTime(item.date),
      sortValue: toTimestamp(item.date),
      href: "/meetings"
    })),
    ...selectedContracts.value.map((item) => ({
      key: `contract-${item.id}`,
      module: "合同金额管理",
      title: item.name,
      text: `${item.status} / ${formatMoney(item.amount)}`,
      time: formatDateTime(item.signingDate),
      sortValue: toTimestamp(item.signingDate),
      href: "/contracts"
    })),
    ...selectedDocuments.value.map((item) => ({
      key: `document-${item.id}`,
      module: "节点文件管理",
      title: item.nodeName,
      text: `${item.status} / ${item.owner || "未填写负责人"}`,
      time: formatDateTime(item.targetDate),
      sortValue: toTimestamp(item.targetDate),
      href: "/documents"
    }))
  ];

  return signals
    .filter((item) => item.sortValue > 0)
    .sort((a, b) => b.sortValue - a.sortValue)
    .slice(0, 8);
});

const phaseName = (phaseId) => selectedPhases.value.find((item) => item.id === phaseId)?.name || "未匹配工作";

onMounted(load);

function isSigned(status) {
  return status === "已签订" || status === "已完成";
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString("zh-CN", {
    style: "currency",
    currency: "CNY",
    minimumFractionDigits: 2
  });
}

function formatDate(value) {
  if (!value) return "未填写";
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return "未填写";
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit"
  });
}

function formatDateTime(value) {
  if (!value) return "未填写";
  const date = value.includes?.("T") ? new Date(value) : new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return "未填写";
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

function toTimestamp(value) {
  if (!value) return 0;
  const date = value.includes?.("T") ? new Date(value) : new Date(`${value}T00:00:00`);
  return Number.isNaN(date.getTime()) ? 0 : date.getTime();
}

function getStatusClass(status) {
  if (status === "已完成") return "done";
  if (status === "风险关注") return "risk";
  return "pending";
}

function getContractStatusClass(status) {
  if (status === "已签订" || status === "已完成") return "done";
  if (status === "已取消") return "risk";
  return "pending";
}
</script>

<style scoped>
:root{color-scheme:light}
.overview-page{min-height:100vh;background:#f7f8fb;color:#18201d}
.topbar{display:flex;align-items:center;justify-content:space-between;gap:24px;padding:28px 40px 20px;background:#fff;border-bottom:1px solid #dfe6e2}
.top-actions,.block-head,.panel-head,.form-head,.item-actions{display:flex;align-items:center;gap:10px;flex-wrap:wrap}
.nav-link{text-decoration:none}
.eyebrow{color:#1f7a5a;font-size:12px;font-weight:700;text-transform:uppercase}
.overview-layout{display:grid;grid-template-columns:minmax(0,1.35fr) minmax(340px,.65fr);gap:24px;padding:24px 40px 40px}
.summary-view,.panel{background:#fff;border:1px solid #dfe6e2;border-radius:8px;box-shadow:0 16px 36px rgba(31,55,48,.12)}
.summary-view{min-height:calc(100vh - 132px);overflow:hidden}
.section-head,.block-head,.panel-head,.form-head{justify-content:space-between;align-items:flex-start;gap:18px}
.section-head{display:flex;align-items:end;padding:22px;border-bottom:1px solid #dfe6e2}
.section-head p{margin-top:7px;color:#63706a}
.block{padding:22px;border-top:1px solid #dfe6e2}
.block-head,.panel-head,.form-head{padding-bottom:18px;border-bottom:1px solid #dfe6e2}
.side-view{display:grid;gap:24px;align-content:start}
.panel{padding:22px}
.search-box{display:grid;gap:8px;min-width:220px;color:#63706a;font-size:13px;font-weight:700}
.search-box select{width:100%;border:1px solid #dfe6e2;border-radius:8px;padding:11px 12px;color:#18201d;background:#fff}
.stat-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;padding:18px 22px 22px}
.core-stat-grid{gap:14px;padding-bottom:24px}
.compact-grid{padding:18px 0 6px}
.stat-card,.indicator-card,.stage-card,.source-item,.signal-item{border:1px solid #dfe6e2;border-radius:8px;background:#fff}
.stat-card{display:grid;gap:8px;min-height:96px;padding:16px;background:#f4f7f5}
.core-stat-grid .stat-card{min-height:132px;align-content:center;background:#ffffff}
.stat-card span,.source-item span,.signal-item span,.stage-card span{color:#63706a;font-size:13px;font-weight:700}
.stat-card strong{font-size:22px;overflow-wrap:anywhere}
.core-stat-grid .stat-card strong{font-size:34px;line-height:1.08}
.stat-card em{color:#63706a;font-size:13px;font-style:normal;font-weight:700;line-height:1.45}
.status-pill,.badge{display:inline-flex;align-items:center;min-height:28px;border-radius:8px;padding:4px 9px;background:#e8f1ff;color:#22518f;font-size:13px;font-weight:700}
.badge.done{background:#e8f6ec;color:#155d43}
.badge.pending{background:#fff1dc;color:#9c6428}
.badge.risk{background:#f9e7e7;color:#b53232}
.primary,.secondary,.ghost{display:inline-flex;align-items:center;justify-content:center;border-radius:8px;padding:10px 15px;font-weight:700}
.primary{background:#1f7a5a;color:#fff;border:0}
.secondary,.ghost{border:1px solid #dfe6e2;background:#fff;color:#18201d}
.link-btn{padding:0;background:transparent;color:#1f7a5a;font-weight:800;border:0;text-decoration:none}
.indicator-grid,.stage-grid,.module-source-list,.signal-list{display:grid;gap:12px;margin-top:18px}
.indicator-grid{grid-template-columns:repeat(2,minmax(0,1fr))}
.indicator-card{display:grid;gap:12px;padding:16px}
.indicator-card header,.stage-card header,.source-item header,.signal-item header{display:flex;align-items:flex-start;justify-content:space-between;gap:12px}
.indicator-card p,.stage-card p,.source-item p,.signal-item p,.empty{color:#63706a;line-height:1.55}
.meta-line{color:#63706a;font-size:13px}
.progress-track,.inline-progress{position:relative;overflow:hidden;height:12px;border-radius:8px;background:#dfe6e2}
.progress-track i,.inline-progress i{position:absolute;inset:0 auto 0 0;border-radius:8px;background:#1f7a5a}
.overview-progress{display:grid;gap:12px;margin-top:18px;padding:18px;border:1px solid #dfe6e2;border-radius:8px;background:#f4f7f5}
.overview-progress strong{font-size:32px}
.detail-stack{display:grid;gap:18px;padding:0 14px 14px}
.table-wrap{position:relative;overflow-x:auto;margin-top:18px}
.detail-collapse{margin-top:18px;border:1px solid #dfe6e2;border-radius:8px;background:#fff}
.detail-collapse summary{display:flex;align-items:center;justify-content:space-between;gap:12px;min-height:48px;padding:12px 14px;color:#18201d;font-weight:800;cursor:pointer;list-style:none}
.detail-collapse summary::-webkit-details-marker{display:none}
.detail-collapse summary::after{content:"+";display:grid;place-items:center;width:24px;height:24px;border-radius:8px;background:#e8f1ff;color:#22518f;font-weight:900}
.detail-collapse[open] summary{border-bottom:1px solid #dfe6e2}
.detail-collapse[open] summary::after{content:"-"}
.detail-collapse .table-wrap{margin-top:0;padding:0 14px 14px}
.side-collapse{margin-top:18px}
.side-collapse .module-source-list,.side-collapse .signal-list{margin:0;padding:14px}
table{width:100%;min-width:780px;border-collapse:collapse}
th,td{padding:14px 12px;border-bottom:1px solid #dfe6e2;text-align:left;vertical-align:top}
th{color:#63706a;font-size:13px;font-weight:700}
tbody tr:hover{background:#eef6f2}
.main-cell strong{display:block;overflow-wrap:anywhere}
.main-cell span{display:block;margin-top:6px;color:#63706a;font-size:13px;line-height:1.5}
.source-item,.signal-item{display:grid;gap:8px;padding:13px}
.source-item a,.signal-item a{color:#1f7a5a;font-weight:800;text-decoration:none}
h1,h2,h3,p{margin:0}
h1{margin-top:4px;font-size:30px}
h2{font-size:22px}
h3{font-size:16px}
@media (max-width:1120px){.overview-layout{grid-template-columns:1fr;padding:18px}.topbar{padding:22px 18px 16px}}
@media (max-width:760px){.topbar,.section-head,.block-head,.panel-head,.form-head,.top-actions{align-items:stretch;flex-direction:column}.stat-grid,.indicator-grid{grid-template-columns:1fr}.search-box{min-width:0}h1{font-size:26px}}
</style>

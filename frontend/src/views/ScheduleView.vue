<template>
  <section class="schedule-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">项目执行</p>
        <h1>进度管理</h1>
        <p class="subtext">保留甘特图视图，并按指标成果完成数 / 总数统一计算控制线进度。</p>
      </div>
      <div class="header-actions">
        <RouterLink class="secondary nav-link" to="/portal">返回入口</RouterLink>
        <button class="secondary" type="button" @click="loadAll">刷新数据</button>
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
        <input v-model.trim="keyword" type="search" placeholder="搜索控制线、指标项、指标成果或工作" />
      </label>
      <div class="toolbar-actions">
        <button class="primary" type="button" :disabled="!canEdit" @click="openControlLineDialog()">新增控制线</button>
        <button class="secondary" type="button" :disabled="!canEdit" @click="openPhaseDialog()">新增工作</button>
        <button class="secondary" type="button" :disabled="!canEdit" @click="openMilestoneDialog()">新增关键里程碑</button>
        <button class="secondary" type="button" :disabled="!canEdit" @click="openReminderDialog()">新增提醒事项</button>
      </div>
    </section>

    <section class="stat-grid">
      <article class="stat-card">
        <span>控制线数量</span>
        <strong>{{ filteredControlLines.length }}</strong>
      </article>
      <article class="stat-card">
        <span>指标成果完成数</span>
        <strong>{{ controlPointSummary.done }}/{{ controlPointSummary.total }}</strong>
      </article>
      <article class="stat-card">
        <span>整体进度</span>
        <strong>{{ controlPointSummary.percent }}%</strong>
      </article>
      <article class="stat-card">
        <span>提醒事项</span>
        <strong>{{ filteredReminders.length }}</strong>
      </article>
    </section>

    <section class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab"
        :class="{ active: activeTab === tab.id }"
        type="button"
        @click="activeTab = tab.id"
      >
        {{ tab.label }}
      </button>
    </section>

    <p v-if="message" class="page-message">{{ message }}</p>

    <section v-if="activeTab === 'control'" class="panel">
      <div class="panel-header">
        <div>
          <h2>控制线与指标成果</h2>
          <p>控制线按二级指标成果完成数 / 总数计算百分比；旧数据未配置成果时按指标项状态兼容计算。</p>
        </div>
      </div>

      <div v-if="!filteredControlLines.length" class="empty-state">当前筛选条件下暂无控制线。</div>

      <article v-for="line in filteredControlLines" :key="line.id" class="control-line-card">
        <header class="control-line-header">
          <div>
            <div class="line-meta">
              <span class="badge">{{ line.projectName }}</span>
              <span class="muted">目标日期 {{ line.targetDate }}</span>
              <span class="muted">负责人 {{ line.owner }}</span>
            </div>
            <h3>{{ line.name }}</h3>
            <p>{{ line.note || "暂无说明" }}</p>
          </div>
          <div class="line-side">
            <div class="progress-block">
              <strong>{{ line.progressPercent }}%</strong>
              <span>{{ line.completedControlPoints }}/{{ line.totalControlPoints }} 个指标成果完成</span>
            </div>
            <div class="inline-actions">
              <button class="secondary small" type="button" :disabled="!canEdit" @click="openControlPointDialog(line)">
                新增指标项
              </button>
              <button class="link-btn" type="button" :disabled="!canEdit" @click="openControlLineDialog(line)">编辑</button>
              <button class="link-btn danger" type="button" :disabled="!canEdit" @click="removeControlLine(line)">删除</button>
            </div>
          </div>
        </header>

        <div v-if="!line.controlPoints.length" class="empty-nested">该控制线下还没有指标项。</div>

        <div v-for="point in line.controlPoints" :key="point.id" class="control-point-card">
          <header class="control-point-header">
            <div>
              <div class="line-meta">
                <span class="badge" :class="{ done: point.completed }">{{ point.completed ? "已完成" : point.status }}</span>
                <span class="muted">计划日期 {{ point.plannedDate }}</span>
                <span class="muted">序号 {{ point.sortOrder }}</span>
                <span class="muted">成果完成 {{ pointItemDone(point) }}/{{ pointItemTotal(point) }}</span>
              </div>
              <h4>{{ point.name }}</h4>
            </div>
            <div class="inline-actions">
              <button class="secondary small" type="button" :disabled="!canEdit" @click="openControlPointItemDialog(point)">
                新增指标成果
              </button>
              <button class="link-btn" type="button" :disabled="!canEdit" @click="openControlPointDialog(line, point)">
                编辑
              </button>
              <button class="link-btn danger" type="button" :disabled="!canEdit" @click="removeControlPoint(point)">删除</button>
            </div>
          </header>

          <div class="mini-progress">
            <div class="mini-progress-track">
              <span class="mini-progress-fill" :style="{ width: `${pointItemPercent(point)}%` }"></span>
            </div>
            <span class="mini-progress-text">{{ pointItemPercent(point) }}%</span>
          </div>

          <ul v-if="point.items.length" class="item-list">
            <li v-for="item in point.items" :key="item.id" class="item-row">
              <label class="item-check">
                <input
                  :checked="Boolean(item.completed)"
                  type="checkbox"
                  :disabled="!canEdit"
                  @change="toggleControlPointItem(point, item, $event.target.checked)"
                />
                <span :class="{ checked: item.completed }">{{ item.name }}</span>
              </label>
              <div class="inline-actions">
                <button class="link-btn" type="button" :disabled="!canEdit" @click="openControlPointItemDialog(point, item)">
                  编辑
                </button>
                <button class="link-btn danger" type="button" :disabled="!canEdit" @click="removeControlPointItem(item)">
                  删除
                </button>
              </div>
            </li>
          </ul>
          <div v-else class="empty-nested">暂未配置指标成果，当前按指标项状态兼容计入进度。</div>
        </div>
      </article>
    </section>

    <section v-else-if="activeTab === 'phases'" class="panel">
      <div class="panel-header">
        <div>
          <h2>工作与甘特图</h2>
          <p>保留工作计划甘特图，按时间轴展示各工作起止区间。</p>
        </div>
      </div>

      <section class="gantt-card">
        <div class="gantt-head">
          <div>
            <h3>甘特图</h3>
            <p>按当前筛选项目显示工作时间安排。</p>
          </div>
          <div class="gantt-range">{{ ganttRangeLabel }}</div>
        </div>

        <div v-if="!ganttRows.length" class="empty-state">当前筛选条件下暂无可展示的工作甘特图数据。</div>

        <div v-else class="gantt-chart">
          <div class="gantt-scale">
            <span
              v-for="tick in ganttTicks"
              :key="`${tick.label}-${tick.position}`"
              class="gantt-tick"
              :style="{ left: `${tick.position}%` }"
            >
              {{ tick.label }}
            </span>
          </div>

          <div v-for="row in ganttRows" :key="row.id" class="gantt-row">
            <div class="gantt-label">
              <strong>{{ row.name }}</strong>
              <span>{{ row.projectName }}</span>
            </div>
            <div class="gantt-track">
              <div class="gantt-track-grid"></div>
              <div class="gantt-bar" :style="{ left: `${row.left}%`, width: `${row.width}%` }">
                <span>{{ row.startDate }} - {{ row.endDate }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div v-if="!filteredPhases.length" class="empty-state">当前筛选条件下暂无工作。</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>项目</th>
            <th>工作名称</th>
            <th>负责人</th>
            <th>开始</th>
            <th>结束</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="phase in filteredPhases" :key="phase.id">
            <td>{{ projectName(phase.projectId) }}</td>
            <td>{{ phase.name }}</td>
            <td>{{ phase.owner }}</td>
            <td>{{ phase.startDate }}</td>
            <td>{{ phase.endDate }}</td>
            <td>{{ phase.status }}</td>
            <td>
              <button class="link-btn" type="button" :disabled="!canEdit" @click="openPhaseDialog(phase)">编辑</button>
              <button class="link-btn danger" type="button" :disabled="!canEdit" @click="removePhase(phase)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <section v-else-if="activeTab === 'milestones'" class="panel">
      <div class="panel-header">
        <div>
          <h2>关键里程碑</h2>
          <p>独立维护关键节点，与控制线并行存在。</p>
        </div>
      </div>
      <div v-if="!filteredMilestones.length" class="empty-state">当前筛选条件下暂无里程碑。</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>项目</th>
            <th>里程碑</th>
            <th>日期</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="milestone in filteredMilestones" :key="milestone.id">
            <td>{{ milestone.projectName }}</td>
            <td>{{ milestone.name }}</td>
            <td>{{ milestone.date }}</td>
            <td>{{ milestone.status }}</td>
            <td>
              <button class="link-btn danger" type="button" :disabled="!canEdit" @click="removeMilestone(milestone)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <section v-else class="panel">
      <div class="panel-header">
        <div>
          <h2>重要事项与提醒</h2>
          <p>提醒与控制线独立维护，可单独管理会议、材料、合同等事项。</p>
        </div>
      </div>
      <div v-if="!filteredReminders.length" class="empty-state">当前筛选条件下暂无提醒。</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>项目</th>
            <th>提醒内容</th>
            <th>日期</th>
            <th>时间</th>
            <th>类型</th>
            <th>说明</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="event in filteredReminders" :key="event.id">
            <td>{{ event.projectName }}</td>
            <td>{{ event.title }}</td>
            <td>{{ event.date }}</td>
            <td>{{ event.time || "全天" }}</td>
            <td>{{ event.type }}</td>
            <td>{{ event.note || "暂无说明" }}</td>
            <td>
              <button class="link-btn" type="button" :disabled="!canEdit" @click="openReminderDialog(event)">编辑</button>
              <button class="link-btn danger" type="button" :disabled="!canEdit" @click="removeReminder(event)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <div v-if="dialog.type" class="modal-overlay" @click.self="closeDialog">
      <div class="modal-card">
        <form class="record-form" @submit.prevent="submitDialog">
          <div class="form-head">
            <div>
              <p class="eyebrow">{{ dialogTitle.eyebrow }}</p>
              <h2>{{ dialogTitle.title }}</h2>
            </div>
            <button class="ghost" type="button" @click="closeDialog">取消</button>
          </div>

          <template v-if="dialog.type === 'phase'">
            <label class="field">
              <span>所属项目</span>
              <select v-model="phaseForm.projectId" required>
                <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
              </select>
            </label>
            <label class="field">
              <span>工作名称</span>
              <input v-model.trim="phaseForm.name" type="text" required />
            </label>
            <div class="grid-two">
              <label class="field">
                <span>负责人</span>
                <input v-model.trim="phaseForm.owner" type="text" required />
              </label>
              <label class="field">
                <span>状态</span>
                <input v-model.trim="phaseForm.status" type="text" required />
              </label>
            </div>
            <div class="grid-two">
              <label class="field">
                <span>开始日期</span>
                <input v-model="phaseForm.startDate" type="date" required />
              </label>
              <label class="field">
                <span>结束日期</span>
                <input v-model="phaseForm.endDate" type="date" required />
              </label>
            </div>
          </template>

          <template v-else-if="dialog.type === 'controlLine'">
            <label class="field">
              <span>所属项目</span>
              <select v-model="controlLineForm.projectId" required>
                <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
              </select>
            </label>
            <label class="field">
              <span>控制线名称</span>
              <input v-model.trim="controlLineForm.name" type="text" required />
            </label>
            <div class="grid-two">
              <label class="field">
                <span>目标日期</span>
                <input v-model="controlLineForm.targetDate" type="date" required />
              </label>
              <label class="field">
                <span>负责人</span>
                <input v-model.trim="controlLineForm.owner" type="text" required />
              </label>
            </div>
            <label class="field">
              <span>说明</span>
              <textarea v-model.trim="controlLineForm.note" rows="4" required></textarea>
            </label>
            <section class="nested-editor">
              <div class="nested-editor-head">
                <div>
                  <span class="nested-title">指标项与指标成果</span>
                  <p class="nested-copy">控制线保存时会同步保存这里的两级指标结构和完成状态。</p>
                </div>
                <button class="secondary small" type="button" @click="addControlLinePoint">新增指标项</button>
              </div>

              <div v-if="!controlLinePointDrafts.length" class="empty-nested">当前还没有指标项，请至少补充需要跟踪的成果指标。</div>

              <article
                v-for="(point, pointIndex) in controlLinePointDrafts"
                :key="point.localId"
                class="nested-point-card"
              >
                <div class="nested-point-head">
                  <strong>指标项 {{ pointIndex + 1 }}</strong>
                  <button class="link-btn danger" type="button" @click="removeControlLinePoint(pointIndex)">删除指标项</button>
                </div>

                <label class="field">
                  <span>指标项名称</span>
                  <input v-model.trim="point.name" type="text" required />
                </label>
                <div class="grid-two">
                  <label class="field">
                    <span>计划日期</span>
                    <input v-model="point.plannedDate" type="date" required />
                  </label>
                  <label class="field">
                    <span>状态</span>
                    <select v-model="point.status" required>
                      <option value="未开始">未开始</option>
                      <option value="进行中">进行中</option>
                      <option value="已完成">已完成</option>
                    </select>
                  </label>
                </div>

                <div class="nested-item-head">
                  <span class="nested-title">指标成果</span>
                  <button class="secondary small" type="button" @click="addControlLinePointItem(point)">新增指标成果</button>
                </div>

                <div v-if="!point.items.length" class="empty-nested">当前指标项下还没有指标成果。</div>

                <div v-for="(item, itemIndex) in point.items" :key="item.localId" class="nested-item-row">
                  <label class="field grow">
                    <span>成果名称 {{ itemIndex + 1 }}</span>
                    <input v-model.trim="item.name" type="text" required />
                  </label>
                  <label class="field checkbox-inline">
                    <span>是否完成</span>
                    <input v-model="item.completed" type="checkbox" />
                  </label>
                  <button class="link-btn danger" type="button" @click="removeControlLinePointItem(point, itemIndex)">
                    删除成果
                  </button>
                </div>
              </article>
            </section>
          </template>

          <template v-else-if="dialog.type === 'controlPoint'">
            <label class="field">
              <span>所属控制线</span>
              <select v-model="controlPointForm.controlLineId" required>
                <option v-for="line in filteredControlLinesForForm" :key="line.id" :value="line.id">{{ line.name }}</option>
              </select>
            </label>
            <label class="field">
              <span>指标项名称</span>
              <input v-model.trim="controlPointForm.name" type="text" required />
            </label>
            <div class="grid-two">
              <label class="field">
                <span>计划日期</span>
                <input v-model="controlPointForm.plannedDate" type="date" required />
              </label>
              <label class="field">
                <span>状态</span>
                <select v-model="controlPointForm.status" required>
                  <option value="未开始">未开始</option>
                  <option value="进行中">进行中</option>
                  <option value="已完成">已完成</option>
                </select>
              </label>
            </div>
            <label class="field">
              <span>排序序号</span>
              <input v-model.number="controlPointForm.sortOrder" min="1" step="1" type="number" required />
            </label>
          </template>

          <template v-else-if="dialog.type === 'controlPointItem'">
            <label class="field">
              <span>所属指标项</span>
              <select v-model="controlPointItemForm.controlPointId" required>
                <option v-for="point in availableControlPoints" :key="point.id" :value="point.id">{{ point.name }}</option>
              </select>
            </label>
            <label class="field">
              <span>指标成果名称</span>
              <input v-model.trim="controlPointItemForm.name" type="text" required />
            </label>
            <div class="grid-two">
              <label class="field">
                <span>排序序号</span>
                <input v-model.number="controlPointItemForm.sortOrder" min="1" step="1" type="number" required />
              </label>
              <label class="field checkbox-field">
                <span>完成状态</span>
                <input v-model="controlPointItemForm.completed" type="checkbox" />
              </label>
            </div>
          </template>

          <template v-else-if="dialog.type === 'milestone'">
            <label class="field">
              <span>所属项目</span>
              <select v-model="milestoneForm.projectId" required>
                <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
              </select>
            </label>
            <label class="field">
              <span>关联工作</span>
              <select v-model="milestoneForm.phaseId" required>
                <option v-for="phase in phaseOptionsForMilestone" :key="phase.id" :value="phase.id">{{ phase.name }}</option>
              </select>
            </label>
            <label class="field">
              <span>里程碑名称</span>
              <input v-model.trim="milestoneForm.name" type="text" required />
            </label>
            <div class="grid-two">
              <label class="field">
                <span>日期</span>
                <input v-model="milestoneForm.date" type="date" required />
              </label>
              <label class="field">
                <span>状态</span>
                <select v-model="milestoneForm.status" required>
                  <option value="未开始">未开始</option>
                  <option value="进行中">进行中</option>
                  <option value="已完成">已完成</option>
                </select>
              </label>
            </div>
          </template>

          <template v-else-if="dialog.type === 'reminder'">
            <label class="field">
              <span>所属项目</span>
              <select v-model="reminderForm.projectId" required>
                <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.name }}</option>
              </select>
            </label>
            <label class="field">
              <span>提醒内容</span>
              <input v-model.trim="reminderForm.title" type="text" required />
            </label>
            <div class="grid-two">
              <label class="field">
                <span>日期</span>
                <input v-model="reminderForm.date" type="date" required />
              </label>
              <label class="field">
                <span>时间</span>
                <input v-model="reminderForm.time" type="time" />
              </label>
            </div>
            <div class="grid-two">
              <label class="field">
                <span>类型</span>
                <select v-model="reminderForm.type" required>
                  <option value="会议提醒">会议提醒</option>
                  <option value="材料提交">材料提交</option>
                  <option value="合同节点">合同节点</option>
                  <option value="其他事项">其他事项</option>
                </select>
              </label>
              <label class="field">
                <span>提前提醒</span>
                <select v-model.number="reminderForm.remindBefore">
                  <option :value="0">当天</option>
                  <option :value="1">提前 1 天</option>
                  <option :value="3">提前 3 天</option>
                  <option :value="7">提前 7 天</option>
                </select>
              </label>
            </div>
            <label class="field">
              <span>说明</span>
              <textarea v-model.trim="reminderForm.note" rows="4"></textarea>
            </label>
          </template>

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
  createCalendarEvent,
  createControlLine,
  createControlLineStructure,
  createControlPoint,
  createControlPointItem,
  createMilestone,
  createPhase,
  deleteCalendarEvent,
  deleteControlLine,
  deleteControlPoint,
  deleteControlPointItem,
  deleteMilestone,
  deletePhase,
  fetchCalendarEvents,
  fetchControlLines,
  fetchMilestones,
  fetchPhases,
  fetchProjects,
  updateCalendarEvent,
  updateControlLine,
  updateControlLineStructure,
  updateControlPoint,
  updateControlPointItem,
  updatePhase
} from "../api/modules";
import { isAdmin } from "../auth";

const tabs = [
  { id: "control", label: "控制线与指标成果" },
  { id: "phases", label: "工作与甘特图" },
  { id: "milestones", label: "关键里程碑" },
  { id: "reminders", label: "重要事项与提醒" }
];

const projects = ref([]);
const phases = ref([]);
const milestones = ref([]);
const controlLines = ref([]);
const reminders = ref([]);
const selectedProjectId = ref("");
const keyword = ref("");
const activeTab = ref("control");
const saving = ref(false);
const formError = ref("");
const message = ref("");

const dialog = reactive({
  type: "",
  editId: ""
});

const phaseForm = reactive({
  projectId: "",
  name: "",
  owner: "",
  startDate: "",
  endDate: "",
  status: "进行中"
});

const controlLineForm = reactive({
  projectId: "",
  name: "",
  targetDate: "",
  owner: "",
  note: ""
});

const controlPointForm = reactive({
  projectId: "",
  controlLineId: "",
  name: "",
  plannedDate: "",
  status: "未开始",
  sortOrder: 1
});

const controlPointItemForm = reactive({
  controlPointId: "",
  name: "",
  completed: false,
  sortOrder: 1
});

const milestoneForm = reactive({
  projectId: "",
  phaseId: "",
  name: "",
  date: "",
  status: "未开始",
  critical: true
});

const reminderForm = reactive({
  projectId: "",
  title: "",
  date: "",
  time: "",
  type: "会议提醒",
  remindBefore: 0,
  note: ""
});

const controlLinePointDrafts = ref([]);

const canEdit = computed(() => isAdmin());

const filteredPhases = computed(() =>
  phases.value.filter((phase) => matchesProject(phase.projectId) && matchesKeyword([phase.name, phase.owner, phase.status]))
);

const filteredControlLines = computed(() =>
  controlLines.value.filter((line) => {
    const itemKeywords = [
      line.name,
      line.owner,
      line.note,
      ...line.controlPoints.flatMap((point) => [point.name, point.status, ...point.items.map((item) => item.name)])
    ];
    return matchesProject(line.projectId) && matchesKeyword(itemKeywords);
  })
);

const filteredMilestones = computed(() =>
  milestones.value.filter((item) => {
    const project = projects.value.find((entry) => entry.name === item.projectName);
    return matchesProject(project?.id || "") && matchesKeyword([item.name, item.projectName, item.status]);
  })
);

const filteredReminders = computed(() =>
  reminders.value.filter((item) => matchesProject(item.projectId) && matchesKeyword([item.title, item.type, item.note]))
);

const controlPointSummary = computed(() => {
  const points = filteredControlLines.value.flatMap((line) => line.controlPoints || []);
  const total = points.reduce((sum, point) => sum + pointItemTotal(point), 0);
  const done = points.reduce((sum, point) => sum + pointItemDone(point), 0);
  return {
    total,
    done,
    percent: total ? Math.round((done / total) * 100) : 0
  };
});

const dialogTitle = computed(() => {
  const titles = {
    phase: ["工作", dialog.editId ? "编辑工作" : "新增工作"],
    controlLine: ["控制线", dialog.editId ? "编辑控制线" : "新增控制线"],
    controlPoint: ["指标项", dialog.editId ? "编辑指标项" : "新增指标项"],
    controlPointItem: ["指标成果", dialog.editId ? "编辑指标成果" : "新增指标成果"],
    milestone: ["里程碑", "新增关键里程碑"],
    reminder: ["提醒", dialog.editId ? "编辑提醒事项" : "新增提醒事项"]
  };
  const current = titles[dialog.type] || ["记录", "编辑记录"];
  return {
    eyebrow: current[0],
    title: current[1]
  };
});

const filteredControlLinesForForm = computed(() =>
  controlLines.value.filter((line) => !controlPointForm.projectId || line.projectId === controlPointForm.projectId)
);

const availableControlPoints = computed(() => {
  const points = controlLines.value.flatMap((line) => line.controlPoints || []);
  const selectedLine = controlLines.value.find((line) => line.id === controlPointForm.controlLineId);
  if (selectedLine) {
    return selectedLine.controlPoints || [];
  }
  const selectedPoint = points.find((item) => item.id === controlPointItemForm.controlPointId);
  if (!selectedPoint) {
    return points;
  }
  return points.filter((item) => item.id === selectedPoint.id || item.controlLineId === selectedPoint.controlLineId);
});

const phaseOptionsForMilestone = computed(() =>
  phases.value.filter((phase) => !milestoneForm.projectId || phase.projectId === milestoneForm.projectId)
);

const ganttRows = computed(() => {
  const rows = filteredPhases.value
    .map((phase) => {
      const start = toDate(phase.startDate);
      const end = toDate(phase.endDate);
      if (!start || !end) {
        return null;
      }
      return {
        id: phase.id,
        name: phase.name,
        projectName: projectName(phase.projectId),
        start,
        end,
        startDate: phase.startDate,
        endDate: phase.endDate
      };
    })
    .filter(Boolean)
    .sort((a, b) => a.start - b.start);

  if (!rows.length) {
    return [];
  }

  const minTime = Math.min(...rows.map((row) => row.start.getTime()));
  const maxTime = Math.max(...rows.map((row) => row.end.getTime()));
  const totalDays = Math.max(1, daysBetween(new Date(minTime), new Date(maxTime)) + 1);

  return rows.map((row) => {
    const offsetDays = daysBetween(new Date(minTime), row.start);
    const spanDays = Math.max(1, daysBetween(row.start, row.end) + 1);
    return {
      ...row,
      left: (offsetDays / totalDays) * 100,
      width: (spanDays / totalDays) * 100
    };
  });
});

const ganttBounds = computed(() => {
  if (!ganttRows.value.length) {
    const today = new Date();
    return { start: today, end: today };
  }
  const start = new Date(Math.min(...ganttRows.value.map((row) => row.start.getTime())));
  const end = new Date(Math.max(...ganttRows.value.map((row) => row.end.getTime())));
  return { start, end };
});

const ganttRangeLabel = computed(() => `${formatDateShort(ganttBounds.value.start)} - ${formatDateShort(ganttBounds.value.end)}`);

const ganttTicks = computed(() => {
  if (!ganttRows.value.length) {
    return [];
  }
  const totalDays = Math.max(1, daysBetween(ganttBounds.value.start, ganttBounds.value.end));
  const tickCount = Math.min(5, totalDays + 1);
  return Array.from({ length: tickCount }, (_, index) => {
    const ratio = tickCount === 1 ? 0 : index / (tickCount - 1);
    const tickDate = addDays(ganttBounds.value.start, Math.round(totalDays * ratio));
    return {
      label: formatDateShort(tickDate),
      position: ratio * 100
    };
  });
});

const loadAll = async () => {
  try {
    const [projectData, phaseData, milestoneData, controlLineData, reminderData] = await Promise.all([
      fetchProjects(),
      fetchPhases(),
      fetchMilestones(),
      fetchControlLines(),
      fetchCalendarEvents()
    ]);
    projects.value = projectData;
    phases.value = phaseData;
    milestones.value = milestoneData;
    controlLines.value = controlLineData;
    reminders.value = reminderData;
    if (selectedProjectId.value && !projects.value.some((item) => item.id === selectedProjectId.value)) {
      selectedProjectId.value = "";
    }
    message.value = "";
  } catch (error) {
    message.value = error?.response?.data?.message || "加载数据失败，请稍后重试。";
  }
};

const openPhaseDialog = (phase = null) => {
  dialog.type = "phase";
  dialog.editId = phase?.id || "";
  phaseForm.projectId = phase?.projectId || selectedProjectId.value || projects.value[0]?.id || "";
  phaseForm.name = phase?.name || "";
  phaseForm.owner = phase?.owner || "";
  phaseForm.startDate = phase?.startDate || "";
  phaseForm.endDate = phase?.endDate || "";
  phaseForm.status = phase?.status || "进行中";
  formError.value = "";
};

const openControlLineDialog = (line = null) => {
  dialog.type = "controlLine";
  dialog.editId = line?.id || "";
  controlLineForm.projectId = line?.projectId || selectedProjectId.value || projects.value[0]?.id || "";
  controlLineForm.name = line?.name || "";
  controlLineForm.targetDate = line?.targetDate || "";
  controlLineForm.owner = line?.owner || "";
  controlLineForm.note = line?.note || "";
  controlLinePointDrafts.value = (line?.controlPoints || []).map((point, pointIndex) => ({
    id: point.id || "",
    localId: createLocalId("point", pointIndex),
    name: point.name || "",
    plannedDate: point.plannedDate || line?.targetDate || "",
    status: point.status || "未开始",
    items: (point.items || []).map((item, itemIndex) => ({
      id: item.id || "",
      localId: createLocalId("item", `${pointIndex}-${itemIndex}`),
      name: item.name || "",
      completed: Boolean(item.completed)
    }))
  }));
  if (!controlLinePointDrafts.value.length) {
    controlLinePointDrafts.value = [createEmptyPointDraft(controlLineForm.targetDate)];
  }
  formError.value = "";
};

const openControlPointDialog = (line = null, point = null) => {
  dialog.type = "controlPoint";
  dialog.editId = point?.id || "";
  const baseLine = line || controlLines.value.find((item) => item.id === point?.controlLineId);
  controlPointForm.projectId = baseLine?.projectId || selectedProjectId.value || projects.value[0]?.id || "";
  controlPointForm.controlLineId = baseLine?.id || point?.controlLineId || "";
  controlPointForm.name = point?.name || "";
  controlPointForm.plannedDate = point?.plannedDate || "";
  controlPointForm.status = point?.status || "未开始";
  controlPointForm.sortOrder = point?.sortOrder || (baseLine?.controlPoints?.length || 0) + 1;
  formError.value = "";
};

const openControlPointItemDialog = (point = null, item = null) => {
  dialog.type = "controlPointItem";
  dialog.editId = item?.id || "";
  controlPointItemForm.controlPointId = point?.id || item?.controlPointId || "";
  controlPointItemForm.name = item?.name || "";
  controlPointItemForm.completed = Boolean(item?.completed);
  controlPointItemForm.sortOrder = item?.sortOrder || (point?.items?.length || 0) + 1;
  formError.value = "";
};

const openMilestoneDialog = () => {
  dialog.type = "milestone";
  dialog.editId = "";
  milestoneForm.projectId = selectedProjectId.value || projects.value[0]?.id || "";
  milestoneForm.phaseId = phaseOptionsForMilestone.value[0]?.id || "";
  milestoneForm.name = "";
  milestoneForm.date = "";
  milestoneForm.status = "未开始";
  milestoneForm.critical = true;
  formError.value = "";
};

const openReminderDialog = (event = null) => {
  dialog.type = "reminder";
  dialog.editId = event?.id || "";
  reminderForm.projectId = event?.projectId || selectedProjectId.value || projects.value[0]?.id || "";
  reminderForm.title = event?.title || "";
  reminderForm.date = event?.date || "";
  reminderForm.time = event?.time || "";
  reminderForm.type = event?.type || "会议提醒";
  reminderForm.remindBefore = event?.remindBefore ?? 0;
  reminderForm.note = event?.note || "";
  formError.value = "";
};

const closeDialog = () => {
  dialog.type = "";
  dialog.editId = "";
  formError.value = "";
};

const submitDialog = async () => {
  formError.value = "";
  saving.value = true;
  try {
    if (dialog.type === "phase") {
      const payload = { ...phaseForm };
      if (dialog.editId) {
        await updatePhase(dialog.editId, payload);
      } else {
        await createPhase(payload);
      }
    } else if (dialog.type === "controlLine") {
      const payload = {
        ...controlLineForm,
        points: controlLinePointDrafts.value.map((point) => ({
          id: point.id || null,
          name: point.name,
          plannedDate: point.plannedDate,
          status: point.status,
          items: point.items.map((item) => ({
            id: item.id || null,
            name: item.name,
            completed: Boolean(item.completed)
          }))
        }))
      };
      validateControlLineDrafts();
      if (dialog.editId) {
        await updateControlLineStructure(dialog.editId, payload);
      } else {
        await createControlLineStructure(payload);
      }
    } else if (dialog.type === "controlPoint") {
      const selectedLine = controlLines.value.find((line) => line.id === controlPointForm.controlLineId);
      const payload = {
        ...controlPointForm,
        projectId: selectedLine?.projectId || controlPointForm.projectId
      };
      if (dialog.editId) {
        await updateControlPoint(dialog.editId, payload);
      } else {
        await createControlPoint(payload);
      }
    } else if (dialog.type === "controlPointItem") {
      const payload = { ...controlPointItemForm };
      if (dialog.editId) {
        await updateControlPointItem(dialog.editId, payload);
      } else {
        await createControlPointItem(payload);
      }
    } else if (dialog.type === "milestone") {
      await createMilestone({ ...milestoneForm });
    } else if (dialog.type === "reminder") {
      const payload = { ...reminderForm };
      if (dialog.editId) {
        await updateCalendarEvent(dialog.editId, payload);
      } else {
        await createCalendarEvent(payload);
      }
    }
    closeDialog();
    await loadAll();
  } catch (error) {
    formError.value = error?.response?.data?.message || error?.message || "保存失败，请检查必填项后重试。";
  } finally {
    saving.value = false;
  }
};

const removePhase = async (phase) => {
  if (!window.confirm(`确认删除工作“${phase.name}”吗？`)) return;
  await deletePhase(phase.id);
  await loadAll();
};

const removeControlLine = async (line) => {
  if (!window.confirm(`确认删除控制线“${line.name}”吗？`)) return;
  await deleteControlLine(line.id);
  await loadAll();
};

const removeControlPoint = async (point) => {
  if (!window.confirm(`确认删除指标项“${point.name}”吗？`)) return;
  await deleteControlPoint(point.id);
  await loadAll();
};

const removeControlPointItem = async (item) => {
  if (!window.confirm(`确认删除指标成果“${item.name}”吗？`)) return;
  await deleteControlPointItem(item.id);
  await loadAll();
};

const toggleControlPointItem = async (point, item, completed) => {
  if (!canEdit.value) return;
  await updateControlPointItem(item.id, {
    controlPointId: point.id,
    name: item.name,
    completed,
    sortOrder: item.sortOrder || 1
  });
  await loadAll();
};

const removeMilestone = async (milestone) => {
  if (!window.confirm(`确认删除里程碑“${milestone.name}”吗？`)) return;
  await deleteMilestone(milestone.id);
  await loadAll();
};

const removeReminder = async (event) => {
  if (!window.confirm(`确认删除提醒“${event.title}”吗？`)) return;
  await deleteCalendarEvent(event.id);
  await loadAll();
};

const pointItemTotal = (point) => (point.items?.length ? point.items.length : 1);
const pointItemDone = (point) => {
  if (point.items?.length) {
    return point.items.filter((item) => Boolean(item.completed)).length;
  }
  return point.completed ? 1 : 0;
};
const pointItemPercent = (point) => {
  const total = pointItemTotal(point);
  return total ? Math.round((pointItemDone(point) / total) * 100) : 0;
};

const matchesProject = (projectId) => !selectedProjectId.value || projectId === selectedProjectId.value;
const matchesKeyword = (values) => !keyword.value || values.filter(Boolean).join(" ").includes(keyword.value);
const projectName = (projectId) => projects.value.find((item) => item.id === projectId)?.name || "未匹配项目";

const addControlLinePoint = () => {
  controlLinePointDrafts.value = [...controlLinePointDrafts.value, createEmptyPointDraft(controlLineForm.targetDate)];
};

const removeControlLinePoint = (pointIndex) => {
  controlLinePointDrafts.value = controlLinePointDrafts.value.filter((_, index) => index !== pointIndex);
};

const addControlLinePointItem = (point) => {
  point.items = [...point.items, createEmptyItemDraft()];
};

const removeControlLinePointItem = (point, itemIndex) => {
  point.items = point.items.filter((_, index) => index !== itemIndex);
};

const validateControlLineDrafts = () => {
  controlLinePointDrafts.value.forEach((point, pointIndex) => {
    if (!point.name || !point.plannedDate || !point.status) {
      throw new Error(`请完整填写第 ${pointIndex + 1} 个指标项。`);
    }
    point.items.forEach((item, itemIndex) => {
      if (!item.name) {
        throw new Error(`请填写第 ${pointIndex + 1} 个指标项下第 ${itemIndex + 1} 个指标成果名称。`);
      }
    });
  });
};

onMounted(loadAll);

function toDate(dateText) {
  if (!dateText) return null;
  const date = new Date(`${dateText}T00:00:00`);
  return Number.isNaN(date.getTime()) ? null : date;
}

function daysBetween(start, end) {
  const oneDay = 24 * 60 * 60 * 1000;
  return Math.round((end.getTime() - start.getTime()) / oneDay);
}

function addDays(date, offset) {
  const next = new Date(date);
  next.setDate(next.getDate() + offset);
  return next;
}

function formatDateShort(date) {
  return date.toLocaleDateString("zh-CN", {
    month: "2-digit",
    day: "2-digit"
  });
}

function createEmptyPointDraft(defaultDate = "") {
  return {
    id: "",
    localId: createLocalId("point", Math.random().toString(36).slice(2, 8)),
    name: "",
    plannedDate: defaultDate || "",
    status: "未开始",
    items: [createEmptyItemDraft()]
  };
}

function createEmptyItemDraft() {
  return {
    id: "",
    localId: createLocalId("item", Math.random().toString(36).slice(2, 8)),
    name: "",
    completed: false
  };
}

function createLocalId(prefix, seed) {
  return `${prefix}-${seed}-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
}
</script>

<style scoped>
.schedule-page{min-height:100vh;padding:clamp(18px,3vw,28px);background:#f5f7fb;color:#18201d}
.page-header,.header-actions,.toolbar-actions,.tabs,.panel-header,.control-line-header,.control-point-header,.inline-actions,.form-head,.grid-two,.gantt-head{display:flex;gap:12px}
.page-header,.panel-header,.control-line-header,.control-point-header,.form-head,.gantt-head{justify-content:space-between;align-items:flex-start}
.page-header{margin-bottom:20px}
.header-actions,.toolbar-actions,.tabs,.inline-actions{flex-wrap:wrap}
.eyebrow{margin:0 0 6px;color:#1f7a5a;font-size:12px;font-weight:700;text-transform:uppercase}
h1,h2,h3,h4,p{margin:0}
h1{font-size:30px}
h2{font-size:22px}
h3{font-size:18px}
h4{font-size:16px}
.subtext{margin-top:8px;color:#63706a}
.toolbar-card,.panel,.stat-card,.gantt-card{background:#fff;border:1px solid #dfe6e2;border-radius:12px;box-shadow:0 12px 28px rgba(31,55,48,.08)}
.toolbar-card{display:grid;grid-template-columns:minmax(180px,240px) minmax(220px,1fr) auto;gap:14px;align-items:end;padding:18px;margin-bottom:18px}
.field{display:grid;gap:8px;color:#63706a;font-size:13px;font-weight:700}
.field input,.field select,.field textarea{width:100%;border:1px solid #dfe6e2;border-radius:10px;padding:11px 12px;background:#fff}
.compact{max-width:240px}
.search-field{min-width:260px}
.stat-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px;margin-bottom:18px}
.stat-card{display:grid;gap:8px;padding:18px}
.stat-card span{color:#63706a;font-size:13px;font-weight:700}
.stat-card strong{font-size:24px}
.tabs{margin-bottom:18px}
.tab,.secondary,.ghost,.primary{border-radius:10px;padding:10px 14px;font-weight:700}
.tab,.secondary,.ghost{border:1px solid #dfe6e2;background:#fff;color:#18201d}
.tab.active,.primary{border-color:#1f7a5a;background:#1f7a5a;color:#fff}
.nav-link{text-decoration:none}
.page-message{margin:0 0 16px;color:#b53232;font-weight:700}
.panel{padding:20px}
.control-line-card,.control-point-card{display:grid;gap:14px;padding:16px;border:1px solid #dfe6e2;border-radius:12px;background:#fafbfc}
.control-line-card + .control-line-card,.control-point-card + .control-point-card{margin-top:14px}
.line-meta{display:flex;gap:8px;flex-wrap:wrap;align-items:center;margin-bottom:8px}
.muted{color:#63706a;font-size:13px}
.line-side{display:grid;gap:10px;justify-items:end}
.progress-block{display:grid;gap:2px;text-align:right}
.progress-block strong{font-size:26px}
.badge{display:inline-flex;align-items:center;min-height:26px;padding:4px 9px;border-radius:999px;background:#e8f1ff;color:#22518f;font-size:12px;font-weight:700}
.badge.done{background:#e8f6ec;color:#155d43}
.mini-progress{display:flex;align-items:center;gap:10px}
.mini-progress-track{position:relative;flex:1;height:8px;border-radius:999px;background:#dfe6e2;overflow:hidden}
.mini-progress-fill{position:absolute;inset:0 auto 0 0;background:#1f7a5a;border-radius:999px}
.mini-progress-text{color:#63706a;font-size:12px;font-weight:700}
.item-list{display:grid;gap:10px;margin:0;padding:0;list-style:none}
.item-row{display:flex;justify-content:space-between;align-items:center;gap:10px;padding:10px 12px;border-radius:10px;background:#fff;border:1px solid #e7ece9}
.item-check{display:flex;align-items:center;gap:10px}
.item-check span.checked{text-decoration:line-through;color:#63706a}
.empty-state,.empty-nested{display:grid;place-items:center;padding:24px;border:1px dashed #dfe6e2;border-radius:12px;color:#63706a;background:#fafbfc}
.empty-nested{margin-top:12px;padding:16px}
.gantt-card{display:grid;gap:18px;padding:18px;margin-bottom:18px}
.gantt-range{color:#1f7a5a;font-size:13px;font-weight:700}
.gantt-chart{display:grid;gap:14px}
.gantt-scale{position:relative;height:24px;margin-left:220px;border-bottom:1px dashed #dfe6e2}
.gantt-tick{position:absolute;top:0;transform:translateX(-50%);color:#63706a;font-size:12px;white-space:nowrap}
.gantt-row{display:grid;grid-template-columns:200px minmax(0,1fr);gap:20px;align-items:center}
.gantt-label{display:grid;gap:4px}
.gantt-label span{color:#63706a;font-size:13px}
.gantt-track{position:relative;height:36px;border-radius:10px;background:linear-gradient(90deg,#f8faf9 0%,#f2f6f4 100%);overflow:hidden;border:1px solid #e3e9e5}
.gantt-track-grid{position:absolute;inset:0;background-image:linear-gradient(90deg,rgba(31,122,90,.08) 1px,transparent 1px);background-size:20% 100%}
.gantt-bar{position:absolute;top:6px;bottom:6px;display:flex;align-items:center;padding:0 12px;border-radius:8px;background:linear-gradient(90deg,#1f7a5a 0%,#3aa37e 100%);color:#fff;font-size:12px;font-weight:700;overflow:hidden;min-width:72px}
.gantt-bar span{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.nested-editor{display:grid;gap:14px;padding:16px;border:1px solid #dfe6e2;border-radius:12px;background:#fafbfc}
.nested-editor-head,.nested-point-head,.nested-item-head{display:flex;align-items:center;justify-content:space-between;gap:12px}
.nested-title{font-size:14px;font-weight:800;color:#18201d}
.nested-copy{margin-top:4px;color:#63706a;font-size:13px}
.nested-point-card{display:grid;gap:12px;padding:14px;border-radius:12px;background:#fff;border:1px solid #e7ece9}
.nested-item-row{display:flex;align-items:end;gap:12px}
.grow{flex:1}
.checkbox-inline{min-width:96px}
.checkbox-inline input{width:auto;justify-self:start}
.data-table{width:100%;border-collapse:collapse}
.data-table th,.data-table td{padding:14px 12px;border-bottom:1px solid #e7ece9;text-align:left;vertical-align:top}
.data-table th{color:#63706a;font-size:13px;font-weight:700}
.link-btn{border:0;background:transparent;padding:0;color:#1f7a5a;font-weight:700}
.link-btn.danger{color:#b53232}
.small{padding:8px 10px;font-size:12px}
.full{width:100%}
.modal-overlay{position:fixed;inset:0;display:grid;place-items:center;padding:24px;background:rgba(24,32,29,.34);z-index:999}
.modal-card{width:min(560px,calc(100vw - 36px));max-height:calc(100vh - 48px);overflow:auto;background:#fff;border:1px solid #dfe6e2;border-radius:16px;box-shadow:0 20px 48px rgba(31,55,48,.24);padding:22px}
.record-form{display:grid;gap:14px}
.form-error{margin:0;color:#b53232;font-size:13px}
.checkbox-field{display:flex;align-items:center;justify-content:space-between}
@media (max-width:1280px){
  .toolbar-card{grid-template-columns:1fr}
  .stat-grid{grid-template-columns:repeat(2,minmax(0,1fr))}
  .gantt-scale{margin-left:0}
  .gantt-row{grid-template-columns:1fr}
}
@media (max-width:760px){
  .schedule-page{padding:18px}
  .page-header,.panel-header,.control-line-header,.control-point-header,.form-head,.grid-two,.gantt-head{flex-direction:column}
  .stat-grid{grid-template-columns:1fr}
  .gantt-bar{min-width:56px;padding:0 8px}
  .nested-editor-head,.nested-point-head,.nested-item-head,.nested-item-row{align-items:stretch;flex-direction:column}
}
</style>

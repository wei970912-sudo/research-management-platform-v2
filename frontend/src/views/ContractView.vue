<template>
  <section class="contract-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">合同管理</p>
        <h1>课题合同金额管理</h1>
      </div>
      <div class="top-actions">
        <button class="secondary" type="button" @click="openTaskBookCreate">新增任务书项</button>
        <button class="primary" type="button" @click="openContractCreate">新增合同</button>
      </div>
    </header>

    <main class="layout">
      <section class="summary-view">
        <div class="section-head">
          <div>
            <h2>任务书项与合同</h2>
            <p>{{ summaryText }}</p>
          </div>
          <div class="summary-tools">
            <label class="search-box compact">
              <span>课题</span>
              <select v-model="projectFilter">
                <option value="all">全部课题</option>
                <option v-for="item in projectOptions" :key="item.id" :value="item.id">{{ item.name }}</option>
              </select>
            </label>
            <label class="search-box">
              <span>搜索</span>
              <input v-model.trim="query" type="search" placeholder="任务书项、合同名称、状态" />
            </label>
          </div>
        </div>

        <div class="stat-grid">
          <article class="stat-card">
            <span>任务书项</span>
            <strong>{{ visibleTaskBookItems.length }}</strong>
          </article>
          <article class="stat-card">
            <span>合同总数</span>
            <strong>{{ visibleContracts.length }}</strong>
          </article>
          <article class="stat-card">
            <span>累计使用金额</span>
            <strong>{{ amountText(totalUsedAmount) }}</strong>
          </article>
          <article class="stat-card">
            <span>剩余可用金额</span>
            <strong>{{ amountText(totalRemainingAmount) }}</strong>
          </article>
        </div>

        <section class="table-section">
          <div class="sub-head">
            <h3>任务书项</h3>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>任务书项</th>
                  <th>所属课题</th>
                  <th>预算</th>
                  <th>累计使用</th>
                  <th>剩余金额</th>
                  <th>合同数</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="item in visibleTaskBookItems"
                  :key="item.id"
                  :class="{ 'active-row': selectedType === 'taskBook' && selectedId === item.id }"
                  @click="selectTaskBook(item.id)"
                >
                  <td class="main-cell"><strong>{{ item.name }}</strong><span>{{ item.note || "暂无备注" }}</span></td>
                  <td>{{ item.projectName }}</td>
                  <td>{{ amountText(item.budget) }}</td>
                  <td>{{ amountText(item.usedAmount) }}</td>
                  <td>{{ amountText(item.remainingAmount) }}</td>
                  <td>{{ item.contractCount }}</td>
                  <td>
                    <button class="link-btn" type="button" @click.stop="openTaskBookEdit(item)">编辑</button>
                    <button class="link-btn danger-link" type="button" @click.stop="removeTaskBookItem(item.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="!visibleTaskBookItems.length">
                  <td colspan="7" class="empty">暂无任务书项，请先新增任务书项。</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section class="table-section">
          <div class="sub-head">
            <h3>合同</h3>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>合同名称</th>
                  <th>任务书项</th>
                  <th>状态</th>
                  <th>合同金额</th>
                  <th>累计使用 / 剩余</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="item in visibleContracts"
                  :key="item.id"
                  :class="{ 'active-row': selectedType === 'contract' && selectedId === item.id }"
                  @click="selectContract(item.id)"
                >
                  <td class="main-cell"><strong>{{ item.name }}</strong><span>{{ item.projectName }}</span></td>
                  <td>{{ item.taskName }}</td>
                  <td><span class="badge" :class="statusClass(item.status)">{{ item.status }}</span></td>
                  <td>{{ amountText(item.amount) }}</td>
                  <td>{{ amountText(item.usedAmount) }} / {{ amountText(item.remainingAmount) }}</td>
                  <td>
                    <button class="link-btn" type="button" @click.stop="openContractEdit(item)">编辑</button>
                    <button class="link-btn danger-link" type="button" @click.stop="removeContract(item.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="!visibleContracts.length">
                  <td colspan="6" class="empty">暂无合同记录。</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <aside class="detail-view">
        <section class="panel">
          <template v-if="editingMode === 'taskBook'">
            <form class="contract-form" @submit.prevent="submitTaskBookForm">
              <div class="form-head">
                <div>
                  <p class="eyebrow">{{ editingId ? "编辑任务书项" : "新增任务书项" }}</p>
                  <h2>任务书项信息</h2>
                </div>
                <button class="ghost" type="button" @click="cancelEdit">取消</button>
              </div>
              <label>
                <span>所属课题</span>
                <select v-model="taskBookForm.projectId" required>
                  <option v-for="item in projects" :key="item.id" :value="item.id">{{ item.name }}</option>
                </select>
              </label>
              <label><span>任务书项名称</span><input v-model.trim="taskBookForm.name" type="text" required /></label>
              <label><span>预算金额（元）</span><input v-model.number="taskBookForm.budget" type="number" min="0" step="0.01" required /></label>
              <label><span>备注</span><textarea v-model.trim="taskBookForm.note" rows="4"></textarea></label>
              <button class="primary full" type="submit">保存任务书项</button>
            </form>
          </template>

          <template v-else-if="editingMode === 'contract'">
            <form class="contract-form" @submit.prevent="submitContractForm">
              <div class="form-head">
                <div>
                  <p class="eyebrow">{{ editingId ? "编辑合同" : "新增合同" }}</p>
                  <h2>合同信息</h2>
                </div>
                <button class="ghost" type="button" @click="cancelEdit">取消</button>
              </div>
              <label>
                <span>所属课题</span>
                <select v-model="contractForm.projectId" required>
                  <option v-for="item in projects" :key="item.id" :value="item.id">{{ item.name }}</option>
                </select>
              </label>
              <label>
                <span>对应任务书项</span>
                <select v-model="contractForm.taskBookItemId" required>
                  <option v-for="item in taskBookOptionsForForm" :key="item.id" :value="item.id">{{ item.name }}</option>
                </select>
              </label>
              <div v-if="selectedTaskBookForForm" class="task-book-hint">
                <span>预算 {{ amountText(selectedTaskBookForForm.budget) }}</span>
                <span>累计使用 {{ amountText(selectedTaskBookForForm.usedAmount) }}</span>
                <span>剩余金额 {{ amountText(selectedTaskBookForForm.remainingAmount) }}</span>
              </div>
              <label><span>合同名称</span><input v-model.trim="contractForm.name" type="text" required /></label>
              <label>
                <span>签订状态</span>
                <select v-model="contractForm.status" required>
                  <option value="已签订">已签订</option>
                  <option value="待签订">待签订</option>
                  <option value="已完成">已完成</option>
                  <option value="已取消">已取消</option>
                </select>
              </label>
              <label><span>合同金额（元）</span><input v-model.number="contractForm.amount" type="number" min="0" step="0.01" required /></label>
              <label><span>签订单位</span><input v-model.trim="contractForm.signingUnit" type="text" required /></label>
              <div class="two-col">
                <label><span>签订日期</span><input v-model="contractForm.signingDate" type="date" required /></label>
                <label><span>交付日期</span><input v-model="contractForm.deliveryDate" type="date" required /></label>
              </div>
              <label><span>合同内容</span><textarea v-model.trim="contractForm.content" rows="4" required></textarea></label>
              <label><span>交付条件</span><textarea v-model.trim="contractForm.deliveryTerms" rows="3" required></textarea></label>
              <label><span>付款条件</span><textarea v-model.trim="contractForm.paymentTerms" rows="3" required></textarea></label>
              <button class="primary full" type="submit">保存合同</button>
            </form>
          </template>

          <template v-else-if="selectedType === 'taskBook' && selectedTaskBookItem">
            <div class="detail-header">
              <div>
                <p class="eyebrow">{{ selectedTaskBookItem.projectName }}</p>
                <h2 class="detail-name">{{ selectedTaskBookItem.name }}</h2>
              </div>
            </div>
            <dl class="detail-meta">
              <div><dt>预算金额</dt><dd>{{ amountText(selectedTaskBookItem.budget) }}</dd></div>
              <div><dt>累计使用</dt><dd>{{ amountText(selectedTaskBookItem.usedAmount) }}</dd></div>
              <div><dt>剩余金额</dt><dd>{{ amountText(selectedTaskBookItem.remainingAmount) }}</dd></div>
              <div><dt>已关联合同</dt><dd>{{ selectedTaskBookItem.contractCount }} 份</dd></div>
            </dl>
            <div class="detail-block">
              <h3>备注</h3>
              <p>{{ selectedTaskBookItem.note || "暂无备注" }}</p>
            </div>
            <div class="detail-block">
              <h3>对应合同</h3>
              <ul class="contract-list">
                <li v-for="contract in contractsByTaskBookItem(selectedTaskBookItem.id)" :key="contract.id">
                  <strong>{{ contract.name }}</strong>
                  <span>{{ amountText(contract.amount) }} / {{ contract.status }}</span>
                </li>
                <li v-if="!contractsByTaskBookItem(selectedTaskBookItem.id).length" class="empty">暂无关联合同</li>
              </ul>
            </div>
            <div class="actions">
              <button class="secondary" type="button" @click="openTaskBookEdit(selectedTaskBookItem)">编辑</button>
              <button class="primary" type="button" @click="openContractCreate(selectedTaskBookItem)">新增关联合同</button>
            </div>
          </template>

          <template v-else-if="selectedType === 'contract' && selectedContract">
            <div class="detail-header">
              <div>
                <p class="eyebrow">{{ selectedContract.projectName }}</p>
                <h2 class="detail-name">{{ selectedContract.name }}</h2>
              </div>
              <span class="badge" :class="statusClass(selectedContract.status)">{{ selectedContract.status }}</span>
            </div>
            <dl class="detail-meta">
              <div><dt>合同金额</dt><dd>{{ amountText(selectedContract.amount) }}</dd></div>
              <div><dt>对应任务书项</dt><dd>{{ selectedContract.taskName }}</dd></div>
              <div><dt>任务书预算</dt><dd>{{ amountText(selectedContract.taskBudget) }}</dd></div>
              <div><dt>累计使用</dt><dd>{{ amountText(selectedContract.usedAmount) }}</dd></div>
              <div><dt>剩余金额</dt><dd>{{ amountText(selectedContract.remainingAmount) }}</dd></div>
              <div><dt>签订单位</dt><dd>{{ selectedContract.signingUnit }}</dd></div>
            </dl>
            <div class="detail-block">
              <h3>合同内容</h3>
              <p>{{ selectedContract.content }}</p>
            </div>
            <div class="detail-block">
              <h3>交付条件</h3>
              <p>{{ selectedContract.deliveryTerms }}</p>
            </div>
            <div class="detail-block">
              <h3>付款条件</h3>
              <p>{{ selectedContract.paymentTerms }}</p>
            </div>
            <div class="actions">
              <button class="secondary" type="button" @click="openContractEdit(selectedContract)">编辑</button>
              <button class="danger" type="button" @click="removeContract(selectedContract.id)">删除</button>
            </div>
          </template>

          <div v-else class="placeholder">
            <h2>选择一条记录</h2>
            <p>先新增任务书项，再让合同关联对应任务书项查看累计使用和剩余金额。</p>
          </div>
        </section>
      </aside>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  createContract,
  createTaskBookItem,
  deleteContract,
  deleteTaskBookItem,
  fetchContracts,
  fetchProjects,
  fetchTaskBookItems,
  updateContract,
  updateTaskBookItem
} from "../api/modules";

const contracts = ref([]);
const taskBookItems = ref([]);
const projects = ref([]);
const query = ref("");
const projectFilter = ref("all");
const selectedId = ref("");
const selectedType = ref("taskBook");
const editingMode = ref("");
const editingId = ref("");

const taskBookForm = reactive({
  projectId: "",
  name: "",
  budget: 0,
  note: ""
});

const contractForm = reactive({
  projectId: "",
  taskBookItemId: "",
  name: "",
  status: "待签订",
  amount: 0,
  signingUnit: "",
  signingDate: "",
  deliveryDate: "",
  content: "",
  deliveryTerms: "",
  paymentTerms: ""
});

const load = async () => {
  const [contractData, taskBookData, projectData] = await Promise.all([
    fetchContracts(),
    fetchTaskBookItems(),
    fetchProjects()
  ]);
  contracts.value = contractData;
  taskBookItems.value = taskBookData;
  projects.value = projectData;

  if (!selectedId.value) {
    if (taskBookItems.value.length) {
      selectedType.value = "taskBook";
      selectedId.value = taskBookItems.value[0].id;
    } else if (contracts.value.length) {
      selectedType.value = "contract";
      selectedId.value = contracts.value[0].id;
    }
  }
};

const visibleTaskBookItems = computed(() =>
  taskBookItems.value.filter((item) => {
    const projectMatched = projectFilter.value === "all" || item.projectId === projectFilter.value;
    const queryMatched = !query.value || [item.name, item.projectName, item.note].join(" ").includes(query.value);
    return projectMatched && queryMatched;
  })
);

const visibleContracts = computed(() =>
  contracts.value.filter((item) => {
    const projectMatched = projectFilter.value === "all" || item.projectId === projectFilter.value;
    const queryMatched = !query.value || [item.name, item.projectName, item.status, item.taskName].join(" ").includes(query.value);
    return projectMatched && queryMatched;
  })
);

const projectOptions = computed(() => projects.value);
const totalUsedAmount = computed(() => visibleTaskBookItems.value.reduce((sum, item) => sum + Number(item.usedAmount || 0), 0));
const totalRemainingAmount = computed(() => visibleTaskBookItems.value.reduce((sum, item) => sum + Number(item.remainingAmount || 0), 0));
const summaryText = computed(() => `共 ${visibleTaskBookItems.value.length} 个任务书项，${visibleContracts.value.length} 份合同`);

const selectedTaskBookItem = computed(() => taskBookItems.value.find((item) => item.id === selectedId.value) || null);
const selectedContract = computed(() => contracts.value.find((item) => item.id === selectedId.value) || null);
const taskBookOptionsForForm = computed(() => taskBookItems.value.filter((item) => item.projectId === contractForm.projectId));
const selectedTaskBookForForm = computed(() => taskBookItems.value.find((item) => item.id === contractForm.taskBookItemId) || null);

const amountText = (value) =>
  Number(value || 0).toLocaleString("zh-CN", { style: "currency", currency: "CNY", minimumFractionDigits: 2 });

const statusClass = (status) => {
  if (status === "已完成" || status === "已签订") return "done";
  if (status === "已取消") return "cancelled";
  return "pending";
};

const selectTaskBook = (id) => {
  selectedType.value = "taskBook";
  selectedId.value = id;
  editingMode.value = "";
};

const selectContract = (id) => {
  selectedType.value = "contract";
  selectedId.value = id;
  editingMode.value = "";
};

const contractsByTaskBookItem = (taskBookItemId) => contracts.value.filter((item) => item.taskBookItemId === taskBookItemId);

const resetTaskBookForm = () => {
  taskBookForm.projectId = projects.value[0]?.id || "";
  taskBookForm.name = "";
  taskBookForm.budget = 0;
  taskBookForm.note = "";
};

const resetContractForm = () => {
  contractForm.projectId = projects.value[0]?.id || "";
  contractForm.taskBookItemId = taskBookItems.value.find((item) => item.projectId === contractForm.projectId)?.id || "";
  contractForm.name = "";
  contractForm.status = "待签订";
  contractForm.amount = 0;
  contractForm.signingUnit = "";
  contractForm.signingDate = "";
  contractForm.deliveryDate = "";
  contractForm.content = "";
  contractForm.deliveryTerms = "";
  contractForm.paymentTerms = "";
};

const openTaskBookCreate = () => {
  editingId.value = "";
  editingMode.value = "taskBook";
  resetTaskBookForm();
};

const openTaskBookEdit = (item) => {
  editingId.value = item.id;
  editingMode.value = "taskBook";
  taskBookForm.projectId = item.projectId;
  taskBookForm.name = item.name;
  taskBookForm.budget = item.budget;
  taskBookForm.note = item.note || "";
};

const openContractCreate = (taskBookItem = null) => {
  editingId.value = "";
  editingMode.value = "contract";
  resetContractForm();
  if (taskBookItem) {
    contractForm.projectId = taskBookItem.projectId;
    contractForm.taskBookItemId = taskBookItem.id;
  }
};

const openContractEdit = (item) => {
  editingId.value = item.id;
  editingMode.value = "contract";
  contractForm.projectId = item.projectId;
  contractForm.taskBookItemId = item.taskBookItemId;
  contractForm.name = item.name;
  contractForm.status = item.status;
  contractForm.amount = item.amount;
  contractForm.signingUnit = item.signingUnit;
  contractForm.signingDate = item.signingDate;
  contractForm.deliveryDate = item.deliveryDate;
  contractForm.content = item.content;
  contractForm.deliveryTerms = item.deliveryTerms;
  contractForm.paymentTerms = item.paymentTerms;
};

const cancelEdit = () => {
  editingMode.value = "";
  editingId.value = "";
};

const submitTaskBookForm = async () => {
  const payload = { ...taskBookForm };
  if (editingId.value) {
    await updateTaskBookItem(editingId.value, payload);
  } else {
    await createTaskBookItem(payload);
  }
  editingMode.value = "";
  editingId.value = "";
  await load();
};

const submitContractForm = async () => {
  const payload = { ...contractForm };
  if (editingId.value) {
    await updateContract(editingId.value, payload);
  } else {
    await createContract(payload);
  }
  editingMode.value = "";
  editingId.value = "";
  await load();
};

const removeTaskBookItem = async (id) => {
  await deleteTaskBookItem(id);
  editingMode.value = "";
  await load();
};

const removeContract = async (id) => {
  await deleteContract(id);
  editingMode.value = "";
  await load();
};

onMounted(load);
</script>

<style scoped>
.contract-page{min-height:100vh;background:#f7f8fb}
.topbar,.section-head,.detail-header,.form-head,.sub-head{display:flex;align-items:flex-start;justify-content:space-between;gap:18px}
.topbar{padding:28px 40px 20px;background:#fff;border-bottom:1px solid #dfe6e2}
.top-actions,.summary-tools,.actions{display:flex;gap:10px;flex-wrap:wrap}
.layout{display:grid;grid-template-columns:minmax(0,1.35fr) minmax(340px,.65fr);gap:24px;padding:24px 40px 40px}
.summary-view,.panel{background:#fff;border:1px solid #dfe6e2;border-radius:8px;box-shadow:0 16px 36px rgba(31,55,48,.12)}
.summary-view{overflow:hidden}
.section-head{align-items:end;padding:22px;border-bottom:1px solid #dfe6e2}
.section-head p{margin:7px 0 0;color:#63706a}
.table-section{padding:0 22px 22px}
.sub-head{padding:18px 0 10px}
.eyebrow{margin:0;color:#1f7a5a;font-size:12px;font-weight:700;text-transform:uppercase}
h1,h2,h3,p{margin:0}h1{margin-top:4px;font-size:30px}h2{font-size:22px}
.search-box{display:grid;gap:8px;min-width:170px;color:#63706a;font-size:13px}
.search-box input,.search-box select,.contract-form input,.contract-form select,.contract-form textarea{width:100%;border:1px solid #dfe6e2;border-radius:8px;padding:11px 12px;background:#fff}
.primary,.secondary,.ghost{border-radius:8px;padding:10px 15px}
.primary{border:0;background:#1f7a5a;color:#fff;font-weight:700}
.secondary,.ghost{border:1px solid #dfe6e2;background:#fff;color:#18201d}
.stat-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;padding:18px 22px}
.stat-card{display:grid;gap:8px;min-height:96px;padding:16px;border:1px solid #dfe6e2;border-radius:8px;background:#f4f7f5}
.stat-card span{color:#63706a;font-size:13px;font-weight:700}
.stat-card strong{font-size:22px}
.table-wrap{overflow-x:auto}
table{width:100%;min-width:860px;border-collapse:collapse}
th,td{padding:15px 12px;border-bottom:1px solid #dfe6e2;text-align:left;vertical-align:top}
th{color:#63706a;font-size:13px;font-weight:700}
tbody tr{cursor:pointer}
tbody tr:hover,tbody tr.active-row{background:#eef6f2}
.main-cell strong,.main-cell span{display:block}
.main-cell span{margin-top:5px;color:#63706a;font-size:13px}
.link-btn{color:#1f7a5a;background:transparent;padding:4px 6px;font-weight:700;border:0}
.danger-link{color:#b53232}
.badge{display:inline-flex;align-items:center;min-height:28px;border-radius:8px;padding:4px 9px;background:#e8f1ff;color:#22518f;font-weight:700;font-size:13px}
.badge.pending{background:#fff1dc;color:#9c6428}
.badge.done{background:#e8f6ec;color:#155d43}
.badge.cancelled{background:#f9e7e7;color:#b53232}
.detail-view{min-width:0}
.panel{position:sticky;top:22px;min-height:420px;padding:22px;overflow-y:auto;max-height:calc(100vh - 100px)}
.placeholder,.empty{color:#63706a}
.placeholder{display:grid;place-items:center;min-height:260px;gap:8px;text-align:center}
.detail-meta{display:grid;gap:12px;margin:18px 0}
.detail-meta div{padding:13px;background:#f4f7f5;border-radius:8px}
dt{color:#63706a;font-size:13px;font-weight:700}
dd{margin:7px 0 0;overflow-wrap:anywhere}
.detail-block{display:grid;gap:10px;margin-top:18px}
.detail-block p{white-space:pre-wrap;line-height:1.75;color:#2b3732;overflow-wrap:anywhere}
.contract-list{display:grid;gap:10px;margin:0;padding:0;list-style:none}
.contract-list li{display:flex;justify-content:space-between;gap:12px;padding:12px;border:1px solid #dfe6e2;border-radius:8px;background:#fff}
.contract-form{display:grid;gap:15px}
.contract-form label{display:grid;gap:8px;color:#63706a;font-size:13px;font-weight:700}
.task-book-hint{display:grid;gap:6px;padding:12px;border:1px solid #dfe6e2;border-radius:8px;background:#f4f7f5;color:#2b3732;font-size:13px;font-weight:700}
.two-col{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.danger{border-radius:8px;padding:9px 13px;background:#fff;border:1px solid #e6c4c4;color:#b53232}
.full{width:100%}
@media (max-width:1120px){.layout{grid-template-columns:1fr;padding:18px}.topbar{padding:22px 18px 16px}.panel{position:static;max-height:none}}
@media (max-width:760px){.topbar,.section-head,.detail-header,.form-head,.summary-tools,.top-actions,.actions,.two-col,.sub-head{align-items:stretch;flex-direction:column}.stat-grid{grid-template-columns:1fr}h1{font-size:26px}}
</style>

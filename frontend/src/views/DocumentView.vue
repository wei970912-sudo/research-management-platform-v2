<template>
  <section class="document-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">节点文件管理</p>
        <h1>节点文件归档</h1>
        <p class="subtitle">支持新增节点文件、选择已有项目，并上传真实附件文件。</p>
      </div>
      <div class="top-actions">
        <button class="primary" type="button" @click="openCreate">新增节点文件</button>
      </div>
    </header>

    <main class="layout">
      <section class="summary-view">
        <div class="section-head">
          <div>
            <h2>节点文件列表</h2>
            <p>{{ recordText }}</p>
          </div>
          <div class="summary-tools">
            <label class="search-box compact">
              <span>项目筛选</span>
              <select v-model="projectFilter">
                <option value="all">全部项目</option>
                <option v-for="item in projectOptions" :key="item" :value="item">{{ item }}</option>
              </select>
            </label>
            <label class="search-box compact">
              <span>状态筛选</span>
              <select v-model="statusFilter">
                <option value="all">全部状态</option>
                <option v-for="status in statusOptions" :key="status" :value="status">{{ status }}</option>
              </select>
            </label>
            <label class="search-box">
              <span>搜索</span>
              <input
                v-model.trim="query"
                type="search"
                placeholder="搜索节点名称、项目名称、负责人、说明或附件名"
              />
            </label>
          </div>
        </div>

        <div class="stat-grid">
          <article class="stat-card">
            <span>节点总数</span>
            <strong>{{ visibleDocuments.length }} 个</strong>
          </article>
          <article class="stat-card">
            <span>附件总数</span>
            <strong>{{ attachmentTotal }} 个</strong>
          </article>
          <article class="stat-card">
            <span>已归档节点</span>
            <strong>{{ archivedCount }} 个</strong>
          </article>
          <article class="stat-card">
            <span>待补充节点</span>
            <strong>{{ pendingCount }} 个</strong>
          </article>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>项目 / 节点</th>
                <th>状态</th>
                <th>负责人</th>
                <th>计划日期</th>
                <th>附件数</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in visibleDocuments"
                :key="item.id"
                :class="{ 'active-row': item.id === selectedId }"
              >
                <td class="main-cell">
                  <strong>{{ item.nodeName }}</strong>
                  <span>{{ item.projectName }}</span>
                </td>
                <td><span class="badge" :class="statusClass(item.status)">{{ item.status }}</span></td>
                <td>{{ item.owner }}</td>
                <td>{{ item.targetDate }}</td>
                <td>{{ item.attachmentCount }} 个</td>
                <td><button class="link-btn" type="button" @click="selectedId = item.id">查看</button></td>
              </tr>
            </tbody>
          </table>

          <div v-if="!visibleDocuments.length" class="empty-state">
            <h3>还没有节点文件记录</h3>
            <p>点击“新增节点文件”，先选择项目，再保存并上传真实附件。</p>
            <button class="primary" type="button" @click="openCreate">立即新增</button>
          </div>
        </div>
      </section>

      <aside class="detail-view">
        <section class="panel">
          <template v-if="editing">
            <form class="record-form" @submit.prevent="submitForm">
              <div class="form-head">
                <div>
                  <p class="eyebrow">{{ editingId ? "编辑节点文件" : "新增节点文件" }}</p>
                  <h2>{{ editingId ? "修改节点文件" : "创建节点文件" }}</h2>
                </div>
                <button class="ghost" type="button" @click="cancelEdit">取消</button>
              </div>

              <div v-if="!projects.length" class="project-warning">
                当前没有可选项目，请先到项目管理中创建项目。
              </div>

              <label>
                <span>所属项目</span>
                <select v-model="form.projectId" :disabled="!projects.length" required>
                  <option value="" disabled>请选择项目</option>
                  <option v-for="item in projects" :key="item.id" :value="item.id">{{ item.name }}</option>
                </select>
              </label>

              <label>
                <span>节点名称</span>
                <input v-model.trim="form.nodeName" type="text" placeholder="请输入节点名称" required />
              </label>

              <div class="field-grid">
                <label>
                  <span>节点状态</span>
                  <select v-model="form.status" required>
                    <option v-for="status in statusOptions" :key="status" :value="status">{{ status }}</option>
                  </select>
                </label>
                <label>
                  <span>计划日期</span>
                  <input v-model="form.targetDate" type="date" required />
                </label>
              </div>

              <label>
                <span>负责人</span>
                <input v-model.trim="form.owner" type="text" placeholder="请输入负责人" required />
              </label>

              <label>
                <span>节点说明</span>
                <textarea v-model.trim="form.description" rows="4" placeholder="请输入节点说明" required></textarea>
              </label>

              <label>
                <span>归档备注</span>
                <textarea v-model.trim="form.notes" rows="4" placeholder="请输入归档备注" required></textarea>
              </label>

              <label>
                <span>上传附件</span>
                <input ref="fileInputRef" type="file" multiple @change="handleFilesChange" />
              </label>

              <div class="detail-block upload-block">
                <h3>待上传文件</h3>
                <p v-if="!pendingFileNames.length">暂未选择文件</p>
                <div v-for="name in pendingFileNames" :key="name" class="attachment-item">
                  <span>{{ name }}</span>
                </div>
              </div>

              <div v-if="editingId && attachmentList(form.attachmentNames).length" class="detail-block upload-block">
                <h3>已有关联附件</h3>
                <div v-for="name in attachmentList(form.attachmentNames)" :key="name" class="attachment-item">
                  <a :href="documentAttachmentUrl(editingId, name)" target="_blank" rel="noreferrer">{{ name }}</a>
                </div>
              </div>

              <button class="primary full" :disabled="submitting || !projects.length" type="submit">
                {{ submitting ? "保存中..." : "保存节点文件" }}
              </button>
            </form>
          </template>

          <template v-else-if="selectedDocument">
            <div class="detail-header">
              <div>
                <p class="eyebrow">{{ selectedDocument.projectName }}</p>
                <h2 class="detail-name">{{ selectedDocument.nodeName }}</h2>
              </div>
              <span class="badge" :class="statusClass(selectedDocument.status)">{{ selectedDocument.status }}</span>
            </div>

            <dl class="detail-meta">
              <div><dt>负责人</dt><dd>{{ selectedDocument.owner }}</dd></div>
              <div><dt>计划日期</dt><dd>{{ selectedDocument.targetDate }}</dd></div>
              <div><dt>附件数量</dt><dd>{{ selectedDocument.attachmentCount }} 个</dd></div>
            </dl>

            <div class="detail-block">
              <h3>节点说明</h3>
              <p>{{ selectedDocument.description }}</p>
            </div>

            <div class="detail-block">
              <h3>归档备注</h3>
              <p>{{ selectedDocument.notes }}</p>
            </div>

            <div class="detail-block">
              <h3>附件列表</h3>
              <div class="attachment-list">
                <div v-for="name in attachmentList(selectedDocument.attachmentNames)" :key="name" class="attachment-item">
                  <a :href="documentAttachmentUrl(selectedDocument.id, name)" target="_blank" rel="noreferrer">{{ name }}</a>
                </div>
                <p v-if="!attachmentList(selectedDocument.attachmentNames).length">暂无附件</p>
              </div>
            </div>

            <div class="actions">
              <button class="secondary" type="button" @click="openEdit(selectedDocument)">编辑</button>
              <button class="danger" type="button" @click="remove(selectedDocument.id)">删除</button>
            </div>
          </template>

          <div v-else class="placeholder">
            <h2>选择一个节点文件</h2>
            <p>左侧查看节点文件详情，或点击“新增节点文件”创建记录并上传附件。</p>
          </div>
        </section>
      </aside>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  buildDocumentAttachmentUrl,
  createDocument,
  deleteDocument,
  fetchDocuments,
  fetchProjects,
  updateDocument,
  uploadDocumentAttachments
} from "../api/modules";

const statusOptions = ["未开始", "进行中", "已完成", "已归档", "需补充"];

const documents = ref([]);
const projects = ref([]);
const selectedId = ref("");
const projectFilter = ref("all");
const statusFilter = ref("all");
const query = ref("");
const editing = ref(false);
const editingId = ref("");
const submitting = ref(false);
const pendingFiles = ref([]);
const fileInputRef = ref(null);

const form = reactive({
  projectId: "",
  nodeName: "",
  attachmentCount: 0,
  status: "进行中",
  owner: "",
  targetDate: "",
  description: "",
  notes: "",
  attachmentNames: ""
});

const pendingFileNames = computed(() => pendingFiles.value.map((file) => file.name));

const resetForm = () => {
  form.projectId = projects.value[0]?.id || "";
  form.nodeName = "";
  form.attachmentCount = 0;
  form.status = "进行中";
  form.owner = "";
  form.targetDate = "";
  form.description = "";
  form.notes = "";
  form.attachmentNames = "";
  pendingFiles.value = [];
  if (fileInputRef.value) {
    fileInputRef.value.value = "";
  }
};

const load = async () => {
  const [documentData, projectData] = await Promise.all([fetchDocuments(), fetchProjects()]);
  documents.value = documentData;
  projects.value = projectData;

  if (!selectedId.value || !documents.value.some((item) => item.id === selectedId.value)) {
    selectedId.value = documents.value[0]?.id || "";
  }

  if (!form.projectId && projects.value.length) {
    form.projectId = projects.value[0].id;
  }
};

const visibleDocuments = computed(() =>
  documents.value.filter((item) => {
    const matchedProject = projectFilter.value === "all" || item.projectName === projectFilter.value;
    const matchedStatus = statusFilter.value === "all" || item.status === statusFilter.value;
    const matchedQuery =
      !query.value ||
      [item.nodeName, item.projectName, item.owner, item.description, item.notes, item.attachmentNames]
        .join(" ")
        .includes(query.value);
    return matchedProject && matchedStatus && matchedQuery;
  })
);

const selectedDocument = computed(
  () => visibleDocuments.value.find((item) => item.id === selectedId.value) || visibleDocuments.value[0] || null
);
const projectOptions = computed(() => [...new Set(documents.value.map((item) => item.projectName).filter(Boolean))]);
const attachmentTotal = computed(() => visibleDocuments.value.reduce((sum, item) => sum + Number(item.attachmentCount || 0), 0));
const archivedCount = computed(() => visibleDocuments.value.filter((item) => item.status === "已归档").length);
const pendingCount = computed(() => visibleDocuments.value.filter((item) => item.status === "需补充").length);
const recordText = computed(() => `共 ${visibleDocuments.value.length} 个节点文件，${attachmentTotal.value} 个附件`);

const attachmentList = (text) =>
  String(text || "")
    .split(/[，,]/)
    .map((item) => item.trim())
    .filter(Boolean);

const documentAttachmentUrl = (documentId, fileName) => buildDocumentAttachmentUrl(documentId, fileName);

const handleFilesChange = (event) => {
  pendingFiles.value = Array.from(event.target.files || []);
};

const openCreate = () => {
  editing.value = true;
  editingId.value = "";
  resetForm();
};

const openEdit = (item) => {
  editing.value = true;
  editingId.value = item.id;
  form.projectId = item.projectId || projects.value.find((project) => project.name === item.projectName)?.id || "";
  form.nodeName = item.nodeName || "";
  form.attachmentCount = Number(item.attachmentCount || 0);
  form.status = item.status || "进行中";
  form.owner = item.owner || "";
  form.targetDate = item.targetDate || "";
  form.description = item.description || "";
  form.notes = item.notes || "";
  form.attachmentNames = item.attachmentNames || "";
  pendingFiles.value = [];
  if (fileInputRef.value) {
    fileInputRef.value.value = "";
  }
};

const cancelEdit = () => {
  editing.value = false;
  editingId.value = "";
  pendingFiles.value = [];
  if (fileInputRef.value) {
    fileInputRef.value.value = "";
  }
};

const submitForm = async () => {
  submitting.value = true;
  try {
    const existingNames = attachmentList(form.attachmentNames);
    const payload = {
      ...form,
      attachmentCount: existingNames.length,
      attachmentNames: existingNames.join(", ")
    };

    const savedDocument = editingId.value
      ? await updateDocument(editingId.value, payload)
      : await createDocument(payload);

    if (pendingFiles.value.length) {
      await uploadDocumentAttachments(savedDocument.id, pendingFiles.value);
    }

    editing.value = false;
    editingId.value = "";
    await load();
  } finally {
    submitting.value = false;
    pendingFiles.value = [];
    if (fileInputRef.value) {
      fileInputRef.value.value = "";
    }
  }
};

const remove = async (id) => {
  await deleteDocument(id);
  await load();
};

const statusClass = (status) => {
  if (status === "已归档" || status === "已完成") return "done";
  if (status === "需补充") return "warning";
  if (status === "未开始") return "pending";
  return "active";
};

onMounted(load);
</script>

<style scoped>
.document-page{min-height:100vh;background:#f7f8fb}
.topbar,.section-head,.detail-header,.form-head{display:flex;align-items:flex-start;justify-content:space-between;gap:18px}
.topbar{padding:28px 40px 20px;background:#fff;border-bottom:1px solid #dfe6e2}
.subtitle{margin-top:8px;color:#63706a}
.top-actions,.actions{display:flex;gap:10px}
.layout{display:grid;grid-template-columns:minmax(0,1.35fr) minmax(360px,.65fr);gap:24px;padding:24px 40px 40px}
.summary-view,.panel{background:#fff;border:1px solid #dfe6e2;border-radius:8px;box-shadow:0 16px 36px rgba(31,55,48,.12)}
.summary-view{min-height:calc(100vh - 132px);overflow:hidden}
.section-head{align-items:end;padding:22px;border-bottom:1px solid #dfe6e2}
.section-head p{margin-top:7px;color:#63706a}
.summary-tools{display:flex;align-items:end;gap:12px}
.search-box{display:grid;gap:8px;min-width:280px;color:#63706a;font-size:13px}
.search-box.compact{min-width:150px}
.search-box input,.search-box select,.record-form input,.record-form select,.record-form textarea{width:100%;border:1px solid #dfe6e2;border-radius:8px;padding:11px 12px;background:#fff}
.stat-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;padding:18px 22px 2px}
.stat-card{display:grid;gap:8px;min-height:96px;padding:16px;border:1px solid #dfe6e2;border-radius:8px;background:#f4f7f5}
.stat-card span{color:#63706a;font-size:13px;font-weight:700}
.stat-card strong{font-size:22px;overflow-wrap:anywhere}
.table-wrap{overflow-x:auto;padding:16px 22px 22px}
table{width:100%;min-width:900px;border-collapse:collapse}
th,td{padding:15px 12px;border-bottom:1px solid #dfe6e2;text-align:left;vertical-align:top}
th{color:#63706a;font-size:13px;font-weight:700}
tbody tr:hover,tbody tr.active-row{background:#eef6f2}
.main-cell{max-width:300px}
.main-cell strong{display:block;overflow-wrap:anywhere}
.main-cell span{display:block;margin-top:6px;color:#63706a;font-size:13px;line-height:1.5}
.badge{display:inline-flex;align-items:center;min-height:28px;border-radius:8px;padding:4px 9px;background:#e8f1ff;color:#22518f;font-weight:700;font-size:13px}
.badge.pending{background:#fff1dc;color:#9c6428}
.badge.warning{background:#f9e7e7;color:#b53232}
.badge.done{background:#e8f6ec;color:#155d43}
.badge.active{background:#e8f1ff;color:#22518f}
.link-btn{color:#1f7a5a;background:transparent;padding:4px 0;font-weight:700;border:0}
.danger{border-radius:8px;padding:9px 13px;background:#fff;border:1px solid #e6c4c4;color:#b53232}
.detail-view{min-width:0}
.panel{position:sticky;top:22px;min-height:420px;padding:22px}
.placeholder,.empty-state{display:grid;place-content:center;min-height:260px;gap:10px;text-align:center;color:#63706a}
.detail-meta{display:grid;gap:12px;margin:18px 0}
.detail-meta div{padding:13px;background:#f4f7f5;border-radius:8px}
dt{color:#63706a;font-size:13px;font-weight:700}
dd{margin:7px 0 0;overflow-wrap:anywhere}
.detail-block{display:grid;gap:10px;margin-top:18px}
.detail-block p{white-space:pre-wrap;line-height:1.75;color:#2b3732;overflow-wrap:anywhere}
.record-form{display:grid;gap:15px}
.record-form label{display:grid;gap:8px;color:#63706a;font-size:13px;font-weight:700}
.field-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.attachment-list{display:grid;gap:10px}
.attachment-item{display:flex;align-items:center;justify-content:space-between;gap:10px;padding:11px 12px;border:1px solid #dfe6e2;border-radius:8px;background:#fff}
.attachment-item a{color:#1f7a5a;text-decoration:none;font-weight:700}
.project-warning{padding:12px 14px;border-radius:8px;background:#fff7e8;border:1px solid #f1d6a8;color:#8a5b11}
.upload-block{margin-top:0}
.full{width:100%}
h1,h2,h3,p{margin:0}
h1{margin-top:4px;font-size:30px}
h2{font-size:22px}
h3{font-size:16px}
.eyebrow{color:#1f7a5a;font-size:12px;font-weight:700;text-transform:uppercase}
.primary,.secondary,.ghost{border-radius:8px;padding:10px 15px}
.primary{border:0;background:#1f7a5a;color:#fff;font-weight:700}
.secondary,.ghost{border:1px solid #dfe6e2;background:#fff;color:#18201d}
@media (max-width:1120px){.layout{grid-template-columns:1fr;padding:18px}.topbar{padding:22px 18px 16px}.panel{position:static}}
@media (max-width:760px){.topbar,.section-head,.detail-header,.form-head,.summary-tools,.top-actions{align-items:stretch;flex-direction:column}.search-box{min-width:0}.stat-grid,.field-grid{grid-template-columns:1fr}h1{font-size:26px}}
</style>

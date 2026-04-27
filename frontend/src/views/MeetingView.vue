<template>
  <section class="meeting-page">
    <header class="topbar">
      <div>
        <p class="eyebrow">会议管理</p>
        <h1>会议记录</h1>
      </div>
      <button class="primary" type="button" @click="openCreate">新增会议</button>
    </header>

    <main class="layout">
      <section class="summary-view">
        <div class="section-head">
          <div>
            <h2>会议汇总</h2>
            <p>{{ meetingCountText }}</p>
          </div>
          <div class="summary-tools">
            <label class="search-box compact">
              <span>项目</span>
              <select v-model="projectFilter">
                <option value="all">全部项目</option>
                <option v-for="item in projectOptions" :key="item" :value="item">{{ item }}</option>
              </select>
            </label>
            <label class="search-box">
              <span>搜索</span>
              <input
                v-model.trim="query"
                type="search"
                placeholder="项目、主题、内容、地点或参会人员"
              />
            </label>
          </div>
        </div>

        <div class="filters">
          <button class="filter" :class="{ active: typeFilter === 'all' }" type="button" @click="typeFilter = 'all'">全部</button>
          <button class="filter" :class="{ active: typeFilter === '线上' }" type="button" @click="typeFilter = '线上'">线上</button>
          <button class="filter" :class="{ active: typeFilter === '线下' }" type="button" @click="typeFilter = '线下'">线下</button>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>会议日期</th>
                <th>会议主题</th>
                <th>会议形式</th>
                <th>附件</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in visibleMeetings" :key="item.id" :class="{ 'active-row': item.id === selectedId }">
                <td>{{ item.date }}</td>
                <td class="topic-cell">
                  <strong>{{ item.topic }}</strong>
                  <span>{{ item.projectName }}</span>
                </td>
                <td><span class="badge" :class="{ offline: item.type === '线下' }">{{ item.type }}</span></td>
                <td>{{ item.attachmentNames ? `${attachmentCount(item.attachmentNames)} 个附件` : "无附件" }}</td>
                <td><button class="link-btn" type="button" @click="selectedId = item.id">查看</button></td>
              </tr>
            </tbody>
          </table>
          <div v-if="!visibleMeetings.length" class="empty-state">
            <h3>还没有会议记录</h3>
            <p>新增一条会议，把日期、主题、纪要和会议附件一起整理进去。</p>
          </div>
        </div>
      </section>

      <aside class="detail-view">
        <section class="panel">
          <template v-if="editing">
            <form class="meeting-form" @submit.prevent="submitForm">
              <div class="form-head">
                <div>
                  <p class="eyebrow">{{ editingId ? "编辑会议" : "新增会议" }}</p>
                  <h2>会议内容</h2>
                </div>
                <button class="ghost" type="button" @click="cancelEdit">取消</button>
              </div>

              <label>
                <span>所属项目</span>
                <select v-model="form.projectId" required>
                  <option v-for="item in projects" :key="item.id" :value="item.id">{{ item.name }}</option>
                </select>
              </label>
              <label>
                <span>会议日期</span>
                <input v-model="form.date" type="date" required />
              </label>
              <label>
                <span>会议主题</span>
                <input v-model.trim="form.topic" type="text" required />
              </label>
              <label>
                <span>会议形式</span>
                <select v-model="form.type" required>
                  <option value="线上">线上</option>
                  <option value="线下">线下</option>
                </select>
              </label>
              <label>
                <span>会议地点/平台</span>
                <input v-model.trim="form.location" type="text" required />
              </label>
              <label>
                <span>参会人员</span>
                <input v-model.trim="form.attendees" type="text" required />
              </label>
              <label>
                <span>会议主持人</span>
                <input v-model.trim="form.host" type="text" required />
              </label>
              <label>
                <span>会议纪要</span>
                <textarea v-model.trim="form.summary" rows="8" required></textarea>
              </label>

              <div class="upload-block">
                <div class="upload-head">
                  <span>会议记录附件</span>
                  <small>支持 Word、PDF、TXT、WPS。保存会议后自动上传。</small>
                </div>
                <input
                  :key="fileInputKey"
                  type="file"
                  accept=".pdf,.doc,.docx,.txt,.wps"
                  multiple
                  @change="handleFileChange"
                />
                <div v-if="selectedFiles.length" class="attachment-list">
                  <div v-for="file in selectedFiles" :key="file.name + file.lastModified" class="attachment-item">
                    <span>{{ file.name }}</span>
                    <small>{{ formatFileSize(file.size) }}</small>
                  </div>
                </div>
                <p v-else class="helper-text">暂未选择文件。</p>
                <div v-if="existingAttachmentList.length" class="existing-block">
                  <h3>当前已上传附件</h3>
                  <div class="attachment-list">
                    <a
                      v-for="name in existingAttachmentList"
                      :key="name"
                      class="attachment-item link-item"
                      :href="attachmentUrl(editingId || selectedMeeting?.id, name)"
                      target="_blank"
                      rel="noopener"
                    >
                      <span>{{ name }}</span>
                      <small>下载</small>
                    </a>
                  </div>
                </div>
              </div>

              <button class="primary full" :disabled="submitting" type="submit">
                {{ submitting ? "保存中..." : "保存会议" }}
              </button>
            </form>
          </template>

          <template v-else-if="selectedMeeting">
            <div class="detail-header">
              <div>
                <p class="eyebrow detail-date">{{ selectedMeeting.date }}</p>
                <h2 class="detail-topic">{{ selectedMeeting.topic }}</h2>
              </div>
              <span class="badge" :class="{ offline: selectedMeeting.type === '线下' }">{{ selectedMeeting.type }}</span>
            </div>
            <dl class="detail-meta">
              <div>
                <dt>所属项目</dt>
                <dd>{{ selectedMeeting.projectName }}</dd>
              </div>
              <div>
                <dt>会议地点/平台</dt>
                <dd>{{ selectedMeeting.location || "未填写" }}</dd>
              </div>
              <div>
                <dt>参会人员</dt>
                <dd>{{ selectedMeeting.attendees || "未填写" }}</dd>
              </div>
            </dl>
            <div class="detail-block">
              <h3>会议纪要</h3>
              <p class="detail-content">{{ selectedMeeting.summary || "暂无详细内容" }}</p>
            </div>
            <div class="detail-block">
              <h3>附件</h3>
              <div class="attachment-list">
                <a
                  v-for="name in attachmentList(selectedMeeting.attachmentNames)"
                  :key="name"
                  class="attachment-item link-item"
                  :href="attachmentUrl(selectedMeeting.id, name)"
                  target="_blank"
                  rel="noopener"
                >
                  <span>{{ name }}</span>
                  <small>下载</small>
                </a>
                <p v-if="!attachmentList(selectedMeeting.attachmentNames).length">无附件</p>
              </div>
            </div>
            <div class="actions">
              <button class="secondary" type="button" @click="openEdit(selectedMeeting)">编辑</button>
              <button class="danger" type="button" @click="remove(selectedMeeting.id)">删除</button>
            </div>
          </template>

          <div v-else class="placeholder">
            <h2>选择一条会议</h2>
            <p>在左侧列表中查看详情，或新增一条会议并上传会议记录附件。</p>
          </div>
        </section>
      </aside>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import {
  createMeeting,
  deleteMeeting,
  fetchMeetings,
  fetchProjects,
  updateMeeting,
  uploadMeetingAttachments
} from "../api/modules";

const meetings = ref([]);
const projects = ref([]);
const selectedId = ref("");
const projectFilter = ref("all");
const typeFilter = ref("all");
const query = ref("");
const editing = ref(false);
const editingId = ref("");
const fileInputKey = ref(0);
const selectedFiles = ref([]);
const submitting = ref(false);

const form = reactive({
  projectId: "",
  topic: "",
  date: "",
  host: "",
  type: "线上",
  location: "",
  attendees: "",
  summary: "",
  attachmentNames: ""
});

const load = async () => {
  const [meetingData, projectData] = await Promise.all([fetchMeetings(), fetchProjects()]);
  meetings.value = meetingData;
  projects.value = projectData;
  selectedId.value = meetings.value[0]?.id || "";
  if (!editing.value) {
    form.projectId = projects.value[0]?.id || "";
  }
};

const visibleMeetings = computed(() =>
  meetings.value
    .filter((item) => {
      const matchedProject = projectFilter.value === "all" || item.projectName === projectFilter.value;
      const matchedType = typeFilter.value === "all" || item.type === typeFilter.value;
      const matchedQuery =
        !query.value ||
        [item.projectName, item.topic, item.host, item.location, item.attendees, item.summary].join(" ").includes(query.value);
      return matchedProject && matchedType && matchedQuery;
    })
    .sort((a, b) => String(b.date || "").localeCompare(String(a.date || "")))
);

const projectOptions = computed(() => [...new Set(meetings.value.map((item) => item.projectName))]);
const selectedMeeting = computed(
  () => visibleMeetings.value.find((item) => item.id === selectedId.value) || visibleMeetings.value[0] || null
);
const meetingCountText = computed(() => `共 ${visibleMeetings.value.length} 条记录`);
const existingAttachmentList = computed(() => attachmentList(form.attachmentNames));

const resetSelectedFiles = () => {
  selectedFiles.value = [];
  fileInputKey.value += 1;
};

const openCreate = () => {
  editing.value = true;
  editingId.value = "";
  form.projectId = projects.value[0]?.id || "";
  form.topic = "";
  form.date = "";
  form.host = "";
  form.type = "线上";
  form.location = "";
  form.attendees = "";
  form.summary = "";
  form.attachmentNames = "";
  resetSelectedFiles();
};

const openEdit = (meeting) => {
  editing.value = true;
  editingId.value = meeting.id;
  form.projectId = projects.value.find((item) => item.name === meeting.projectName)?.id || projects.value[0]?.id || "";
  form.topic = meeting.topic;
  form.date = meeting.date;
  form.host = meeting.host;
  form.type = meeting.type;
  form.location = meeting.location;
  form.attendees = meeting.attendees;
  form.summary = meeting.summary || "";
  form.attachmentNames = meeting.attachmentNames || "";
  resetSelectedFiles();
};

const cancelEdit = () => {
  editing.value = false;
  editingId.value = "";
  resetSelectedFiles();
};

const handleFileChange = (event) => {
  selectedFiles.value = Array.from(event.target.files || []);
};

const attachmentList = (text) =>
  String(text || "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);

const attachmentCount = (text) => attachmentList(text).length;

const attachmentUrl = (meetingId, name) => `/api/meetings/${meetingId}/attachments/${encodeURIComponent(name)}`;

const formatFileSize = (size) => {
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
};

const submitForm = async () => {
  submitting.value = true;
  try {
    let savedMeeting;
    if (editingId.value) {
      savedMeeting = await updateMeeting(editingId.value, { ...form });
    } else {
      savedMeeting = await createMeeting({ ...form });
    }

    if (selectedFiles.value.length) {
      savedMeeting = await uploadMeetingAttachments(savedMeeting.id, selectedFiles.value);
    }

    editing.value = false;
    editingId.value = "";
    resetSelectedFiles();
    await load();
    selectedId.value = savedMeeting.id;
  } finally {
    submitting.value = false;
  }
};

const remove = async (id) => {
  await deleteMeeting(id);
  await load();
};

onMounted(load);
</script>

<style scoped>
.meeting-page{min-height:100vh;background:#f7f8fb}
.topbar,.section-head,.detail-header,.form-head{display:flex;align-items:flex-start;justify-content:space-between;gap:18px}
.topbar{padding:28px clamp(18px,3vw,40px) 20px;background:#fff;border-bottom:1px solid #dfe6e2}
.layout{display:grid;grid-template-columns:minmax(0,1.35fr) minmax(300px,clamp(300px,28vw,420px));gap:clamp(16px,2vw,24px);padding:24px clamp(18px,3vw,40px) 40px}
.summary-view,.panel{background:#fff;border:1px solid #dfe6e2;border-radius:8px;box-shadow:0 16px 36px rgba(31,55,48,.12)}
.summary-view{overflow:hidden}
.section-head{align-items:end;padding:22px;border-bottom:1px solid #dfe6e2}
.section-head p{margin-top:7px;color:#63706a}
.summary-tools,.filters,.actions{display:flex;gap:10px}
.search-box{display:grid;gap:8px;min-width:170px;color:#63706a;font-size:13px}
.search-box input,.search-box select,.meeting-form input,.meeting-form select,.meeting-form textarea{width:100%;border:1px solid #dfe6e2;border-radius:8px;padding:11px 12px;background:#fff}
.filter,.secondary,.ghost{border:1px solid #dfe6e2;border-radius:8px;padding:9px 13px;background:#fff;color:#18201d}
.filter.active,.primary{border-radius:8px;padding:10px 15px;background:#1f7a5a;color:#fff;font-weight:700}
.table-wrap{overflow-x:auto;padding:16px 22px 22px}
table{width:100%;min-width:720px;border-collapse:collapse}
th,td{padding:15px 12px;border-bottom:1px solid #dfe6e2;text-align:left;vertical-align:top}
th{color:#63706a;font-size:13px;font-weight:700}
tbody tr:hover,tbody tr.active-row{background:#eef6f2}
.topic-cell strong,.topic-cell span{display:block}
.topic-cell span{margin-top:6px;color:#63706a;font-size:13px;line-height:1.5}
.badge{display:inline-flex;align-items:center;min-height:28px;border-radius:8px;padding:4px 9px;background:#e8f1ff;color:#22518f;font-weight:700;font-size:13px}
.badge.offline{background:#fff1dc;color:#9c6428}
.link-btn{color:#1f7a5a;background:transparent;padding:4px 0;font-weight:700;border:0}
.detail-view{min-width:0}
.panel{position:sticky;top:22px;min-height:420px;padding:22px}
.placeholder,.empty-state{display:grid;place-content:center;min-height:260px;gap:8px;text-align:center;color:#63706a}
.detail-meta{display:grid;gap:12px;margin:18px 0}
.detail-meta div{padding:13px;background:#f4f7f5;border-radius:8px}
dt{color:#63706a;font-size:13px;font-weight:700}
dd{margin:7px 0 0}
.detail-block,.upload-block,.existing-block{display:grid;gap:10px;margin-top:18px}
.detail-content{white-space:pre-wrap;line-height:1.75;color:#2b3732}
.meeting-form{display:grid;gap:15px}
.meeting-form label{display:grid;gap:8px;color:#63706a;font-size:13px;font-weight:700}
.upload-head{display:grid;gap:4px;color:#63706a;font-size:13px;font-weight:700}
.upload-head small,.helper-text{color:#7f8a84;font-size:12px}
.attachment-list{display:grid;gap:10px}
.attachment-item{display:flex;align-items:center;justify-content:space-between;gap:10px;padding:11px 12px;border:1px solid #dfe6e2;border-radius:8px;background:#fff;color:#18201d;text-decoration:none}
.link-item:hover{border-color:#1f7a5a}
.danger{border-radius:8px;padding:9px 13px;background:#fff;border:1px solid #e6c4c4;color:#b53232}
.full{width:100%}
h1,h2,h3,p{margin:0}
h1{margin-top:4px;font-size:30px}
h2{font-size:22px}
h3{font-size:16px}
.eyebrow{color:#1f7a5a;font-size:12px;font-weight:700;text-transform:uppercase}
@media (max-width:1280px){.layout{grid-template-columns:1fr;padding:18px}.topbar{padding:22px 18px 16px}.panel{position:static}}
@media (max-width:640px){.topbar,.section-head,.detail-header,.form-head,.summary-tools,.filters,.actions{align-items:stretch;flex-direction:column}h1{font-size:26px}}
</style>

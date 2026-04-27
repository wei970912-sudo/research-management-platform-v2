<template>
  <section class="view">
    <div class="section-title">
      <div>
        <h2>项目管理</h2>
        <p class="muted">可在此直接新增、编辑和删除项目。</p>
      </div>
      <button class="btn-primary" @click="openCreate">新增项目</button>
    </div>

    <article class="page-card" v-if="showForm">
      <div class="section-title">
        <div>
          <h3>{{ editingId ? "编辑项目" : "新增项目" }}</h3>
        </div>
      </div>
      <div class="form-grid">
        <label>
          <span>名称</span>
          <input v-model="form.name" class="input" />
        </label>
        <label>
          <span>负责人</span>
          <input v-model="form.owner" class="input" />
        </label>
        <label>
          <span>状态</span>
          <input v-model="form.status" class="input" />
        </label>
        <label>
          <span>当前阶段</span>
          <input v-model="form.currentStage" class="input" />
        </label>
        <label>
          <span>完成率</span>
          <input v-model.number="form.milestoneCompletionRate" type="number" min="0" max="100" class="input" />
        </label>
        <label>
          <span>Description</span>
          <textarea v-model="form.description" rows="4" class="textarea" />
        </label>
        <div class="actions-row">
          <button class="btn-primary" @click="submitForm">{{ editingId ? "保存" : "新增" }}</button>
          <button class="btn-secondary" @click="resetForm">取消</button>
        </div>
      </div>
    </article>

    <article class="page-card">
      <table class="table">
        <thead>
          <tr>
            <th>名称</th>
            <th>负责人</th>
            <th>状态</th>
            <th>阶段</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="project in projects" :key="project.id">
            <td>{{ project.name }}</td>
            <td>{{ project.owner }}</td>
            <td><span class="tag">{{ project.status }}</span></td>
            <td>{{ project.currentStage }}</td>
            <td class="actions-row">
              <RouterLink :to="`/projects/${project.id}`">详情</RouterLink>
              <button class="btn-secondary" @click="openEdit(project)">编辑</button>
              <button class="btn-danger" @click="remove(project.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </article>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { createProject, deleteProject, fetchProjectDetail, fetchProjects, updateProject } from "../api/modules";

const projects = ref([]);
const editingId = ref("");
const showForm = ref(false);
const form = reactive({
  name: "",
  owner: "",
  status: "IN_PROGRESS",
  currentStage: "",
  milestoneCompletionRate: 0,
  description: ""
});

const loadProjects = async () => {
  projects.value = await fetchProjects();
};

const resetForm = () => {
  editingId.value = "";
  showForm.value = false;
  form.name = "";
  form.owner = "";
  form.status = "IN_PROGRESS";
  form.currentStage = "";
  form.milestoneCompletionRate = 0;
  form.description = "";
};

const openCreate = () => {
  resetForm();
  showForm.value = true;
};

const openEdit = async (project) => {
  const detail = await fetchProjectDetail(project.id);
  editingId.value = detail.id;
  showForm.value = true;
  form.name = detail.name;
  form.owner = detail.owner;
  form.status = detail.status;
  form.currentStage = detail.currentStage;
  form.milestoneCompletionRate = detail.milestoneCompletionRate;
  form.description = detail.description;
};

const submitForm = async () => {
  const payload = { ...form };
  if (editingId.value) {
    await updateProject(editingId.value, payload);
  } else {
    await createProject(payload);
  }
  await loadProjects();
  resetForm();
};

const remove = async (projectId) => {
  await deleteProject(projectId);
  await loadProjects();
};

onMounted(loadProjects);
</script>

<style scoped>
.view {
  display: grid;
  gap: 20px;
}
</style>

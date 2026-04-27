<template>
  <section class="view">
    <div class="section-title">
      <div>
        <h2>项目详情</h2>
        <p class="muted">项目的基础信息、进度和关联模块入口。</p>
      </div>
      <div class="header-actions" v-if="project && !editing">
        <RouterLink to="/projects" class="btn-secondary">返回列表</RouterLink>
        <button class="btn-secondary" @click="openEdit">编辑</button>
        <button class="btn-danger" @click="remove">删除</button>
      </div>
    </div>

    <article class="page-card" v-if="editing && project">
      <div class="section-title">
        <h3>编辑项目</h3>
      </div>
      <div class="form-grid">
        <label>
          <span>项目名称</span>
          <input v-model="form.name" class="input" required />
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
          <span>里程碑完成率</span>
          <input v-model.number="form.milestoneCompletionRate" type="number" min="0" max="100" class="input" />
        </label>
        <label>
          <span>项目说明</span>
          <textarea v-model="form.description" rows="4" class="textarea" />
        </label>
        <div class="actions-row">
          <button class="btn-primary" @click="saveEdit">保存</button>
          <button class="btn-secondary" @click="editing = false">取消</button>
        </div>
      </div>
    </article>

    <article class="page-card" v-if="project && !editing">
      <div class="hero">
        <div>
          <h3>{{ project.name }}</h3>
          <p class="muted">{{ project.description }}</p>
        </div>
        <span class="tag">{{ project.status }}</span>
      </div>

      <div class="grid-3 metrics">
        <div>
          <span class="muted">负责人</span>
          <strong>{{ project.owner }}</strong>
        </div>
        <div>
          <span class="muted">当前阶段</span>
          <strong>{{ project.currentStage }}</strong>
        </div>
        <div>
          <span class="muted">里程碑完成率</span>
          <strong>{{ project.milestoneCompletionRate }}%</strong>
        </div>
      </div>
    </article>

    <div class="module-links" v-if="project && !editing">
      <RouterLink to="/meetings" class="module-link">会议记录</RouterLink>
      <RouterLink to="/contracts" class="module-link">合同管理</RouterLink>
      <RouterLink to="/documents" class="module-link">节点文档</RouterLink>
      <RouterLink to="/schedule" class="module-link">进度里程碑</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { deleteProject, fetchProjectDetail, updateProject } from "../api/modules";

const props = defineProps({
  id: { type: String, required: true }
});

const router = useRouter();
const project = ref(null);
const editing = ref(false);
const form = reactive({
  name: "",
  owner: "",
  status: "",
  currentStage: "",
  milestoneCompletionRate: 0,
  description: ""
});

onMounted(async () => {
  project.value = await fetchProjectDetail(props.id);
});

const openEdit = () => {
  form.name = project.value.name;
  form.owner = project.value.owner;
  form.status = project.value.status;
  form.currentStage = project.value.currentStage;
  form.milestoneCompletionRate = project.value.milestoneCompletionRate;
  form.description = project.value.description;
  editing.value = true;
};

const saveEdit = async () => {
  await updateProject(props.id, { ...form });
  project.value = await fetchProjectDetail(props.id);
  editing.value = false;
};

const remove = async () => {
  await deleteProject(props.id);
  router.push("/projects");
};
</script>

<style scoped>
.view {
  display: grid;
  gap: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 22px;
}

.hero h3,
.hero p {
  margin: 0;
}

.metrics strong,
.metrics span {
  display: block;
}

.metrics strong {
  margin-top: 8px;
  font-size: 22px;
}

.module-links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.module-link {
  padding: 10px 18px;
  border: 1px solid #dfe6e2;
  border-radius: 8px;
  background: #fff;
  color: #1f7a5a;
  font-weight: 700;
  text-decoration: none;
  transition: background 0.15s;
}

.module-link:hover {
  background: #eef6f2;
  border-color: #1f7a5a;
}
</style>

<template>
  <aside class="sidebar">
    <div class="brand">
      <div class="logo">RM</div>
      <div>
        <strong>科研管理平台</strong>
        <p>前后端分离版</p>
      </div>
    </div>

    <nav class="nav">
      <RouterLink v-for="item in items" :key="item.to" :to="item.to" class="nav-link">
        <span>{{ item.label }}</span>
        <small>{{ item.description }}</small>
      </RouterLink>
    </nav>
  </aside>
</template>

<script setup>
const items = [
  { to: "/dashboard", label: "总览驾驶舱", description: "聚合统计与核心提醒" },
  { to: "/projects", label: "项目中心", description: "项目主数据与状态管理" },
  { to: "/schedule", label: "进度里程碑", description: "阶段、控制线、考核项" },
  { to: "/meetings", label: "会议记录", description: "纪要、提醒与跟踪" },
  { to: "/contracts", label: "合同管理", description: "任务书与合同金额跟踪" },
  { to: "/documents", label: "节点文档", description: "节点归档与附件管理" },
  { to: "/assistant", label: "科研助手", description: "Ollama 本地模型接入" }
];
</script>

<style scoped>
.sidebar {
  padding: 24px;
  background: linear-gradient(180deg, #133a33 0%, #102f2a 100%);
  color: #f3fbf9;
}

.brand {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 28px;
}

.brand p,
.brand strong {
  margin: 0;
}

.logo {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
  font-weight: 800;
}

.nav {
  display: grid;
  gap: 10px;
}

.nav-link {
  display: grid;
  gap: 4px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.04);
  color: inherit;
}

.nav-link.router-link-active {
  background: rgba(111, 219, 187, 0.16);
}

.nav-link small {
  color: rgba(243, 251, 249, 0.74);
}
</style>

<template>
  <div>
    <header v-if="showBar" class="auth-bar">
      <div class="auth-meta">
        <strong>{{ authState.user?.displayName || "未登录" }}</strong>
        <span>{{ authState.user?.role === "ADMIN" ? "管理员" : "查看员" }}</span>
      </div>
      <div class="auth-actions">
        <RouterLink class="nav-link" to="/account">账号设置</RouterLink>
        <RouterLink v-if="authState.user?.role === 'ADMIN'" class="nav-link" to="/users">用户管理</RouterLink>
        <button class="logout-btn" type="button" @click="handleLogout">退出登录</button>
      </div>
    </header>
    <AppShell />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { authState, logout } from "./auth";
import AppShell from "./layouts/AppShell.vue";

const route = useRoute();
const router = useRouter();

const showBar = computed(() => route.path !== "/login" && authState.user);

const handleLogout = async () => {
  await logout();
  await router.replace("/login");
};
</script>

<style scoped>
.auth-bar{position:sticky;top:0;z-index:50;display:flex;align-items:center;justify-content:space-between;padding:10px 18px;background:#13211d;color:#f6fbf9}
.auth-meta{display:flex;gap:10px;align-items:center}
.auth-meta span{padding:4px 8px;border-radius:999px;background:rgba(255,255,255,.12);font-size:12px}
.auth-actions{display:flex;gap:10px;align-items:center}
.nav-link{color:#fff;text-decoration:none;padding:8px 12px;border-radius:8px;border:1px solid rgba(255,255,255,.16)}
.logout-btn{border:1px solid rgba(255,255,255,.2);background:transparent;color:#fff;border-radius:8px;padding:8px 12px}
</style>

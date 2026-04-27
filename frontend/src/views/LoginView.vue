<template>
  <section class="login-page">
    <div class="login-card">
      <div class="brand">
        <p class="eyebrow">权限登录</p>
        <h1>科研管理平台</h1>
        <p class="desc">请使用管理员或查看员账号登录后访问系统。</p>
      </div>

      <form class="login-form" @submit.prevent="submit">
        <label>
          <span>账号</span>
          <input v-model.trim="form.username" type="text" autocomplete="username" required />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.password" type="password" autocomplete="current-password" required />
        </label>
        <button class="primary" :disabled="submitting" type="submit">
          {{ submitting ? "登录中..." : "登录" }}
        </button>
      </form>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { login } from "../auth";

const router = useRouter();
const route = useRoute();

const form = reactive({
  username: "",
  password: ""
});

const submitting = ref(false);
const errorMessage = ref("");

const submit = async () => {
  submitting.value = true;
  errorMessage.value = "";
  try {
    await login({ ...form });
    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "/portal";
    await router.replace(redirect);
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || "登录失败，请检查账号或密码。";
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.login-page{min-height:100vh;display:grid;place-items:center;padding:24px;background:linear-gradient(135deg,#f3f7f4 0%,#e5f0ea 100%)}
.login-card{width:min(100%,420px);display:grid;gap:22px;padding:30px;border-radius:18px;background:#fff;box-shadow:0 24px 60px rgba(18,45,37,.12);border:1px solid #dfe6e2}
.brand{display:grid;gap:8px}
.eyebrow{margin:0;color:#1f7a5a;font-size:12px;font-weight:700;text-transform:uppercase}
h1{margin:0;font-size:32px;color:#13211d}
.desc{margin:0;color:#63706a;line-height:1.6}
.login-form{display:grid;gap:14px}
.login-form label{display:grid;gap:8px;color:#42514b;font-size:14px;font-weight:700}
.login-form input{width:100%;border:1px solid #dfe6e2;border-radius:10px;padding:12px 13px;background:#fff}
.primary{border:0;border-radius:10px;padding:12px 14px;background:#1f7a5a;color:#fff;font-weight:700}
.error{margin:0;color:#b53232;background:#fff3f3;border:1px solid #eccccc;border-radius:10px;padding:10px 12px}
</style>

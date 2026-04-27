<template>
  <section class="page">
    <div class="card">
      <div class="header">
        <div>
          <p class="eyebrow">账号设置</p>
          <h1>修改密码</h1>
        </div>
      </div>

      <form class="form" @submit.prevent="submit">
        <label>
          <span>当前密码</span>
          <input v-model="form.currentPassword" type="password" autocomplete="current-password" required />
        </label>
        <label>
          <span>新密码</span>
          <input v-model="form.newPassword" type="password" autocomplete="new-password" required />
        </label>
        <button class="primary" :disabled="submitting" type="submit">
          {{ submitting ? "提交中..." : "保存新密码" }}
        </button>
      </form>

      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { changePassword } from "../api/modules";

const form = reactive({
  currentPassword: "",
  newPassword: ""
});

const submitting = ref(false);
const message = ref("");
const errorMessage = ref("");

const submit = async () => {
  submitting.value = true;
  message.value = "";
  errorMessage.value = "";
  try {
    await changePassword({ ...form });
    message.value = "密码已更新。下次请使用新密码登录。";
    form.currentPassword = "";
    form.newPassword = "";
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || "修改密码失败。";
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.page{display:grid;gap:16px}
.card{background:#fff;border:1px solid #dde6e0;border-radius:18px;padding:24px;box-shadow:0 10px 30px rgba(18,45,37,.06);display:grid;gap:18px}
.header{display:flex;justify-content:space-between;align-items:center}
.eyebrow{margin:0;color:#1f7a5a;font-size:12px;font-weight:700}
h1{margin:6px 0 0;font-size:28px;color:#13211d}
.form{display:grid;gap:14px;max-width:420px}
.form label{display:grid;gap:8px;font-weight:700;color:#42514b}
.form input{border:1px solid #dfe6e2;border-radius:10px;padding:12px 13px}
.primary{border:0;border-radius:10px;padding:12px 14px;background:#1f7a5a;color:#fff;font-weight:700}
.success{margin:0;color:#17643f;background:#eef8f1;border:1px solid #cfe6d5;border-radius:10px;padding:10px 12px}
.error{margin:0;color:#b53232;background:#fff3f3;border:1px solid #eccccc;border-radius:10px;padding:10px 12px}
</style>

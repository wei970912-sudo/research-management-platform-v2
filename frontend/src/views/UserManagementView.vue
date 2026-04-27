<template>
  <section class="page">
    <div class="card">
      <div class="header">
        <div>
          <p class="eyebrow">管理员功能</p>
          <h1>用户管理</h1>
        </div>
        <button class="secondary" type="button" @click="loadUsers">刷新列表</button>
      </div>

      <form class="form" @submit.prevent="handleCreate">
        <label>
          <span>用户名</span>
          <input v-model.trim="createForm.username" type="text" required />
        </label>
        <label>
          <span>显示名称</span>
          <input v-model.trim="createForm.displayName" type="text" required />
        </label>
        <label>
          <span>角色</span>
          <select v-model="createForm.role">
            <option value="VIEWER">查看员</option>
            <option value="ADMIN">管理员</option>
          </select>
        </label>
        <label>
          <span>初始密码</span>
          <input v-model="createForm.password" type="password" required />
        </label>
        <button class="primary" :disabled="creating" type="submit">
          {{ creating ? "创建中..." : "创建账号" }}
        </button>
      </form>

      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <table class="table">
        <thead>
          <tr>
            <th>用户名</th>
            <th>显示名称</th>
            <th>角色</th>
            <th>重置密码</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.username">
            <td>{{ user.username }}</td>
            <td>{{ user.displayName }}</td>
            <td>{{ user.role === "ADMIN" ? "管理员" : "查看员" }}</td>
            <td>
              <div class="reset-row">
                <input
                  v-model="resetPasswords[user.username]"
                  type="password"
                  placeholder="输入新密码"
                />
                <button type="button" class="secondary" @click="handleReset(user.username)">
                  重置
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { createUser, fetchUsers, resetUserPassword } from "../api/modules";

const users = ref([]);
const creating = ref(false);
const message = ref("");
const errorMessage = ref("");
const resetPasswords = reactive({});

const createForm = reactive({
  username: "",
  displayName: "",
  role: "VIEWER",
  password: ""
});

const loadUsers = async () => {
  users.value = await fetchUsers();
};

const handleCreate = async () => {
  creating.value = true;
  message.value = "";
  errorMessage.value = "";
  try {
    await createUser({ ...createForm });
    message.value = "账号已创建。";
    createForm.username = "";
    createForm.displayName = "";
    createForm.role = "VIEWER";
    createForm.password = "";
    await loadUsers();
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || "创建账号失败。";
  } finally {
    creating.value = false;
  }
};

const handleReset = async (username) => {
  const newPassword = resetPasswords[username];
  message.value = "";
  errorMessage.value = "";
  if (!newPassword) {
    errorMessage.value = "请先输入新密码。";
    return;
  }
  try {
    await resetUserPassword(username, { newPassword });
    message.value = `已重置 ${username} 的密码。`;
    resetPasswords[username] = "";
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || "重置密码失败。";
  }
};

onMounted(loadUsers);
</script>

<style scoped>
.page{display:grid;gap:16px}
.card{background:#fff;border:1px solid #dde6e0;border-radius:18px;padding:24px;box-shadow:0 10px 30px rgba(18,45,37,.06);display:grid;gap:18px}
.header{display:flex;justify-content:space-between;align-items:center}
.eyebrow{margin:0;color:#1f7a5a;font-size:12px;font-weight:700}
h1{margin:6px 0 0;font-size:28px;color:#13211d}
.form{display:grid;grid-template-columns:repeat(4,minmax(0,1fr)) auto;gap:12px;align-items:end}
.form label{display:grid;gap:8px;font-weight:700;color:#42514b}
.form input,.form select{border:1px solid #dfe6e2;border-radius:10px;padding:12px 13px}
.primary,.secondary{border-radius:10px;padding:12px 14px;font-weight:700}
.primary{border:0;background:#1f7a5a;color:#fff}
.secondary{border:1px solid #d4ddd7;background:#fff;color:#173126}
.success{margin:0;color:#17643f;background:#eef8f1;border:1px solid #cfe6d5;border-radius:10px;padding:10px 12px}
.error{margin:0;color:#b53232;background:#fff3f3;border:1px solid #eccccc;border-radius:10px;padding:10px 12px}
.table{width:100%;border-collapse:collapse}
.table th,.table td{padding:12px;border-bottom:1px solid #ecf0ed;text-align:left;vertical-align:top}
.reset-row{display:flex;gap:8px}
.reset-row input{flex:1;min-width:180px;border:1px solid #dfe6e2;border-radius:10px;padding:10px 12px}
@media (max-width: 1080px){
  .form{grid-template-columns:1fr}
  .reset-row{flex-direction:column}
}
</style>

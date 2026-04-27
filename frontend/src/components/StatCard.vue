<template>
  <article class="stat-card">
    <span>{{ label }}</span>
    <strong>{{ value }}</strong>
    <p>{{ hint }}</p>
  </article>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: String, required: true },
  hint: { type: String, default: "" }
});
</script>

<style scoped>
.stat-card {
  display: grid;
  gap: 8px;
  padding: 20px;
  border-radius: 16px;
  border: 1px solid #d7e1de;
  background: #fff;
}

.stat-card span {
  color: #66736f;
  font-size: 13px;
}

.stat-card strong {
  font-size: 28px;
}

.stat-card p {
  margin: 0;
  color: #66736f;
  line-height: 1.5;
}
</style>


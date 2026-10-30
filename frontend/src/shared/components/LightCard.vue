<template>
  <div class="light-card">
    <slot/>
  </div>
</template>

<style scoped>
.light-card {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 16px;
}
</style>

<script setup>
import {Sort} from "@element-plus/icons-vue";

defineProps({
  icon: Object,
  title: String,
  desc: String
})
</script>

<template>
  <div class="card">
    <div class="card-header" v-if="title">
      <div>
        <el-icon style="margin-right: 3px;translate: 0 2px">
          <component :is="icon"/>
        </el-icon>
        {{title}}
      </div>
      <div>{{desc}}</div>
    </div>
    <slot/>
  </div>
</template>

<style scoped>

.card{
  border-radius: var(--radius);
  border: solid 1px var(--border);
  background: var(--bg);
  box-sizing: border-box;
  min-height: 20px;
  padding: 16px;
}

.card-header {
  border-bottom: solid 1px var(--border);
  padding-bottom: 8px;
  margin-bottom: 12px;

  &>:first-child {
    font-size: 15px;
    font-weight: 600;
  }

  &>:last-child {
    font-size: 12px;
    color: var(--text-muted);
  }
}
</style>
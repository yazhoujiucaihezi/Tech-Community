<script setup>
defineProps({
    title: {type: String, default: '这里还没有内容'},
    desc: {type: String, default: ''},
    action: {type: String, default: ''}
})
defineEmits(['action'])
</script>

<template>
    <div class="empty">
        <div class="empty-title">{{ title }}</div>
        <div class="empty-desc" v-if="desc">{{ desc }}</div>
        <el-button v-if="action" size="small" style="margin-top: 14px" @click="$emit('action')">
            {{ action }}
        </el-button>
    </div>
</template>

<style scoped>
.empty {
    padding: 56px 16px;
    text-align: center;
    border: 1px dashed var(--border);
    border-radius: var(--radius);
    background: var(--bg);
}

.empty-title {
    font-size: 15px;
    font-weight: 500;
    color: var(--text);
}

.empty-desc {
    margin-top: 6px;
    font-size: 13px;
    color: var(--text-muted);
}
</style>

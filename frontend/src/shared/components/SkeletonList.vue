<script setup>
defineProps({
    rows: {type: Number, default: 6}
})
</script>

<template>
    <div class="list" style="margin-top: 12px">
        <div class="skeleton-row" v-for="i in rows" :key="i">
            <div style="flex: 1">
                <div class="skeleton-bar" :style="{width: (55 + (i % 4) * 10) + '%'}"></div>
                <div class="skeleton-bar" style="margin-top: 8px; width: 30%; height: 10px"></div>
            </div>
            <div class="skeleton-bar" style="width: 60px; height: 10px"></div>
        </div>
    </div>
</template>

<script setup>
/**
 * 应用入口页：只负责套上 AppShell 并渲染子路由。
 * 顶栏、左侧板块导航、搜索、消息都在 AppShell 里。
 */
import AppShell from "@/shared/layouts/AppShell.vue";
</script>

<template>
    <app-shell>
        <router-view v-slot="{ Component }">
            <transition name="el-fade-in-linear" mode="out-in">
                <component :is="Component"/>
            </transition>
        </router-view>
    </app-shell>
</template>

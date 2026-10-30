<template>
    <div class="welcome">
        <div class="welcome-left">
            <div class="welcome-brand">
                <img src="/brand-logo.png" alt="logo"/>
                <span>亚洲韭菜盒子</span>
            </div>
            <div class="welcome-slogan">
                <div class="slogan-main">写代码，也写复盘。</div>
                <div class="slogan-sub">
                    <span class="mono">// 面向校招的技术问答社区</span>
                    <span class="mono">// 贴代码 · 提问 · 被采纳</span>
                </div>
            </div>
            <div class="welcome-foot mono">v1.0 · Spring Boot 3 + Vue 3</div>
        </div>
        <div class="right-card">
            <router-view v-slot="{ Component }">
                <transition name="el-fade-in-linear" mode="out-in">
                    <component :is="Component" style="height: 100%"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<script setup>
</script>

<style scoped>
.welcome {
    display: flex;
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    background: var(--bg);
}

.welcome-left {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 40px;
    background: var(--code-bg);
    color: #e6edf3;
}

.welcome-brand {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    font-weight: 600;
    letter-spacing: .02em;

    img {
        width: 24px;
        height: 24px;
        object-fit: contain;
    }
}

.slogan-main {
    font-size: 32px;
    font-weight: 600;
    letter-spacing: -.01em;
    line-height: 1.3;
}

.slogan-sub {
    margin-top: 18px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 13px;
    color: #7d8794;
}

.welcome-foot {
    font-size: 12px;
    color: #4d5764;
}

.right-card {
    width: 420px;
    flex-shrink: 0;
    z-index: 1;
    background: var(--bg);
    border-left: 1px solid var(--border);
}

@media (max-width: 900px) {
    .welcome-left {
        display: none;
    }

    .right-card {
        width: 100%;
        border-left: none;
    }
}
</style>

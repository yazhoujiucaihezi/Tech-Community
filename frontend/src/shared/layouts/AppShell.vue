<script setup>
/**
 * 应用外壳：顶栏 + 左侧板块导航 + 主内容区。
 * 只负责「框架和导航」，页面内容通过默认插槽传入。
 */
import {inject, ref} from "vue";
import {Bell, Check, ChatDotSquare, CoffeeCup, Lock, Plus, Search, User} from "@element-plus/icons-vue";
import UserInfo from "@/features/user/components/UserInfo.vue";
import AiChatWindow from "@/features/ai/components/AiChatWindow.vue";
import {apiNotificationDelete, apiNotificationDeleteAll, apiNotificationList} from "@/api/notification";
import {apiTopicSearch, apiForumTypes} from "@/api/topic";
import {useStore} from "@/stores/general";
import router from "@/router";

const store = useStore()
const loading = inject("userLoading")

apiForumTypes(data => {
    const list = [{name: '全部', id: 0, color: '#9ca3af'}]
    data.forEach(item => list.push(item))
    store.forum.types = list
})

const menus = [
    {title: '帖子广场', icon: ChatDotSquare, index: '/index'},
    {title: '我的帖子', icon: CoffeeCup, index: '/index/forum-setting'},
    {title: '个人信息', icon: User, index: '/index/user-setting'},
    {title: '账号安全', icon: Lock, index: '/index/privacy-setting'}
]

const keyword = ref('')
const notification = ref([])
const loadNotification = () => apiNotificationList(data => notification.value = data)
loadNotification()

function search(keyword, callback) {
    if (!keyword) return
    apiTopicSearch(keyword, data => callback(data))
}

const toTopic = ({id}) => router.push('/index/topic-detail/' + id)

function confirmNotification(id, url) {
    apiNotificationDelete(id, () => {
        loadNotification()
        window.open(url)
    })
}

function goBoard(typeId) {
    router.push({path: '/index', query: {type: typeId}})
}
</script>

<template>
    <div class="shell" v-loading="loading" element-loading-text="正在进入，请稍后...">
        <header class="shell-header">
            <div class="brand" @click="router.push('/index')">
                <img class="brand-logo" src="/brand-logo.png" alt="极客技术社区"/>
                <span class="brand-name">极客技术社区</span>
            </div>

            <div class="search-box">
                <el-autocomplete v-model="keyword" style="width: 100%" fit-input-width
                                 :fetch-suggestions="search" @select="toTopic"
                                 placeholder="搜索帖子、板块、关键字">
                    <template #prefix>
                        <el-icon><Search/></el-icon>
                    </template>
                    <template #default="{ item }">
                        <div class="search-row">
                            <div class="search-title">{{ item.title }}</div>
                            <div class="search-desc">{{ item.intro }}</div>
                        </div>
                    </template>
                </el-autocomplete>
            </div>

            <div class="header-actions">
                <el-popover placement="bottom-end" :width="360" trigger="click">
                    <template #reference>
                        <el-badge is-dot :hidden="!notification.length">
                            <div class="icon-btn" title="消息">
                                <el-icon :size="18"><Bell/></el-icon>
                            </div>
                        </el-badge>
                    </template>
                    <div class="notice-head">
                        <span>消息</span>
                        <el-button link size="small" :icon="Check" @click="apiNotificationDeleteAll(loadNotification)">
                            全部已读
                        </el-button>
                    </div>
                    <div class="notice-empty" v-if="!notification.length">暂时没有新消息</div>
                    <el-scrollbar :max-height="420" v-else>
                        <div class="notice-item" v-for="item in notification" :key="item.id"
                             @click="confirmNotification(item.id, item.url)">
                            <div class="notice-title">{{ item.title }}</div>
                            <div class="notice-content">{{ item.content }}</div>
                        </div>
                    </el-scrollbar>
                </el-popover>
                <user-info/>
            </div>
        </header>

        <div class="shell-body">
            <aside class="shell-aside">
                <div class="nav-group">
                    <div class="nav-label">板块</div>
                    <div class="nav-item" :class="{active: $route.query.type === undefined && $route.path === '/index'}"
                         @click="router.push('/index')">
                        全部
                    </div>
                    <div class="nav-item" v-for="type in store.forum.types.filter(t => t.id !== 0)" :key="type.id"
                         :class="{active: String($route.query.type) === String(type.id)}"
                         @click="goBoard(type.id)">
                        <span class="dot" :style="{background: type.color}"></span>
                        {{ type.name }}
                    </div>
                </div>
                <div class="nav-group">
                    <div class="nav-label">我的</div>
                    <div class="nav-item" v-for="menu in menus" :key="menu.index"
                         :class="{active: $route.path === menu.index}"
                         @click="router.push(menu.index)">
                        {{ menu.title }}
                    </div>
                </div>
                <div class="nav-foot">
                    <a href="https://github.com/yazhoujiucaihezi/campus_forum" target="_blank" rel="noopener">
                        GitHub 源码
                    </a>
                </div>
            </aside>

            <main class="shell-main">
                <slot/>
            </main>
        </div>

        <!-- AI 助手悬浮窗，详情页不显示（避免挡住评论输入） -->
        <ai-chat-window v-if="$route.name !== 'topic-detail'"/>
    </div>
</template>

<style lang="less" scoped>
.shell {
    min-height: 100vh;
    background: var(--bg);
    color: var(--text);
}

.shell-header {
    position: sticky;
    top: 0;
    z-index: 20;
    height: var(--header-h);
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 0 24px;
    background: var(--bg);
    border-bottom: 1px solid var(--border);
}

.brand {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
    cursor: pointer;

    .brand-logo {
        width: 26px;
        height: 26px;
        object-fit: contain;
    }

    .brand-name {
        font-size: 15px;
        font-weight: 600;
        letter-spacing: .02em;
        white-space: nowrap;
    }
}

.search-box {
    flex: 1;
    max-width: 460px;

    :deep(.el-input__wrapper) {
        background: var(--bg-subtle);
        box-shadow: none;
        border: 1px solid var(--border);
    }
}

.header-actions {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 16px;
}

.write-btn {
    height: 30px;
    padding: 0 14px;
    font-size: 13px;
}

.icon-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: var(--radius);
    color: var(--text-muted);
    cursor: pointer;

    &:hover {
        background: var(--bg-hover);
        color: var(--text);
    }
}

.shell-body {
    display: flex;
    align-items: flex-start;
}

.shell-aside {
    position: sticky;
    top: var(--header-h);
    width: var(--nav-w);
    flex-shrink: 0;
    height: calc(100vh - var(--header-h));
    overflow-y: auto;
    padding: 20px 12px;
    border-right: 1px solid var(--border);
    box-sizing: border-box;
}

.nav-group + .nav-group {
    margin-top: 22px;
}

.nav-label {
    font-size: 11px;
    letter-spacing: .08em;
    text-transform: uppercase;
    color: var(--text-faint);
    padding: 0 10px 6px;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 8px;
    height: 32px;
    padding: 0 10px;
    font-size: 14px;
    color: var(--text-muted);
    border-radius: var(--radius);
    cursor: pointer;

    &:hover {
        background: var(--bg-hover);
        color: var(--text);
    }

    &.active {
        background: var(--accent-soft);
        color: var(--accent);
        font-weight: 500;
    }

    .dot {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        flex-shrink: 0;
    }
}

.nav-foot {
    margin-top: 28px;
    padding: 0 10px;
    font-size: 12px;

    a {
        color: var(--text-faint);
    }
}

.shell-main {
    flex: 1;
    min-width: 0;
    background: var(--bg);
}

.search-row {
    padding: 8px 0;
    line-height: 1.4;
}

.search-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--text);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.search-desc {
    font-size: 12px;
    color: var(--text-muted);
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
}

.notice-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 13px;
    font-weight: 600;
    padding-bottom: 8px;
    border-bottom: 1px solid var(--border);
}

.notice-empty {
    padding: 24px 0;
    text-align: center;
    font-size: 13px;
    color: var(--text-faint);
}

.notice-item {
    padding: 10px 0;
    border-bottom: 1px solid var(--border);
    cursor: pointer;

    &:last-child {
        border-bottom: none;
    }

    &:hover .notice-title {
        color: var(--accent);
    }

    .notice-title {
        font-size: 13px;
        font-weight: 500;
    }

    .notice-content {
        font-size: 12px;
        color: var(--text-muted);
        margin-top: 2px;
    }
}

@media (max-width: 1024px) {
    .shell-aside {
        display: none;
    }

    .shell-header {
        padding: 0 16px;
        gap: 12px;
    }

    .write-btn {
        display: none;
    }
}
</style>

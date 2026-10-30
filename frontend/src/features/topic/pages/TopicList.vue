<script setup>
import {
    CollectionTag,
    EditPen,
    FolderOpened,
    TrendCharts
} from "@element-plus/icons-vue";
import {computed, onMounted, reactive, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {ElMessage} from "element-plus";
import TopicEditor from "@/features/topic/components/TopicEditor.vue";
import {useStore} from "@/stores/general";
import router from "@/router";
import TopicTag from "@/shared/components/TopicTag.vue";
import TopicCollectList from "@/features/topic/components/TopicCollectList.vue";
import {apiForumTopicList, apiForumTopTopics, apiForumWeather, apiForumHotTopics} from "@/api/topic";
import EmptyState from "@/shared/components/EmptyState.vue";
import SkeletonList from "@/shared/components/SkeletonList.vue";

const store = useStore();
const route = useRoute();

const editor = ref(false)
const collects = ref(false)
const loading = ref(false)

const topics = reactive({
    list: [],
    type: Number(route.query.type || 0),
    page: 0,
    end: false,
    top: []
})

watch(() => route.query.type, value => {
    const next = Number(value || 0)
    if (next !== topics.type) {
        topics.type = next
    }
})

watch(() => topics.type, () => resetList(), {immediate: true})

function setType(type) {
    router.replace({path: '/index', query: type ? {type} : {}})
}

function updateList() {
    if (topics.end || loading.value) return
    loading.value = true
    apiForumTopicList(topics.page, topics.type, data => {
        loading.value = false
        if (data && data.length) {
            data.forEach(d => topics.list.push(d))
            topics.page++
        }
        if (!data || data.length < 10) topics.end = true
    }, () => loading.value = false)
}

function resetList() {
    topics.page = 0
    topics.end = false
    topics.list = []
    updateList()
}

function onTopicCreate() {
    editor.value = false
    resetList()
}

const weather = reactive({location: {}, now: {}, hourly: [], success: false})

const today = computed(() => {
    const date = new Date()
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
})

const hotTopics = ref([])
const loadHotTopics = () => apiForumHotTopics(data => hotTopics.value = data)

function timeAgo(time) {
    const diff = Date.now() - new Date(time).getTime()
    const min = Math.floor(diff / 60000)
    if (min < 1) return '刚刚'
    if (min < 60) return `${min} 分钟前`
    const hour = Math.floor(min / 60)
    if (hour < 24) return `${hour} 小时前`
    const day = Math.floor(hour / 24)
    if (day < 30) return `${day} 天前`
    return new Date(time).toLocaleDateString()
}

const toTopic = id => router.push('/index/topic-detail/' + id)

navigator.geolocation.getCurrentPosition(position => {
    apiForumWeather(position.coords.longitude, position.coords.latitude, data => {
        Object.assign(weather, data)
        weather.success = true
    })
}, () => {
    apiForumWeather(116.40529, 39.90499, data => {
        Object.assign(weather, data)
        weather.success = true
    })
}, {timeout: 3000, enableHighAccuracy: true})

onMounted(() => {
    apiForumTopTopics(data => topics.top = data)
    loadHotTopics()
})
</script>

<template>
    <div class="page-wrap with-aside">
        <div class="page-main">
            <div class="toolbar">
                <div class="segmented">
                    <div class="seg" v-for="item in store.forum.types" :key="item.id"
                         :class="{active: topics.type === item.id}" @click="setType(item.id)">
                        {{ item.name }}
                    </div>
                </div>
                <el-button type="primary" size="small" :icon="EditPen" @click="editor = true">
                    发帖
                </el-button>
            </div>

            <div class="list" v-if="topics.top.length">
                <div class="list-row" v-for="item in topics.top" :key="'top-' + item.id" @click="toTopic(item.id)">
                    <span class="chip pin">置顶</span>
                    <div class="row-main">
                        <div class="row-title">{{ item.title }}</div>
                    </div>
                    <div class="row-stats">{{ new Date(item.time).toLocaleDateString() }}</div>
                </div>
            </div>

            <div class="list topic-list" style="margin-top: 12px" v-if="topics.list.length"
                 v-infinite-scroll="updateList">
                <div class="list-row" v-for="item in topics.list" :key="item.id" @click="toTopic(item.id)">
                    <div class="row-main">
                        <div class="row-title">
                            <span class="chip lock" v-if="item.locked">已锁定</span>
                            {{ item.title }}
                        </div>
                        <div class="row-meta">
                            <topic-tag :type="item.type"/>
                            <span>{{ item.username }}</span>
                            <span>·</span>
                            <span>{{ timeAgo(item.time) }}</span>
                        </div>
                        <div class="row-intro" v-if="item.intro">{{ item.intro }}</div>
                    </div>
                    <div class="row-stats">
                        <span>❤ {{ item.like }}</span>
                        <span>★ {{ item.collect }}</span>
                    </div>
                </div>
            </div>

            <skeleton-list v-if="loading && !topics.list.length"/>

            <empty-state v-if="topics.end && !topics.list.length && !loading"
                         title="这个板块还没有帖子"
                         desc="发第一篇，占个位置。"
                         action="发帖" @action="editor = true"/>

            <div class="list-foot" v-if="topics.list.length && topics.end">没有更多了</div>
        </div>

        <aside class="page-aside">
            <div class="panel">
                <div class="panel-title">我的</div>
                <div class="side-link" @click="collects = true">
                    <span><el-icon><FolderOpened/></el-icon> 我的收藏</span>
                    <span class="faint">→</span>
                </div>
                <div class="side-link" @click="router.push('/index/forum-setting')">
                    <span><el-icon><EditPen/></el-icon> 我的帖子</span>
                    <span class="faint">→</span>
                </div>
            </div>

            <div class="panel" style="margin-top: 16px">
                <div class="panel-title"><el-icon><TrendCharts/></el-icon> 今日热帖</div>
                <div class="hot-item" v-for="(item, index) in hotTopics" :key="item.id" @click="toTopic(item.id)">
                    <span class="rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
                    <span class="hot-title">{{ item.title }}</span>
                </div>
                <div class="faint" v-if="!hotTopics.length">暂无热门帖子</div>
            </div>

            <div class="panel" style="margin-top: 16px">
                <div class="panel-title"><el-icon><CollectionTag/></el-icon> 社区公告</div>
                <div class="notice-text">禁止人身攻击与广告；提问请贴出可复现的代码和报错信息。</div>
            </div>

            <div class="aside-foot">
                <span v-if="weather.success">
                    {{ weather.location.name }} {{ weather.now.text }} {{ weather.now.temp }}℃
                </span>
                <span>{{ today }}</span>
            </div>
        </aside>

        <topic-editor :show="editor" @success="onTopicCreate" @close="editor = false"/>
        <topic-collect-list :show="collects" @close="collects = false"/>
    </div>
</template>

<style lang="less" scoped>
.toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 16px;
}

.chip.pin {
    align-self: center;
    color: var(--accent);
    border-color: var(--accent);
    background: var(--accent-soft);
}

.chip.lock {
    color: var(--danger);
    border-color: var(--danger);
    background: transparent;
    margin-right: 6px;
}

.row-intro {
    margin-top: 4px;
    font-size: 13px;
    color: var(--text-muted);
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
}

.topic-list .list-row {
    padding: 14px 16px;
}

.list-foot {
    padding: 16px 0;
    text-align: center;
    font-size: 12px;
    color: var(--text-faint);
}

.side-link {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    color: var(--text-muted);
    padding: 7px 0;
    cursor: pointer;

    &:hover {
        color: var(--text);
    }
}

.hot-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 0;
    cursor: pointer;

    &:hover .hot-title {
        color: var(--accent);
    }

    .rank {
        width: 18px;
        text-align: center;
        font-size: 12px;
        font-weight: 600;
        color: var(--text-faint);
        flex-shrink: 0;
    }

    .rank-1 { color: #b42318; }
    .rank-2 { color: #b54708; }
    .rank-3 { color: #92700c; }

    .hot-title {
        font-size: 13px;
        color: var(--text);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}

.notice-text {
    font-size: 13px;
    color: var(--text-muted);
    line-height: 1.7;
}

.aside-foot {
    margin-top: 16px;
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: var(--text-faint);
}
</style>

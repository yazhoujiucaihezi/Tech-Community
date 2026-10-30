<script setup>
    import {useRoute} from "vue-router";
    import {reactive, ref} from "vue";
    import {
    ArrowLeft,
    ChatSquare,
    CircleCheck,
    Delete,
    EditPen,
    Female,
    Lock,
    Male,
    Plus,
    Star
    } from "@element-plus/icons-vue";
    import { QuillDeltaToHtmlConverter } from 'quill-delta-to-html';
    import Card from "@/shared/components/Card.vue";
    import router from "@/router";
    import TopicTag from "@/shared/components/TopicTag.vue";
    import InteractButton from "@/features/topic/components/InteractButton.vue";
    import {ElMessage} from "element-plus";
    import {useStore} from "@/stores/general";
    import TopicEditor from "@/features/topic/components/TopicEditor.vue";
    import TopicCommentEditor from "@/features/topic/components/TopicCommentEditor.vue";
    import {
    apiForumCommentDelete,
    apiForumComments,
    apiForumInteract,
    apiForumTopic,
    apiForumUpdateTopic
    } from "@/api/topic";
    import { apiForumUserTopicDelete } from "@/api/topic"; 

    const route = useRoute()
    const store = useStore()
    const tid = route.params.tid

    const topic = reactive({
    data: null,
    like: false,
    collect: false,
    likeCount: 0,
    collectCount: 0,
    components: null,
    page: 1
    })
    const edit=ref(false)
    const comment = reactive({
    show: false,
    text: '',
    quote: null
    })

    const init =()=> apiForumTopic(tid,data=>{
    topic.data=data
    topic.like=data.interact.like
    topic.collect=data.interact.collect
    topic.likeCount=data.interact.likeCount
    topic.collectCount=data.interact.collectCount
    loadComments(1)
    })
    init()

    /**
     * TODO(简历项 1｜代码高亮)：把 Delta 里的代码块渲染成带语法着色的 <pre>
     *  1. npm i highlight.js
     *  2. 下面 convert 完的 HTML 不要直接 v-html，先挂到临时 DOM，遍历 <pre><code>，
     *     用 hljs.highlightElement(el) 上色后再取 innerHTML
     *  3. 主题用 github-dark，代码块右上角加「复制」按钮
     *  验收：老帖子里已有的代码块也能高亮，且不影响非代码内容
     */
    function convertToHtml(content){
    const ops = JSON.parse(content).ops
    const converter = new QuillDeltaToHtmlConverter(ops, { inlineStyles: true });
    return converter.convert();
    }

    function interact(type, message) {
  const next = !topic[type]
  if (type === 'like') {
    topic.likeCount += next ? 1 : -1
  } else if (type === 'collect') {
    topic.collectCount += next ? 1 : -1
  }
  topic[type] = next
  apiForumInteract(tid, type, next, message)
}

    function updateTopic(editor) {
    apiForumUpdateTopic({
    id: tid,
    type: editor.type.id,
    title: editor.title,
    content: JSON.stringify(editor.text)
    }, () => {
    ElMessage.success('帖子内容更新成功！')
    edit.value = false
    init()
    })
    }

    function loadComments(page) {
    topic.comments = null
    topic.page = page
    apiForumComments(tid, page - 1, data => topic.comments = data)
    }

    function onCommentAdd() {
    comment.show = false
    loadComments(Math.floor(++topic.data.comments / 10) + 1)
    }

    function deleteComment(id) {
    apiForumCommentDelete(id, () => {
    ElMessage.success('删除评论成功！')
    loadComments(topic.page)
    })
    }

    function deleteTopic() {
       if (!confirm('确定删除这个帖子吗？')) return
       apiForumUserTopicDelete(tid, () => {
        ElMessage.success('删除成功')
        router.push('/index')
       })
}
</script>

<template>
    <div class="topic-page" v-if="topic.data">
        <div class="topic-bar">
            <el-button link :icon="ArrowLeft" @click="router.push('/index')">返回列表</el-button>
            <span class="bar-title">{{ topic.data.title }}</span>
        </div>
        <div class="topic-main">
            <div class="topic-main-left">
                <el-avatar :src="store.avatarUserUrl(topic.data.user.avatar)"
                           :size="60" />
                <div>
                    <div style="font-size: 18px;font-weight: bold">
                        {{topic.data.user.username}}
                        <span style="color: hotpink" v-if="topic.data.user.gender === 1">
                            <el-icon><Female /></el-icon>
                        </span>
                        <span style="color: dodgerblue" v-if="topic.data.user.gender === 0">
                            <el-icon><Male /></el-icon>
                        </span>
                    </div>
                    <div class="desc">{{topic.data.user.email}}</div>
                </div>
                <el-divider style="margin: 10px 0" />
                <div style="text-align: left;margin: 0 5px">
                    <div class="desc">微信号: {{topic.data.user.wx || '已隐藏或未填写'}}</div>
                    <div class="desc">QQ号: {{topic.data.user.qq || '已隐藏或未填写'}}</div>
                    <div class="desc">手机号: {{topic.data.user.phone || '已隐藏或未填写'}}</div>
                </div>
                <el-divider style="margin: 10px 0" />
                <div class="desc" style="margin: 0 5px">{{topic.data.user.desc}}</div>
            </div>
            <div class="topic-main-right">
                <div class="topic-content" v-html="convertToHtml(topic.data.content)"></div>
                <el-divider />
                <div style="font-size: 13px;color: grey;text-align: center">
                    <div style="text-align: right">发帖时间: {{new Date(topic.data.time).toLocaleString()}}</div>
                </div>
                <div style="text-align: right;margin-top: 30px">
                    <interact-button name="编辑帖子" color="dodgerblue" :check="false"
                                     @check="edit=true" style="margin-right: 20px"
                                     :disabled="topic.data.locked"
                                     v-if="store.user.id===topic.data.user.id">
                        <el-icon><EditPen /></el-icon>
                    </interact-button>

                    <interact-button name="删除帖子" color="red" :check="false"
                                     @check="deleteTopic" style="margin-right: 20px"
                                     v-if="store.user.id===topic.data.user.id || store.user.role==='admin'">
                        <el-icon><Delete /></el-icon>
                    </interact-button>

                    <interact-button :name="`点个赞吧 (${topic.likeCount})`" :check-name="`已点赞 (${topic.likeCount})`" color="pink" :check="topic.like"
                                     @check="interact('like','点赞')">
                        {{ topic.like ? '❤️' : '🤍' }}
                    </interact-button>
                    <interact-button :name="`收藏本帖 (${topic.collectCount})`" :check-name="`已收藏 (${topic.collectCount})`" color="orange" :check="topic.collect"
                                     @check="interact('collect','收藏')"
                                     style="margin-left: 20px">
                        {{ topic.collect ? '⭐' : '☆' }}
                    </interact-button>
                </div>
            </div>
        </div>
        <transition name="el-fade-in-linear" mode="out-in">
            <div class="comment-list" v-if="topic.comments">
                <div class="comment-row" v-for="(item, index) in topic.comments" :key="item.id">
                    <el-avatar :src="store.avatarUserUrl(item.user.avatar)" :size="32"/>
                    <div class="comment-body">
                        <div class="comment-head">
                            <span class="comment-user">{{ item.user.username }}</span>
                            <span class="faint">{{ new Date(item.time).toLocaleString() }}</span>
                            <span class="floor">#{{ (topic.page - 1) * 10 + index + 1 }}</span>
                        </div>
                        <div class="comment-quote" v-if="item.quote">{{ item.quote }}</div>
                        <div class="topic-content" v-html="convertToHtml(item.content)"></div>
                        <div class="comment-actions">
                            <el-link :icon="ChatSquare" type="info" :underline="false"
                                     @click="comment.show = true; comment.quote = item">回复</el-link>
                            <el-link :icon="Delete" type="danger" :underline="false"
                                     v-if="item.user.id === store.user.id || topic.data.user.id === store.user.id || store.user.role === 'admin'"
                                     @click="deleteComment(item.id)">删除</el-link>
                        </div>
                    </div>
                </div>
                <div class="pager">
                    <el-pagination background layout="prev, pager, next"
                                   v-model:current-page="topic.page" @current-change="loadComments"
                                   :total="topic.data.comments" :page-size="10"
                                   hide-on-single-page/>
                </div>
            </div>
        </transition>
        <topic-editor :show="edit" @close="edit =false" v-if="topic.data&&store.forum.types"
                      :default-type="topic.data.type" :default-text="topic.data.content"
                      :default-title="topic.data.title" submit-button="更新帖子内容" :submit="updateTopic" />
        <topic-comment-editor :show="comment.show" @close="comment.show=false" :tid="tid"
                              :quote="comment.quote" @comment="onCommentAdd" />
        <div class="add-comment" v-if="!comment.show && !edit"
             @click="comment.show=true;comment.quote = null">
            <el-icon><Plus /></el-icon>
        </div>
    </div>
</template>

<style scoped>
.topic-page {
    max-width: var(--main-w);
    margin: 0 auto;
    padding: 8px 20px 120px;
}

.topic-bar {
    position: sticky;
    top: var(--header-h);
    z-index: 10;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;
    background: var(--bg);
    border-bottom: 1px solid var(--border);
}

.bar-title {
    font-size: 13px;
    color: var(--text-muted);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.topic-main {
    display: flex;
    margin-top: 20px;
    border: 1px solid var(--border);
    border-radius: var(--radius);
    background: var(--bg);
}

.topic-main-left {
    width: 168px;
    padding: 16px;
    text-align: center;
    border-right: 1px solid var(--border);
    flex-shrink: 0;
}

.topic-main-left .desc {
    font-size: 12px;
    color: var(--text-muted);
    word-break: break-all;
}

.topic-main-right {
    flex: 1;
    min-width: 0;
    padding: 20px 24px;
    display: flex;
    flex-direction: column;
}

.topic-content {
    font-size: 15px;
    line-height: 1.8;
    color: var(--text);
    flex: 1;
    word-break: break-word;
}

.topic-content :deep(img) {
    max-width: 100% !important;
    width: auto !important;
    height: auto !important;
    display: block;
}

/* ---------- 评论楼层 ---------- */
.comment-list {
    margin-top: 24px;
    border-top: 1px solid var(--border);
}

.comment-row {
    display: flex;
    gap: 12px;
    padding: 18px 0;
    border-bottom: 1px solid var(--border);
}

.comment-body {
    flex: 1;
    min-width: 0;
}

.comment-head {
    display: flex;
    align-items: baseline;
    gap: 10px;
    margin-bottom: 6px;
}

.comment-user {
    font-size: 14px;
    font-weight: 600;
    color: var(--text);
}

.floor {
    margin-left: auto;
    font-size: 12px;
    color: var(--text-faint);
}

.comment-quote {
    font-size: 13px;
    color: var(--text-muted);
    background: var(--bg-subtle);
    border-left: 2px solid var(--border-strong);
    padding: 8px 12px;
    margin-bottom: 8px;
    border-radius: 0 var(--radius) var(--radius) 0;
}

.comment-actions {
    display: flex;
    gap: 16px;
    margin-top: 6px;
    font-size: 13px;
}

.pager {
    display: flex;
    justify-content: center;
    padding: 24px 0;
}

/* ---------- 发评论按钮 ---------- */
.add-comment {
    position: fixed;
    bottom: 32px;
    right: 32px;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    color: #fff;
    background: var(--accent);
    cursor: pointer;
    z-index: 9999;
}

.add-comment:hover {
    background: var(--accent-hover);
}

@media (max-width: 768px) {
    .topic-page {
        padding: 8px 12px 100px;
    }

    .topic-main {
        flex-direction: column;
    }

    .topic-main-left {
        width: 100%;
        border-right: none;
        border-bottom: 1px solid var(--border);
    }
}
</style>
<style>
.topic-content img {
  max-width: 100% !important;
  max-height: 500px !important;
  width: auto !important;
  height: auto !important;
  display: block;
  object-fit: contain;
}
</style>

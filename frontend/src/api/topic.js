import {del, get, patch, post} from "@/api/request";
import {ElMessage} from "element-plus";

/* ---------------- 帖子读取 ---------------- */

export const apiForumTypes = (success) =>
    get('/api/forum/types', success)

export const apiForumTopic = (tid, success) =>
    get('/api/forum/topic', {tid}, success)

export const apiForumComments = (tid, page, success) =>
    get('/api/forum/comments', {tid, page}, success)

/**
 * TODO(简历项 3｜采纳答案 + 声望)：后端还没有这两块，需要一起补
 *  1. db_topic 加 accepted_comment_id，db_comment 加 is_accepted
 *  2. POST /api/forum/comments/{id}/accept：只有楼主能采纳，一个帖子只能采纳一条
 *  3. GET /api/user/{id}/stats：采纳数 / 被赞数 / 发帖数，声望 = 采纳×10 + 被赞×2
 *  4. 前端在评论卡片上加「采纳」按钮（仅楼主可见），个人主页展示声望
 *  验收：楼主采纳后，评论出现「已采纳」标记，被采纳者声望 +10
 */
export const apiForumTopicList = (page, type, success) =>
    get('/api/forum/list-topic', {page, type}, success)

export const apiForumTopTopics = (success) =>
    get('/api/forum/top-topic', success)

export const apiForumHotTopics = (success) =>
    get('/api/forum/hot-topic', success)

export const apiTopicSearch = (keyword, success) =>
    get('/api/forum/search-topic', {keyword}, success)

export const apiForumUserTopic = (success) =>
    get('/api/forum/user-topic', success)

export const apiForumCollect = (success) =>
    get('/api/forum/collects', success)

export const apiForumWeather = (longitude, latitude, success) =>
    get('/api/forum/weather', {longitude, latitude}, success)

/* ---------------- 帖子写入 ---------------- */

export const apiForumTopicCreate = (data, success) =>
    post('/api/forum/create-topic', data, success)

export const apiForumUpdateTopic = (data, success) =>
    post('/api/forum/update-topic', data, success)

export const apiForumUserTopicDelete = (tid, success) =>
    del(`/api/forum/topics/${tid}`, success)

export const apiForumCommentSubmit = (data, success) =>
    post('/api/forum/add-comment', data, success)

export const apiForumCommentDelete = (id, success) =>
    del(`/api/forum/comments/${id}`, success)

/**
 * 点赞 / 收藏：显式传目标状态，不再用“取反”表达，重复请求也不会翻转
 */
export const apiForumInteract = (tid, type, state, message) => {
    post('/api/forum/interactions', {tid, type, state}, () => {
        ElMessage.success(state ? `${message}成功！` : `已取消${message}！`)
    })
}

export const apiForumCollectDelete = (tid, success) =>
    post('/api/forum/interactions', {tid, type: 'collect', state: false}, success)

export {patch}

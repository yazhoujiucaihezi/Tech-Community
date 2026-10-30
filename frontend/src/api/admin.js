import {del, get, patch, post} from "@/api/request";

/* ---------------- 帖子管理 ---------------- */

export const apiForumTopicAllList = (page, size, keyword, success) =>
    get('/api/admin/forum/list', {page, size, keyword}, success)

export const apiForumTopicTop = (data, success) =>
    post('/api/admin/forum/top', data, success)

export const apiForumTopicLocked = (data, success) =>
    post('/api/admin/forum/locked', data, success)

export const apiForumTopicInvisible = (data, success) =>
    post('/api/admin/forum/invisible', data, success)

export const apiForumTopicDelete = (tid, success) =>
    del(`/api/admin/topics/${tid}`, success)

export const apiTopicChangeType = (tid, type, success) =>
    patch(`/api/admin/topics/${tid}/type`, {type}, success)

/* ---------------- 分类管理 ---------------- */

export const apiTopicTypeCreate = (data, success) =>
    post('/api/admin/forum/create-type', data, success)

export const apiTopicTypeUpdate = (data, success) =>
    post('/api/admin/forum/update-type', data, success)

// TODO(待实现)：后端还没有删除分类的接口，需要补 DELETE /api/admin/forum/types/{id}
//  1. AdminController 加 @DeleteMapping("/forum/types/{id}")，参数从 @RequestParam 改成 @PathVariable
//  2. AdminService.deleteType(role, id)：先校验角色，再校验该分类下是否还有帖子（有则拒绝删除）
//  3. 前端这里把 tid 传下去即可，现在的实现里参数名写成了 id，是历史遗留的错误
export const apiTopicTypeDelete = (tid, success) =>
    del(`/api/admin/forum/types/${tid}`, success)

// TODO(待实现)：以下两个接口后端不存在（前端调用会 404/500），需要按“版规/禁言名单”补一整套
//  - GET  /api/admin/forum/prohibited-list
//  - POST /api/admin/forum/prohibited-save
export const apiForumProhibitedList = (success) =>
    get('/api/admin/forum/prohibited-list', success)

export const apiForumProhibit = (data, success) =>
    post('/api/admin/forum/prohibited-save', data, success)

/* ---------------- 用户管理 ---------------- */

export const apiUserList = (page, size, keyword, success) =>
    get('/api/admin/user/list', {page, size, keyword}, success)

export const apiUserDetailTotal = (id, success) =>
    get('/api/admin/user/detail', {id}, success)

export const apiUserSave = (data, success) =>
    post('/api/admin/user/save', data, success)

export const apiUserModifyPassword = (data, success) =>
    post('/api/admin/user/change-password', data, success)

/* ---------------- 邮件管理 ---------------- */

export const apiEmailRecordList = (page, size, success) =>
    get('/api/admin/email/list', {page, size}, success)

export const apiEmailResend = (id, success, failure) =>
    post(`/api/admin/emails/${id}/resend`, {}, success, failure)

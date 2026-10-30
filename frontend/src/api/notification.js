import {del, get} from "@/api/request";

export const apiNotificationList = (success) =>
    get('/api/notification/list', success)

export const apiNotificationDelete = (id, success) =>
    del(`/api/notification/${id}`, success)

export const apiNotificationDeleteAll = (success) =>
    del('/api/notification/all', success)

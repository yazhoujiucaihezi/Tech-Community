import {useStore} from "@/stores/general";
import {get, post} from "@/api/request";
import {ElMessage} from "element-plus";

export const apiUserInfo = (loadingRef) => {
    loadingRef.value = true
    const store = useStore();
    get('/api/user/info', (data) => {
        store.user = data
        loadingRef.value = false
    })
}

export const apiUserPrivacy = (success) =>
    get('/api/user/privacy', success)

export const apiUserPrivacySave = (data, loadingRef) => {
    loadingRef.value = true
    post('/api/user/save-privacy', data, () => {
        ElMessage.success('隐私设置修改成功！')
        loadingRef.value = false
    })
}

export const apiUserChangePassword = (data, success) =>
    post('/api/user/change-password', data, success)

export const apiUserDetail = (success) =>
    get('/api/user/details', success)

export const apiUserDetailSave = (form, success, failure) =>
    post('/api/user/save-details', form, success, failure)

export const apiUserModifyEmail = (form, success) =>
    post('/api/user/modify-email', form, success)

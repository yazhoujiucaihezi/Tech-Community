import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router";

const authItemName = "authorize"

axios.defaults.baseURL = import.meta.env.VITE_API_BASE ?? ''
axios.defaults.timeout = 10000

/** 登录态失效的两种表现：真正的 401，或后端统一包成 500 的“登录已过期” */
const isSessionExpired = (code, message) =>
    code === 401 || (typeof message === 'string' && message.includes('登录'))

export const accessHeader = () => ({
    'Authorization': `Bearer ${takeAccessToken()?.token}`
})

const defaultFailure = (message, status, url) => {
    console.warn(`请求地址: ${url}, 状态码: ${status}, 错误信息: ${message}`)
    ElMessage.warning(message)
}

function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if (!str) return null
    const authObj = JSON.parse(str)
    if (new Date(authObj.expire) <= new Date()) {
        deleteAccessToken()
        ElMessage.warning("登录状态已过期，请重新登录！")
        return null
    }
    return authObj
}

function storeAccessToken(remember, token, expire, role) {
    const authObj = {token, expire, role}
    const str = JSON.stringify(authObj)
    if (remember) {
        sessionStorage.removeItem(authItemName)
        localStorage.setItem(authItemName, str)
    } else {
        localStorage.removeItem(authItemName)
        sessionStorage.setItem(authItemName, str)
    }
}

function deleteAccessToken(redirect = false) {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
    if (redirect) {
        router.push({name: 'welcome-login'})
    }
}

/**
 * 统一请求出口：所有方法（get/post/put/patch/delete）都走这里。
 * 约定：HTTP 恒为 200，真正的结果看响应体里的 code。
 */
function request(method, url, payload, success, failure = defaultFailure) {
    const config = {method, url, headers: accessHeader()}
    if (method === 'get') {
        config.params = payload
    } else {
        config.data = payload
    }
    axios(config).then(({data}) => {
        if (data.code === 200) {
            success(data.data)
        } else if (isSessionExpired(data.code, data.message) && url !== '/api/auth/login') {
            deleteAccessToken(true)
            failure(data.message || '登录状态已过期，请重新登录！', data.code, url)
        } else {
            failure(data.message, data.code, url)
        }
    }).catch(err => {
        const response = err.response
        const message = response?.data?.message
            || (err.code === 'ECONNABORTED' ? '请求超时，请稍后重试' : '网络请求失败，请稍后重试')
        failure(message, response?.status, url)
    })
}

/** get 的第二个参数可以是 params，也可以直接是成功回调 */
export const get = (url, params, success, failure) => {
    if (typeof params === 'function') {
        failure = success
        success = params
        params = undefined
    }
    return request('get', url, params, success, failure)
}
export const post = (url, data, success, failure) => request('post', url, data, success, failure)
export const put = (url, data, success, failure) => request('put', url, data, success, failure)
export const patch = (url, data, success, failure) => request('patch', url, data, success, failure)
export const del = (url, success, failure) => request('delete', url, undefined, success, failure)

export function login(username, password, remember, success, failure = defaultFailure) {
    post('/api/auth/login', {username, password}, (data) => {
        storeAccessToken(remember, data.token, data.expire, data.role)
        ElMessage.success(`登录成功，欢迎 ${data.username} 来到我们的系统`)
        success(data)
    }, failure)
}

export function logout(success, failure = defaultFailure) {
    post('/api/auth/logout', {}, () => {
        deleteAccessToken()
        ElMessage.success(`退出登录成功，欢迎您再次使用`)
        success()
    }, failure)
}

/** 流式接口（AI 对话）必须走 fetch，axios 拿不到分块响应 */
export function fetchPost(url, data) {
    return fetch(axios.defaults.baseURL + url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${takeAccessToken()?.token}`
        },
        body: JSON.stringify(data)
    })
}

export const isUnauthorized = () => !takeAccessToken()
export const isRoleAdmin = () => takeAccessToken()?.role === 'admin'
export {deleteAccessToken, storeAccessToken}

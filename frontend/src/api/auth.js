import {post} from "@/api/request";
import {ElMessage} from "element-plus";
import router from "@/router";

export {login, logout} from "@/api/request"

export const apiAuthRegister = (data) => {
    post('/api/auth/register', data, () => {
        ElMessage.success('注册成功，欢迎加入我们')
        router.push("/")
    })
}

export const apiAuthAskCode = (email, coldTime, type = 'register') => {
    coldTime.value = 60
    post('/api/auth/code', {email, type}, () => {
        ElMessage.success(`验证码已发送到邮箱: ${email}，请注意查收`)
        const handle = setInterval(() => {
            coldTime.value--
            if (coldTime.value === 0) {
                clearInterval(handle)
            }
        }, 1000)
    }, (message) => {
        ElMessage.warning(message)
        coldTime.value = 0
    })
}

export const apiAuthRestConfirm = (data, activeRef) =>
    post('/api/auth/reset-confirm', data, () => activeRef.value++)

export const apiAuthResetPassword = (data) => {
    post('/api/auth/reset-password', data, () => {
        ElMessage.success('密码重置成功，请重新登录')
        router.push('/')
    })
}

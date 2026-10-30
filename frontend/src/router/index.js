import { createRouter, createWebHistory } from 'vue-router'
import {isRoleAdmin, isUnauthorized} from "@/api/request";

/**
 * TODO(简历项 5｜技术社区路由)：当前还是论坛的嵌套结构，改版时按这个骨架加
 *  /code-review        → features/code-review/pages/CodeReview.vue   （AI 代码辨析）
 *  /boards/:type       → 板块页，复用 TopicList，把 type 从 query 提到路径
 *  /u/:uid             → features/user/pages/UserProfile.vue        （个人主页 + 声望）
 *  同时把 index 下的 user-setting / forum-setting / privacy-setting 收进 /settings 子路由
 */
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/features/auth/pages/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/features/auth/pages/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/features/auth/pages/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/features/auth/pages/ForgetPage.vue')
                }
            ]
        }, {
            path: '/index',
            name: 'index',
            component: () => import('@/features/home/pages/IndexView.vue'),
            children: [
                {
                    path: '',
                    name: 'topics',
                    component: () => import('@/features/topic/pages/Forum.vue'),
                    children: [
                        {
                            path: "",
                            name: "topic-list",
                            component: () => import('@/features/topic/pages/TopicList.vue')
                        },{
                            path: "topic-detail/:tid",
                            name: "topic-detail",
                            component: () => import('@/features/topic/pages/TopicDetail.vue')
                        }
                    ]
                }, {
                    path: "user-setting",
                    name: "user-setting",
                    component: () => import('@/features/settings/pages/UserSetting.vue')
                },{
                    path: "forum-setting",
                    name: "forum-setting",
                    component: () => import('@/features/settings/pages/ForumSetting.vue')
                },{
                    path: "privacy-setting",
                    name: "privacy-setting",
                    component: () => import('@/features/settings/pages/PrivacySetting.vue')
                }
            ]
        },{
            path: '/admin',
            name: 'admin',
            component: () => import('@/features/admin/pages/AdminView.vue'),
            children: [
                {
                    path: '',
                    name: 'admin-welcome',
                    component: () => import('@/features/admin/pages/WelcomeAdmin.vue')
                }, {
                    path: 'user',
                    name: 'admin-user',
                    component: () => import('@/features/admin/pages/UserAdmin.vue')
                }, {
                    path: 'email',
                    name: 'admin-email',
                    component: () => import('@/features/admin/pages/EmailAdmin.vue')
                }, {
                    path: 'forum',
                    name: 'admin-forum',
                    component: () => import('@/features/admin/pages/ForumAdmin.vue')
                }
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    const unauthorized = isUnauthorized(),admin=isRoleAdmin()
    if(to.name.startsWith('welcome') && !unauthorized) {
        next('/index')
    } else if(to.fullPath.startsWith('/admin') && !admin){
        next('/index')
    } else if(to.fullPath.startsWith('/index') && unauthorized) {
        next('/')
    } else {
        next()
    }
})

export default router

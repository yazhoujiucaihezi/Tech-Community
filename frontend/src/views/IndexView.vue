<script setup>
import {inject, reactive, ref} from "vue";
import {
    Bell,
    ChatDotSquare, Check, CoffeeCup, Collection, DataLine, Document, Files,
    Location, Lock, Monitor,
    Notification, Operation, Position,
    Search,
    Umbrella, User, WindPower
} from "@element-plus/icons-vue";
import LightCard from "@/components/LightCard.vue";
import UserInfo from "@/components/UserInfo.vue";
import {apiNotificationDelete, apiNotificationDeleteAll, apiNotificationList} from "@/net/api/user";
import AiChatWindow from "@/components/AiChatWindow.vue";
import {apiForumTypes, apiTopicSearch} from "@/net/api/forum";
import TopicTag from "@/components/TopicTag.vue";
import {useStore} from "@/store";
import router from "@/router";

const store = useStore()

const userMenu = [
  { title: '帖子广场', icon: ChatDotSquare, index: '/index' },
  { title: '个人信息设置', icon: User, index: '/index/user-setting' },
  { title: '论坛帖子管理', icon: CoffeeCup, index: '/index/forum-setting' },
  { title: '账号安全设置', icon: Lock, index: '/index/privacy-setting' }
]

const loading =inject("userLoading")

const searchInput =reactive({
  type: '1',
  test:''
})




const notification = ref([])


const loadNotification =
    () => apiNotificationList(data => notification.value = data)
loadNotification()

function confirmNotification(id, url) {
  apiNotificationDelete(id, () => {
    loadNotification()
    window.open(url)
  })
}

const searchTopic = (keyword, callback) => {
    if(!keyword) {
        return
    }
    apiTopicSearch(keyword, data => {
        callback(data)
    })
}

const toTopic = ({ id }) => {
    router.push('/index/topic-detail/'+id)
}

function deleteAllNotification() {
  apiNotificationDeleteAll(loadNotification)
}

apiForumTypes(data => {
    const array= []
    array.push({name: '全部', id: 0, color: 'linear-gradient(45deg,white,red,orange,gold,green,blue'})
    data.forEach(d=>array.push(d))
    store.forum.types =array
})
</script>

<template>
  <div class="main-content" v-loading="loading" element-loading-text="正在进入，请稍后...">
    <ai-chat-window v-if="$route.name !== 'topic-detail'"/>
    <el-container style="height: 100%" v-if="!loading">
      <el-header class="main-content-header">
        <div style="width: 320px;height: 32px;display:flex;align-items:center;gap:10px">
          <img class="logo" src="/brand-logo.png" alt="亚洲韭菜盒子"/>
          <span class="brand-name">亚洲韭菜盒子</span>
        </div>
        <div style="flex: 1;padding:0 20px;display:flex;align-items:center;justify-content:center;gap:14px">
          <a class="github-mark" href="https://github.com/yazhoujiucaihezi/campus_forum"
             target="_blank" rel="noopener" title="yazhoujiucaihezi / campus_forum">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor" aria-hidden="true">
              <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/>
            </svg>
          </a>
          <el-autocomplete v-model="searchInput.test" style="width: 100%;max-width: 500px"
                           fit-input-width
                           :fetch-suggestions="searchTopic"
                           @select="toTopic"
                           placeholder="搜索论坛相关内容...">
            <template #prefix>
              <el-icon><Search/></el-icon>
            </template>
            <template #default="{ item }">
              <div class="search-item">
                 <div class="title" v-if="item.highlight?.title">
                      <topic-tag style="margin-right: 10px;" :type="item.type"/>
                      <span v-html="item.highlight.title"></span>
                    </div>
                    <div class="title" v-else>
                      <topic-tag style="margin-right: 10px;" :type="item.type"/>
                      <span>{{ item.title }}</span>
                    </div>
                    <div class="desc" v-if="item.highlight?.intro" v-html="item.highlight.intro"></div>
                    <div class="desc" v-else>{{ item.intro }}</div>
                </div>
            </template>
              <template #append>
              <el-select style="width: 120px" v-model="searchInput.type">
                <el-option value="1" label="帖子广场"/>
                <el-option value="2" label="校园活动"/>
                <el-option value="3" label="表白墙"/>
                <el-option value="4" label="教务通知"/>
              </el-select>
            </template>
          </el-autocomplete>
        </div>
        <user-info>
          <el-popover placement="bottom" :width="350" trigger="click">
            <template #reference>
              <el-badge is-dot :hidden="!notification.length">
                <div class="notification">
                  <el-icon><Bell/></el-icon>
                  <div style="font-size: 10px">消息</div>
                </div>
              </el-badge>
            </template>
            <el-empty :image-size="80" description="暂时没有未读消息哦~" v-if="!notification.length"/>
            <el-scrollbar :max-height="500" v-else>
              <light-card v-for="item in notification" class="notification-item"
                          @click="confirmNotification(item.id, item.url)">
                <div>
                  <el-tag size="small" :type="item.type">消息</el-tag>&nbsp;
                  <span style="font-weight: bold">{{item.title}}</span>
                </div>
                <el-divider style="margin: 7px 0 3px 0"/>
                <div style="font-size: 13px;color: grey">
                  {{item.content}}
                </div>
              </light-card>
            </el-scrollbar>
            <div style="margin-top: 10px">
              <el-button size="small" type="info" :icon="Check" @click="deleteAllNotification"
                         style="width: 100%" plain>清除全部未读消息</el-button>
            </div>
          </el-popover>
        </user-info>
      </el-header>
      <el-container>
        <el-aside width="230px">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <el-menu
    router
    :default-active="$route.path"
    style="min-height: calc(100vh - 55px)">
  <el-menu-item :index="menu.index" v-for="menu in userMenu" :key="menu.index">
    <el-icon>
      <component :is="menu.icon"/>
    </el-icon>
    <span>{{ menu.title }}</span>
  </el-menu-item>
</el-menu>
          </el-scrollbar>
        </el-aside>
        <el-main class="main-content-page">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <router-view v-slot="{ Component }">
              <transition name="el-fade-in-linear" mode="out-in">
                <Component :is="Component" style="height: 100%"/>
              </transition>
            </router-view>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>



<style lang="less" scoped>

.github-mark {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    color: #8a8a8a;
    opacity: .45;
    transition: opacity .25s, color .25s;

    &:hover {
        opacity: 1;
        color: #24292f;
    }
}

.dark .github-mark:hover {
    color: #ffffff;
}

.dark .brand-name {
    color: #e8e8e8;
}

.search-item {
    line-height: 1.5;
    padding: 10px 0;

    :deep(em) {
        color: #1a1a1a;
        background-color: yellow;
        font-style: normal;
    }

    .title {
        font-size: 15px;
        font-weight: bold;
        margin-bottom: 5px;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 1;
        overflow: hidden;
    }

    .desc {
        font-size: 13px;
        white-space: pre-wrap;
        color: gray;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
    }
}

.notification-item {
  transition: .3s;
  &:hover {
    cursor: pointer;
    opacity: 0.7;
  }
}

.notification {
  font-size: 22px;
  line-height: 14px;
  text-align: center;
  transition: color .3s;

  &:hover {
    color: grey;
    cursor: pointer;
  }
}

.main-content-page{
  padding: 0;
  background-color: #f7f8fa;
}

.dark .main-content-page{
  background-color: #212225;
}

.main-content{
  height: 100vh;
  width: 100vw;
}
.main-content-header {
  border-bottom: solid 1px var(--el-border-color);
  height: 55px;
  display: flex;
  align-items: center;
  box-sizing: border-box;

  .logo {
    width: 32px;
    height: 32px;
    object-fit: contain;
    flex-shrink: 0;
  }

  .brand-name {
    font-size: 17px;
    font-weight: 600;
    letter-spacing: 1px;
    white-space: nowrap;
    color: #2c2c2a;
  }

  .user-info{
    display: flex;
    justify-content: flex-end;
    align-items: center;

    .el-avatar:hover {
      cursor: pointer;
    }

    .profile{
      text-align: right;
      margin-right: 20px;

      :first-child {
        font-size: 18px;
        font-weight: bold;
        line-height: 20px;
      }

      :last-child {
        font-size: 10px ;
        color: grey;
      }
    }
  }

}
</style>

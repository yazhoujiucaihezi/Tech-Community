<script setup>
import {ref} from "vue";
import ForumTopicAdmin from "@/features/admin/components/ForumTopicAdmin.vue";
import {apiForumTypes} from "@/api/topic";
import ForumTopicTypeAdmin from "@/features/admin/components/ForumTopicTypeAdmin.vue";

const initTypes = () => apiForumTypes(data => types.value = data)
const types = ref([])

apiForumTypes(data => types.value = data)
initTypes()
</script>
<template>
<div class="forum-admin">
    <div>
        <forum-topic-admin :types="types"/>
    </div>
    <forum-topic-type-admin :types="types" @update="initTypes"/>

</div>
</template>


<style lang="less" scoped>
.forum-admin {
    display: flex;
    flex-direction: column;
    gap: 50px;
}


</style>
import {fetchPost} from "@/api/request";

/**
 * TODO(简历项 2｜AI 代码辨析)：现在只有通用对话，要改成「贴代码 → 指出问题」
 *  1. 新建页面 features/code-review/pages/CodeReview.vue：左边 textarea 贴代码，右边流式输出
 *  2. 复用下面这个 SSE 接口，但 messages 里固定一条 system：
 *     「你是资深 Java 面试官，只输出：潜在 bug / 复杂度 / 命名与可读性 / 改进后的代码」
 *  3. 输出用 markdown-it 渲染，代码块走 hljs
 *  4. 悬浮球（AiChatWindow）保留，代码辨析作为独立入口并存
 *  验收：贴一段有明显 NPE 的代码，能按上面四段结构返回
 */
export const apiChatWithAI = async (context, onMessage, onError, onComplete) => {
    try {
        const response = await fetchPost('/api/ai/chat', context)

        if (!response.ok || !response.body) {
            throw new Error(`AI 请求失败：HTTP ${response.status}`)
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = ''

        const consumeEvents = (flush = false) => {
            const parts = buffer.split(/\r?\n\r?\n/)
            if (!flush) buffer = parts.pop() || ''
            else buffer = ''
            for (const event of parts) {
                const data = event.split(/\r?\n/)
                    .filter(line => line.startsWith('data:'))
                    .map(line => line.slice(5).replace(/^ /, ''))
                    .join('\n')
                if (data) onMessage(data)
            }
        }

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            buffer += decoder.decode(value, {stream: true})
            consumeEvents()
        }
        buffer += decoder.decode()
        if (buffer) {
            buffer += '\n\n'
            consumeEvents(true)
        }
        onComplete()
    } catch (e) {
        onError(e)
    }
}

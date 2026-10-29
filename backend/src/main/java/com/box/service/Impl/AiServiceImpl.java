package com.box.service.Impl;

import com.box.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;

    /** 保留最近 5 轮对话（1 轮 = 提问 + 回答 = 2 条消息） */
    private static final int MAX_ROUNDS = 5;

    /** 同一用户 60 秒内最多请求 5 次 */
    private static final int RATE_LIMIT = 5;

    /** 限频窗口，必须设过期，否则用户被锁死后永远恢复不了 */
    private static final long RATE_WINDOW_SECONDS = 60;

    /** 系统提示词由服务端固定，不接受前端传入，避免提示词注入 */
    private static final String SYSTEM_PROMPT =
            "你是校园论坛的助手，只回答与学习、校园生活相关的问题";

    @Override
    public Flux<String> chat(Integer uid, List<Map<String, Object>> messages) {
        String rateKey = "AICount" + uid;

        // 计数一进来就 +1；只有第一次把 key 建出来时才设过期，之后不再重置窗口
        Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(rateKey, RATE_WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        if (count != null && count > RATE_LIMIT) {
            // 流式接口没法再返回 JSON 错误，只能以一段文本的形式告知前端
            return Flux.just("请求过于频繁，请稍后再试");
        }

        List<Message> trimmed = trim(buildMessages(messages));

        return chatClient.prompt()
                .messages(trimmed)
                .system(SYSTEM_PROMPT)
                .stream()
                .content()
                .onErrorResume(e -> Flux.just("\n[网络异常，请重试]"));
    }

    /**
     * 把前端传来的 Map 列表转成 Spring AI 的消息对象。
     * 只接受 user / assistant 两种角色，其余（含 system）一律丢弃。
     */
    private List<Message> buildMessages(List<Map<String, Object>> messages) {
        return messages.stream()
                .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                .map(m -> {
                    // 缺 content 时给空串，避免 String.valueOf(null) 得到字面量 "null"
                    String content = m.get("content") == null ? "" : String.valueOf(m.get("content"));
                    return (Message) ("assistant".equals(m.get("role"))
                            ? new AssistantMessage(content)
                            : new UserMessage(content));
                })
                .toList();
    }

    /**
     * 只保留最近 N 轮，并去掉开头可能出现的 assistant 消息。
     * 对话以模型发言开头会让模型困惑，部分厂商接口也会直接报错。
     */
    private List<Message> trim(List<Message> messages) {
        int keep = MAX_ROUNDS * 2;
        List<Message> result = messages.size() > keep
                ? new ArrayList<>(messages.subList(messages.size() - keep, messages.size()))
                : new ArrayList<>(messages);

        while (!result.isEmpty() && result.get(0) instanceof AssistantMessage) {
            result.remove(0);
        }
        return result;
    }
}

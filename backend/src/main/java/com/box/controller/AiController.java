package com.box.controller;

import com.box.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody List<Map<String, Object>> messages, @RequestHeader("Authorization") String auth) {
        Integer uid = JwtUtils.getUid(auth);
        String userMessage = String.valueOf(messages.get(messages.size() - 1).get("text"));

        String limitKey = "chat_limit:" + uid;
        Long increment = redisTemplate.opsForValue().increment(limitKey);

        if(increment != null && increment == 1){
            redisTemplate.expire(limitKey, 30, TimeUnit.SECONDS);
        }

        if(increment != null && increment > 10){
            throw new RuntimeException("聊天频率过高，请稍后再试");
        }


        //截断对话
        if(messages.size() > 10){
            messages = new ArrayList<>(messages.subList(messages.size() - 10 , messages.size()));
            Map<String,Object> summary = Map.of("system", "总结前十轮对话放进此对话");
            messages.add(0, summary);
            }

        return chatClient.prompt()
                .system("你是一个校园论坛的AI助手,如若用户没有要求,请用中文回答")
                .user(userMessage)
                .stream()
                .content();
    }
}
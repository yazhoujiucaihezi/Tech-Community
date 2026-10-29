package com.box.controller;

import com.box.service.AiService;
import com.box.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final JwtUtils jwtUtils;

    /**
     * 流式对话：逐块推送，首字即可返回，不必等整段生成完。
     * 限频、上下文裁剪与系统提示词都在 AiServiceImpl 里。
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody List<Map<String, Object>> messages,
                             @RequestHeader("Authorization") String auth) {
        return aiService.chat(jwtUtils.getUid(auth), messages);
    }
}

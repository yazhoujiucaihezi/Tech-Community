package com.box.service;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * AI 对话服务：负责限频、上下文裁剪与流式调用，Controller 只做参数解析。
 */
public interface AiService {

    /**
     * 发起一次流式对话
     *
     * @param uid      当前用户 ID，用于限频
     * @param messages 前端传来的多轮对话，每项含 role / content
     * @return 逐块推送的回答内容；被限频时返回一句提示而不是抛异常
     */
    Flux<String> chat(Integer uid, List<Map<String, Object>> messages);
}

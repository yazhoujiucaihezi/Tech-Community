package com.box.constant;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

// 发送到 MQ 的通知消息
@Data
@Accessors(chain = true)
public class NotificationMessage implements Serializable {

    /**
     * 消息唯一标识，用于消费端幂等去重。
     * 规则：事件类型 + 业务主键，例如 comment:123 / like:4:32
     */
    private String msgId;

    // 接收者用户 ID
    private Integer uid;

    // 通知标题
    private String title;

    // 通知内容
    private String content;

    // 类型：like / comment / follow / system
    private String type;

    // 跳转链接
    private String url;
}
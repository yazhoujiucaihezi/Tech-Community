package com.box.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 通知，对应 db_notification */
@Data
@TableName("db_notification")
public class Notification {

    /** 通知 ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 消息唯一标识，与 db_notification.msg_id 唯一索引配合做幂等兜底 */
    private String msgId;

    /** 接收者用户 ID */
    private Integer uid;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 类型：like / comment / follow / system */
    private String type;

    /** 跳转链接 */
    private String url;

    /** 时间 */
    private LocalDateTime time;
}
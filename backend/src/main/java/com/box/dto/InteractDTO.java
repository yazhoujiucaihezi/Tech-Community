package com.box.dto;

import lombok.Data;

/**
 * 帖子互动（点赞 / 收藏）请求体
 * <p>
 * 由 GET 改 POST 后，参数走请求体，避免被浏览器预取或爬虫误触发。
 */
@Data
public class InteractDTO {

    /** 帖子 ID */
    private Integer tid;

    /** 互动类型：like / collect */
    private String type;

    /** 目标状态：true 表示点赞/收藏，false 表示取消 */
    private Boolean state;
}

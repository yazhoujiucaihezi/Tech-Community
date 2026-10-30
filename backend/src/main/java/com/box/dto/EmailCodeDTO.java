package com.box.dto;

import lombok.Data;

/**
 * 邮箱验证码请求体
 */
@Data
public class EmailCodeDTO {

    /** 邮箱地址 */
    private String email;

    /** 用途：register / reset */
    private String type;
}

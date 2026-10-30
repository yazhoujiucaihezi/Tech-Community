package com.box.dto;

import lombok.Data;

/**
 * 修改帖子分类请求体
 */
@Data
public class ChangeTypeDTO {

    /** 目标分类 ID */
    private Integer type;
}

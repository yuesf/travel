package com.travel.dto;

import lombok.Data;

/**
 * 更新文章标签请求DTO
 * 
 * @author travel-platform
 */
@Data
public class ArticleTagUpdateRequest {
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 标签颜色
     */
    private String color;
}

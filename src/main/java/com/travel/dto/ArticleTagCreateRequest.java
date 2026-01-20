package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建文章标签请求DTO
 * 
 * @author travel-platform
 */
@Data
public class ArticleTagCreateRequest {
    
    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    private String name;
    
    /**
     * 标签颜色
     */
    private String color;
}

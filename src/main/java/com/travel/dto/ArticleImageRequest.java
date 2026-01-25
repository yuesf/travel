package com.travel.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章图片请求DTO
 * 
 * @author travel-platform
 */
@Data
public class ArticleImageRequest {
    
    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    /**
     * 图片URL列表（按顺序）
     */
    @NotEmpty(message = "图片URL列表不能为空")
    @Size(max = 10, message = "图片数量不能超过10张")
    private List<String> imageUrls;
}

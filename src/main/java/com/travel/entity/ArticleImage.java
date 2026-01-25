package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章图片实体类
 * 
 * @author travel-platform
 */
@Data
public class ArticleImage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 文章ID
     */
    private Long articleId;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

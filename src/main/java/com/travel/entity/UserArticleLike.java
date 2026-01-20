package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户文章点赞实体类
 * 
 * @author travel-platform
 */
@Data
public class UserArticleLike implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 文章ID
     */
    private Long articleId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

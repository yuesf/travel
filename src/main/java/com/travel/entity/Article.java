package com.travel.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体类
 * 
 * @author travel-platform
 */
@Data
public class Article implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章摘要
     */
    private String summary;
    
    /**
     * 封面图URL
     */
    private String coverImage;
    
    /**
     * 文章内容（富文本）
     */
    private String content;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称（非数据库字段，用于前端展示）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String categoryName;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 作者ID（管理员ID）
     */
    private Long authorId;
    
    /**
     * 状态：0-草稿，1-已发布，2-已下架
     */
    private Integer status;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 阅读量
     */
    private Integer viewCount;
    
    /**
     * 点赞量
     */
    private Integer likeCount;
    
    /**
     * 收藏量
     */
    private Integer favoriteCount;
    
    /**
     * 是否推荐：0-否，1-是
     */
    private Integer isRecommend;
    
    /**
     * 排序
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
    
    /**
     * 标签ID列表（非数据库字段，用于前端展示）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Long> tagIds;
}

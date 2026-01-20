package com.travel.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新文章请求DTO
 * 
 * @author travel-platform
 */
@Data
public class ArticleUpdateRequest {
    
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
     * 作者
     */
    private String author;
    
    /**
     * 状态：0-草稿，1-已发布，2-已下架
     */
    private Integer status;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 是否推荐：0-否，1-是
     */
    private Integer isRecommend;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 标签ID列表
     */
    private List<Long> tagIds;
}

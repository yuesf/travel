package com.travel.dto;

import lombok.Data;

/**
 * 文章列表查询请求DTO
 * 
 * @author travel-platform
 */
@Data
public class ArticleListRequest {
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
    
    /**
     * 标题（模糊查询）
     */
    private String title;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 作者（模糊查询）
     */
    private String author;
    
    /**
     * 状态：0-草稿，1-已发布，2-已下架
     */
    private Integer status;
    
    /**
     * 发布时间开始
     */
    private String publishTimeStart;
    
    /**
     * 发布时间结束
     */
    private String publishTimeEnd;
}

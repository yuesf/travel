package com.travel.dto;

import lombok.Data;

/**
 * 更新文章分类请求DTO
 * 
 * @author travel-platform
 */
@Data
public class ArticleCategoryUpdateRequest {
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}

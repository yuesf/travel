package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建商品分类请求DTO
 * 
 * @author travel-platform
 */
@Data
public class ProductCategoryCreateRequest {
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 父分类ID（0表示一级分类）
     */
    private Long parentId;
    
    /**
     * 分类层级：1-一级，2-二级
     */
    private Integer level;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 分类类型：DISPLAY-展示类型，CONFIG-配置类型（用于Icon配置）
     */
    private String type;
}

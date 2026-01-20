package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类实体类
 * 
 * @author travel-platform
 */
@Data
public class ProductCategory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类图标
     */
    private String icon;
    
    /**
     * 父分类ID
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
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 子分类列表（用于树形结构）
     */
    private List<ProductCategory> children;
}

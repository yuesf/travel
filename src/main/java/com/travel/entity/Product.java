package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商品实体类
 * 
 * @author travel-platform
 */
@Data
public class Product implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 商品编码
     */
    private String code;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 原价
     */
    private BigDecimal originalPrice;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 销量
     */
    private Integer sales;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 图片列表（JSON数组）
     */
    private List<String> images;
    
    /**
     * 规格（JSON对象）
     */
    private Map<String, Object> specifications;
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 分类名称（关联查询）
     */
    private String categoryName;
    
    /**
     * 外部链接（H5类型商品使用）
     */
    private String h5Link;
}

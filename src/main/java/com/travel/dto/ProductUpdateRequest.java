package com.travel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 更新商品请求DTO
 * 
 * @author travel-platform
 */
@Data
public class ProductUpdateRequest {
    
    /**
     * 商品名称
     */
    private String name;
    
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
     * 描述
     */
    private String description;
    
    /**
     * 图片列表
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
     * 外部链接（H5类型商品使用）
     */
    private String h5Link;
}

package com.travel.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表查询请求DTO
 * 
 * @author travel-platform
 */
@Data
public class ProductListRequest {
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
    
    /**
     * 商品名称（模糊搜索）
     */
    private String name;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 价格区间-最低价
     */
    private BigDecimal minPrice;
    
    /**
     * 价格区间-最高价
     */
    private BigDecimal maxPrice;
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
}

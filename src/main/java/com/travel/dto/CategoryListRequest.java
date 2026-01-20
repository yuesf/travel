package com.travel.dto;

import lombok.Data;

/**
 * 小程序分类列表查询请求DTO
 * 
 * @author travel-platform
 */
@Data
public class CategoryListRequest {
    
    /**
     * 分类ID（商品分类）
     */
    private Long categoryId;
    
    /**
     * 城市（景点、酒店）
     */
    private String city;
    
    /**
     * 价格区间最小值
     */
    private java.math.BigDecimal minPrice;
    
    /**
     * 价格区间最大值
     */
    private java.math.BigDecimal maxPrice;
    
    /**
     * 星级（酒店）
     */
    private Integer starLevel;
    
    /**
     * 排序字段：price-价格，sales-销量，create_time-创建时间
     */
    private String sortField = "create_time";
    
    /**
     * 排序方式：asc-升序，desc-降序
     */
    private String sortOrder = "desc";
    
    /**
     * 页码（从1开始）
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}

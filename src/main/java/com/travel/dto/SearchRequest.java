package com.travel.dto;

import lombok.Data;

/**
 * 搜索请求DTO
 * 
 * @author travel-platform
 */
@Data
public class SearchRequest {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 搜索类型：ALL-全部，ATTRACTION-景点，HOTEL-酒店，PRODUCT-商品
     */
    private String type;
    
    /**
     * 页码
     */
    private Integer page;
    
    /**
     * 每页数量
     */
    private Integer pageSize;
}

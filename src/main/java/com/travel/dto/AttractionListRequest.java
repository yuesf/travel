package com.travel.dto;

import lombok.Data;

/**
 * 景点列表查询请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionListRequest {
    
    /**
     * 景点名称（模糊查询）
     */
    private String name;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
    
    /**
     * 页码（从1开始）
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}

package com.travel.dto;

import lombok.Data;

/**
 * 酒店列表查询请求DTO
 * 
 * @author travel-platform
 */
@Data
public class HotelListRequest {
    
    /**
     * 酒店名称（模糊查询）
     */
    private String name;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 星级
     */
    private Integer starLevel;
    
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

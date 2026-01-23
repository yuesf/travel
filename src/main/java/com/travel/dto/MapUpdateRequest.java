package com.travel.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新地图请求DTO
 * 
 * @author travel-platform
 */
@Data
public class MapUpdateRequest {
    
    /**
     * 地图名称
     */
    private String name;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 地址描述
     */
    private String address;
    
    /**
     * 公告内容
     */
    private String announcement;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}

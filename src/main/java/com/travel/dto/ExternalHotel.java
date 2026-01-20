package com.travel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 外部酒店数据DTO（用于接收外部API数据）
 * 
 * @author travel-platform
 */
@Data
public class ExternalHotel {
    
    /**
     * 酒店名称
     */
    private String name;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 星级：1-5
     */
    private Integer starLevel;
    
    /**
     * 简介
     */
    private String description;
    
    /**
     * 图片列表
     */
    private List<String> images;
    
    /**
     * 设施列表
     */
    private List<String> facilities;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 房型列表
     */
    private List<ExternalHotelRoom> rooms;
}

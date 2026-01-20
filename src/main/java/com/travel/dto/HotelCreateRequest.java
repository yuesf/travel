package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建酒店请求DTO
 * 
 * @author travel-platform
 */
@Data
public class HotelCreateRequest {
    
    /**
     * 酒店名称
     */
    @NotBlank(message = "酒店名称不能为空")
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
     * 状态：0-下架，1-上架
     */
    private Integer status;
}

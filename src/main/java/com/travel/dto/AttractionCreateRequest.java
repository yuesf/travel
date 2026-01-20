package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建景点请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionCreateRequest {
    
    /**
     * 景点名称
     */
    @NotBlank(message = "景点名称不能为空")
    private String name;
    
    /**
     * 位置
     */
    private String location;
    
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
     * 详细地址
     */
    private String address;
    
    /**
     * 简介
     */
    private String description;
    
    /**
     * 图片列表
     */
    private List<String> images;
    
    /**
     * 视频URL
     */
    private String videoUrl;
    
    /**
     * 开放时间
     */
    private String openTime;
    
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
     * 门票价格
     */
    @NotNull(message = "门票价格不能为空")
    private BigDecimal ticketPrice;
    
    /**
     * 门票库存
     */
    @NotNull(message = "门票库存不能为空")
    private Integer ticketStock;
    
    /**
     * 有效期
     */
    private String validPeriod;
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
}

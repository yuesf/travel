package com.travel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 外部景点数据DTO（用于接收外部API数据）
 * 
 * @author travel-platform
 */
@Data
public class ExternalAttraction {
    
    /**
     * 景点名称
     */
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
    private BigDecimal ticketPrice;
    
    /**
     * 门票库存
     */
    private Integer ticketStock;
    
    /**
     * 有效期
     */
    private String validPeriod;
}

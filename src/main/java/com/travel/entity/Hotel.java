package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 酒店实体类
 * 
 * @author travel-platform
 */
@Data
public class Hotel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
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
     * 图片列表（JSON数组）
     */
    private List<String> images;
    
    /**
     * 设施列表（JSON数组）
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
    
    /**
     * 同步来源
     */
    private String syncSource;
    
    /**
     * 同步时间
     */
    private LocalDateTime syncTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

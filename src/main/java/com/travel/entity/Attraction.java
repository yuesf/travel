package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 景点实体类
 * 
 * @author travel-platform
 */
@Data
public class Attraction implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
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
     * 图片列表（JSON数组）
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
    
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
    
    /**
     * 景区评级：1A-5A
     */
    private String rating;
    
    /**
     * 景区标签列表
     */
    private List<String> tags;
    
    /**
     * 入园须知内容
     */
    private String admissionNotice;
    
    /**
     * 入园须知链接
     */
    private String admissionNoticeUrl;
    
    /**
     * 是否启用金顶预约：0-否，1-是
     */
    private Integer goldenSummitEnabled;
    
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

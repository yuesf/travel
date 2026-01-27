package com.travel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 更新具体票种请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionTicketUpdateRequest {
    
    /**
     * 票种名称
     */
    private String name;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 包含景点列表
     */
    private List<String> includedAttractions;
    
    /**
     * 核验方式：ID_CARD-身份证，VALID_DOCUMENT-有效证件
     */
    private String verificationMethod;
    
    /**
     * 退改规则：ANYTIME_REFUND-随时可退，NO_REFUND-不可退
     */
    private String refundRule;
    
    /**
     * 预订须知链接
     */
    private String bookingNoticeUrl;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}

package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 具体票种实体类
 * 
 * @author travel-platform
 */
@Data
public class AttractionTicket implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 景点ID
     */
    private Long attractionId;
    
    /**
     * 票种分类ID
     */
    private Long categoryId;
    
    /**
     * 票种名称
     */
    private String name;
    
    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 包含景点列表（JSON数组）
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
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

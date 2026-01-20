package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付配置实体类
 * 
 * @author travel-platform
 */
@Data
public class PaymentConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 小程序AppID
     */
    private String appId;
    
    /**
     * 商户号
     */
    private String mchId;
    
    /**
     * API密钥（加密存储）
     */
    private String apiKey;
    
    /**
     * 支付回调地址
     */
    private String notifyUrl;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

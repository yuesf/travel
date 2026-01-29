package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建具体票种请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionTicketCreateRequest {
    
    /**
     * 景点ID（从路径参数获取，不需要验证）
     */
    private Long attractionId;
    
    /**
     * 票种分类ID
     */
    @NotNull(message = "票种分类ID不能为空")
    private Long categoryId;
    
    /**
     * 票种名称
     */
    @NotBlank(message = "票种名称不能为空")
    private String name;
    
    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /**
     * 库存
     */
    @NotNull(message = "库存不能为空")
    private Integer stock;
    
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
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}

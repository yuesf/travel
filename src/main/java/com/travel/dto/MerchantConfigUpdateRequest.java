package com.travel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新商家配置请求DTO
 * 
 * @author travel-platform
 */
@Data
public class MerchantConfigUpdateRequest {
    
    /**
     * 商家名称
     */
    private String merchantName;
    
    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;
    
    /**
     * 联系邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String contactEmail;
    
    /**
     * 商家地址
     */
    private String address;
    
    /**
     * 商家描述
     */
    private String description;
}

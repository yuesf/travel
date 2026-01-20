package com.travel.service;

import com.travel.config.WechatConfig;
import com.travel.common.ResultCode;
import com.travel.dto.PaymentConfigCreateRequest;
import com.travel.dto.PaymentConfigUpdateRequest;
import com.travel.entity.PaymentConfig;
import com.travel.exception.BusinessException;
import com.travel.mapper.PaymentConfigMapper;
import com.travel.util.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付配置服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class PaymentConfigService {
    
    @Autowired
    private PaymentConfigMapper paymentConfigMapper;
    
    @Autowired
    private WechatConfig wechatConfig;
    
    /**
     * 根据ID查询
     */
    public PaymentConfig getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "配置ID不能为空");
        }
        PaymentConfig config = paymentConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return config;
    }
    
    /**
     * 查询启用的配置（使用缓存）
     */
    @Cacheable(value = "paymentConfig", key = "'enabled'")
    public PaymentConfig getEnabled() {
        return paymentConfigMapper.selectEnabled();
    }
    
    /**
     * 根据AppID查询
     */
    public PaymentConfig getByAppId(String appId) {
        if (appId == null || appId.trim().isEmpty()) {
            return null;
        }
        return paymentConfigMapper.selectByAppId(appId);
    }
    
    /**
     * 创建配置
     */
    @Transactional
    @CacheEvict(value = "paymentConfig", allEntries = true)
    public PaymentConfig create(PaymentConfigCreateRequest request) {
        // 验证AppID格式
        if (request.getAppId() == null || !request.getAppId().startsWith("wx")) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "AppID格式不正确，必须以wx开头");
        }
        
        // 验证是否已存在
        PaymentConfig existingConfig = paymentConfigMapper.selectByAppId(request.getAppId());
        if (existingConfig != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "该AppID的配置已存在");
        }
        
        PaymentConfig config = new PaymentConfig();
        BeanUtils.copyProperties(request, config);
        
        // 加密API密钥
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            config.setApiKey(EncryptionUtil.encrypt(config.getApiKey()));
        }
        
        // 设置默认状态
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        
        paymentConfigMapper.insert(config);
        log.info("创建支付配置成功，ID: {}, AppID: {}", config.getId(), config.getAppId());
        return config;
    }
    
    /**
     * 更新配置
     */
    @Transactional
    @CacheEvict(value = "paymentConfig", allEntries = true)
    public PaymentConfig update(Long id, PaymentConfigUpdateRequest request) {
        PaymentConfig config = getById(id);
        
        // 验证AppID格式
        if (request.getAppId() != null && !request.getAppId().startsWith("wx")) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "AppID格式不正确，必须以wx开头");
        }
        
        // 如果更新AppID，检查是否与其他配置冲突
        if (request.getAppId() != null && !request.getAppId().equals(config.getAppId())) {
            PaymentConfig existingConfig = paymentConfigMapper.selectByAppId(request.getAppId());
            if (existingConfig != null && !existingConfig.getId().equals(id)) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "该AppID的配置已存在");
            }
        }
        
        // 更新字段
        if (request.getAppId() != null) {
            config.setAppId(request.getAppId());
        }
        if (request.getMchId() != null) {
            config.setMchId(request.getMchId());
        }
        if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
            // 如果API密钥以****开头，说明是部分隐藏的显示值，不更新
            if (!request.getApiKey().startsWith("****")) {
                config.setApiKey(EncryptionUtil.encrypt(request.getApiKey()));
            }
        }
        if (request.getNotifyUrl() != null) {
            config.setNotifyUrl(request.getNotifyUrl());
        }
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        
        paymentConfigMapper.updateById(config);
        log.info("更新支付配置成功，ID: {}", id);
        return config;
    }
    
    /**
     * 更新状态
     */
    @Transactional
    @CacheEvict(value = "paymentConfig", allEntries = true)
    public void updateStatus(Long id, Integer status) {
        getById(id); // 验证配置是否存在
        paymentConfigMapper.updateStatus(id, status);
        log.info("更新支付配置状态成功，ID: {}, 状态: {}", id, status);
    }
    
    /**
     * 获取AppID（优先从数据库读取，支持回退）
     */
    public String getAppId() {
        PaymentConfig config = getEnabled();
        if (config != null && config.getAppId() != null && !config.getAppId().isEmpty()) {
            return config.getAppId();
        }
        // 回退到配置文件
        return wechatConfig.getAppid();
    }
    
    /**
     * 获取商户号（优先从数据库读取，支持回退）
     */
    public String getMchId() {
        PaymentConfig config = getEnabled();
        if (config != null && config.getMchId() != null && !config.getMchId().isEmpty()) {
            return config.getMchId();
        }
        // 回退到配置文件
        if (wechatConfig.getPay() != null && wechatConfig.getPay().getMchId() != null) {
            return wechatConfig.getPay().getMchId();
        }
        // 回退到环境变量
        return System.getenv("WECHAT_MCH_ID");
    }
    
    /**
     * 获取API密钥（优先从数据库读取，支持回退）
     */
    public String getApiKey() {
        PaymentConfig config = getEnabled();
        if (config != null && config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            // 解密API密钥
            try {
                return EncryptionUtil.decrypt(config.getApiKey());
            } catch (Exception e) {
                log.error("解密API密钥失败", e);
                return null;
            }
        }
        // 回退到配置文件
        if (wechatConfig.getPay() != null && wechatConfig.getPay().getApiKey() != null) {
            return wechatConfig.getPay().getApiKey();
        }
        // 回退到环境变量
        return System.getenv("WECHAT_API_KEY");
    }
    
    /**
     * 获取支付回调地址（优先从数据库读取，支持回退）
     */
    public String getNotifyUrl() {
        PaymentConfig config = getEnabled();
        if (config != null && config.getNotifyUrl() != null && !config.getNotifyUrl().isEmpty()) {
            return config.getNotifyUrl();
        }
        // 回退到配置文件
        if (wechatConfig.getPay() != null && wechatConfig.getPay().getNotifyUrl() != null) {
            return wechatConfig.getPay().getNotifyUrl();
        }
        // 回退到环境变量
        String url = System.getenv("WECHAT_NOTIFY_URL");
        if (url == null || url.isEmpty()) {
            return "http://localhost:8080/api/v1/miniprogram/payment/notify";
        }
        return url;
    }
    
    /**
     * 获取配置用于显示（API密钥部分隐藏）
     */
    public PaymentConfig getForDisplay(Long id) {
        PaymentConfig config = getById(id);
        if (config != null && config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            // 解密后部分隐藏
            try {
                String decrypted = EncryptionUtil.decrypt(config.getApiKey());
                config.setApiKey(EncryptionUtil.maskString(decrypted, 4));
            } catch (Exception e) {
                log.error("解密API密钥失败", e);
                config.setApiKey("****");
            }
        }
        return config;
    }
    
    /**
     * 获取启用的配置用于显示（API密钥部分隐藏）
     */
    public PaymentConfig getEnabledForDisplay() {
        PaymentConfig config = getEnabled();
        if (config != null && config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            // 解密后部分隐藏
            try {
                String decrypted = EncryptionUtil.decrypt(config.getApiKey());
                config.setApiKey(EncryptionUtil.maskString(decrypted, 4));
            } catch (Exception e) {
                log.error("解密API密钥失败", e);
                config.setApiKey("****");
            }
        }
        return config;
    }
}

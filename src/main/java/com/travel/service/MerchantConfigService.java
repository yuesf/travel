package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.MerchantConfigCreateRequest;
import com.travel.dto.MerchantConfigUpdateRequest;
import com.travel.entity.MerchantConfig;
import com.travel.exception.BusinessException;
import com.travel.mapper.MerchantConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商家配置服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class MerchantConfigService {
    
    @Autowired
    private MerchantConfigMapper merchantConfigMapper;
    
    /**
     * 获取商家配置（系统只保留一条记录）
     */
    public MerchantConfig get() {
        return merchantConfigMapper.selectFirst();
    }
    
    /**
     * 创建商家配置
     */
    @Transactional
    public MerchantConfig create(MerchantConfigCreateRequest request) {
        // 检查是否已存在
        MerchantConfig existingConfig = merchantConfigMapper.selectFirst();
        if (existingConfig != null) {
            // 如果已存在，则更新
            return update(existingConfig.getId(), new MerchantConfigUpdateRequest() {{
                setMerchantName(request.getMerchantName());
                setContactPhone(request.getContactPhone());
                setContactEmail(request.getContactEmail());
                setAddress(request.getAddress());
                setDescription(request.getDescription());
            }});
        }
        
        MerchantConfig config = new MerchantConfig();
        BeanUtils.copyProperties(request, config);
        merchantConfigMapper.insert(config);
        log.info("创建商家配置成功，ID: {}", config.getId());
        return config;
    }
    
    /**
     * 更新商家配置
     */
    @Transactional
    public MerchantConfig update(Long id, MerchantConfigUpdateRequest request) {
        MerchantConfig config = merchantConfigMapper.selectFirst();
        if (config == null || (id != null && !config.getId().equals(id))) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        // 更新字段
        if (request.getMerchantName() != null) {
            config.setMerchantName(request.getMerchantName());
        }
        if (request.getContactPhone() != null) {
            config.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            config.setContactEmail(request.getContactEmail());
        }
        if (request.getAddress() != null) {
            config.setAddress(request.getAddress());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        
        merchantConfigMapper.updateById(config);
        log.info("更新商家配置成功，ID: {}", config.getId());
        return config;
    }
    
    /**
     * 更新商家配置（使用现有配置ID）
     */
    @Transactional
    public MerchantConfig update(MerchantConfigUpdateRequest request) {
        MerchantConfig config = merchantConfigMapper.selectFirst();
        if (config == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return update(config.getId(), request);
    }
}

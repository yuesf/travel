package com.travel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.travel.common.ResultCode;
import com.travel.dto.HomeResponse;
import com.travel.dto.MiniProgramConfigCreateRequest;
import com.travel.dto.MiniProgramConfigUpdateRequest;
import com.travel.entity.MiniProgramConfig;
import com.travel.exception.BusinessException;
import com.travel.mapper.MiniProgramConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 小程序配置服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class MiniProgramConfigService {
    
    @Autowired
    private MiniProgramConfigMapper miniProgramConfigMapper;
    
    @Autowired
    @Qualifier("miniprogramConfigCache")
    private Cache<String, MiniProgramConfig> configCache;
    
    @Autowired
    @Qualifier("homeCache")
    private Cache<String, HomeResponse> homeCache;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 根据ID查询配置
     */
    public MiniProgramConfig getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "配置ID不能为空");
        }
        MiniProgramConfig config = miniProgramConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return config;
    }
    
    /**
     * 根据配置键查询配置
     */
    public MiniProgramConfig getByConfigKey(String configKey) {
        if (configKey == null || configKey.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "配置键不能为空");
        }
        
        // 先从缓存中获取
        MiniProgramConfig cachedConfig = configCache.getIfPresent(configKey);
        if (cachedConfig != null) {
            return cachedConfig;
        }
        
        // 从数据库查询
        MiniProgramConfig config = miniProgramConfigMapper.selectByConfigKey(configKey);
        if (config != null && config.getStatus() == 1) {
            // 存入缓存
            configCache.put(configKey, config);
        }
        return config;
    }
    
    /**
     * 根据配置类型查询配置列表
     */
    public List<MiniProgramConfig> getByConfigType(String configType, Integer status) {
        return miniProgramConfigMapper.selectByConfigType(configType, status);
    }
    
    /**
     * 查询所有配置列表
     */
    public List<MiniProgramConfig> getAll(String configType, Integer status) {
        return miniProgramConfigMapper.selectAll(configType, status);
    }
    
    /**
     * 创建配置
     */
    @Transactional
    public MiniProgramConfig create(MiniProgramConfigCreateRequest request) {
        // 验证配置键是否已存在
        MiniProgramConfig existingConfig = miniProgramConfigMapper.selectByConfigKey(request.getConfigKey());
        if (existingConfig != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "配置键已存在");
        }
        
        // 验证JSON格式
        if (request.getConfigValue() != null && !request.getConfigValue().trim().isEmpty()) {
            validateJsonFormat(request.getConfigValue());
        }
        
        // 创建配置对象
        MiniProgramConfig config = new MiniProgramConfig();
        BeanUtils.copyProperties(request, config);
        
        // 设置默认值
        if (config.getStatus() == null) {
            config.setStatus(1); // 默认启用
        }
        if (config.getSort() == null) {
            config.setSort(0);
        }
        
        // 插入数据库
        int result = miniProgramConfigMapper.insert(config);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        // 清除相关缓存
        clearCache(config.getConfigKey());
        
        log.info("创建小程序配置成功，配置键：{}", config.getConfigKey());
        return config;
    }
    
    /**
     * 更新配置
     */
    @Transactional
    public MiniProgramConfig update(Long id, MiniProgramConfigUpdateRequest request) {
        // 查询配置是否存在
        MiniProgramConfig config = getById(id);
        String oldConfigKey = config.getConfigKey();
        
        // 验证JSON格式
        if (request.getConfigValue() != null && !request.getConfigValue().trim().isEmpty()) {
            validateJsonFormat(request.getConfigValue());
        }
        
        // 更新配置
        if (request.getConfigValue() != null) {
            config.setConfigValue(request.getConfigValue());
        }
        if (request.getConfigType() != null) {
            config.setConfigType(request.getConfigType());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getSort() != null) {
            config.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }
        
        // 更新数据库
        int result = miniProgramConfigMapper.updateById(config);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        // 清除相关缓存
        clearCache(oldConfigKey);
        
        log.info("更新小程序配置成功，配置ID：{}", id);
        return config;
    }
    
    /**
     * 根据配置键更新配置
     */
    @Transactional
    public MiniProgramConfig updateByConfigKey(String configKey, MiniProgramConfigUpdateRequest request) {
        MiniProgramConfig config = miniProgramConfigMapper.selectByConfigKey(configKey);
        if (config == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return update(config.getId(), request);
    }
    
    /**
     * 删除配置
     */
    @Transactional
    public void delete(Long id) {
        MiniProgramConfig config = getById(id);
        String configKey = config.getConfigKey();
        
        // 删除数据库记录
        int result = miniProgramConfigMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        // 清除相关缓存
        clearCache(configKey);
        
        log.info("删除小程序配置成功，配置ID：{}", id);
    }
    
    /**
     * 根据配置键删除配置
     */
    @Transactional
    public void deleteByConfigKey(String configKey) {
        MiniProgramConfig config = miniProgramConfigMapper.selectByConfigKey(configKey);
        if (config == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        delete(config.getId());
    }
    
    /**
     * 验证JSON格式
     */
    private void validateJsonFormat(String json) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "配置值必须是有效的JSON格式");
        }
    }
    
    /**
     * 清除缓存
     */
    private void clearCache(String configKey) {
        if (configKey != null) {
            configCache.invalidate(configKey);
            log.debug("已清除小程序配置缓存: {}", configKey);
        }
        // 清除首页数据缓存（修复：使用正确的 homeCache）
        homeCache.invalidateAll();
        log.info("已清除首页数据缓存（因小程序配置变更）");
    }
}

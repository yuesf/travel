package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.PageResult;
import com.travel.dto.SyncConfigCreateRequest;
import com.travel.dto.SyncConfigUpdateRequest;
import com.travel.dto.SyncLogListRequest;
import com.travel.entity.SyncConfig;
import com.travel.entity.SyncLog;
import com.travel.exception.BusinessException;
import com.travel.mapper.SyncConfigMapper;
import com.travel.mapper.SyncLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 同步配置服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class SyncConfigService {
    
    @Autowired
    private SyncConfigMapper syncConfigMapper;
    
    @Autowired
    private SyncLogMapper syncLogMapper;
    
    /**
     * 查询所有同步配置
     */
    public List<SyncConfig> listAll() {
        return syncConfigMapper.selectAll();
    }
    
    /**
     * 根据ID查询同步配置
     */
    public SyncConfig getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        SyncConfig config = syncConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return config;
    }
    
    /**
     * 根据同步类型查询同步配置
     */
    public SyncConfig getBySyncType(String syncType) {
        return syncConfigMapper.selectBySyncType(syncType);
    }
    
    /**
     * 创建同步配置
     */
    @Transactional(rollbackFor = Exception.class)
    public SyncConfig create(SyncConfigCreateRequest request) {
        // 检查是否已存在相同类型的配置
        SyncConfig existing = syncConfigMapper.selectBySyncType(request.getSyncType());
        if (existing != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), 
                "该同步类型已存在配置");
        }
        
        SyncConfig config = new SyncConfig();
        BeanUtils.copyProperties(request, config);
        
        // 设置默认状态
        if (config.getStatus() == null) {
            config.setStatus(1); // 默认启用
        }
        
        int result = syncConfigMapper.insert(config);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建同步配置成功: id={}, syncType={}", config.getId(), config.getSyncType());
        
        return config;
    }
    
    /**
     * 更新同步配置
     */
    @Transactional(rollbackFor = Exception.class)
    public SyncConfig update(Long id, SyncConfigUpdateRequest request) {
        SyncConfig config = getById(id);
        
        if (request.getApiUrl() != null) {
            config.setApiUrl(request.getApiUrl());
        }
        if (request.getApiKey() != null) {
            config.setApiKey(request.getApiKey());
        }
        if (request.getApiSecret() != null) {
            config.setApiSecret(request.getApiSecret());
        }
        if (request.getSyncFrequency() != null) {
            config.setSyncFrequency(request.getSyncFrequency());
        }
        if (request.getSyncTime() != null) {
            config.setSyncTime(request.getSyncTime());
        }
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }
        
        int result = syncConfigMapper.updateById(config);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新同步配置成功: id={}, syncType={}", config.getId(), config.getSyncType());
        
        return config;
    }
    
    /**
     * 删除同步配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SyncConfig config = getById(id);
        
        int result = syncConfigMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除同步配置成功: id={}, syncType={}", config.getId(), config.getSyncType());
    }
    
    /**
     * 根据ID查询同步日志
     */
    public SyncLog getLogById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        SyncLog log = syncLogMapper.selectById(id);
        if (log == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return log;
    }
    
    /**
     * 分页查询同步日志
     */
    public PageResult<SyncLog> listLogs(SyncLogListRequest request) {
        // 参数校验
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(10);
        }
        
        // 计算偏移量
        int offset = (request.getPage() - 1) * request.getPageSize();
        
        // 查询列表
        List<SyncLog> list = syncLogMapper.selectList(
            request.getSyncType(),
            request.getStatus(),
            offset,
            request.getPageSize()
        );
        
        // 查询总数
        long total = syncLogMapper.count(
            request.getSyncType(),
            request.getStatus()
        );
        
        return new PageResult<>(list, total, request.getPage(), request.getPageSize());
    }
}

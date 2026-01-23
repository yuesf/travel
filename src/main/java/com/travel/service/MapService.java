package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.MapCreateRequest;
import com.travel.dto.MapUpdateRequest;
import com.travel.entity.MapLocation;
import com.travel.exception.BusinessException;
import com.travel.mapper.MapMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 地图服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class MapService {
    
    @Autowired
    private MapMapper mapMapper;
    
    /**
     * 查询地图列表（按状态筛选）
     */
    public List<MapLocation> list(Integer status) {
        return mapMapper.selectList(status);
    }
    
    /**
     * 根据ID查询地图详情
     */
    public MapLocation getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        MapLocation mapLocation = mapMapper.selectById(id);
        if (mapLocation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return mapLocation;
    }
    
    /**
     * 创建地图
     */
    @Transactional(rollbackFor = Exception.class)
    public MapLocation create(MapCreateRequest request) {
        // 创建地图实体
        MapLocation mapLocation = new MapLocation();
        BeanUtils.copyProperties(request, mapLocation);
        
        // 设置默认状态
        if (mapLocation.getStatus() == null) {
            mapLocation.setStatus(1); // 默认启用
        }
        
        // 插入数据库
        int result = mapMapper.insert(mapLocation);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建地图成功: id={}, name={}", mapLocation.getId(), mapLocation.getName());
        
        return mapLocation;
    }
    
    /**
     * 更新地图
     */
    @Transactional(rollbackFor = Exception.class)
    public MapLocation update(Long id, MapUpdateRequest request) {
        // 检查地图是否存在
        MapLocation mapLocation = getById(id);
        
        // 更新字段
        if (request.getName() != null) {
            mapLocation.setName(request.getName());
        }
        if (request.getLongitude() != null) {
            mapLocation.setLongitude(request.getLongitude());
        }
        if (request.getLatitude() != null) {
            mapLocation.setLatitude(request.getLatitude());
        }
        if (request.getAddress() != null) {
            mapLocation.setAddress(request.getAddress());
        }
        if (request.getAnnouncement() != null) {
            mapLocation.setAnnouncement(request.getAnnouncement());
        }
        if (request.getStatus() != null) {
            mapLocation.setStatus(request.getStatus());
        }
        
        // 更新数据库
        int result = mapMapper.updateById(mapLocation);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新地图成功: id={}, name={}", mapLocation.getId(), mapLocation.getName());
        
        return mapLocation;
    }
    
    /**
     * 删除地图（物理删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查地图是否存在
        MapLocation mapLocation = getById(id);
        
        // 删除数据库
        int result = mapMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除地图成功: id={}, name={}", mapLocation.getId(), mapLocation.getName());
    }
}

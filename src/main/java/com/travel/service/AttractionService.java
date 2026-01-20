package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.AttractionCreateRequest;
import com.travel.dto.AttractionListRequest;
import com.travel.dto.AttractionUpdateRequest;
import com.travel.dto.PageResult;
import com.travel.entity.Attraction;
import com.travel.exception.BusinessException;
import com.travel.mapper.AttractionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 景点服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class AttractionService {
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    /**
     * 分页查询景点列表
     */
    public PageResult<Attraction> list(AttractionListRequest request) {
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
        List<Attraction> list = attractionMapper.selectList(
            request.getName(),
            request.getCity(),
            request.getStatus(),
            offset,
            request.getPageSize()
        );
        
        // 查询总数
        long total = attractionMapper.count(
            request.getName(),
            request.getCity(),
            request.getStatus()
        );
        
        return new PageResult<>(list, total, request.getPage(), request.getPageSize());
    }
    
    /**
     * 根据ID查询景点详情
     */
    public Attraction getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        Attraction attraction = attractionMapper.selectById(id);
        if (attraction == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return attraction;
    }
    
    /**
     * 创建景点
     */
    @Transactional(rollbackFor = Exception.class)
    public Attraction create(AttractionCreateRequest request) {
        // 创建景点实体
        Attraction attraction = new Attraction();
        BeanUtils.copyProperties(request, attraction);
        
        // 设置默认状态
        if (attraction.getStatus() == null) {
            attraction.setStatus(1); // 默认上架
        }
        
        // 插入数据库
        int result = attractionMapper.insert(attraction);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建景点成功: id={}, name={}", attraction.getId(), attraction.getName());
        
        return attraction;
    }
    
    /**
     * 更新景点
     */
    @Transactional(rollbackFor = Exception.class)
    public Attraction update(Long id, AttractionUpdateRequest request) {
        // 检查景点是否存在
        Attraction attraction = getById(id);
        
        // 更新字段
        if (request.getName() != null) {
            attraction.setName(request.getName());
        }
        if (request.getLocation() != null) {
            attraction.setLocation(request.getLocation());
        }
        if (request.getProvince() != null) {
            attraction.setProvince(request.getProvince());
        }
        if (request.getCity() != null) {
            attraction.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            attraction.setDistrict(request.getDistrict());
        }
        if (request.getAddress() != null) {
            attraction.setAddress(request.getAddress());
        }
        if (request.getDescription() != null) {
            attraction.setDescription(request.getDescription());
        }
        if (request.getImages() != null) {
            attraction.setImages(request.getImages());
        }
        if (request.getVideoUrl() != null) {
            attraction.setVideoUrl(request.getVideoUrl());
        }
        if (request.getOpenTime() != null) {
            attraction.setOpenTime(request.getOpenTime());
        }
        if (request.getContactPhone() != null) {
            attraction.setContactPhone(request.getContactPhone());
        }
        if (request.getLongitude() != null) {
            attraction.setLongitude(request.getLongitude());
        }
        if (request.getLatitude() != null) {
            attraction.setLatitude(request.getLatitude());
        }
        if (request.getTicketPrice() != null) {
            attraction.setTicketPrice(request.getTicketPrice());
        }
        if (request.getTicketStock() != null) {
            attraction.setTicketStock(request.getTicketStock());
        }
        if (request.getValidPeriod() != null) {
            attraction.setValidPeriod(request.getValidPeriod());
        }
        if (request.getStatus() != null) {
            attraction.setStatus(request.getStatus());
        }
        
        // 更新数据库
        int result = attractionMapper.updateById(attraction);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新景点成功: id={}, name={}", attraction.getId(), attraction.getName());
        
        return attraction;
    }
    
    /**
     * 删除景点（软删除，改为下架）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查景点是否存在
        Attraction attraction = getById(id);
        
        // 检查是否有关联订单
        long orderCount = attractionMapper.countOrdersByAttractionId(id);
        if (orderCount > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "该景点存在关联订单，无法删除，只能下架");
        }
        
        // 软删除：将状态改为下架
        AttractionUpdateRequest updateRequest = new AttractionUpdateRequest();
        updateRequest.setStatus(0);
        update(id, updateRequest);
        
        log.info("删除景点成功（软删除）: id={}, name={}", attraction.getId(), attraction.getName());
    }
}

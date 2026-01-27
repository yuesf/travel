package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.AttractionGoldenSummitTimeSlotCreateRequest;
import com.travel.dto.AttractionGoldenSummitTimeSlotUpdateRequest;
import com.travel.entity.Attraction;
import com.travel.entity.AttractionGoldenSummitTimeSlot;
import com.travel.exception.BusinessException;
import com.travel.mapper.AttractionGoldenSummitTimeSlotMapper;
import com.travel.mapper.AttractionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 金顶时间段服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class AttractionGoldenSummitTimeSlotService {
    
    @Autowired
    private AttractionGoldenSummitTimeSlotMapper timeSlotMapper;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    /**
     * 查询时间段列表
     */
    public List<AttractionGoldenSummitTimeSlot> list(Long attractionId, LocalDate bookingDate) {
        return timeSlotMapper.selectList(attractionId, bookingDate);
    }
    
    /**
     * 根据ID查询时间段
     */
    public AttractionGoldenSummitTimeSlot getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        AttractionGoldenSummitTimeSlot timeSlot = timeSlotMapper.selectById(id);
        if (timeSlot == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return timeSlot;
    }
    
    /**
     * 创建时间段
     */
    @Transactional(rollbackFor = Exception.class)
    public AttractionGoldenSummitTimeSlot create(AttractionGoldenSummitTimeSlotCreateRequest request) {
        // 检查景点是否存在
        Attraction attraction = attractionMapper.selectById(request.getAttractionId());
        if (attraction == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "景点不存在");
        }
        
        AttractionGoldenSummitTimeSlot timeSlot = new AttractionGoldenSummitTimeSlot();
        BeanUtils.copyProperties(request, timeSlot);
        
        // 设置默认值
        if (timeSlot.getStock() == null) {
            timeSlot.setStock(0);
        }
        if (timeSlot.getAvailable() == null) {
            timeSlot.setAvailable(1);
        }
        
        int result = timeSlotMapper.insert(timeSlot);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建金顶时间段成功: id={}, attractionId={}", timeSlot.getId(), request.getAttractionId());
        
        return timeSlot;
    }
    
    /**
     * 更新时间段
     */
    @Transactional(rollbackFor = Exception.class)
    public AttractionGoldenSummitTimeSlot update(Long id, AttractionGoldenSummitTimeSlotUpdateRequest request) {
        AttractionGoldenSummitTimeSlot timeSlot = getById(id);
        
        if (request.getStartTime() != null) {
            timeSlot.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            timeSlot.setEndTime(request.getEndTime());
        }
        if (request.getStock() != null) {
            timeSlot.setStock(request.getStock());
        }
        if (request.getAvailable() != null) {
            timeSlot.setAvailable(request.getAvailable());
        }
        
        int result = timeSlotMapper.updateById(timeSlot);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新金顶时间段成功: id={}", id);
        
        return timeSlot;
    }
    
    /**
     * 删除时间段
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);
        
        int result = timeSlotMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除金顶时间段成功: id={}", id);
    }
}

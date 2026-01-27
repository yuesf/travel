package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.AttractionBookingDateBatchRequest;
import com.travel.dto.AttractionBookingDateCreateRequest;
import com.travel.dto.AttractionBookingDateUpdateRequest;
import com.travel.entity.Attraction;
import com.travel.entity.AttractionBookingDate;
import com.travel.exception.BusinessException;
import com.travel.mapper.AttractionBookingDateMapper;
import com.travel.mapper.AttractionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 景点可订日期服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class AttractionBookingDateService {
    
    @Autowired
    private AttractionBookingDateMapper bookingDateMapper;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    /**
     * 查询可订日期列表
     */
    public List<AttractionBookingDate> list(Long attractionId, LocalDate startDate, LocalDate endDate) {
        return bookingDateMapper.selectList(attractionId, startDate, endDate);
    }
    
    /**
     * 根据ID查询可订日期
     */
    public AttractionBookingDate getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        AttractionBookingDate bookingDate = bookingDateMapper.selectById(id);
        if (bookingDate == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return bookingDate;
    }
    
    /**
     * 创建可订日期
     */
    @Transactional(rollbackFor = Exception.class)
    public AttractionBookingDate create(AttractionBookingDateCreateRequest request) {
        // 检查景点是否存在
        Attraction attraction = attractionMapper.selectById(request.getAttractionId());
        if (attraction == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "景点不存在");
        }
        
        // 检查日期是否已存在
        AttractionBookingDate existing = bookingDateMapper.selectByAttractionIdAndDate(
            request.getAttractionId(), request.getBookingDate());
        if (existing != null) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "该日期已存在");
        }
        
        // 检查日期不能早于今天
        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "可订日期不能早于今天");
        }
        
        AttractionBookingDate bookingDate = new AttractionBookingDate();
        BeanUtils.copyProperties(request, bookingDate);
        
        // 设置默认值
        if (bookingDate.getStock() == null) {
            bookingDate.setStock(0);
        }
        if (bookingDate.getAvailable() == null) {
            bookingDate.setAvailable(1);
        }
        
        int result = bookingDateMapper.insert(bookingDate);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建可订日期成功: id={}, attractionId={}, date={}", 
            bookingDate.getId(), request.getAttractionId(), request.getBookingDate());
        
        return bookingDate;
    }
    
    /**
     * 批量设置可订日期
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(AttractionBookingDateBatchRequest request) {
        // 检查景点是否存在
        Attraction attraction = attractionMapper.selectById(request.getAttractionId());
        if (attraction == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "景点不存在");
        }
        
        // 转换为实体列表
        List<AttractionBookingDate> list = request.getDates().stream().map(item -> {
            AttractionBookingDate bookingDate = new AttractionBookingDate();
            bookingDate.setAttractionId(request.getAttractionId());
            bookingDate.setBookingDate(item.getBookingDate());
            bookingDate.setPrice(item.getPrice());
            bookingDate.setStock(item.getStock() != null ? item.getStock() : 0);
            bookingDate.setAvailable(item.getAvailable() != null ? item.getAvailable() : 1);
            return bookingDate;
        }).collect(Collectors.toList());
        
        // 批量插入
        int result = bookingDateMapper.insertBatch(list);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("批量创建可订日期成功: attractionId={}, count={}", request.getAttractionId(), list.size());
    }
    
    /**
     * 更新可订日期
     */
    @Transactional(rollbackFor = Exception.class)
    public AttractionBookingDate update(Long id, AttractionBookingDateUpdateRequest request) {
        AttractionBookingDate bookingDate = getById(id);
        
        if (request.getPrice() != null) {
            bookingDate.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            bookingDate.setStock(request.getStock());
        }
        if (request.getAvailable() != null) {
            bookingDate.setAvailable(request.getAvailable());
        }
        
        int result = bookingDateMapper.updateById(bookingDate);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新可订日期成功: id={}", id);
        
        return bookingDate;
    }
    
    /**
     * 删除可订日期
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);
        
        int result = bookingDateMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除可订日期成功: id={}", id);
    }
}

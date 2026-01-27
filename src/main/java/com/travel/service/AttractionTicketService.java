package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.AttractionTicketCreateRequest;
import com.travel.dto.AttractionTicketUpdateRequest;
import com.travel.entity.Attraction;
import com.travel.entity.AttractionTicket;
import com.travel.entity.AttractionTicketCategory;
import com.travel.exception.BusinessException;
import com.travel.mapper.AttractionMapper;
import com.travel.mapper.AttractionTicketCategoryMapper;
import com.travel.mapper.AttractionTicketMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 具体票种服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class AttractionTicketService {
    
    @Autowired
    private AttractionTicketMapper ticketMapper;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    @Autowired
    private AttractionTicketCategoryMapper categoryMapper;
    
    /**
     * 查询票种列表
     */
    public List<AttractionTicket> list(Long attractionId, Long categoryId) {
        return ticketMapper.selectList(attractionId, categoryId);
    }
    
    /**
     * 根据ID查询票种
     */
    public AttractionTicket getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        AttractionTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return ticket;
    }
    
    /**
     * 创建票种
     */
    @Transactional(rollbackFor = Exception.class)
    public AttractionTicket create(AttractionTicketCreateRequest request) {
        // 检查景点是否存在
        Attraction attraction = attractionMapper.selectById(request.getAttractionId());
        if (attraction == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "景点不存在");
        }
        
        // 检查分类是否存在
        AttractionTicketCategory category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "票种分类不存在");
        }
        
        AttractionTicket ticket = new AttractionTicket();
        BeanUtils.copyProperties(request, ticket);
        
        // 设置默认值
        if (ticket.getSort() == null) {
            ticket.setSort(0);
        }
        if (ticket.getStatus() == null) {
            ticket.setStatus(1);
        }
        
        int result = ticketMapper.insert(ticket);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建票种成功: id={}, name={}", ticket.getId(), ticket.getName());
        
        return ticket;
    }
    
    /**
     * 更新票种
     */
    @Transactional(rollbackFor = Exception.class)
    public AttractionTicket update(Long id, AttractionTicketUpdateRequest request) {
        AttractionTicket ticket = getById(id);
        
        if (request.getName() != null) {
            ticket.setName(request.getName());
        }
        if (request.getPrice() != null) {
            ticket.setPrice(request.getPrice());
        }
        if (request.getIncludedAttractions() != null) {
            ticket.setIncludedAttractions(request.getIncludedAttractions());
        }
        if (request.getVerificationMethod() != null) {
            ticket.setVerificationMethod(request.getVerificationMethod());
        }
        if (request.getRefundRule() != null) {
            ticket.setRefundRule(request.getRefundRule());
        }
        if (request.getBookingNoticeUrl() != null) {
            ticket.setBookingNoticeUrl(request.getBookingNoticeUrl());
        }
        if (request.getSort() != null) {
            ticket.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
        }
        
        int result = ticketMapper.updateById(ticket);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新票种成功: id={}", id);
        
        return ticket;
    }
    
    /**
     * 删除票种
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);
        
        int result = ticketMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除票种成功: id={}", id);
    }
}

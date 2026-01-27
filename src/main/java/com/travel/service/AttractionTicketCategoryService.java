package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.AttractionTicketCategoryCreateRequest;
import com.travel.dto.AttractionTicketCategoryUpdateRequest;
import com.travel.entity.Attraction;
import com.travel.entity.AttractionTicketCategory;
import com.travel.exception.BusinessException;
import com.travel.mapper.AttractionMapper;
import com.travel.mapper.AttractionTicketCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 票种分类服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class AttractionTicketCategoryService {
    
    @Autowired
    private AttractionTicketCategoryMapper categoryMapper;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    /**
     * 查询票种分类列表
     */
    public List<AttractionTicketCategory> list(Long attractionId) {
        return categoryMapper.selectList(attractionId);
    }
    
    /**
     * 根据ID查询票种分类
     */
    public AttractionTicketCategory getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        AttractionTicketCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return category;
    }
    
    /**
     * 创建票种分类
     */
    @Transactional(rollbackFor = Exception.class)
    public AttractionTicketCategory create(AttractionTicketCategoryCreateRequest request) {
        // 检查景点是否存在
        Attraction attraction = attractionMapper.selectById(request.getAttractionId());
        if (attraction == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "景点不存在");
        }
        
        AttractionTicketCategory category = new AttractionTicketCategory();
        BeanUtils.copyProperties(request, category);
        
        // 设置默认值
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        int result = categoryMapper.insert(category);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建票种分类成功: id={}, name={}", category.getId(), category.getName());
        
        return category;
    }
    
    /**
     * 更新票种分类
     */
    @Transactional(rollbackFor = Exception.class)
    public AttractionTicketCategory update(Long id, AttractionTicketCategoryUpdateRequest request) {
        AttractionTicketCategory category = getById(id);
        
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getSort() != null) {
            category.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        
        int result = categoryMapper.updateById(category);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新票种分类成功: id={}", id);
        
        return category;
    }
    
    /**
     * 删除票种分类
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);
        
        int result = categoryMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除票种分类成功: id={}", id);
    }
}

package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.ArticleCategoryCreateRequest;
import com.travel.dto.ArticleCategoryUpdateRequest;
import com.travel.entity.ArticleCategory;
import com.travel.exception.BusinessException;
import com.travel.mapper.ArticleCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文章分类服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class ArticleCategoryService {
    
    @Autowired
    private ArticleCategoryMapper articleCategoryMapper;
    
    /**
     * 查询所有分类列表
     */
    public List<ArticleCategory> list(Integer status) {
        return articleCategoryMapper.selectAll(status);
    }
    
    /**
     * 根据ID查询分类详情
     */
    public ArticleCategory getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        ArticleCategory category = articleCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return category;
    }
    
    /**
     * 创建分类
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleCategory create(ArticleCategoryCreateRequest request) {
        // 创建分类实体
        ArticleCategory category = new ArticleCategory();
        BeanUtils.copyProperties(request, category);
        
        // 设置默认值
        if (category.getStatus() == null) {
            category.setStatus(1); // 默认启用
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        
        // 插入数据库
        int result = articleCategoryMapper.insert(category);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建文章分类成功: id={}, name={}", category.getId(), category.getName());
        
        return category;
    }
    
    /**
     * 更新分类
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleCategory update(Long id, ArticleCategoryUpdateRequest request) {
        // 检查分类是否存在
        ArticleCategory category = getById(id);
        
        // 更新字段
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
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
        
        // 更新数据库
        int result = articleCategoryMapper.updateById(category);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新文章分类成功: id={}, name={}", category.getId(), category.getName());
        
        return category;
    }
    
    /**
     * 删除分类
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查分类是否存在
        ArticleCategory category = getById(id);
        
        // 检查分类下是否有文章
        long articleCount = articleCategoryMapper.countArticlesByCategoryId(id);
        if (articleCount > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                "该分类下存在文章，无法删除，请先处理文章分类");
        }
        
        // 删除分类
        int result = articleCategoryMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除文章分类成功: id={}, name={}", category.getId(), category.getName());
    }
}

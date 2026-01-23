package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.ProductCategoryCreateRequest;
import com.travel.dto.ProductCategoryUpdateRequest;
import com.travel.entity.ProductCategory;
import com.travel.exception.BusinessException;
import com.travel.mapper.ProductCategoryMapper;
import com.travel.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品分类服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class ProductCategoryService {
    
    @Autowired
    private ProductCategoryMapper productCategoryMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    /**
     * 查询所有分类列表
     */
    public List<ProductCategory> list(Integer status, String type) {
        return productCategoryMapper.selectAll(status, type);
    }
    
    /**
     * 查询分类树（一级分类及其子分类）
     */
    public List<ProductCategory> getTree(Integer status, String type) {
        // 查询所有一级分类
        List<ProductCategory> rootCategories = productCategoryMapper.selectTree(status, type);
        
        // 为每个一级分类查询子分类
        for (ProductCategory category : rootCategories) {
            List<ProductCategory> children = productCategoryMapper.selectByParentId(category.getId(), status, type);
            category.setChildren(children);
        }
        
        return rootCategories;
    }
    
    /**
     * 根据ID查询分类详情
     */
    public ProductCategory getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return category;
    }
    
    /**
     * 创建分类
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductCategory create(ProductCategoryCreateRequest request) {
        // 验证父分类是否存在
        if (request.getParentId() != null && request.getParentId() > 0) {
            ProductCategory parentCategory = productCategoryMapper.selectById(request.getParentId());
            if (parentCategory == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "父分类不存在");
            }
            if (parentCategory.getLevel() >= 2) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "最多支持两级分类");
            }
            // 设置层级为二级
            if (request.getLevel() == null) {
                request.setLevel(2);
            }
        } else {
            // 一级分类
            request.setParentId(0L);
            if (request.getLevel() == null) {
                request.setLevel(1);
            }
        }
        
        // 设置默认值
        if (request.getSort() == null) {
            request.setSort(0);
        }
        if (request.getStatus() == null) {
            request.setStatus(1); // 默认启用
        }
        if (request.getType() == null || request.getType().isEmpty()) {
            request.setType("DISPLAY"); // 默认为展示类型
        }
        
        // 创建分类实体
        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(request, category);
        
        // 插入数据库
        int result = productCategoryMapper.insert(category);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建商品分类成功: id={}, name={}", category.getId(), category.getName());
        
        return category;
    }
    
    /**
     * 更新分类
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductCategory update(Long id, ProductCategoryUpdateRequest request) {
        // 检查分类是否存在
        ProductCategory category = getById(id);
        
        // 如果修改父分类，需要验证
        if (request.getParentId() != null && !request.getParentId().equals(category.getParentId())) {
            if (request.getParentId() > 0) {
                ProductCategory parentCategory = productCategoryMapper.selectById(request.getParentId());
                if (parentCategory == null) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "父分类不存在");
                }
                if (parentCategory.getLevel() >= 2) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "最多支持两级分类");
                }
                // 更新层级
                request.setLevel(2);
            } else {
                request.setLevel(1);
            }
        }
        
        // 更新字段
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getParentId() != null) {
            category.setParentId(request.getParentId());
        }
        if (request.getLevel() != null) {
            category.setLevel(request.getLevel());
        }
        if (request.getSort() != null) {
            category.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        if (request.getType() != null) {
            category.setType(request.getType());
        }
        
        // 更新数据库
        int result = productCategoryMapper.updateById(category);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新商品分类成功: id={}, name={}", category.getId(), category.getName());
        
        return category;
    }
    
    /**
     * 删除分类
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查分类是否存在
        ProductCategory category = getById(id);
        
        // 检查是否有子分类
        long childrenCount = productCategoryMapper.countChildrenByParentId(id);
        if (childrenCount > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "该分类下有子分类，无法删除");
        }
        
        // 检查是否有商品使用该分类
        long productCount = productCategoryMapper.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                String.format("该分类下有 %d 个商品，无法直接删除", productCount));
        }
        
        // 删除分类
        int result = productCategoryMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除商品分类成功: id={}, name={}", category.getId(), category.getName());
    }
    
    /**
     * 删除分类及其下的所有商品（级联删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithProducts(Long id) {
        // 检查分类是否存在
        ProductCategory category = getById(id);
        
        // 检查是否有子分类
        long childrenCount = productCategoryMapper.countChildrenByParentId(id);
        if (childrenCount > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "该分类下有子分类，无法删除");
        }
        
        // 查询分类下的所有商品 ID
        List<Long> productIds = productMapper.selectIdsByCategoryId(id);
        
        // 删除分类下的所有商品
        for (Long productId : productIds) {
            productMapper.deleteById(productId);
        }
        
        // 删除分类
        int result = productCategoryMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除分类及商品成功: categoryId={}, categoryName={}, productCount={}", 
            id, category.getName(), productIds.size());
    }
}

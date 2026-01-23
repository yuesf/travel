package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.PageResult;
import com.travel.dto.ProductCreateRequest;
import com.travel.dto.ProductListRequest;
import com.travel.dto.ProductUpdateRequest;
import com.travel.entity.Product;
import com.travel.entity.ProductCategory;
import com.travel.exception.BusinessException;
import com.travel.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class ProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    /**
     * 分页查询商品列表
     */
    public PageResult<Product> list(ProductListRequest request) {
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
        List<Product> list = productMapper.selectList(
            request.getName(),
            request.getCategoryId(),
            request.getMinPrice(),
            request.getMaxPrice(),
            request.getStatus(),
            offset,
            request.getPageSize()
        );
        
        // 查询总数
        long total = productMapper.count(
            request.getName(),
            request.getCategoryId(),
            request.getMinPrice(),
            request.getMaxPrice(),
            request.getStatus()
        );
        
        return new PageResult<>(list, total, request.getPage(), request.getPageSize());
    }
    
    /**
     * 根据ID查询商品详情
     */
    public Product getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return product;
    }
    
    /**
     * 创建商品
     */
    @Transactional(rollbackFor = Exception.class)
    public Product create(ProductCreateRequest request) {
        // 获取分类信息，判断是否为H5类型
        boolean isH5Type = false;
        if (request.getCategoryId() != null) {
            ProductCategory category = productCategoryService.getById(request.getCategoryId());
            if ("H5".equals(category.getType())) {
                isH5Type = true;
                // H5类型分类，h5Link必填
                if (request.getH5Link() == null || request.getH5Link().trim().isEmpty()) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "H5类型商品必须填写H5链接");
                }
                // 验证URL格式
                if (!request.getH5Link().startsWith("http://") && !request.getH5Link().startsWith("https://")) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "H5链接格式不正确，必须以http://或https://开头");
                }
            }
        }
        
        // 非H5类型商品，验证价格和库存必填
        if (!isH5Type) {
            if (request.getPrice() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "价格不能为空");
            }
            if (request.getStock() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "库存不能为空");
            }
        }
        
        // 验证库存不能为负数（如果提供了库存）
        if (request.getStock() != null && request.getStock() < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "库存不能为负数");
        }
        
        // 设置默认值
        if (request.getStatus() == null) {
            request.setStatus(1); // 默认上架
        }
        
        // 创建商品实体
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        // 设置默认销量为0
        product.setSales(0);
        
        // 插入数据库
        int result = productMapper.insert(product);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建商品成功: id={}, name={}", product.getId(), product.getName());
        
        return product;
    }
    
    /**
     * 更新商品
     */
    @Transactional(rollbackFor = Exception.class)
    public Product update(Long id, ProductUpdateRequest request) {
        // 检查商品是否存在
        Product product = getById(id);
        
        // 获取分类ID（可能是新的分类ID或原有的分类ID）
        Long categoryId = request.getCategoryId() != null ? request.getCategoryId() : product.getCategoryId();
        
        // 获取分类信息，判断是否为H5类型
        boolean isH5Type = false;
        if (categoryId != null) {
            ProductCategory category = productCategoryService.getById(categoryId);
            if ("H5".equals(category.getType())) {
                isH5Type = true;
                // H5类型分类，h5Link必填
                String h5Link = request.getH5Link() != null ? request.getH5Link() : product.getH5Link();
                if (h5Link == null || h5Link.trim().isEmpty()) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "H5类型商品必须填写H5链接");
                }
                // 验证URL格式
                if (!h5Link.startsWith("http://") && !h5Link.startsWith("https://")) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "H5链接格式不正确，必须以http://或https://开头");
                }
            }
        }
        
        // 非H5类型商品，如果更新了价格或库存，需要验证
        if (!isH5Type) {
            // 如果更新了价格，验证价格不能为空
            if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "价格不能为负数");
            }
            // 如果更新了库存，验证库存不能为负数
            if (request.getStock() != null && request.getStock() < 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "库存不能为负数");
            }
        } else {
            // H5类型商品，验证库存不能为负数（如果提供了库存）
            if (request.getStock() != null && request.getStock() < 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "库存不能为负数");
            }
        }
        
        // 更新字段
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }
        if (request.getSpecifications() != null) {
            product.setSpecifications(request.getSpecifications());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getH5Link() != null) {
            product.setH5Link(request.getH5Link());
        }
        
        // 更新数据库
        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新商品成功: id={}, name={}", product.getId(), product.getName());
        
        return product;
    }
    
    /**
     * 删除商品（物理删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查商品是否存在
        Product product = getById(id);
        
        // 检查是否有关联订单
        long orderCount = productMapper.countOrdersByProductId(id);
        if (orderCount > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "该商品存在关联订单，无法删除");
        }
        
        // 物理删除
        int result = productMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除商品成功: id={}, name={}", product.getId(), product.getName());
    }
    
    /**
     * 下架商品（仅修改状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long id) {
        // 检查商品是否存在
        Product product = getById(id);
        
        // 更新商品状态为下架
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setStatus(0);
        update(id, updateRequest);
        
        log.info("下架商品成功: id={}, name={}", product.getId(), product.getName());
    }
    
    /**
     * 更新库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStock(Long id, Integer stock) {
        if (stock == null || stock < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "库存不能为负数");
        }
        
        // 检查商品是否存在
        getById(id);
        
        int result = productMapper.updateStock(id, stock);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新商品库存成功: id={}, stock={}", id, stock);
    }
    
    /**
     * 增加销量
     */
    @Transactional(rollbackFor = Exception.class)
    public void increaseSales(Long id, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "销量增量必须大于0");
        }
        
        // 检查商品是否存在
        getById(id);
        
        int result = productMapper.increaseSales(id, quantity);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("增加商品销量成功: id={}, quantity={}", id, quantity);
    }
}

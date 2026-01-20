package com.travel.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.travel.entity.Attraction;
import com.travel.entity.Product;
import com.travel.mapper.AttractionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序详情服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class DetailService {
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    @Autowired
    @Qualifier("attractionDetailCache")
    private Cache<Long, Attraction> attractionDetailCache;
    
    @Autowired
    private ProductService productService;
    
    /**
     * 获取景点详情
     */
    public Attraction getAttractionDetail(Long id) {
        if (id == null) {
            throw new com.travel.exception.BusinessException(com.travel.common.ResultCode.PARAM_ERROR.getCode(), "景点ID不能为空");
        }
        
        // 先从缓存中获取
        Attraction cached = attractionDetailCache.getIfPresent(id);
        if (cached != null) {
            return cached;
        }
        
        // 从数据库查询
        Attraction attraction = attractionMapper.selectById(id);
        if (attraction == null) {
            throw new com.travel.exception.BusinessException(com.travel.common.ResultCode.NOT_FOUND);
        }
        
        // 只返回上架的景点
        if (attraction.getStatus() == null || attraction.getStatus() != 1) {
            throw new com.travel.exception.BusinessException(com.travel.common.ResultCode.NOT_FOUND);
        }
        
        // 存入缓存
        attractionDetailCache.put(id, attraction);
        
        return attraction;
    }
    
    /**
     * 获取酒店详情（包含房型列表）
     */
    public Map<String, Object> getHotelDetail(Long id) {
        // 酒店功能还未实现，返回空结果
        Map<String, Object> result = new HashMap<>();
        result.put("message", "酒店功能暂未实现");
        return result;
    }
    
    /**
     * 获取商品详情
     */
    public Map<String, Object> getProductDetail(Long id) {
        log.info("查询商品详情，商品ID: {}", id);
        
        if (id == null) {
            throw new com.travel.exception.BusinessException(
                com.travel.common.ResultCode.PARAM_ERROR.getCode(), 
                "商品ID不能为空"
            );
        }
        
        // 查询商品详情
        Product product = productService.getById(id);
        
        // 只返回上架的商品
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new com.travel.exception.BusinessException(com.travel.common.ResultCode.NOT_FOUND);
        }
        
        // 转换为Map格式
        Map<String, Object> result = convertProductToMap(product);
        
        log.info("商品详情查询成功，商品名称: {}", product.getName());
        
        return result;
    }
    
    /**
     * 将Product实体转换为Map格式
     */
    private Map<String, Object> convertProductToMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("categoryId", product.getCategoryId());
        map.put("price", product.getPrice());
        map.put("minPrice", product.getPrice()); // 小程序使用minPrice字段
        map.put("originalPrice", product.getOriginalPrice());
        map.put("stock", product.getStock());
        map.put("sales", product.getSales() != null ? product.getSales() : 0);
        map.put("description", product.getDescription());
        
        // 处理图片：取第一张图片作为封面图
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            map.put("image", product.getImages().get(0));
            map.put("coverImage", product.getImages().get(0));
        } else {
            map.put("image", "");
            map.put("coverImage", "");
        }
        
        map.put("images", product.getImages());
        map.put("specifications", product.getSpecifications());
        map.put("status", product.getStatus());
        map.put("createTime", product.getCreateTime());
        map.put("updateTime", product.getUpdateTime());
        map.put("categoryName", product.getCategoryName());
        
        // 小程序需要的其他字段
        map.put("rating", 0); // 评分（商品暂时没有评分）
        map.put("reviewCount", 0); // 评论数（商品暂时没有评论）
        map.put("location", "");
        map.put("address", "");
        map.put("city", "");
        map.put("starLevel", 0);
        map.put("productType", "PRODUCT");
        
        return map;
    }
}

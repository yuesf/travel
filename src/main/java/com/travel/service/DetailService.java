package com.travel.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.travel.entity.Attraction;
import com.travel.entity.Product;
import com.travel.mapper.AttractionMapper;
import com.travel.util.OssUrlUtil;
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
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    @Autowired
    private OssUrlUtil ossUrlUtil;
    
    /**
     * 获取景点详情
     * 注意：返回的图片URL（images字段）都是签名URL（OSS文件），可直接使用
     */
    public Attraction getAttractionDetail(Long id) {
        if (id == null) {
            throw new com.travel.exception.BusinessException(com.travel.common.ResultCode.PARAM_ERROR.getCode(), "景点ID不能为空");
        }
        
        // 先从缓存中获取
        Attraction cached = attractionDetailCache.getIfPresent(id);
        if (cached != null) {
            log.info("从缓存获取景点详情，ID: {}", id);
            // 处理OSS URL签名（缓存中的数据也需要处理，因为签名URL有过期时间）
            processOssUrlsInAttraction(cached);
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
        
        // 处理OSS URL签名
        processOssUrlsInAttraction(attraction);
        
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
     * 注意：返回的图片URL（image、coverImage、images字段）都是签名URL（OSS文件），可直接使用
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
        
        // 处理OSS URL签名
        processOssUrlsInProductMap(result);
        
        log.info("商品详情查询成功，商品名称: {}", product.getName());
        
        return result;
    }
    
    /**
     * 处理景点中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInAttraction(Attraction attraction) {
        if (attraction == null) {
            return;
        }
        // 处理图片列表（images字段是List<String>，需要特殊处理）
        if (attraction.getImages() != null && !attraction.getImages().isEmpty()) {
            java.util.List<String> signedImages = new java.util.ArrayList<>();
            for (String imageUrl : attraction.getImages()) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    signedImages.add(ossUrlUtil.processUrl(imageUrl));
                } else {
                    signedImages.add(imageUrl);
                }
            }
            attraction.setImages(signedImages);
        }
    }
    
    /**
     * 处理商品Map中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInProductMap(Map<String, Object> productMap) {
        if (productMap == null) {
            return;
        }
        // 处理封面图片
        if (productMap.get("image") != null) {
            String imageUrl = (String) productMap.get("image");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                productMap.put("image", ossUrlUtil.processUrl(imageUrl));
            }
        }
        if (productMap.get("coverImage") != null) {
            String coverImageUrl = (String) productMap.get("coverImage");
            if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                productMap.put("coverImage", ossUrlUtil.processUrl(coverImageUrl));
            }
        }
        // 处理图片列表
        if (productMap.get("images") != null) {
            @SuppressWarnings("unchecked")
            java.util.List<String> images = (java.util.List<String>) productMap.get("images");
            if (images != null && !images.isEmpty()) {
                java.util.List<String> signedImages = new java.util.ArrayList<>();
                for (String imageUrl : images) {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        signedImages.add(ossUrlUtil.processUrl(imageUrl));
                    } else {
                        signedImages.add(imageUrl);
                    }
                }
                productMap.put("images", signedImages);
            }
        }
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
        map.put("h5Link", product.getH5Link()); // 外部链接
        
        // 获取分类类型（如果分类ID存在）
        String categoryType = null;
        if (product.getCategoryId() != null) {
            try {
                com.travel.entity.ProductCategory category = productCategoryService.getById(product.getCategoryId());
                if (category != null) {
                    categoryType = category.getType();
                }
            } catch (Exception e) {
                log.warn("获取商品分类类型失败: {}", e.getMessage());
            }
        }
        map.put("categoryType", categoryType); // 分类类型
        
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

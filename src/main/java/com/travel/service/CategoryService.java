package com.travel.service;

import com.travel.dto.CategoryListRequest;
import com.travel.dto.PageResult;
import com.travel.entity.Attraction;
import com.travel.entity.Product;
import com.travel.entity.ProductCategory;
import com.travel.mapper.AttractionMapper;
import com.travel.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小程序分类服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class CategoryService {
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    @Autowired
    private ProductMapper productMapper;
    
    /**
     * 获取分类列表（商品分类）
     * 直接查询 product_category 表中 type='DISPLAY' 的分类
     */
    public List<Map<String, Object>> getCategoryList() {
        // 查询所有启用状态且类型为 DISPLAY 的商品分类
        List<ProductCategory> categories = productCategoryService.list(1, "DISPLAY");
        
        // 转换为 Map 格式返回
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductCategory category : categories) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", category.getId());
            categoryMap.put("name", category.getName());
            categoryMap.put("icon", category.getIcon());
            categoryMap.put("parentId", category.getParentId());
            categoryMap.put("level", category.getLevel());
            categoryMap.put("sort", category.getSort());
            result.add(categoryMap);
        }
        
        return result;
    }
    
    /**
     * 获取景点列表（支持分类筛选）
     */
    public PageResult<Attraction> getAttractionList(CategoryListRequest request) {
        // 参数校验
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(10);
        }
        
        // 计算偏移量
        int offset = (request.getPage() - 1) * request.getPageSize();
        
        // 查询列表（暂时使用AttractionService的逻辑）
        // 注意：这里需要扩展AttractionMapper支持价格筛选和排序
        List<Attraction> list = attractionMapper.selectList(
            null, // name
            request.getCity(),
            1, // status: 只查询上架的
            offset,
            request.getPageSize()
        );
        
        // 查询总数
        long total = attractionMapper.count(
            null,
            request.getCity(),
            1
        );
        
        // 构建分页结果
        PageResult<Attraction> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(request.getPage());
        result.setPageSize(request.getPageSize());
        result.setTotalPages((int) Math.ceil((double) total / request.getPageSize()));
        
        return result;
    }
    
    /**
     * 获取酒店列表（支持分类筛选）
     */
    public PageResult<Map<String, Object>> getHotelList(CategoryListRequest request) {
        // 酒店功能还未实现，返回空结果
        PageResult<Map<String, Object>> result = new PageResult<>();
        result.setList(new ArrayList<>());
        result.setTotal(0L);
        result.setPage(request.getPage() != null ? request.getPage() : 1);
        result.setPageSize(request.getPageSize() != null ? request.getPageSize() : 10);
        result.setTotalPages(0);
        return result;
    }
    
    /**
     * 获取商品列表（支持分类筛选）
     */
    public PageResult<Map<String, Object>> getProductList(CategoryListRequest request) {
        log.info("查询商品列表，参数: categoryId={}, minPrice={}, maxPrice={}, page={}, pageSize={}, sort={}",
                request.getCategoryId(), request.getMinPrice(), request.getMaxPrice(),
                request.getPage(), request.getPageSize(), request.getSortField() + "_" + request.getSortOrder());
        
        // 参数校验
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(10);
        }
        
        // 查询总数
        long total = productMapper.count(
                null, // name
                request.getCategoryId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                1 // status: 只查询上架的商品
        );
        
        // 处理排序
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        
        // 如果指定了排序，需要先查询所有数据，排序后再分页
        // 注意：这里为了简化实现，如果指定了排序就先查询所有数据
        // 如果数据量很大，建议修改ProductMapper的SQL在数据库层面排序
        List<Product> productList;
        if (sortField != null && !sortField.isEmpty() && !"create_time".equals(sortField)) {
            // 如果指定了非默认排序，先查询所有数据
            productList = productMapper.selectList(
                    null, // name
                    request.getCategoryId(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    1, // status: 只查询上架的商品
                    0, // offset: 从0开始
                    Integer.MAX_VALUE // limit: 查询所有
            );
            
            // 排序
            productList = sortProductList(productList, sortField, sortOrder);
            
            // 手动分页
            int offset = (request.getPage() - 1) * request.getPageSize();
            int endIndex = Math.min(offset + request.getPageSize(), productList.size());
            if (offset < productList.size()) {
                productList = productList.subList(offset, endIndex);
            } else {
                productList = new ArrayList<>();
            }
        } else {
            // 默认排序（按创建时间），直接在数据库层面分页
            int offset = (request.getPage() - 1) * request.getPageSize();
            productList = productMapper.selectList(
                    null, // name
                    request.getCategoryId(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    1, // status: 只查询上架的商品
                    offset,
                    request.getPageSize()
            );
        }
        
        // 转换为Map格式
        List<Map<String, Object>> resultList = productList.stream()
                .map(this::convertProductToMap)
                .collect(Collectors.toList());
        
        // 构建分页结果
        PageResult<Map<String, Object>> result = new PageResult<>();
        result.setList(resultList);
        result.setTotal(total);
        result.setPage(request.getPage());
        result.setPageSize(request.getPageSize());
        result.setTotalPages((int) Math.ceil((double) total / request.getPageSize()));
        
        log.info("商品列表查询完成，返回{}条数据，总数{}", resultList.size(), total);
        
        return result;
    }
    
    /**
     * 对商品列表进行排序
     */
    private List<Product> sortProductList(List<Product> list, String sortField, String sortOrder) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        
        Comparator<Product> comparator = null;
        
        switch (sortField.toLowerCase()) {
            case "price":
                comparator = Comparator.comparing(
                        product -> product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO,
                        Comparator.nullsLast(BigDecimal::compareTo)
                );
                break;
            case "sales":
                comparator = Comparator.comparing(
                        product -> product.getSales() != null ? product.getSales() : 0,
                        Comparator.nullsLast(Integer::compareTo)
                );
                break;
            case "create_time":
            default:
                comparator = Comparator.comparing(
                        product -> product.getCreateTime() != null ? product.getCreateTime() : java.time.LocalDateTime.MIN,
                        Comparator.nullsLast(java.time.LocalDateTime::compareTo)
                );
                break;
        }
        
        if (comparator != null) {
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            return list.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }
        
        return list;
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
        map.put("h5Link", product.getH5Link()); // H5链接
        
        // 获取分类类型（如果分类ID存在）
        String categoryType = null;
        if (product.getCategoryId() != null) {
            try {
                ProductCategory category = productCategoryService.getById(product.getCategoryId());
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

package com.travel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.travel.dto.HomeResponse;
import com.travel.dto.PageResult;
import com.travel.dto.SearchRequest;
import com.travel.entity.Attraction;
import com.travel.entity.MiniProgramConfig;
import com.travel.entity.Hotel;
import com.travel.entity.Product;
import com.travel.entity.ProductCategory;
import com.travel.mapper.AttractionMapper;
import com.travel.mapper.MiniProgramConfigMapper;
import com.travel.mapper.ProductMapper;
import com.travel.mapper.ProductCategoryMapper;
import com.travel.util.OssUrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class HomeService {
    
    @Autowired
    private MiniProgramConfigMapper miniProgramConfigMapper;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private com.travel.mapper.HotelMapper hotelMapper;
    
    @Autowired
    private ProductCategoryMapper productCategoryMapper;
    
    @Autowired
    private OssUrlUtil ossUrlUtil;
    
    @Autowired
    @Qualifier("homeCache")
    private Cache<String, HomeResponse> homeCache;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CACHE_KEY_HOME = "miniprogram:home";
    
    /**
     * 获取首页数据
     * 注意：返回的所有URL字段（image、video、icon等）都是签名URL（OSS文件），可直接使用
     */
    public HomeResponse getHomeData() {
        // 先从缓存获取
        HomeResponse cached = homeCache.getIfPresent(CACHE_KEY_HOME);
        if (cached != null) {
            log.info("从缓存获取首页数据 - 轮播图数量: {}", 
                cached.getBanners() != null ? cached.getBanners().size() : 0);
            // 直接返回缓存数据，缓存中已存储签名后的URL（由定时任务定期更新）
            return cached;
        }
        
        log.info("缓存未命中，从数据库查询首页数据");
        
        HomeResponse response = new HomeResponse();
        
        // 获取轮播图配置
        List<MiniProgramConfig> bannerConfigs = miniProgramConfigMapper.selectByConfigType("BANNER", 1);
        log.info("查询到的轮播图配置数量: {}", bannerConfigs.size());
        for (MiniProgramConfig config : bannerConfigs) {
            log.info("轮播图配置 - ID: {}, Key: {}, Type: {}, Status: {}, Value: {}", 
                config.getId(), config.getConfigKey(), config.getConfigType(), 
                config.getStatus(), config.getConfigValue());
        }
        List<HomeResponse.BannerItem> banners = buildBanners(bannerConfigs);
        log.info("构建后的轮播图数量: {}", banners.size());
        // 处理OSS URL签名
        processOssUrlsInBanners(banners);
        response.setBanners(banners);
        
        // 获取Icon图标配置
        List<MiniProgramConfig> iconConfigs = miniProgramConfigMapper.selectByConfigType("ICON", 1);
        List<HomeResponse.IconItem> icons = buildIcons(iconConfigs);
        // 处理OSS URL签名
        processOssUrlsInIcons(icons);
        response.setIcons(icons);
        
        // 获取推荐配置（景点/酒店等）
        List<MiniProgramConfig> recommendConfigs = miniProgramConfigMapper.selectByConfigType("RECOMMEND", 1);
        // 构建推荐景点
        List<HomeResponse.AttractionItem> recommendAttractions = buildRecommendAttractions(recommendConfigs);
        // 处理OSS URL签名
        processOssUrlsInAttractions(recommendAttractions);
        response.setRecommendAttractions(recommendAttractions);
        
        // 构建推荐酒店
        List<HomeResponse.HotelItem> recommendHotels = buildRecommendHotels(recommendConfigs);
        processOssUrlsInHotels(recommendHotels);
        response.setRecommendHotels(recommendHotels);
        
        // 获取推荐商品配置（从推荐商品分类配置中获取）
        MiniProgramConfig recommendProductCategoryConfig = miniProgramConfigMapper.selectByConfigKey("RECOMMEND_PRODUCT_CATEGORY");
        if (recommendProductCategoryConfig != null && recommendProductCategoryConfig.getStatus() == 1) {
            // 构建按分类分组的推荐商品数据
            List<HomeResponse.ProductCategoryWithProducts> categoryProducts = buildRecommendProductCategories(recommendProductCategoryConfig);
            // 处理OSS URL签名
            processOssUrlsInProductCategories(categoryProducts);
            response.setRecommendProductCategories(categoryProducts);
            // 为了兼容性，也设置 recommendProducts（扁平化所有商品）
            List<HomeResponse.ProductItem> allProducts = new ArrayList<>();
            for (HomeResponse.ProductCategoryWithProducts cp : categoryProducts) {
                if (cp.getProducts() != null) {
                    allProducts.addAll(cp.getProducts());
                }
            }
            response.setRecommendProducts(allProducts);
        } else {
            response.setRecommendProductCategories(new ArrayList<>());
            response.setRecommendProducts(new ArrayList<>());
        }
        
        // 获取分类导航配置
        List<MiniProgramConfig> categoryConfigs = miniProgramConfigMapper.selectByConfigType("CATEGORY", 1);
        response.setCategories(buildCategories(categoryConfigs));
        
        // 存入缓存
        homeCache.put(CACHE_KEY_HOME, response);
        
        return response;
    }
    
    /**
     * 搜索接口
     */
    public PageResult<Object> search(SearchRequest request) {
        // 参数校验
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(10);
        }
        
        String keyword = request.getKeyword();
        String type = request.getType();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            PageResult<Object> emptyResult = new PageResult<>();
            emptyResult.setList(new ArrayList<>());
            emptyResult.setTotal(0L);
            emptyResult.setPage(request.getPage());
            emptyResult.setPageSize(request.getPageSize());
            return emptyResult;
        }
        
        List<Object> results = new ArrayList<>();
        long total = 0;
        
        // 计算偏移量
        int offset = (request.getPage() - 1) * request.getPageSize();
        
        if (type == null || "ALL".equals(type) || "ATTRACTION".equals(type)) {
            // 搜索景点
            List<Attraction> attractions = attractionMapper.selectList(keyword, null, 1, offset, request.getPageSize());
            long attractionCount = attractionMapper.count(keyword, null, 1);
            results.addAll(attractions);
            total += attractionCount;
        }
        
        if (type == null || "ALL".equals(type) || "PRODUCT".equals(type)) {
            // 搜索商品（暂时使用空列表，等商品模块实现后补充）
            // List<Product> products = productMapper.search(keyword, offset, request.getPageSize());
            // results.addAll(products);
        }
        
        // 如果搜索全部类型，需要合并结果并重新分页
        if (type == null || "ALL".equals(type)) {
            // 简单实现：返回前pageSize条结果
            if (results.size() > request.getPageSize()) {
                results = results.subList(0, request.getPageSize());
            }
        }
        
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(results);
        pageResult.setTotal(total);
        pageResult.setPage(request.getPage());
        pageResult.setPageSize(request.getPageSize());
        
        return pageResult;
    }
    
    /**
     * 构建轮播图列表
     */
    private List<HomeResponse.BannerItem> buildBanners(List<MiniProgramConfig> configs) {
        List<HomeResponse.BannerItem> banners = new ArrayList<>();
        
        if (configs == null || configs.isEmpty()) {
            log.warn("轮播图配置列表为空");
            return banners;
        }
        
        for (MiniProgramConfig config : configs) {
            try {
                log.debug("处理轮播图配置 - ID: {}, Key: {}, Value: {}", 
                    config.getId(), config.getConfigKey(), config.getConfigValue());
                
                if (config.getConfigValue() == null || config.getConfigValue().trim().isEmpty()) {
                    log.warn("轮播图配置值为空，跳过 - ID: {}, Key: {}", config.getId(), config.getConfigKey());
                    continue;
                }
                
                String configValue = config.getConfigValue().trim();
                log.debug("解析轮播图配置值: {}", configValue);
                
                List<Map<String, Object>> bannerList;
                try {
                    // 先尝试解析为数组
                    bannerList = objectMapper.readValue(
                        configValue, 
                        new TypeReference<List<Map<String, Object>>>() {}
                    );
                } catch (Exception e) {
                    // 如果解析数组失败，尝试解析为单个对象
                    log.debug("解析为数组失败，尝试解析为单个对象");
                    try {
                        Map<String, Object> singleBanner = objectMapper.readValue(
                            configValue,
                            new TypeReference<Map<String, Object>>() {}
                        );
                        // 将单个对象包装成数组
                        bannerList = new ArrayList<>();
                        bannerList.add(singleBanner);
                        log.debug("成功解析为单个对象并包装成数组");
                    } catch (Exception e2) {
                        log.error("无法解析轮播图配置值，既不是数组也不是对象 - configValue: {}", configValue, e2);
                        throw new RuntimeException("轮播图配置格式错误，必须是数组或对象格式", e2);
                    }
                }
                
                log.info("解析到 {} 个轮播图项", bannerList.size());
                
                int index = 0;
                for (Map<String, Object> bannerData : bannerList) {
                    HomeResponse.BannerItem banner = new HomeResponse.BannerItem();
                    // 使用配置ID和索引组合生成唯一ID，避免多个轮播图有相同ID
                    banner.setId(config.getId() * 1000L + index);
                    banner.setType((String) bannerData.getOrDefault("type", "image"));
                    banner.setImage((String) bannerData.get("image"));
                    banner.setVideo((String) bannerData.get("video"));
                    banner.setLink((String) bannerData.get("link")); // 兼容旧版本
                    banner.setTitle((String) bannerData.get("title"));
                    // 新的链接配置字段
                    banner.setLinkType((String) bannerData.get("linkType"));
                    banner.setLinkValue((String) bannerData.get("linkValue"));
                    banner.setLinkDisplay((String) bannerData.get("linkDisplay"));
                    
                    log.debug("构建轮播图项 - ID: {}, Type: {}, Image: {}, Title: {}, LinkType: {}, LinkValue: {}", 
                        banner.getId(), banner.getType(), banner.getImage(), banner.getTitle(), 
                        banner.getLinkType(), banner.getLinkValue());
                    
                    banners.add(banner);
                    index++;
                }
            } catch (Exception e) {
                log.error("解析轮播图配置失败，configKey: {}, configValue: {}", 
                    config.getConfigKey(), config.getConfigValue(), e);
            }
        }
        
        log.info("最终构建的轮播图总数: {}", banners.size());
        return banners;
    }
    
    /**
     * 构建Icon图标列表
     */
    private List<HomeResponse.IconItem> buildIcons(List<MiniProgramConfig> configs) {
        List<HomeResponse.IconItem> icons = new ArrayList<>();
        
        for (MiniProgramConfig config : configs) {
            try {
                if (config.getConfigValue() != null && !config.getConfigValue().trim().isEmpty()) {
                    Map<String, Object> configData = objectMapper.readValue(
                        config.getConfigValue(), 
                        new TypeReference<Map<String, Object>>() {}
                    );
                    
                    HomeResponse.IconItem icon = new HomeResponse.IconItem();
                    icon.setId(config.getId());
                    icon.setType((String) configData.get("type"));
                    if (configData.get("relatedId") != null) {
                        icon.setRelatedId(((Number) configData.get("relatedId")).longValue());
                    }
                    icon.setRelatedName((String) configData.get("relatedName"));
                    icon.setName((String) configData.get("name"));
                    icon.setIcon((String) configData.get("icon"));
                    // 解析 linkUrl 字段（外部链接类型使用）
                    if (configData.get("linkUrl") != null) {
                        icon.setLinkUrl((String) configData.get("linkUrl"));
                    }
                    // 解析 categoryId 字段（文章分类类型使用）
                    if (configData.get("categoryId") != null) {
                        icon.setCategoryId(((Number) configData.get("categoryId")).longValue());
                    }
                    icons.add(icon);
                }
            } catch (Exception e) {
                log.warn("解析Icon图标配置失败，configKey: {}", config.getConfigKey(), e);
            }
        }
        
        return icons;
    }
    
    /**
     * 构建推荐景点列表
     */
    private List<HomeResponse.AttractionItem> buildRecommendAttractions(List<MiniProgramConfig> configs) {
        List<HomeResponse.AttractionItem> attractions = new ArrayList<>();
        
        if (configs == null || configs.isEmpty()) {
            // 无任何 RECOMMEND 配置时直接返回空列表
            return attractions;
        }
        
        for (MiniProgramConfig config : configs) {
            try {
                if (config.getConfigValue() != null && !config.getConfigValue().trim().isEmpty()) {
                    String configKey = config.getConfigKey();
                    if (configKey != null && configKey.contains("ATTRACTION")) {
                        Map<String, Object> configData = objectMapper.readValue(
                            config.getConfigValue(),
                            new TypeReference<Map<String, Object>>() {}
                        );
                        
                        @SuppressWarnings("unchecked")
                        List<Number> attractionIds = (List<Number>) configData.get("ids");
                        
                        if (attractionIds != null && !attractionIds.isEmpty()) {
                            for (Number idNum : attractionIds) {
                                Long id = idNum.longValue();
                                Attraction attraction = attractionMapper.selectById(id);
                                if (attraction != null && attraction.getStatus() == 1) {
                                    HomeResponse.AttractionItem item = new HomeResponse.AttractionItem();
                                    BeanUtils.copyProperties(attraction, item);
                                    if (attraction.getImages() != null && !attraction.getImages().isEmpty()) {
                                        item.setImage(attraction.getImages().get(0));
                                    }
                                    item.setPrice(attraction.getTicketPrice());
                                    attractions.add(item);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析推荐景点配置失败，configKey: {}", config.getConfigKey(), e);
            }
        }
        
        // 无配置或配置为空时返回空列表，由小程序端控制是否展示
        return attractions;
    }
    
    /**
     * 构建推荐酒店列表
     */
    private List<HomeResponse.HotelItem> buildRecommendHotels(List<MiniProgramConfig> configs) {
        List<HomeResponse.HotelItem> hotels = new ArrayList<>();
        
        if (configs == null || configs.isEmpty()) {
            return hotels;
        }
        
        for (MiniProgramConfig config : configs) {
            try {
                if (config.getConfigValue() != null && !config.getConfigValue().trim().isEmpty()) {
                    String configKey = config.getConfigKey();
                    if (configKey != null && configKey.contains("HOTEL")) {
                        Map<String, Object> configData = objectMapper.readValue(
                            config.getConfigValue(),
                            new TypeReference<Map<String, Object>>() {}
                        );
                        
                        @SuppressWarnings("unchecked")
                        List<Number> hotelIds = (List<Number>) configData.get("ids");
                        
                        if (hotelIds != null && !hotelIds.isEmpty()) {
                            for (Number idNum : hotelIds) {
                                Long id = idNum.longValue();
                                Hotel hotel = hotelMapper.selectById(id);
                                if (hotel != null && hotel.getStatus() != null && hotel.getStatus() == 1) {
                                    HomeResponse.HotelItem item = new HomeResponse.HotelItem();
                                    item.setId(hotel.getId());
                                    item.setName(hotel.getName());
                                    if (hotel.getImages() != null && !hotel.getImages().isEmpty()) {
                                        item.setImage(hotel.getImages().get(0));
                                    }
                                    item.setCity(hotel.getCity());
                                    // 暂无独立价格字段时可根据房型/其他逻辑扩展，这里先置为 null
                                    item.setPrice(null);
                                    item.setStarLevel(hotel.getStarLevel());
                                    item.setStatus(hotel.getStatus());
                                    hotels.add(item);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析推荐酒店配置失败，configKey: {}", config.getConfigKey(), e);
            }
        }
        
        return hotels;
    }
    
    /**
     * 构建推荐商品分类列表（按分类分组）
     * 从推荐商品分类配置中获取分类ID，然后查询这些分类下的商品
     */
    private List<HomeResponse.ProductCategoryWithProducts> buildRecommendProductCategories(MiniProgramConfig config) {
        List<HomeResponse.ProductCategoryWithProducts> result = new ArrayList<>();
        
        if (config == null || config.getConfigValue() == null || config.getConfigValue().trim().isEmpty()) {
            log.warn("推荐商品分类配置为空");
            return result;
        }
        
        try {
            String configValue = config.getConfigValue().trim();
            log.debug("解析推荐商品分类配置值: {}", configValue);
            
            // 解析配置值，格式：{"ids": [分类ID1, 分类ID2, ...]}
            Map<String, Object> configData = objectMapper.readValue(
                configValue,
                new TypeReference<Map<String, Object>>() {}
            );
            
            @SuppressWarnings("unchecked")
            List<Number> categoryIds = (List<Number>) configData.get("ids");
            
            if (categoryIds == null || categoryIds.isEmpty()) {
                log.warn("推荐商品分类ID列表为空");
                return result;
            }
            
            log.info("推荐商品分类ID列表: {}", categoryIds);
            
            // 遍历每个分类，查询分类信息和该分类下的商品
            for (Number categoryIdNum : categoryIds) {
                Long categoryId = categoryIdNum.longValue();
                try {
                    // 查询分类信息
                    ProductCategory category = productCategoryMapper.selectById(categoryId);
                    if (category == null || category.getStatus() == null || category.getStatus() != 1) {
                        log.warn("分类 {} 不存在或已禁用", categoryId);
                        continue;
                    }
                    
                    // 查询该分类下的商品（状态为上架，按创建时间倒序）
                    List<Product> categoryProducts = productMapper.selectList(
                        null, // name
                        categoryId, // categoryId
                        null, // categoryType: 指定分类时不需要限制类型
                        null, // minPrice
                        null, // maxPrice
                        1, // status: 1-上架
                        0, // offset
                        10 // limit: 每个分类最多10个商品
                    );
                    
                    log.debug("分类 {} ({}) 下的商品数量: {}", categoryId, category.getName(), categoryProducts.size());
                    
                    // 转换为 ProductItem
                    List<HomeResponse.ProductItem> productItems = new ArrayList<>();
                    for (Product product : categoryProducts) {
                        HomeResponse.ProductItem item = new HomeResponse.ProductItem();
                        item.setId(product.getId());
                        item.setName(product.getName());
                        // 取第一张图片
                        if (product.getImages() != null && !product.getImages().isEmpty()) {
                            item.setImage(product.getImages().get(0));
                        }
                        item.setPrice(product.getPrice());
                        item.setOriginalPrice(product.getOriginalPrice());
                        item.setSales(product.getSales());
                        item.setStatus(product.getStatus());
                        
                        productItems.add(item);
                    }
                    
                    // 创建分类及其商品列表对象
                    HomeResponse.ProductCategoryWithProducts categoryWithProducts = new HomeResponse.ProductCategoryWithProducts();
                    categoryWithProducts.setCategoryId(category.getId());
                    categoryWithProducts.setCategoryName(category.getName());
                    categoryWithProducts.setCategoryIcon(category.getIcon());
                    categoryWithProducts.setProducts(productItems);
                    
                    result.add(categoryWithProducts);
                } catch (Exception e) {
                    log.error("查询分类 {} 下的商品失败", categoryId, e);
                }
            }
            
            log.info("最终构建的推荐商品分类总数: {}", result.size());
        } catch (Exception e) {
            log.error("解析推荐商品分类配置失败，configKey: {}, configValue: {}", 
                config.getConfigKey(), config.getConfigValue(), e);
        }
        
        return result;
    }
    
    /**
     * 构建推荐商品列表（已废弃，保留用于兼容性）
     * 从推荐商品分类配置中获取分类ID，然后查询这些分类下的商品
     */
    private List<HomeResponse.ProductItem> buildRecommendProducts(MiniProgramConfig config) {
        List<HomeResponse.ProductItem> products = new ArrayList<>();
        List<HomeResponse.ProductCategoryWithProducts> categories = buildRecommendProductCategories(config);
        for (HomeResponse.ProductCategoryWithProducts cp : categories) {
            if (cp.getProducts() != null) {
                products.addAll(cp.getProducts());
            }
        }
        return products;
    }
    
    /**
     * 构建分类导航列表
     */
    private List<HomeResponse.CategoryItem> buildCategories(List<MiniProgramConfig> configs) {
        List<HomeResponse.CategoryItem> categories = new ArrayList<>();
        
        for (MiniProgramConfig config : configs) {
            try {
                if (config.getConfigValue() != null && !config.getConfigValue().trim().isEmpty()) {
                    List<Map<String, Object>> categoryList = objectMapper.readValue(
                        config.getConfigValue(), 
                        new TypeReference<List<Map<String, Object>>>() {}
                    );
                    
                    for (Map<String, Object> categoryData : categoryList) {
                        HomeResponse.CategoryItem category = new HomeResponse.CategoryItem();
                        category.setId(((Number) categoryData.get("id")).longValue());
                        category.setName((String) categoryData.get("name"));
                        category.setIcon((String) categoryData.get("icon"));
                        category.setLevel(((Number) categoryData.getOrDefault("level", 1)).intValue());
                        if (categoryData.get("parentId") != null) {
                            category.setParentId(((Number) categoryData.get("parentId")).longValue());
                        }
                        categories.add(category);
                    }
                }
            } catch (Exception e) {
                log.warn("解析分类配置失败，configKey: {}", config.getConfigKey(), e);
            }
        }
        
        return categories;
    }
    
    /**
     * 处理轮播图中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInBanners(List<HomeResponse.BannerItem> banners) {
        if (banners == null || banners.isEmpty()) {
            return;
        }
        log.info("开始处理轮播图OSS URL签名，轮播图数量: {}", banners.size());
        ossUrlUtil.processList(banners, new String[]{"image", "video"});
        log.info("轮播图OSS URL签名处理完成");
    }
    
    /**
     * 处理Icon图标中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInIcons(List<HomeResponse.IconItem> icons) {
        if (icons == null || icons.isEmpty()) {
            return;
        }
        ossUrlUtil.processList(icons, new String[]{"icon"});
    }
    
    /**
     * 处理推荐景点中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInAttractions(List<HomeResponse.AttractionItem> attractions) {
        if (attractions == null || attractions.isEmpty()) {
            return;
        }
        ossUrlUtil.processList(attractions, new String[]{"image"});
    }
    
    /**
     * 处理推荐酒店中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInHotels(List<HomeResponse.HotelItem> hotels) {
        if (hotels == null || hotels.isEmpty()) {
            return;
        }
        ossUrlUtil.processList(hotels, new String[]{"image"});
    }
    
    /**
     * 处理推荐商品分类中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInProductCategories(List<HomeResponse.ProductCategoryWithProducts> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        // 处理分类图标和商品图片（OssUrlUtil会自动递归处理嵌套对象）
        ossUrlUtil.processList(categories, new String[]{"categoryIcon", "image"});
    }
    
    /**
     * 处理整个响应对象中的OSS URL，生成签名URL
     * 用于处理缓存中的数据
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInResponse(HomeResponse response) {
        if (response == null) {
            return;
        }
        // 处理轮播图
        if (response.getBanners() != null) {
            processOssUrlsInBanners(response.getBanners());
        }
        // 处理图标
        if (response.getIcons() != null) {
            processOssUrlsInIcons(response.getIcons());
        }
        // 处理推荐景点
        if (response.getRecommendAttractions() != null) {
            processOssUrlsInAttractions(response.getRecommendAttractions());
        }
        // 处理推荐商品分类
        if (response.getRecommendProductCategories() != null) {
            processOssUrlsInProductCategories(response.getRecommendProductCategories());
        }
        // 处理推荐商品列表（扁平化的商品列表）
        if (response.getRecommendProducts() != null && !response.getRecommendProducts().isEmpty()) {
            ossUrlUtil.processList(response.getRecommendProducts(), new String[]{"image"});
        }
        // 处理分类导航
        if (response.getCategories() != null && !response.getCategories().isEmpty()) {
            ossUrlUtil.processList(response.getCategories(), new String[]{"icon"});
        }
    }
}

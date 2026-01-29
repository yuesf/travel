package com.travel.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.travel.entity.Attraction;
import com.travel.entity.AttractionTicket;
import com.travel.entity.AttractionTicketCategory;
import com.travel.entity.Hotel;
import com.travel.entity.HotelRoom;
import com.travel.entity.Product;
import com.travel.mapper.AttractionMapper;
import com.travel.mapper.AttractionTicketCategoryMapper;
import com.travel.mapper.AttractionTicketMapper;
import com.travel.mapper.HotelRoomMapper;
import com.travel.util.OssUrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private AttractionTicketCategoryMapper attractionTicketCategoryMapper;

    @Autowired
    private AttractionTicketMapper attractionTicketMapper;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductCategoryService productCategoryService;
    
    @Autowired
    private OssUrlUtil ossUrlUtil;
    
    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private HotelRoomMapper hotelRoomMapper;
    
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

        // 加载票种分类和票种列表，用于小程序景点详情页
        try {
            List<AttractionTicketCategory> categories = attractionTicketCategoryMapper.selectList(id);
            List<AttractionTicket> tickets = attractionTicketMapper.selectList(id, null);

            if (categories != null && !categories.isEmpty()) {
                // 仅保留启用状态的分类
                List<AttractionTicketCategory> activeCategories = new ArrayList<>();

                Map<Long, List<AttractionTicket>> ticketsByCategory = new HashMap<>();
                if (tickets != null && !tickets.isEmpty()) {
                    for (AttractionTicket ticket : tickets) {
                        if (ticket.getStatus() == null || ticket.getStatus() != 1) {
                            // 仅暴露启用状态的票种
                            continue;
                        }
                        Long categoryId = ticket.getCategoryId();
                        if (categoryId == null) {
                            continue;
                        }
                        ticketsByCategory
                            .computeIfAbsent(categoryId, k -> new ArrayList<>())
                            .add(ticket);
                    }
                }

                List<AttractionTicket> flatTickets = new ArrayList<>();

                for (AttractionTicketCategory category : categories) {
                    if (category == null || category.getStatus() == null || category.getStatus() != 1) {
                        continue;
                    }
                    List<AttractionTicket> categoryTickets = ticketsByCategory.getOrDefault(
                        category.getId(),
                        new ArrayList<>()
                    );
                    // 分类下至少有一个票种才返回，避免前端出现空分类
                    if (!categoryTickets.isEmpty()) {
                        category.setTickets(categoryTickets);
                        activeCategories.add(category);
                        flatTickets.addAll(categoryTickets);
                    }
                }

                attraction.setTicketCategories(activeCategories);
                attraction.setTickets(flatTickets);
            }
        } catch (Exception e) {
            // 为避免影响主流程，这里仅记录日志，不抛出异常
            log.warn("加载景点票务信息失败，attractionId={}，error={}", id, e.getMessage());
        }
        
        // 存入缓存
        attractionDetailCache.put(id, attraction);
        
        return attraction;
    }
    
    /**
     * 获取酒店详情（包含房型列表）
     * 注意：返回的图片URL（images字段）都是签名URL（OSS文件），可直接使用
     */
    public Map<String, Object> getHotelDetail(Long id) {
        log.info("查询酒店详情，酒店ID: {}", id);
        
        if (id == null) {
            throw new com.travel.exception.BusinessException(
                com.travel.common.ResultCode.PARAM_ERROR.getCode(), 
                "酒店ID不能为空"
            );
        }
        
        // 查询酒店详情
        Hotel hotel = hotelService.getById(id);
        
        // 只返回上架的酒店
        if (hotel.getStatus() == null || hotel.getStatus() != 1) {
            throw new com.travel.exception.BusinessException(com.travel.common.ResultCode.NOT_FOUND);
        }
        
        // 转换为Map格式
        Map<String, Object> result = convertHotelToMap(hotel);
        
        // 查询房型列表（只返回上架的房型）
        List<HotelRoom> allRooms = hotelRoomMapper.selectByHotelId(id);
        List<Map<String, Object>> rooms = new ArrayList<>();
        BigDecimal minPrice = null;
        
        if (allRooms != null && !allRooms.isEmpty()) {
            // 过滤出上架的房型
            List<HotelRoom> activeRooms = allRooms.stream()
                .filter(room -> room.getStatus() != null && room.getStatus() == 1)
                .collect(Collectors.toList());
            
            // 转换为Map格式并处理OSS URL
            for (HotelRoom room : activeRooms) {
                Map<String, Object> roomMap = convertHotelRoomToMap(room);
                rooms.add(roomMap);
                
                // 计算最低价格
                if (room.getPrice() != null) {
                    if (minPrice == null || room.getPrice().compareTo(minPrice) < 0) {
                        minPrice = room.getPrice();
                    }
                }
            }
        }
        
        // 设置房型列表和最低价格
        result.put("rooms", rooms);
        result.put("price", minPrice != null ? minPrice : BigDecimal.ZERO);
        result.put("minPrice", minPrice != null ? minPrice : BigDecimal.ZERO);
        
        // 处理OSS URL签名
        processOssUrlsInHotelMap(result);
        
        log.info("酒店详情查询成功，酒店名称: {}", hotel.getName());
        
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
     * 处理酒店Map中的OSS URL，生成签名URL
     * 使用OssUrlUtil统一处理，返回的URL都是签名URL
     */
    private void processOssUrlsInHotelMap(Map<String, Object> hotelMap) {
        if (hotelMap == null) {
            return;
        }
        // 处理图片列表
        if (hotelMap.get("images") != null) {
            @SuppressWarnings("unchecked")
            java.util.List<String> images = (java.util.List<String>) hotelMap.get("images");
            if (images != null && !images.isEmpty()) {
                java.util.List<String> signedImages = new java.util.ArrayList<>();
                for (String imageUrl : images) {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        signedImages.add(ossUrlUtil.processUrl(imageUrl));
                    } else {
                        signedImages.add(imageUrl);
                    }
                }
                hotelMap.put("images", signedImages);
            }
        }
        // 处理封面图片
        if (hotelMap.get("image") != null) {
            String imageUrl = (String) hotelMap.get("image");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                hotelMap.put("image", ossUrlUtil.processUrl(imageUrl));
            }
        }
        if (hotelMap.get("coverImage") != null) {
            String coverImageUrl = (String) hotelMap.get("coverImage");
            if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                hotelMap.put("coverImage", ossUrlUtil.processUrl(coverImageUrl));
            }
        }
        // 处理房型中的图片
        if (hotelMap.get("rooms") != null) {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> rooms = (java.util.List<Map<String, Object>>) hotelMap.get("rooms");
            if (rooms != null) {
                for (Map<String, Object> room : rooms) {
                    if (room.get("images") != null) {
                        @SuppressWarnings("unchecked")
                        java.util.List<String> roomImages = (java.util.List<String>) room.get("images");
                        if (roomImages != null && !roomImages.isEmpty()) {
                            java.util.List<String> signedRoomImages = new java.util.ArrayList<>();
                            for (String imageUrl : roomImages) {
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    signedRoomImages.add(ossUrlUtil.processUrl(imageUrl));
                                } else {
                                    signedRoomImages.add(imageUrl);
                                }
                            }
                            room.put("images", signedRoomImages);
                        }
                    }
                }
            }
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
    
    /**
     * 将Hotel实体转换为Map格式
     */
    private Map<String, Object> convertHotelToMap(Hotel hotel) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", hotel.getId());
        map.put("name", hotel.getName());
        map.put("city", hotel.getCity());
        map.put("province", hotel.getProvince());
        map.put("district", hotel.getDistrict());
        map.put("address", hotel.getAddress());
        map.put("starLevel", hotel.getStarLevel());
        map.put("description", hotel.getDescription());
        map.put("facilities", hotel.getFacilities());
        map.put("contactPhone", hotel.getContactPhone());
        map.put("longitude", hotel.getLongitude());
        map.put("latitude", hotel.getLatitude());
        map.put("status", hotel.getStatus());
        map.put("createTime", hotel.getCreateTime());
        map.put("updateTime", hotel.getUpdateTime());
        
        // 处理图片：取第一张图片作为封面图
        if (hotel.getImages() != null && !hotel.getImages().isEmpty()) {
            map.put("image", hotel.getImages().get(0));
            map.put("coverImage", hotel.getImages().get(0));
        } else {
            map.put("image", "");
            map.put("coverImage", "");
        }
        
        map.put("images", hotel.getImages());
        
        // 小程序需要的其他字段
        map.put("rating", 0); // 评分（酒店暂时没有评分）
        map.put("reviewCount", 0); // 评论数（酒店暂时没有评论）
        map.put("location", hotel.getAddress()); // 位置信息
        map.put("productType", "HOTEL");
        
        return map;
    }
    
    /**
     * 将HotelRoom实体转换为Map格式
     */
    private Map<String, Object> convertHotelRoomToMap(HotelRoom room) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", room.getId());
        map.put("hotelId", room.getHotelId());
        map.put("name", room.getRoomType()); // 使用roomType作为name
        map.put("roomType", room.getRoomType());
        map.put("price", room.getPrice());
        map.put("stock", room.getStock());
        map.put("bedType", room.getBedType());
        map.put("area", room.getArea());
        map.put("facilities", room.getFacilities());
        map.put("images", room.getImages());
        map.put("status", room.getStatus());
        map.put("createTime", room.getCreateTime());
        map.put("updateTime", room.getUpdateTime());
        
        // 构建描述信息
        List<String> descParts = new ArrayList<>();
        if (room.getBedType() != null && !room.getBedType().isEmpty()) {
            descParts.add(room.getBedType());
        }
        if (room.getArea() != null) {
            descParts.add(room.getArea() + "㎡");
        }
        String description = String.join(" | ", descParts);
        map.put("description", description);
        
        // 构建特性列表（features）
        List<String> features = new ArrayList<>();
        if (room.getBedType() != null && !room.getBedType().isEmpty()) {
            features.add(room.getBedType());
        }
        if (room.getArea() != null) {
            features.add(room.getArea() + "㎡");
        }
        if (room.getFacilities() != null && !room.getFacilities().isEmpty()) {
            features.addAll(room.getFacilities());
        }
        map.put("features", features);
        
        return map;
    }
}

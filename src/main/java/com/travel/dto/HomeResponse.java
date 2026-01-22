package com.travel.dto;

import lombok.Data;
import java.util.List;

/**
 * 首页数据响应DTO
 * 
 * @author travel-platform
 */
@Data
public class HomeResponse {
    
    /**
     * 轮播图列表
     */
    private List<BannerItem> banners;
    
    /**
     * Icon图标列表
     */
    private List<IconItem> icons;
    
    /**
     * 推荐景点列表
     */
    private List<AttractionItem> recommendAttractions;
    
    /**
     * 推荐酒店列表
     */
    private List<HotelItem> recommendHotels;
    
    /**
     * 推荐商品列表（已废弃，使用 recommendProductCategories）
     */
    private List<ProductItem> recommendProducts;
    
    /**
     * 推荐商品分类列表（包含分类信息和该分类下的商品）
     */
    private List<ProductCategoryWithProducts> recommendProductCategories;
    
    /**
     * 分类导航列表
     */
    private List<CategoryItem> categories;
    
    /**
     * 轮播图项
     */
    @Data
    public static class BannerItem {
        private Long id;
        /**
         * 类型：image-图片，video-视频
         */
        private String type;
        /**
         * 图片URL（类型为image时使用）
         */
        private String image;
        /**
         * 视频URL（类型为video时使用）
         */
        private String video;
        private String link;
        private String title;
    }
    
    /**
     * 景点项
     */
    @Data
    public static class AttractionItem {
        private Long id;
        private String name;
        private String image;
        private String city;
        private java.math.BigDecimal price;
        private Integer status;
    }
    
    /**
     * 酒店项
     */
    @Data
    public static class HotelItem {
        private Long id;
        private String name;
        private String image;
        private String city;
        private java.math.BigDecimal price;
        private Integer starLevel;
        private Integer status;
    }
    
    /**
     * 商品项
     */
    @Data
    public static class ProductItem {
        private Long id;
        private String name;
        private String image;
        private java.math.BigDecimal price;
        private java.math.BigDecimal originalPrice;
        private Integer sales;
        private Integer status;
    }
    
    /**
     * 分类项
     */
    @Data
    public static class CategoryItem {
        private Long id;
        private String name;
        private String icon;
        private Integer level;
        private Long parentId;
    }
    
    /**
     * Icon图标项
     */
    @Data
    public static class IconItem {
        private Long id;
        /**
         * 类型：product_category-商品分类，article_category-文章分类，product-商品，article-文章，attraction-景点，hotel-酒店，h5_link-H5链接
         */
        private String type;
        /**
         * 关联对象ID
         */
        private Long relatedId;
        /**
         * 关联对象名称
         */
        private String relatedName;
        /**
         * 图标名称
         */
        private String name;
        /**
         * 图标图片URL
         */
        private String icon;
        /**
         * H5链接地址（仅h5_link类型使用）
         */
        private String linkUrl;
        /**
         * 文章分类ID（仅article_category类型使用，用于判断relatedId是分类ID还是文章ID）
         */
        private Long categoryId;
    }
    
    /**
     * 商品分类及其商品列表
     */
    @Data
    public static class ProductCategoryWithProducts {
        /**
         * 分类ID
         */
        private Long categoryId;
        /**
         * 分类名称
         */
        private String categoryName;
        /**
         * 分类图标
         */
        private String categoryIcon;
        /**
         * 该分类下的商品列表
         */
        private List<ProductItem> products;
    }
}

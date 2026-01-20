package com.travel.dto;

import com.travel.entity.Attraction;
import com.travel.entity.Cart;
import com.travel.entity.HotelRoom;
import com.travel.entity.Product;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车响应DTO（包含商品详情）
 * 
 * @author travel-platform
 */
@Data
public class CartResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 购物车ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 商品类型：ATTRACTION-景点，HOTEL_ROOM-酒店房型，PRODUCT-商品
     */
    private String itemType;
    
    /**
     * 商品ID
     */
    private Long itemId;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;
    
    /**
     * 商品详情（根据itemType不同，可能是Attraction、HotelRoom或Product）
     */
    private Object itemDetail;
    
    /**
     * 商品名称
     */
    private String itemName;
    
    /**
     * 商品价格
     */
    private BigDecimal itemPrice;
    
    /**
     * 商品图片（第一张）
     */
    private String itemImage;
    
    /**
     * 商品库存
     */
    private Integer itemStock;
    
    /**
     * 商品状态
     */
    private Integer itemStatus;
    
    /**
     * 从Cart实体和商品详情构建CartResponse
     */
    public static CartResponse fromCart(Cart cart, Object itemDetail) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUserId());
        response.setItemType(cart.getItemType());
        response.setItemId(cart.getItemId());
        response.setQuantity(cart.getQuantity());
        response.setCreateTime(cart.getCreateTime());
        response.setUpdateTime(cart.getUpdateTime());
        response.setItemDetail(itemDetail);
        
        // 根据商品类型提取信息
        if (itemDetail instanceof Attraction) {
            Attraction attraction = (Attraction) itemDetail;
            response.setItemName(attraction.getName());
            response.setItemPrice(attraction.getTicketPrice());
            response.setItemStock(attraction.getTicketStock());
            response.setItemStatus(attraction.getStatus());
            if (attraction.getImages() != null && !attraction.getImages().isEmpty()) {
                response.setItemImage(attraction.getImages().get(0));
            }
        } else if (itemDetail instanceof HotelRoom) {
            HotelRoom room = (HotelRoom) itemDetail;
            response.setItemName(room.getRoomType());
            response.setItemPrice(room.getPrice());
            response.setItemStock(room.getStock());
            response.setItemStatus(room.getStatus());
            if (room.getImages() != null && !room.getImages().isEmpty()) {
                response.setItemImage(room.getImages().get(0));
            }
        } else if (itemDetail instanceof Product) {
            Product product = (Product) itemDetail;
            response.setItemName(product.getName());
            response.setItemPrice(product.getPrice());
            response.setItemStock(product.getStock());
            response.setItemStatus(product.getStatus());
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                response.setItemImage(product.getImages().get(0));
            }
        }
        
        return response;
    }
}

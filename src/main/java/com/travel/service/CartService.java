package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.CartAddRequest;
import com.travel.dto.CartResponse;
import com.travel.dto.CartUpdateRequest;
import com.travel.entity.Attraction;
import com.travel.entity.Cart;
import com.travel.entity.HotelRoom;
import com.travel.entity.Product;
import com.travel.exception.BusinessException;
import com.travel.mapper.AttractionMapper;
import com.travel.mapper.CartMapper;
import com.travel.mapper.HotelRoomMapper;
import com.travel.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class CartService {
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    @Autowired
    private HotelRoomMapper hotelRoomMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    /**
     * 获取购物车列表（包含商品详情）
     */
    public List<CartResponse> getCartList(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        // 从数据库查询购物车列表
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        
        // 构建包含商品详情的响应列表
        List<CartResponse> responseList = new ArrayList<>();
        for (Cart cart : cartList) {
            Object itemDetail = getItemDetail(cart.getItemType(), cart.getItemId());
            CartResponse response = CartResponse.fromCart(cart, itemDetail);
            responseList.add(response);
        }
        
        return responseList;
    }
    
    /**
     * 根据商品类型和ID获取商品详情
     */
    private Object getItemDetail(String itemType, Long itemId) {
        switch (itemType) {
            case "ATTRACTION":
                return attractionMapper.selectById(itemId);
            case "HOTEL_ROOM":
                return hotelRoomMapper.selectById(itemId);
            case "PRODUCT":
                return productMapper.selectById(itemId);
            default:
                return null;
        }
    }
    
    /**
     * 添加商品到购物车
     */
    @Transactional
    public Cart addToCart(Long userId, CartAddRequest request) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        // 验证库存
        validateStock(request.getItemType(), request.getItemId(), request.getQuantity());
        
        // 检查购物车中是否已存在该商品
        Cart existingCart = cartMapper.selectByUserAndItem(
            userId,
            request.getItemType(),
            request.getItemId()
        );
        
        if (existingCart != null) {
            // 如果已存在，更新数量
            int newQuantity = existingCart.getQuantity() + request.getQuantity();
            validateStock(request.getItemType(), request.getItemId(), newQuantity);
            existingCart.setQuantity(newQuantity);
            cartMapper.updateById(existingCart);
            return existingCart;
        } else {
            // 如果不存在，创建新记录
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setItemType(request.getItemType());
            cart.setItemId(request.getItemId());
            cart.setQuantity(request.getQuantity());
            
            int result = cartMapper.insert(cart);
            if (result <= 0) {
                throw new BusinessException(ResultCode.OPERATION_FAILED);
            }
            
            return cart;
        }
    }
    
    /**
     * 更新购物车商品数量
     */
    @Transactional
    public Cart updateCart(Long userId, Long cartId, CartUpdateRequest request) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        // 查询购物车
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        // 验证是否是当前用户的购物车
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        // 验证库存
        validateStock(cart.getItemType(), cart.getItemId(), request.getQuantity());
        
        // 更新数量
        cart.setQuantity(request.getQuantity());
        int result = cartMapper.updateById(cart);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        return cart;
    }
    
    /**
     * 删除购物车商品
     */
    @Transactional
    public void deleteCart(Long userId, Long cartId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        // 查询购物车
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        
        // 验证是否是当前用户的购物车
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        
        // 删除
        int result = cartMapper.deleteById(cartId);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
    }
    
    /**
     * 验证库存
     */
    private void validateStock(String itemType, Long itemId, Integer quantity) {
        if ("ATTRACTION".equals(itemType)) {
            // 验证景点库存
            Attraction attraction = attractionMapper.selectById(itemId);
            if (attraction == null) {
                throw new BusinessException(ResultCode.NOT_FOUND);
            }
            if (attraction.getStatus() == null || attraction.getStatus() != 1) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "景点已下架");
            }
            if (attraction.getTicketStock() != null && attraction.getTicketStock() < quantity) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "库存不足");
            }
        } else if ("HOTEL_ROOM".equals(itemType)) {
            // 验证酒店房型库存
            HotelRoom room = hotelRoomMapper.selectById(itemId);
            if (room == null) {
                throw new BusinessException(ResultCode.NOT_FOUND);
            }
            if (room.getStatus() == null || room.getStatus() != 1) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "房型已下架");
            }
            if (room.getStock() != null && room.getStock() < quantity) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "库存不足");
            }
        } else if ("PRODUCT".equals(itemType)) {
            // 验证商品库存
            Product product = productMapper.selectById(itemId);
            if (product == null) {
                throw new BusinessException(ResultCode.NOT_FOUND);
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "商品已下架");
            }
            if (product.getStock() != null && product.getStock() < quantity) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "库存不足");
            }
        }
    }
}

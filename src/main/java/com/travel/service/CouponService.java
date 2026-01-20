package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.PageResult;
import com.travel.entity.UserCoupon;
import com.travel.exception.BusinessException;
import com.travel.mapper.UserCouponMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class CouponService {
    
    @Autowired
    private UserCouponMapper userCouponMapper;
    
    /**
     * 获取用户优惠券列表（按状态分类）
     */
    public PageResult<UserCoupon> getUserCoupons(Long userId, Integer status, Integer page, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询列表
        List<UserCoupon> list = userCouponMapper.selectByUserId(userId, status, offset, pageSize);
        
        // 更新过期状态（检查并更新已过期的优惠券）
        updateExpiredCoupons(list);
        
        // 查询总数
        long total = userCouponMapper.countByUserId(userId, status);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 更新过期优惠券状态
     */
    private void updateExpiredCoupons(List<UserCoupon> list) {
        LocalDateTime now = LocalDateTime.now();
        for (UserCoupon userCoupon : list) {
            if (userCoupon.getCoupon() != null && userCoupon.getStatus() == 0) {
                // 未使用的优惠券，检查是否过期
                if (userCoupon.getCoupon().getValidEndTime() != null 
                    && userCoupon.getCoupon().getValidEndTime().isBefore(now)) {
                    // 更新为已过期
                    userCoupon.setStatus(2);
                    userCouponMapper.updateStatus(userCoupon.getId(), 2);
                }
            }
        }
    }
}

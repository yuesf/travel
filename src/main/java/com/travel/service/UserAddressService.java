package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.entity.UserAddress;
import com.travel.exception.BusinessException;
import com.travel.mapper.UserAddressMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户收货地址服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class UserAddressService {
    
    @Autowired
    private UserAddressMapper userAddressMapper;
    
    /**
     * 获取用户收货地址列表（默认地址排在第一位）
     */
    public List<UserAddress> getAddressList(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        return userAddressMapper.selectByUserId(userId);
    }
    
    /**
     * 根据ID获取收货地址详情
     */
    public UserAddress getAddressById(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "地址ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        UserAddress address = userAddressMapper.selectByIdAndUserId(id, userId);
        if (address == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "收货地址不存在");
        }
        return address;
    }
    
    /**
     * 创建收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAddress createAddress(Long userId, UserAddress address) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        // 验证必填字段
        validateAddress(address);
        
        // 设置用户ID
        address.setUserId(userId);
        
        // 如果设置为默认地址，先取消其他地址的默认状态
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            userAddressMapper.cancelAllDefault(userId);
        } else {
            address.setIsDefault(0);
        }
        
        // 插入地址
        userAddressMapper.insert(address);
        log.info("创建收货地址成功: userId={}, addressId={}", userId, address.getId());
        
        return address;
    }
    
    /**
     * 更新收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAddress updateAddress(Long userId, Long id, UserAddress address) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "地址ID不能为空");
        }
        
        // 验证地址是否存在且属于该用户
        UserAddress existingAddress = userAddressMapper.selectByIdAndUserId(id, userId);
        if (existingAddress == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "收货地址不存在");
        }
        
        // 验证必填字段
        validateAddress(address);
        
        // 设置ID和用户ID
        address.setId(id);
        address.setUserId(userId);
        
        // 如果设置为默认地址，先取消其他地址的默认状态
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            userAddressMapper.cancelAllDefault(userId);
        }
        
        // 更新地址
        userAddressMapper.updateById(address);
        log.info("更新收货地址成功: userId={}, addressId={}", userId, id);
        
        return address;
    }
    
    /**
     * 删除收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long userId, Long id) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "地址ID不能为空");
        }
        
        // 验证地址是否存在且属于该用户
        UserAddress address = userAddressMapper.selectByIdAndUserId(id, userId);
        if (address == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "收货地址不存在");
        }
        
        // 删除地址
        userAddressMapper.deleteById(id);
        log.info("删除收货地址成功: userId={}, addressId={}", userId, id);
    }
    
    /**
     * 设置默认收货地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long id) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "地址ID不能为空");
        }
        
        // 验证地址是否存在且属于该用户
        UserAddress address = userAddressMapper.selectByIdAndUserId(id, userId);
        if (address == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "收货地址不存在");
        }
        
        // 先取消所有地址的默认状态
        userAddressMapper.cancelAllDefault(userId);
        
        // 设置指定地址为默认地址
        userAddressMapper.setDefault(id, userId);
        log.info("设置默认收货地址成功: userId={}, addressId={}", userId, id);
    }
    
    /**
     * 验证收货地址必填字段
     */
    private void validateAddress(UserAddress address) {
        if (!StringUtils.hasText(address.getReceiverName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "收货人姓名不能为空");
        }
        if (!StringUtils.hasText(address.getReceiverPhone())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "收货人手机号不能为空");
        }
        // 验证手机号格式（11位数字，1开头）
        String phone = address.getReceiverPhone();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "手机号格式不正确");
        }
        if (!StringUtils.hasText(address.getProvince())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "省份不能为空");
        }
        if (!StringUtils.hasText(address.getCity())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "城市不能为空");
        }
        if (!StringUtils.hasText(address.getDistrict())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "区县不能为空");
        }
        if (!StringUtils.hasText(address.getDetailAddress())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "详细地址不能为空");
        }
    }
}

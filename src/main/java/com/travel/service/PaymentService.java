package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.config.WechatConfig;
import com.travel.entity.Order;
import com.travel.entity.PaymentConfig;
import com.travel.exception.BusinessException;
import com.travel.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class PaymentService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private WechatConfig wechatConfig;
    
    @Autowired
    private PaymentConfigService paymentConfigService;
    
    private RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 创建微信支付订单
     * 
     * @param order 订单
     * @param openid 用户openid
     * @return 支付参数（用于小程序调用wx.requestPayment）
     */
    public Map<String, String> createWechatPayment(Order order, String openid) {
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不能为空");
        }
        
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "订单状态不正确，无法支付");
        }
        
        // 检查支付配置是否启用
        PaymentConfig config = paymentConfigService.getEnabled();
        if (config == null || config.getStatus() == null || config.getStatus() == 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "支付功能已禁用，请联系管理员");
        }
        
        // 检查微信支付配置
        String appId = getAppId();
        String mchId = getMchId();
        String apiKey = getApiKey();
        
        if (appId == null || appId.isEmpty()) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "支付配置不完整：AppID未配置");
        }
        
        if (mchId == null || mchId.isEmpty() || apiKey == null || apiKey.isEmpty()) {
            // 测试模式：返回模拟支付参数
            log.warn("微信支付配置未完成，使用测试模式");
            return createTestPaymentParams(order);
        }
        
        try {
            // 调用微信统一下单API
            Map<String, String> paymentParams = callWechatUnifiedOrder(order, openid, appId, mchId, apiKey);
            return paymentParams;
        } catch (Exception e) {
            log.error("创建微信支付订单失败", e);
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "创建支付订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 调用微信统一下单API
     */
    private Map<String, String> callWechatUnifiedOrder(Order order, String openid, String appId, String mchId, String apiKey) {
        // 构建统一下单请求参数
        Map<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
        params.put("body", "旅游平台订单-" + order.getOrderNo());
        params.put("out_trade_no", order.getOrderNo());
        params.put("total_fee", String.valueOf(order.getPayAmount().multiply(BigDecimal.valueOf(100)).intValue()));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", getNotifyUrl());
        params.put("trade_type", "JSAPI");
        params.put("openid", openid);
        
        // 生成签名
        String sign = generateSign(params, apiKey);
        params.put("sign", sign);
        
        // 转换为XML格式
        String xml = mapToXml(params);
        
        // 调用微信API
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        String responseXml = restTemplate.postForObject(url, xml, String.class);
        
        // 解析响应
        Map<String, String> responseMap = xmlToMap(responseXml);
        
        // 检查返回码
        if (!"SUCCESS".equals(responseMap.get("return_code"))) {
            String returnMsg = responseMap.get("return_msg");
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "微信支付统一下单失败: " + returnMsg);
        }
        
        if (!"SUCCESS".equals(responseMap.get("result_code"))) {
            String errCodeDes = responseMap.get("err_code_des");
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "微信支付统一下单失败: " + errCodeDes);
        }
        
        // 获取prepay_id
        String prepayId = responseMap.get("prepay_id");
        
        // 构建小程序支付参数
        Map<String, String> paymentParams = new HashMap<>();
        paymentParams.put("appId", appId);
        paymentParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        paymentParams.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
        paymentParams.put("package", "prepay_id=" + prepayId);
        paymentParams.put("signType", "RSA");
        
        // 生成小程序支付签名（使用RSA签名）
        String paySign = generatePaySign(paymentParams, apiKey);
        paymentParams.put("paySign", paySign);
        
        return paymentParams;
    }
    
    /**
     * 处理微信支付回调
     */
    @Transactional
    public void handlePaymentNotify(String xmlData) {
        try {
            // 解析XML数据
            Map<String, String> notifyMap = xmlToMap(xmlData);
            
            // 验证签名
            String sign = notifyMap.get("sign");
            String apiKey = getApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("微信支付API密钥未配置，跳过签名验证（测试模式）");
            } else {
                notifyMap.remove("sign");
                String calculatedSign = generateSign(notifyMap, apiKey);
                if (!sign.equals(calculatedSign)) {
                    log.error("微信支付回调签名验证失败");
                    throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "签名验证失败");
                }
            }
            
            // 检查返回码
            if (!"SUCCESS".equals(notifyMap.get("return_code"))) {
                log.error("微信支付回调返回码错误: {}", notifyMap.get("return_msg"));
                return;
            }
            
            if (!"SUCCESS".equals(notifyMap.get("result_code"))) {
                log.error("微信支付回调结果码错误: {}", notifyMap.get("err_code_des"));
                return;
            }
            
            // 获取订单号
            String orderNo = notifyMap.get("out_trade_no");
            String transactionId = notifyMap.get("transaction_id");
            
            // 查询订单
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                log.error("订单不存在: {}", orderNo);
                return;
            }
            
            // 检查订单状态
            if (!"PENDING_PAY".equals(order.getStatus())) {
                log.warn("订单状态不正确，已处理: {}", orderNo);
                return;
            }
            
            // 更新订单状态
            order.setStatus("PAID");
            order.setPayTime(LocalDateTime.now());
            order.setPayType("WECHAT");
            order.setPayNo(transactionId);
            orderMapper.updateById(order);
            
            // 如果使用了优惠券，更新优惠券状态
            if (order.getCouponId() != null) {
                // 查询用户优惠券（需要通过订单的couponId和userId查询）
                // 这里简化处理，实际应该查询UserCoupon表
                log.info("订单使用了优惠券，优惠券ID: {}", order.getCouponId());
            }
            
            log.info("订单支付成功，订单号: {}, 交易号: {}", orderNo, transactionId);
            
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "处理支付回调失败");
        }
    }
    
    /**
     * 创建测试支付参数（开发测试模式）
     */
    private Map<String, String> createTestPaymentParams(Order order) {
        String appId = getAppId();
        Map<String, String> params = new HashMap<>();
        params.put("appId", appId != null ? appId : wechatConfig.getAppid());
        params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
        params.put("package", "prepay_id=test_prepay_id_" + order.getOrderNo());
        params.put("signType", "RSA");
        params.put("paySign", "test_pay_sign_" + order.getOrderNo());
        return params;
    }
    
    /**
     * 生成签名（MD5）
     */
    private String generateSign(Map<String, String> params, String apiKey) {
        // 按字典序排序
        StringBuilder sb = new StringBuilder();
        params.entrySet().stream()
            .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> sb.append(e.getKey()).append("=").append(e.getValue()).append("&"));
        sb.append("key=").append(apiKey);
        
        // MD5加密
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes("UTF-8"));
            return bytesToHex(digest).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }
    
    /**
     * 生成小程序支付签名（RSA）
     * 注意：实际应该使用RSA签名，这里简化使用MD5
     */
    private String generatePaySign(Map<String, String> params, String apiKey) {
        // 简化处理，实际应该使用RSA签名
        return generateSign(params, apiKey);
    }
    
    /**
     * Map转XML
     */
    private String mapToXml(Map<String, String> params) {
        StringBuilder xml = new StringBuilder("<xml>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            xml.append("<").append(entry.getKey()).append(">")
               .append("<![CDATA[").append(entry.getValue()).append("]]>")
               .append("</").append(entry.getKey()).append(">");
        }
        xml.append("</xml>");
        return xml.toString();
    }
    
    /**
     * XML转Map
     */
    private Map<String, String> xmlToMap(String xml) {
        Map<String, String> map = new HashMap<>();
        try {
            // 简化XML解析，实际应该使用更完善的XML解析库
            // 这里使用简单的字符串处理
            String content = xml.replaceAll("<xml>|</xml>", "");
            String[] entries = content.split("</");
            for (String entry : entries) {
                if (entry.contains("><")) {
                    String[] parts = entry.split("><");
                    if (parts.length == 2) {
                        String key = parts[0].replace("<", "").replace(">", "");
                        String value = parts[1].replace("![CDATA[", "").replace("]]", "").replace(">", "");
                        map.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析XML失败", e);
        }
        return map;
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * 获取AppID（优先从数据库读取，支持回退）
     */
    private String getAppId() {
        return paymentConfigService.getAppId();
    }
    
    /**
     * 获取商户号（优先从数据库读取，支持回退）
     */
    private String getMchId() {
        return paymentConfigService.getMchId();
    }
    
    /**
     * 获取API密钥（优先从数据库读取，支持回退）
     */
    private String getApiKey() {
        return paymentConfigService.getApiKey();
    }
    
    /**
     * 获取支付回调地址（优先从数据库读取，支持回退）
     */
    private String getNotifyUrl() {
        return paymentConfigService.getNotifyUrl();
    }
}

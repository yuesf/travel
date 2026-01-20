package com.travel.common;

/**
 * 响应码枚举
 * 
 * @author travel-platform
 */
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    
    // 4xx 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    
    // 5xx 服务器错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    // 业务错误码
    LOGIN_FAILED(1001, "登录失败，用户名或密码错误"),
    WECHAT_LOGIN_FAILED(1007, "微信登录失败，请重试"),
    WECHAT_CODE_INVALID(1008, "微信登录code无效或已过期"),
    TOKEN_INVALID(1002, "Token无效或已过期"),
    TOKEN_EXPIRED(1003, "Token已过期"),
    USER_DISABLED(1004, "用户已被禁用"),
    PASSWORD_ERROR(1005, "密码错误"),
    USER_NOT_FOUND(1006, "用户不存在"),
    
    // 参数校验错误
    PARAM_ERROR(2001, "参数错误"),
    PARAM_MISSING(2002, "缺少必要参数"),
    
    // 业务操作错误
    OPERATION_FAILED(3001, "操作失败"),
    DATA_NOT_FOUND(3002, "数据不存在"),
    DATA_ALREADY_EXISTS(3003, "数据已存在");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

package com.travel.exception;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有异常，返回统一格式的响应
 * 
 * @author travel-platform
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     * 业务异常返回友好的错误提示，不暴露系统内部细节
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数校验异常（@Valid）
     * 返回第一个字段的错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.warn("参数校验异常: {}", message);
        return Result.error(ResultCode.PARAM_ERROR);
    }
    
    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.warn("参数绑定异常: {}", message);
        return Result.error(ResultCode.PARAM_ERROR);
    }
    
    /**
     * 处理约束校验异常（@Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.isEmpty() 
            ? "参数校验失败" 
            : violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束校验异常: {}", message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = String.format("缺少必要参数: %s", e.getParameterName());
        log.warn("缺少请求参数异常: {}", message);
        return Result.error(ResultCode.PARAM_MISSING.getCode(), message);
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数类型错误: %s", e.getName());
        log.warn("参数类型不匹配异常: {}", message);
        return Result.error(ResultCode.PARAM_ERROR);
    }
    
    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String message = String.format("请求方法 %s 不支持，支持的方法: %s", 
            e.getMethod(), 
            String.join(", ", e.getSupportedMethods()));
        log.warn("HTTP请求方法不支持异常: {}", message);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "请求方法不支持");
    }
    
    /**
     * 处理HTTP媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.warn("HTTP媒体类型不支持异常: {}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "不支持的媒体类型");
    }
    
    /**
     * 处理404异常（资源不存在）
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("资源不存在: {}", e.getRequestURL());
        return Result.error(ResultCode.NOT_FOUND);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        return Result.error(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }
    
    /**
     * 处理客户端主动断开连接异常
     * 这种情况通常发生在客户端取消请求或网络中断时，不需要返回错误响应
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e, HttpServletRequest request) {
        // 客户端主动断开连接，只记录日志，不返回响应
        log.debug("客户端断开连接: {} - {}", request.getRequestURI(), e.getMessage());
    }
    
    /**
     * 处理IO异常（包括 Broken pipe 等客户端断开连接的情况）
     * 检查是否是客户端断开连接导致的异常
     */
    @ExceptionHandler(IOException.class)
    public Result<?> handleIOException(IOException e, HttpServletRequest request, HttpServletResponse response) {
        // 检查是否是客户端断开连接的情况
        String message = e.getMessage();
        if (message != null && 
            (message.contains("Broken pipe") || 
             message.contains("Connection reset") ||
             message.contains("Connection closed"))) {
            // 客户端断开连接，只记录调试日志，不返回响应
            log.debug("客户端断开连接: {} - {}", request.getRequestURI(), message);
            return null;
        }
        
        // 如果不是客户端断开连接，且响应未提交，则记录错误并返回错误响应
        if (!response.isCommitted()) {
            log.error("IO异常: {} - {}", request.getRequestURI(), message, e);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
        
        // 响应已提交，无法返回错误信息
        log.warn("IO异常且响应已提交: {} - {}", request.getRequestURI(), message);
        return null;
    }
    
    /**
     * 处理其他所有异常
     * 系统异常不暴露内部细节，返回通用错误信息
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        // 检查响应是否已经提交（已经开始写入响应体）
        if (response.isCommitted()) {
            log.warn("响应已提交，无法返回错误信息: {} - {}", request.getRequestURI(), e.getMessage());
            return null;
        }
        
        // 检查 Content-Type 是否已经被设置为非 JSON 类型（如视频、图片等）
        String contentType = response.getContentType();
        if (contentType != null && !contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            log.warn("响应 Content-Type 已设置为 {}，无法返回 JSON 错误信息: {} - {}", 
                contentType, request.getRequestURI(), e.getMessage());
            // 尝试重置响应状态
            try {
                response.reset();
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            } catch (IllegalStateException ex) {
                // 如果无法重置，说明响应已经开始写入，直接返回 null
                log.warn("无法重置响应: {}", ex.getMessage());
                return null;
            }
        }
        
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        
        // 设置 Content-Type 为 application/json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        // 不暴露异常堆栈信息给前端，只返回通用错误信息
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
    }
}

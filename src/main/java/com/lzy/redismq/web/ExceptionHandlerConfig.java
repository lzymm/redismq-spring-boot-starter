package com.lzy.redismq.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * @Description: TODO
 * @Author: liuzy-t
 * @Date: 2024-08-07 下午 12:39
 **/
@Slf4j
@RestControllerAdvice
public class ExceptionHandlerConfig {
    /**
     * 拦截valid参数校验返回的异常，并转化成基本的返回样式
     */
    @ExceptionHandler(value = Exception.class)
    public Object dealMethodArgumentNotValidException(Exception e) {
        log.error("this is controller param valid failed", e);
        Throwable cause = e.getCause();
        String msg = e.getMessage();
        if(cause!=null){
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            msg = cause.getMessage();
        }
        return Map.of("code",-10,"msg",msg);
    }
}

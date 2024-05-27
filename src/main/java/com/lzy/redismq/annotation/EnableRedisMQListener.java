package com.lzy.redismq.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用@RedisMQListener监听
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RedisMQListenerConfigurationSelector.class)
public @interface EnableRedisMQListener {
}
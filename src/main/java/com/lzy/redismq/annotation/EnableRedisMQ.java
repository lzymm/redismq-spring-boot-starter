package com.lzy.redismq.annotation;

import com.lzy.redismq.config.RedisMQStreamConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({RedisMQStreamConfig.class,RedisMQListenerConfigurationSelector.class})
public @interface EnableRedisMQ {
}
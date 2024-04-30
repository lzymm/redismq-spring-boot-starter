package com.lzy.redismq.annotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 延迟加载redisMQ启动配置类
 */
@Slf4j(topic = "redis-mq:RedisMQListenerConfigurationSelector")
@Order
public class RedisMQListenerConfigurationSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] { RedisMQBootstrapConfiguration.class.getName() };
    }

}
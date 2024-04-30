package com.lzy.redismq.annotation;

import com.lzy.redismq.config.RedisMQConfigUtils;
import com.lzy.redismq.config.RedisMQListenerEndpointRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * a {@link RedisMQListenerAnnotationBeanPostProcessor} bean capable of processing
 *  * Spring's @{@link RedisMQListener} annotation
 */
public class RedisMQBootstrapConfiguration implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {


        if (!registry.containsBeanDefinition(
                RedisMQConfigUtils.REDIS_MQ_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)) {

            registry.registerBeanDefinition(RedisMQConfigUtils.REDIS_MQ_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME,
                    new RootBeanDefinition(RedisMQListenerAnnotationBeanPostProcessor.class));
        }

        if (!registry.containsBeanDefinition(RedisMQConfigUtils.REDIS_MQ_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)) {
            registry.registerBeanDefinition(RedisMQConfigUtils.REDIS_MQ_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
                    new RootBeanDefinition(RedisMQListenerEndpointRegistry.class));
        }
        // if (!registry.containsBeanDefinition(RedisMQConfigUtils.REDIS_MQ_STREAM_HELPER_BEAN_NAME)) {
        //     registry.registerBeanDefinition(RedisMQConfigUtils.REDIS_MQ_STREAM_HELPER_BEAN_NAME,
        //             new RootBeanDefinition(RedisMQStreamHelper.class));
        // }
    }

}
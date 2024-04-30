package com.lzy.redismq.config;
/**
 * Configuration constants for internal sharing across subpackages.
 */
public abstract class RedisMQConfigUtils {
    /**
     * The bean name of the internally managed Rabbit listener annotation processor.
     */
    public static final String REDIS_MQ_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME =
            "com.lzy.redis.mq.config.internalRedisMQListenerAnnotationProcessor";

    /**
     * The bean name of the internally managed Rabbit listener endpoint registry.
     */
    public static final String REDIS_MQ_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME =
            "com.lzy.redis.mq.config.internalRedisMQListenerEndpointRegistry";



    public static final String REDIS_MQ_STREAM_HELPER_BEAN_NAME =
            "redisMQStreamHelper";
    public static final String REDIS_MQ_CONTAINER_FACTORY_BEAN_NAME =
            "redisMQContainerFactory";
}

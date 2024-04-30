package com.lzy.redismq.config;

import com.lzy.redismq.annotation.EnableRedisMQ;
import com.lzy.redismq.producer.RedisMQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(value = {EnableRedisMQ.class})
@Slf4j(topic = "redis-mq:RedisMQStreamConfig")
public class RedisMQStreamConfig {

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 这个地方不可使用 json 序列化，否则会有问题，会出现一个 java.lang.IllegalArgumentException: Value must not be null! 错误
        // redisTemplate.setHashValueSerializer(RedisSerializer.string());
        return redisTemplate;
    }
    @Bean(RedisMQConfigUtils.REDIS_MQ_CONTAINER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(RedisMQContainerFactory.class)
    public RedisMQContainerFactory redisMQContainerFactory(RedisConnectionFactory redisConnectionFactory) {
        return new RedisMQContainerFactory(redisConnectionFactory);
    }

    @Bean(RedisMQConfigUtils.REDIS_MQ_STREAM_HELPER_BEAN_NAME)
    @ConditionalOnBean(StringRedisTemplate.class)
    public RedisMQStreamHelper redisMQStreamHelper(StringRedisTemplate redisTemplate) {
        return new RedisMQStreamHelper(redisTemplate);
    }

    @Bean
    @ConditionalOnBean(RedisMQStreamHelper.class)
    public RedisMQProducer redisMQProducer(RedisMQStreamHelper redisMQStreamHelper) {
        return new RedisMQProducer(redisMQStreamHelper);
    }

}


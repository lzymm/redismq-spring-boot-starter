package com.lzy.redismq.config;

import com.lzy.redismq.annotation.EnableRedisMQListener;
import com.lzy.redismq.producer.RedisMQProducer;
import com.lzy.redismq.task.RedisMQTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * RedisMQ 初始化配置类
 * @author: lzy
 * @date: 2024-09-13 00:01:40
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(value = {EnableRedisMQListener.class})
@EnableConfigurationProperties(RedisMQProperties.class)
@ConditionalOnProperty(value = "spring.redis.stream.enable",matchIfMissing = false)
@Slf4j(topic = "redis-mq:RedisMQStreamConfig")
public class RedisMQConfig {
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // ?? 这个地方不可使用 json 序列化，否则会有问题，会出现一个 java.lang.IllegalArgumentException: Value must not be null! 错误
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }

    @Bean(value = RedisMQConfigUtils.REDIS_MQ_HELPER_BEAN_NAME,initMethod = "initStreams")
    @ConditionalOnBean(RedisTemplate.class)
    public RedisMQHelper redisMQHelper(RedisTemplate redisTemplate,RedisMQProperties redisMQProperties) {
        return new RedisMQHelper(redisTemplate, redisMQProperties);
    }

    @Bean
    @ConditionalOnBean(RedisMQHelper.class)
    public RedisMQProducer redisMQProducer(RedisMQHelper redisMQHelper) {
        return new RedisMQProducer(redisMQHelper);
    }

    @Bean
    @ConditionalOnBean(RedisMQHelper.class)
    public RedisMQTask redisMQTask(RedisMQHelper redisMQHelper) {
        return new RedisMQTask(redisMQHelper);
    }


}


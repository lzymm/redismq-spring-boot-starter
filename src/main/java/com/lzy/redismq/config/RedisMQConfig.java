package com.lzy.redismq.config;

import com.lzy.redismq.annotation.EnableRedisMQListener;
import com.lzy.redismq.producer.RedisMQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(value = {EnableRedisMQListener.class})
@EnableConfigurationProperties(RedisMQProperties.class)
@ConditionalOnProperty(value = "spring.redis.stream.enable",matchIfMissing = false)
@Slf4j(topic = "redis-mq:RedisMQStreamConfig")
public class RedisMQConfig {

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 这个地方不可使用 json 序列化，否则会有问题，会出现一个 java.lang.IllegalArgumentException: Value must not be null! 错误
        // redisTemplate.setHashValueSerializer(RedisSerializer.string());
        return redisTemplate;
    }

    @Bean(value = RedisMQConfigUtils.REDIS_MQ_HELPER_BEAN_NAME,initMethod = "initStreams")
    @ConditionalOnBean(StringRedisTemplate.class)
    public RedisMQHelper redisMQHelper(StringRedisTemplate redisTemplate,RedisMQProperties redisMQProperties) {
        return new RedisMQHelper(redisTemplate, redisMQProperties);
    }

    @Bean
    @ConditionalOnBean(RedisMQHelper.class)
    public RedisMQProducer redisMQProducer(RedisMQHelper redisMQHelper) {
        return new RedisMQProducer(redisMQHelper);
    }
}


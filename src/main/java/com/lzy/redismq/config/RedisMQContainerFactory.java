package com.lzy.redismq.config;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

import java.time.Duration;

/**
 * redis mq listener container
 * listener 容器
 */
public class RedisMQContainerFactory {
    private RedisConnectionFactory redisConnectionFactory;

    public RedisMQContainerFactory(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public StreamMessageListenerContainer createContainer(RedisMQListenerEndpoint endpoint) {

        StreamMessageListenerContainerOptions<String, MapRecord<String, String, Object>> options =
                StreamMessageListenerContainerOptions.builder()
                        // ObjectRecord 时，将 对象的 filed 和 value 转换成一个 Map 比如：将Book对象转换成map
                        // .objectMapper()
                        // 将发送到Stream中的Record转换成ObjectRecord，转换成具体的类型是这个地方指定的类型
                        // .targetType()
                        // 获取消息的过程或获取到消息给具体的消息者处理的过程中，发生了异常的处理
                        // 一次最多获取多少条消息
                        .batchSize(endpoint.getPerPollSize())
                        // Stream 中没有消息时，阻塞多长时间，需要比 `spring.redis.timeout` 的时间小
                        .pollTimeout(Duration.ofSeconds(endpoint.getPollTimeoutSeconds()))
                        // 运行 Stream 的 poll task
                        .executor(endpoint.getTaskExecutor())
                        .errorHandler(endpoint.getErrorHandler())
                        .hashKeySerializer(RedisSerializer.string())
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, Object>> container = StreamMessageListenerContainer.create(redisConnectionFactory, options);
        return container;
    }
}

package com.lzy.redismq.consumer;

import com.lzy.redismq.annotation.RedisMQListenerEndpoint;
import com.lzy.redismq.exception.RedisMQException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.lang.reflect.InvocationTargetException;

/**
 * 异步消息消费者
 * @author: lzy
 * @date: 2024-09-12 23:57:38
 */
@Slf4j
public class AsyncRedisMQConsumer extends AbstractRedisMQConsumer {
    public AsyncRedisMQConsumer(RedisMQListenerEndpoint endpoint, Consumer consumer) {
        super(endpoint,consumer);
    }

    /**
     * 处理消息方法 :实现业务逻辑
     * @param message MapRecord
     */
    @Override
    public void dealMessageAck(MapRecord<String, String, Object> message) {
        try {
            log.debug("自动akc处理一条消息：id={},content={} ", message.getId(), message.getValue());
            this.getEndpoint().getMethod().invoke(this.getEndpoint().getBean(), message,getConsumer());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RedisMQException(e);
        }

    }

    /**
     * 处理消息方法 :实现业务逻辑
     * 需要手动调用 {@link RedisMQAcknowledge#ack(MapRecord)} 方法
     */
    @Override
    public void dealMessage(MapRecord<String, String, Object> message, RedisMQAcknowledge acknowledge) {
        try {
            log.debug("手动ack处理一条消息：id={},content={} ", message.getId(), message.getValue());
            this.getEndpoint().getMethod().invoke(this.getEndpoint().getBean(), message,getConsumer(), acknowledge);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RedisMQException(e);
        }
    }
}

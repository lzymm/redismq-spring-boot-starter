package com.lzy.redismq.consumer;

import com.lzy.redismq.config.RedisMQListenerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class AsyncStreamConsumer extends AbstractStreamConsumer {
    public AsyncStreamConsumer(RedisMQListenerEndpoint endpoint) {
        super(endpoint);
    }

    /**
     * 处理消息方法 :实现业务逻辑
     * @param message MapRecord
     */
    @Override
    public void dealMessageAck(MapRecord<String, String, Object> message) {
        try {
            log.debug("处理一条消息：id={},content={} ", message.getId(), message.getValue());
            this.getEndpoint().getMethod().invoke(this.getEndpoint().getBean(), message);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 处理消息方法 :实现业务逻辑
     * 需要手动调用 {@link StreamAcknowledge#ack(MapRecord)} 方法
     */
    @Override
    public void dealMessage(MapRecord<String, String, Object> message, StreamAcknowledge acknowledge) {
        try {
            log.debug("处理一条消息：id={},content={} ", message.getId(), message.getValue());
            this.getEndpoint().getMethod().invoke(this.getEndpoint().getBean(), message, acknowledge);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

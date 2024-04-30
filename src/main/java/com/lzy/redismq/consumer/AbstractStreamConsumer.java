package com.lzy.redismq.consumer;

import com.lzy.redismq.config.RedisMQListenerEndpoint;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

/**
 * 通过监听器异步消费
 *
 * @author lzy
 */
@Slf4j
@Getter
@Setter
public abstract class AbstractStreamConsumer implements StreamListener<String, MapRecord<String, String, Object>> {
    /**
     * 消费者类型：独立消费、消费组消费
     */
    private RedisMQListenerEndpoint endpoint;
    private Consumer consumer;

    public AbstractStreamConsumer(RedisMQListenerEndpoint endpoint,Consumer consumer) {
        this.endpoint = endpoint;
        this.consumer = consumer;

    }

    /**
     * 消息监听器接收消息方法
     *
     * @param message MapRecord类型的消息
     */
    @Override
    public void onMessage(MapRecord<String, String, Object> message) {
        if (endpoint.isAutoAck()) {
            dealMessageAck(message);
        } else {
            dealMessage(message, new StreamAcknowledge(endpoint));
        }
    }

    /**
     * 处理消息方法 :实现业务逻辑
     * 如果设置{@link RedisMQListenerEndpoint#isAutoAck()}==false，
     * 需要手动调用 {@link StreamAcknowledge#ack(MapRecord)} 方法
     */
    public abstract void dealMessageAck(MapRecord<String, String, Object> message);

    public abstract void dealMessage(MapRecord<String, String, Object> message, StreamAcknowledge acknowledge);
}

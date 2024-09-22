package com.lzy.redismq.consumer;

import com.lzy.redismq.annotation.RedisMQListenerEndpoint;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
/**
 * RedisMQ 消息手动确认
 * @author: lzy
 * @date: 2024-09-12 23:56:54
 */
public class RedisMQAcknowledge {
    private final RedisMQListenerEndpoint endpoint;

    public RedisMQAcknowledge(RedisMQListenerEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 手动Ack 方法
     * @param message MapRecord类型的消息
     * @return Long ack的消息长度
     */
    public Long ack(MapRecord<String, Object, Object> message) {
        String streamKey = endpoint.getStream();
        String group = endpoint.getGroup();
        RecordId id = message.getId();
        return endpoint.getRedisMQHelper().ack(streamKey, group, id.getValue());
    }
}

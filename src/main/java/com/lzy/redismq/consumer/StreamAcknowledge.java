package com.lzy.redismq.consumer;

import com.lzy.redismq.config.RedisMQListenerEndpoint;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;

public class StreamAcknowledge {
    private RedisMQListenerEndpoint endpoint;

    public StreamAcknowledge(RedisMQListenerEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 手动Ack 方法
     * @param message MapRecord类型的消息
     * @return Long ack的消息长度
     */
    public Long ack(MapRecord<String, String, Object> message) {
        String streamKey = message.getStream();
        String group = endpoint.getGroup();
        RecordId id = message.getId();
        return endpoint.getRedisMQStreamHelper().ack(streamKey, group, id.getValue());
    }
}

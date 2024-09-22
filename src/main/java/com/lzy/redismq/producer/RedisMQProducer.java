package com.lzy.redismq.producer;

import com.alibaba.fastjson.JSON;
import com.lzy.redismq.config.RedisMQHelper;

import java.util.HashMap;
import java.util.Map;

public class RedisMQProducer {

    private final RedisMQHelper redisMQHelper;
    private volatile boolean createdStreamKey = false;

    private final Map<String, Long> streamLenRecord = new HashMap<>();
    private final Long defMaxLen;

    public RedisMQProducer(RedisMQHelper redisMQHelper) {
        this.defMaxLen = redisMQHelper.getDefMaxLen();
        this.redisMQHelper = redisMQHelper;
    }

    /**
     * 向指定的stream发送消息
     *
     * @return java.lang.String
     */
    // public String sendMessage(String streamKey, Map<String, Object> message) {
    //     return sendMap(streamKey, message);
    // }
    public String sendMessage(String streamKey, Object message) {
        Class<?> aClass = message.getClass();
        if (aClass.getName().toLowerCase().contains("immutable")) {
            message = JSON.toJSON(message);
        }
        return sendMap(streamKey, Map.of("data", message));
    }

    /**
     * 添加Map消息
     *
     * @param streamKey stream对应的key
     * @param message   消息数据
     * @return recordId
     */
    private String sendMap(String streamKey, Map<String, Object> message) {
        if (!createdStreamKey) {
            synchronized (this) {
                if (!createdStreamKey) {
                    boolean hasKey = redisMQHelper.hasStream(streamKey);
                    if (!hasKey) {
                        redisMQHelper.createStream(streamKey);
                    }
                    createdStreamKey = true;
                }
            }
        }
        String msgId = redisMQHelper.sendMap(streamKey, message);
        return msgId;
    }

}

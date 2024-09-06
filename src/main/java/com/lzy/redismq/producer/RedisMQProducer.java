package com.lzy.redismq.producer;

import com.lzy.redismq.config.RedisMQHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class RedisMQProducer {

    private final RedisMQHelper redisMQHelper;
    private volatile boolean createdStreamKey = false;

    private final Map<String, LongAdder> streamLenCounter = new ConcurrentHashMap<>();
    private final Map<String, Long> streamLenRecord = new HashMap<>();
    private final Long defMaxLen;

    public RedisMQProducer(RedisMQHelper redisMQHelper) {
        Map<String, Long> streamLenMap = redisMQHelper.getStreamMap();

        this.defMaxLen = redisMQHelper.getDefMaxLen();
        this.redisMQHelper = redisMQHelper;
        this.streamLenRecord.putAll(streamLenMap);
        streamLenMap.forEach((k, v)-> {
            streamLenCounter.put(k, new LongAdder());
        });
    }

    /**
     * 向指定的stream发送消息
     * @date: 028 24/5/28 下午 11:18
     * @params [streamKey, message(Map)]
     * @return java.lang.String
     */
    public String sendMessage(String streamKey, Map<String, Object> message) {
            return sendMap(streamKey, message);
    }

    /**
     * 添加Map消息
     * @param streamKey stream对应的key
     * @param message 消息数据
     * @return recordId
     */
    private String sendMap(String streamKey, Map<String, Object> message){
        if(!createdStreamKey){
            synchronized (this) {
                if(!createdStreamKey){
                    boolean hasKey = redisMQHelper.hasStream(streamKey);
                    if (!hasKey) {
                redisMQHelper.createStream(streamKey);
                    }
                    createdStreamKey = true;
                }
            }
        }
        String msgId = redisMQHelper.sendMap(streamKey, message);
        tryTrim(streamKey);
        return msgId;
    }

    private void tryTrim(String streamKey){
        LongAdder lenCounter = streamLenCounter.get(streamKey);
        lenCounter.decrement();
        Long maxLen = streamLenRecord.getOrDefault(streamKey, defMaxLen);
        if (lenCounter.sum() >= 1.5*maxLen) {
                redisMQHelper.trim(streamKey, maxLen);
        }
    }
}

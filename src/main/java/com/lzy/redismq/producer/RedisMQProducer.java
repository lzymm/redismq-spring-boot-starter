package com.lzy.redismq.producer;

import com.lzy.redismq.config.RedisMQStreamHelper;

import java.util.Map;

public class RedisMQProducer {

    private RedisMQStreamHelper redisMQStreamHelper;
    private volatile boolean createdStreamKey = false;

    public RedisMQProducer(RedisMQStreamHelper redisMQStreamHelper) {
        this.redisMQStreamHelper = redisMQStreamHelper;
    }

    public String sendMessage(String streamKey, Map<String, Object> message) {
        return sendMap(streamKey, message);
    }

    /**
     * 添加Map消息
     * @param streamKey stream对应的key
     * @param message 消息数据
     * @return
     */
    private String sendMap(String streamKey, Map<String, Object> message){
        if(!createdStreamKey){
            synchronized (this.getClass()) {
                if(!createdStreamKey){
                    boolean hasKey = redisMQStreamHelper.hasStream(streamKey);
                    if (!hasKey) {
                        redisMQStreamHelper.createStream(streamKey);
                    }
                    createdStreamKey = true;
                }
            }
        }
        return  redisMQStreamHelper.sendMap(streamKey,message);
    }
}

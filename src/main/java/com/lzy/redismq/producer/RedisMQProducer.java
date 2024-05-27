package com.lzy.redismq.producer;

import com.lzy.redismq.config.RedisMQHelper;

import java.util.Map;

public class RedisMQProducer {

    private RedisMQHelper redisMQHelper;
    private volatile boolean createdStreamKey = false;

    public RedisMQProducer(RedisMQHelper redisMQHelper) {
        this.redisMQHelper = redisMQHelper;
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
                    boolean hasKey = redisMQHelper.hasStream(streamKey);
                    if (!hasKey) {
                        redisMQHelper.createStream(streamKey);
                    }
                    createdStreamKey = true;
                }
            }
        }
        return  redisMQHelper.sendMap(streamKey,message);
    }
}

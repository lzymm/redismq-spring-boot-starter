package com.lzy.redismq.config;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;


public class RedisMQStreamHelper {
    private AtomicInteger uniqCounter = new AtomicInteger(0);
    private StringRedisTemplate stringRedisTemplate;

    public RedisMQStreamHelper(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Integer createUniqNum(){
        return uniqCounter.getAndIncrement();
    }

    /**
     * 创建Stream
     * @param streamKey stream-key值
     * @return java.lang.String
     */
    public RecordId createStream(String streamKey){
        RecordId recordId = stringRedisTemplate.opsForStream().add(streamKey, Map.of("initKey", "initValue"));
        stringRedisTemplate.opsForStream().delete(streamKey, recordId);
        return recordId;
    }
    /**
     * 创建消费组
     * @param streamKey stream-key值
     * @param group 消费组
     * @return java.lang.String
     */
    public String createGroup(String streamKey, String group){
        return stringRedisTemplate.opsForStream().createGroup(streamKey, group);
    }

    /**
     * 获取消费者信息
     * @param streamKey stream-key值
     * @param group 消费组
     * @return org.springframework.data.redis.connection.stream.StreamInfo.XInfoConsumers
     */
    public StreamInfo.XInfoConsumers queryConsumers(String streamKey, String group){
        return stringRedisTemplate.opsForStream().consumers(streamKey, group);
    }

    /**
     * 添加Map消息
     * @param streamKey stream对应的key
     * @param value 消息数据
     * @return
     */
    public String sendMap(String streamKey, Map<String, Object> value){
        return stringRedisTemplate.opsForStream().add(streamKey, value).getValue();
    }

    /**
     * 读取消息
     * @param: streamKey
     * @return java.util.List<org.springframework.data.redis.connection.stream.MapRecord<java.lang.String,java.lang.Object,java.lang.Object>>
     */
    public List<MapRecord<String, Object, Object>> read(String streamKey){
        return stringRedisTemplate.opsForStream().read(StreamOffset.fromStart(streamKey));
    }

    /**
     * 确认消费
     * @param streamKey
     * @param group
     * @param recordIds
     * @return java.lang.Long
     */
    public Long ack(String streamKey, String group, String... recordIds){
        return stringRedisTemplate.opsForStream().acknowledge(streamKey, group, recordIds);
    }



    /**
     * 删除消息。当一个节点的所有消息都被删除，那么该节点会自动销毁
     * @param: streamKey
     * @param: recordIds
     * @return java.lang.Long
     */
    public Long del(String streamKey, String... recordIds){
        return stringRedisTemplate.opsForStream().delete(streamKey, recordIds);
    }

    /**
     * 判断是否存在streamKey
     * @param streamKey
     * @return
     */
    public boolean hasStream(String streamKey){
        Boolean aBoolean = stringRedisTemplate.hasKey(streamKey);
        return Optional.ofNullable(aBoolean).orElse(Boolean.FALSE);
    }
    /**
     * 判断是否存在streamKey
     * @param streamKey
     * @return
     */
    public boolean hasGroup(String streamKey,String group){
        StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(streamKey);
        boolean anyMatch = groups.stream().anyMatch(xInfoGroup -> xInfoGroup.groupName().equals(group));
        return Optional.ofNullable(anyMatch).orElse(Boolean.FALSE);
    }

}
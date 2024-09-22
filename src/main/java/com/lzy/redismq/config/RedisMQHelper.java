package com.lzy.redismq.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * RedisMQ 基础操作类
 * redisTemplate stream操作封装
 * @author: lzy
 * @date: 2024-09-07 01:28:56
 */
@Getter
@Setter
@Slf4j(topic = "redis-mq:RedisMQHelper")
public class RedisMQHelper {
    private final AtomicInteger uniqCounter = new AtomicInteger(0);

    private RedisTemplate redisTemplate;

    private RedisMQProperties redisMQProperties;
    private Map<String, Long> streamConfigMap = new HashMap<>();
    private Long defMaxLen;

    public RedisMQHelper(RedisTemplate redisTemplate, RedisMQProperties redisMQProperties) {
        this.redisTemplate = redisTemplate;
        this.redisMQProperties = redisMQProperties;
        if (this.redisMQProperties.isEnable()) {
            Assert.isTrue(!this.redisMQProperties.getKeys().isEmpty(),"stream keys is empty,please check properties");
            this.streamConfigMap.putAll(redisMQProperties.getKeys().stream().collect(Collectors.toMap(RedisMQProperties.RedisMQInfo::getName, RedisMQProperties.RedisMQInfo::getMaxLen, (s1, s2) -> s2)));
            this.defMaxLen = redisMQProperties.getDefMaxLen();
        }


    }


    public Integer createUniqNum() {
        return uniqCounter.getAndIncrement();
    }

    /**
     * 初始化创建Streams
     *
     * @return java.lang.String
     */
    public void initStreams() {
        streamConfigMap.keySet().forEach(this::createStream);
    }

    /**
     * 创建Stream
     *
     * @param streamKey stream-key值
     * @return java.lang.String
     */
    public void createStream(String streamKey) {
        boolean hasStreamKey = hasStream(streamKey);
        if (hasStreamKey) {
            log.info("redis-mq stream[{}] already exists", streamKey);
            return;
        }
        RecordId recordId = redisTemplate.opsForStream().add(streamKey, Map.of("init-test-msg", "hello-redis-stream"));
        redisTemplate.opsForStream().delete(streamKey, recordId);
        log.info("redis-mq stream[{}] initialize success", streamKey);
    }


    /**
     * 创建消费组
     *
     * @param streamKey stream-key值
     * @param group     消费组
     * @return java.lang.String
     */
    public String createGroup(String streamKey, String group) {
        boolean hasGroup = hasGroup(streamKey, group);
        if (hasGroup) {
            log.info("redis-mq stream-group[{}-{}] already exists", streamKey, group);
            return null;
        }
        String ret = redisTemplate.opsForStream().createGroup(streamKey, group);
        log.info("redis-mq stream-group[{}-{}] initialize success", streamKey, group, ret);
        return ret;
    }



    /**
     * 添加Map消息
     *
     * @param streamKey stream对应的key
     * @param value     消息数据
     */
    public String sendMap(String streamKey, Map<String, Object> value) {
        RecordId recordId = redisTemplate.opsForStream().add(streamKey, value);
        return recordId!=null?recordId.getValue():null;
    }

    public Long trim(String streamKey, Long maxLen) {
        Long trimCount = redisTemplate.opsForStream().trim(streamKey, maxLen);
        return trimCount;
    }

    /**
     * 读取消息
     *
     * @param streamKey
     * @return java.util.List<org.springframework.data.redis.connection.stream.MapRecord < java.lang.String, java.lang.Object, java.lang.Object>>
     */
    public List<MapRecord<String, Object, Object>> read(String streamKey) {
        return redisTemplate.opsForStream().read(StreamOffset.fromStart(streamKey));
    }

    /**
     * 确认消费
     *
     * @param streamKey
     * @param group
     * @param recordIds
     * @return java.lang.Long
     */
    public Long ack(String streamKey, String group, String... recordIds) {
        return redisTemplate.opsForStream().acknowledge(streamKey, group, recordIds);
    }


    /**
     * 删除消息。当一个节点的所有消息都被删除，那么该节点会自动销毁
     *
     * @return java.lang.Long
     */
    public Long del(String streamKey, String... recordIds) {
        return redisTemplate.opsForStream().delete(streamKey, recordIds);
    }

    /**
     * 判断是否存在streamKey
     *
     * @param streamKey
     */
    public boolean hasStream(String streamKey) {
        Boolean aBoolean = redisTemplate.hasKey(streamKey);
        return Optional.ofNullable(aBoolean).orElse(Boolean.FALSE);
    }

    /**
     * 判断是否存在streamKey
     *
     * @param streamKey
     */
    public boolean hasGroup(String streamKey, String group) {
        StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(streamKey);
        boolean anyMatch = groups.stream().anyMatch(xInfoGroup -> xInfoGroup.groupName().equals(group));
        return anyMatch;
    }

    public Map<String, Object> getStreamInfo(String streamKey) {
        return getStreamInfo(new RedisMQProperties.RedisMQInfo(streamKey,getDefMaxLen()));
    }
    public Map<String, Object> getStreamInfo(RedisMQProperties.RedisMQInfo streamKeyInfo) {
        StreamInfo.XInfoStream info = redisTemplate.opsForStream().info(streamKeyInfo.getName());
        return Map.of(
                "name",streamKeyInfo.getName(),
                "maxLen", streamKeyInfo.getMaxLen(),
                "currLen",info.getRaw().get("length"),
                "groups",info.getRaw().get("groups"),
                "last-generated-id",info.getRaw().get("last-generated-id")
        );
    }
    public List<Map> streams() {
        List<RedisMQProperties.RedisMQInfo> streams = this.redisMQProperties.getKeys();
        List<Map> streamList = streams.stream().map(streamKey -> getStreamInfo(streamKey)).collect(Collectors.toList());
        return streamList;
    }
    public StreamInfo.XInfoGroups groups(String streamKey) {
        StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(streamKey);
        return groups;
    }
    public StreamInfo.XInfoConsumers consumers(String streamKey, String group) {
        return redisTemplate.opsForStream().consumers(streamKey, group);
    }
    public Long size(String streamKey) {
        return redisTemplate.opsForStream().size(streamKey);
    }
}
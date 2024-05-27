package com.lzy.redismq.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j(topic = "redis-mq:RedisMQHelper")
public class RedisMQHelper {
    private final AtomicInteger uniqCounter = new AtomicInteger(0);

    private StringRedisTemplate stringRedisTemplate;
    private Map<String, Long> streamMap = new HashMap<>();
    private Long defMaxLen;

    public RedisMQHelper(StringRedisTemplate stringRedisTemplate, RedisMQProperties redisMQProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.streamMap.putAll(redisMQProperties.getInfos().stream().collect(Collectors.toMap(RedisMQProperties.info::getName, RedisMQProperties.info::getMaxLen, (s1, s2) -> s2)));
        this.defMaxLen = redisMQProperties.getDefMaxLen();
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
        streamMap.forEach((streamKey, streamLen) -> {
            createStream(streamKey);
        });
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
        RecordId recordId = stringRedisTemplate.opsForStream().add(streamKey, Map.of("init-test-msg", "hello-redis-stream"));
        stringRedisTemplate.opsForStream().delete(streamKey, recordId);
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
        String ret = stringRedisTemplate.opsForStream().createGroup(streamKey, group);
        log.info("redis-mq stream-group[{}-{}] initialize success", streamKey, group, ret);
        return ret;
    }

    /**
     * 获取消费者信息
     *
     * @param streamKey stream-key值
     * @param group     消费组
     * @return org.springframework.data.redis.connection.stream.StreamInfo.XInfoConsumers
     */
    public StreamInfo.XInfoConsumers queryConsumers(String streamKey, String group) {
        return stringRedisTemplate.opsForStream().consumers(streamKey, group);
    }

    /**
     * 添加Map消息
     *
     * @param streamKey stream对应的key
     * @param value     消息数据
     */
    public String sendMap(String streamKey, Map<String, Object> value) {
        String id = stringRedisTemplate.opsForStream().add(streamKey, value).getValue();
        Long maxLen = streamMap.getOrDefault(streamKey, defMaxLen);
        if (maxLen != null && maxLen != 0) {
           trim(streamKey, maxLen);
        }
        return id;
    }

    public Long trim(String streamKey, Long maxLen) {
        // MapRecord<String, String, Object> record = StreamRecords.newRecord().in(streamKey).ofMap(value);
        // Assert.notNull(record, "redis-mq stream init:Record must not be null");
        // RedisSerializer<String> stringSerializer = stringRedisTemplate.getStringSerializer();
        // ByteRecord serialize = record.serialize(stringSerializer);
        // RecordId recordId = stringRedisTemplate.getConnectionFactory().getConnection().xAdd(serialize, RedisStreamCommands.XAddOptions.maxlen(maxLen));
        Long trimCount = stringRedisTemplate.opsForStream().trim(streamKey, maxLen);
        return trimCount;
    }

    /**
     * 读取消息
     *
     * @param streamKey
     * @return java.util.List<org.springframework.data.redis.connection.stream.MapRecord < java.lang.String, java.lang.Object, java.lang.Object>>
     */
    public List<MapRecord<String, Object, Object>> read(String streamKey) {
        return stringRedisTemplate.opsForStream().read(StreamOffset.fromStart(streamKey));
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
        return stringRedisTemplate.opsForStream().acknowledge(streamKey, group, recordIds);
    }


    /**
     * 删除消息。当一个节点的所有消息都被删除，那么该节点会自动销毁
     *
     * @return java.lang.Long
     */
    public Long del(String streamKey, String... recordIds) {
        return stringRedisTemplate.opsForStream().delete(streamKey, recordIds);
    }

    /**
     * 判断是否存在streamKey
     *
     * @param streamKey
     */
    public boolean hasStream(String streamKey) {
        Boolean aBoolean = stringRedisTemplate.hasKey(streamKey);
        return Optional.ofNullable(aBoolean).orElse(Boolean.FALSE);
    }

    /**
     * 判断是否存在streamKey
     *
     * @param streamKey
     */
    public boolean hasGroup(String streamKey, String group) {
        StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(streamKey);
        boolean anyMatch = groups.stream().anyMatch(xInfoGroup -> xInfoGroup.groupName().equals(group));
        return Optional.ofNullable(anyMatch).orElse(Boolean.FALSE);
    }

}
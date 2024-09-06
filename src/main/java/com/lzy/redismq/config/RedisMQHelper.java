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
        this.streamMap.putAll(redisMQProperties.getKeys().stream().collect(Collectors.toMap(RedisMQProperties.StreamKey::getName, RedisMQProperties.StreamKey::getMaxLen, (s1, s2) -> s2)));
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
        streamMap.keySet().forEach(this::createStream);
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
        RecordId recordId = stringRedisTemplate.opsForStream().add(streamKey, value);
        return recordId!=null?recordId.getValue():null;
    }

    /**
     * 根据最大长度修剪指定stream(近似最大长度,非精确)。
     *
     * 该方法通过调用Redis的stream trim命令，来限制指定流键（streamKey）中消费组的最大长度。
     * 如果流中的消息数量超过了指定的最大长度（maxLen），则会删除最旧的消息，以保证流的长度不超过限制。
     *
     * @param streamKey 需要修剪的流键。
     * @param maxLen 指定的最大长度，超过该长度的旧消息将被删除。
     * @return 返回被删除的消息数量。
     */
    public Long trim(String streamKey, Long maxLen) {
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
        return anyMatch;
    }

    public Object pending(String streamKey, String group, Long timeout) {
        return stringRedisTemplate.opsForStream().pending(streamKey, group);
    }

}
package com.lzy.redismq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "spring.redis.stream")
public class RedisMQProperties {
    /**
     * 是否启用 redis stream
     */
    private boolean enable = false;
    /**
     * stream 队列默认最大长度
     */
    private Long defMaxLen = 5000L;
    /**
     * stream key 信息
     * name: stream key 名称
     * maxLen: stream key 长度
     */
    private List<StreamKey> keys = new ArrayList<>();



    @Data
    public static class StreamKey {
        private String name;
        private Long maxLen;
    }
}

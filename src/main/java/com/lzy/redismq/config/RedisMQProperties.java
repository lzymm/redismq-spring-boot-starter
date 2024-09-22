package com.lzy.redismq.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * RedisMQ 配置信息
 * @author: lzy
 * @date: 2024-09-12 23:59:19
 */
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
    private List<RedisMQInfo> keys = new ArrayList<>();



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisMQInfo {
        private String name;
        private Long maxLen;
    }
}

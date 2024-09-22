package com.lzy.redismq.task;

import com.lzy.redismq.config.RedisMQHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redis mq 定时任务
 */
@EnableScheduling
@Slf4j(topic = "redis-mq: task")
public class RedisMQTask {

    private static final int FIXED_RATE = 10;
    private RedisMQHelper redisMQHelper;

    public RedisMQTask(RedisMQHelper redisMQHelper) {
        this.redisMQHelper = redisMQHelper;
    }

    /**
     * pending消息处理任务
     * 方案1： 取出异常消息，手动ack，重新投递
     * 方案2：
     *
     * @return void
     */
    // @Scheduled(fixedRate = FIXED_RATE, timeUnit = TimeUnit.SECONDS)
    public void pendingMessageHandle() {
        //todo : 处理消费异常的消息
    }

    /**
     * 消息队列长度修剪
     *
     * @return void
     */
    @Scheduled(fixedRate = FIXED_RATE, timeUnit = TimeUnit.SECONDS)
    public void trimStreamLengthHandle() {
        Map<String, Long> streamMap = redisMQHelper.getStreamConfigMap();
        streamMap.forEach((streamKey, maxLen) -> {
            Map<String, Object> streamInfo = redisMQHelper.getStreamInfo(streamKey);
            Long currLen = (Long) streamInfo.get("currLen");
            maxLen = maxLen == null || maxLen <= 0 ? redisMQHelper.getDefMaxLen() : maxLen;
            if (currLen > maxLen) {
                redisMQHelper.trim(streamKey, maxLen);
            }
        });
    }
}

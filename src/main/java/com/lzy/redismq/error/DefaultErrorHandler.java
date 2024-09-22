package com.lzy.redismq.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ErrorHandler;

/**
 * StreamPollTask 获取消息或对应的listener消费消息过程中发生了异常
 *
 */
@Slf4j(topic = "redis-mq:ErrorHandler")
public class DefaultErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t) {
        log.error("redis stream exception",t);
    }
}

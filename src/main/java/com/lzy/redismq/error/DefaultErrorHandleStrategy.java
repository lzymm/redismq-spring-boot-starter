package com.lzy.redismq.error;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: TODO
 * @Author: liuzy-t
 * @Date: 2024-08-09 下午 03:31
 **/
@Slf4j(topic = "redis-mq:ErrorHandleStrategy")
public class DefaultErrorHandleStrategy implements ErrorHandleStrategy{

    public boolean exec(Throwable error) {
        log.error("consumer process message error,cancel the subscription", error);
        return true;
    }
}

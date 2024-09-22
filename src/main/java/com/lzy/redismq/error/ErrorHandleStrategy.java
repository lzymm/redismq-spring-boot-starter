package com.lzy.redismq.error;

/**
 * @Description: 错误处理侧率
 * @Author: liuzy-t
 * @Date: 2024-08-09 下午 03:31
 **/
public interface ErrorHandleStrategy {
    boolean exec(Throwable error);
}

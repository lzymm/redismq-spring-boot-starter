package com.lzy.redismq.annotation;

import com.lzy.redismq.error.DefaultErrorHandler;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.util.ErrorHandler;

import java.lang.annotation.*;
import java.util.concurrent.Executor;

@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisMQListener {
    /**
     * redis stream key
     */
    @NonNull
    String stream() default "";

    /**
     * 消费者组
     */
    @NonNull
    String group() default "" ;

    /**
     * 消费者名（数组） {C1,C2,C3}
     */
    @NonNull
    String[] names() default {};

    /**
     * 是否自动确认，默认 true
     */
    boolean autoAck() default true;

    /**
     * 每次拉去消息的数量，默认10
     */
    int perPollSize() default 10;

    /**
     * 拉取数据超时时间（s），默认2s
     * 2s拉取不到停止阻塞
     */
    int pollTimeoutSeconds() default 2;

    /**
     * 默认错误处理器
     */
    Class<? extends ErrorHandler> errorHandler() default DefaultErrorHandler.class;

    Class<? extends Executor> taskExecutor() default SimpleAsyncTaskExecutor.class;
}

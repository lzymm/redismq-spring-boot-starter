package com.lzy.redismq.exception;

public class RedisMQException extends RuntimeException {

    public RedisMQException(Exception e) {
        super(e);
    }
    public RedisMQException(String msg,Exception e) {
        super(msg,e);
    }
}

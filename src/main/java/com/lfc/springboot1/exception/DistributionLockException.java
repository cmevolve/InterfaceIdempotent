package com.lfc.springboot1.exception;

/**
 * 〈一句话功能简述〉<br>
 * 分布式锁异常
 *
 * @author lfc
 * @create 2019/11/14 21:42
 * @since 1.0.0
 */
public class DistributionLockException extends RuntimeException{
    public DistributionLockException(String msg){
        super(msg);
    }

    public DistributionLockException() {
        super();
    }
}
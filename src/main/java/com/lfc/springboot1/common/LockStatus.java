package com.lfc.springboot1.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author lfc
 * @create 2019/11/16 10:18
 * @since 1.0.0
 */
@Getter
@Setter
public class LockStatus<T> {
    /**
     * 提示代码
     */
    private int code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 加锁的token
     */
    private String token;

    /**
     * 承接接口数据
     */
    private T data;


    /**
     * 加锁成功返回结果
     *
     * @param token
     */
    public static  LockStatus lockUpSuccess(String token) {
        return success(ConstantEnum.P_200.getCode(),ConstantEnum.P_200.getMsg(), token);
    }

    /**
     * 解锁成功返回结果
     *
     */
    public static  LockStatus unLockSuccess() {
        return success(ConstantEnum.P_201.getCode(),ConstantEnum.P_201.getMsg(), null);
    }

    /**
     * 加锁失败返回结果
     */
    public static LockStatus lockUpFailed() {
        return  failed(ConstantEnum.E_500.getCode(),ConstantEnum.E_500.getMsg(), null);
    }

    /**
     * 解锁失败返回结果 当前请求未加锁
     */
    public static LockStatus unLockFailed() {
        return failed(ConstantEnum.E_501.getCode(),ConstantEnum.E_501.getMsg(), null);
    }

    /**
     * 解锁失败返回结果 当前请求不是加锁的请求
     */
    public static LockStatus unLockFailed(String token) {
        return failed(ConstantEnum.E_502.getCode(),ConstantEnum.E_502.getMsg(), token);
    }

    /**
     * 失败返回结果
     * @param errMsg 错误信息
     */
    public static <T> LockStatus failed(int code,String errMsg,T data) {
        return new LockStatus(code, errMsg, null,data);
    }

    public static <T> LockStatus retFailed(T data) {
        return failed(ConstantEnum.E_505.getCode(),ConstantEnum.E_505.getMsg(),data);
    }

    /**
     * 成功返回结果
     *
     * @param token
     */
    public static  LockStatus success(int code,String msg,String token) {
        return new LockStatus(code,msg, token);
    }

    /**
     * 接口操作返回结果
     * @param data
     * @param <T>
     * @return
     */
    public static  <T> LockStatus <T> retSuccess(T data) {
        return new LockStatus(data);
    }


    private LockStatus(int code, String message, String token) {
        this.code = code;
        this.msg = message;
        this.token = token;
    }

    private LockStatus(int code, String message, String token,T data) {
        this.code = code;
        this.msg = message;
        this.token = token;
        this.data = data;
    }

    private LockStatus() {
    }

    private LockStatus(T data) {
        this.data = data;
    }
}
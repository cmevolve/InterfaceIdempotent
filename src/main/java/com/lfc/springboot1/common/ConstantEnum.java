package com.lfc.springboot1.common;

import lombok.Getter;

@Getter
public enum ConstantEnum {
    P_200(200 , "加锁成功!"),
    P_201(201 , "解锁成功!"),
    E_500(500 , "加锁失败,当前请求已加锁"),
    E_501(501 , "解锁失败，当前请求未加锁"),
    E_502(502 , "解锁失败，当前请求不是加锁请求"),
    E_505(505 , "数据处理异常!");

    /**
     * 提示代码
     */
    private int code;

    /**
     * 提示信息
     */
    private String msg;

    ConstantEnum(){}


    ConstantEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}

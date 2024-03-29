package com.lfc.springboot1.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用分布式锁
 *
 * @author lfc
 * @create 2019/11/14 16:34
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterfaceIdempotence {
    String module() default "";
    //有效时间
    int expireTime() default 50000;
}
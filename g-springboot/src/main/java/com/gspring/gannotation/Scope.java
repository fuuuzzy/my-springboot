package com.gspring.gannotation;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-08 13:56
 * @Description Bean作用域注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {
    /**
     * 作用范围
     * prototype 原型
     * singleton 单例
     */
    String value();
}

package com.gspring.mybaties.annotation;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-19 13:48
 * @Description 查询注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Select {

    /**
     * sql语句
     */
    String value();
}

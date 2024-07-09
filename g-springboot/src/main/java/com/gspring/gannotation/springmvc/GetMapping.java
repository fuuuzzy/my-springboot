package com.gspring.gannotation.springmvc;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-08 13:56
 * @Description Get请求注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GetMapping {

    /**
     * 路径
     */
    String value();
}

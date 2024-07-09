package com.gspring.gannotation.springmvc;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-02-24 23:06
 * @Description Post请求注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostMapping {

    /**
     * 路径
     */
    String value();
}

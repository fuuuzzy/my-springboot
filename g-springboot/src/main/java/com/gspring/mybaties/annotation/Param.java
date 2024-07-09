package com.gspring.mybaties.annotation;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-19 18:40
 * @Description 属性注解
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    /**
     * BeanName
     */
    String value();
}

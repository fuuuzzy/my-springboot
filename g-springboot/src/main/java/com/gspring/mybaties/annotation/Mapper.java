package com.gspring.mybaties.annotation;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-19 18:40
 * @Description mapper注解
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapper {
    /**
     * BeanName
     */
    String value() default "";
}

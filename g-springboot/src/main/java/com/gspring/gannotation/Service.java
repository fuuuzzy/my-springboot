package com.gspring.gannotation;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-08 13:56
 * @Description 服务注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Service {

    /**
     * bean名称
     */
    String value() default "";
}

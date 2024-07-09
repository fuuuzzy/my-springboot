package com.gspring.gannotation.springmvc;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-08 13:56
 * @Description 控制器注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Controller {

    /**
     * 路径
     */
    String value();

    /**
     * bean名称
     */
    String name() default "";

}

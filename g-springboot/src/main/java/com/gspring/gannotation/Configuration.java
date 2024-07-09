package com.gspring.gannotation;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-10 21:06
 * @Description 配置类注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {

    String value() default "";
}

package com.gspring.gannotation;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-09 20:28
 * @Description bean注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Bean {

    String value() default "";

}

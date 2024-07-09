package com.gspring.gannotation.springmvc;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-02-25 0:03
 * @Description Path请求注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathVariable {

    /**
     * 值
     */
    String value();
}

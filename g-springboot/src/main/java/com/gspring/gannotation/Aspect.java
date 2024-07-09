package com.gspring.gannotation;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-08 13:56
 * @Description AOP切面注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Aspect {
   
}

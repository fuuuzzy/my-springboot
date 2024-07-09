package com.gspring.gannotation;

import com.gspring.boot.autoconfiguration.SelectImportClass;

import java.lang.annotation.*;

/**
 * @author xiangGang
 * @date 2023-01-09 19:32
 * @Description 启动注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(SelectImportClass.class)
public @interface MySpringBootApplication {
}

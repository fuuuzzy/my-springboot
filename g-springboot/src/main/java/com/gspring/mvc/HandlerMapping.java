package com.gspring.mvc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author xiangGang
 * @date 2022-07-09 1:19
 * @Description 地址映射实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandlerMapping {

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * controller实例对象
     */
    private Object target;

    /**
     * controller实例对象的某个方法
     */
    private Method method;

    /**
     * 记录方法的参数
     */
    private Map<Integer, String> methodParams;

    private List<String> urlParams;

}

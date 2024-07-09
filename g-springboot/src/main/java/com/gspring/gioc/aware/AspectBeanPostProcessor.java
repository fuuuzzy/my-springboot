package com.gspring.gioc.aware;

import com.gspring.gannotation.Point;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

/**
 * @author xiangGang
 * @date 2023-01-08 21:53
 * @Description 切面处理器
 */
public class AspectBeanPostProcessor implements BeanPostProcessor {

    private List<Class<?>> aspectList;
    private Set<String> pointList;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    public void setAspectList(List<Class<?>> aspectList) {
        this.aspectList = aspectList;
    }

    public void setPointList(Set<String> pointList) {
        this.pointList = pointList;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (true) {
            return bean;
        }
        return Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {
            if (method.isAnnotationPresent(Point.class) && pointList.contains(method.getAnnotation(Point.class).value())) {
                System.out.println("切面前置通知");
                Object invoke = method.invoke(bean, args);
                System.out.println("切面后置通知");
                return invoke;
            }
            return method.invoke(bean, args);
        });
    }
}




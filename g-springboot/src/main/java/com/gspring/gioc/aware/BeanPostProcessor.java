package com.gspring.gioc.aware;

/**
 * @author xiangGang
 * @date 2023-01-08 21:03
 * @Description 初始化bean前后加工 每个bean的创建都会调用一次
 */
public interface BeanPostProcessor {

    /**
     * 初始化前处理
     *
     * @param bean     处理对象
     * @param beanName 对象名
     * @return {@link Object} 处理后的对象
     */
    Object postProcessBeforeInitialization(Object bean, String beanName);

    /**
     * 初始化后处理
     *
     * @param bean     处理对象
     * @param beanName 对象名
     * @return {@link Object} 处理后的对象
     */
    Object postProcessAfterInitialization(Object bean, String beanName);
}

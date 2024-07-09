package com.gspring.gioc.aware;

/**
 * @author xiangGang
 * @date 2023-01-08 20:24
 * @Description BeanNameAware回调 创建bean时会调用一次
 */
public interface BeanNameAware {
    /**
     * 设置BeanName
     *
     * @param beanName Bean名称
     */
    void setBeanName(String beanName);
}

package com.gspring.gioc.aware;

/**
 * @author xiangGang
 * @date 2023-01-08 20:44
 * @Description 初始化Bean回调
 */
public interface InitializingBean {

    /**
     * 初始化回调
     */
    void afterPropertiesSet();
}

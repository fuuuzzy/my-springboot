package com.gspring.gioc;

/**
 * @author xiangGang
 * @date 2023-01-08 15:57
 * @Description Bean描述类
 */
public class BeanDefinition {

    /**
     * bean类型
     */
    private Class<?> clazz;
    /**
     * 作用域
     */
    private String scope;

    /**
     * 代理类
     */
    private Object proxyObject;

    /**
     * 是否有代理类
     */
    private Boolean isProxy = false;

    public Object getProxyObject() {
        return proxyObject;
    }

    public void setProxyObject(Object proxyObject) {
        this.proxyObject = proxyObject;
    }

    public Boolean getProxy() {
        return isProxy;
    }

    public void setProxy(Boolean proxy) {
        isProxy = proxy;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}

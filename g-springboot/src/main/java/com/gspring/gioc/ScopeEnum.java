package com.gspring.gioc;

/**
 * @author xiangGang
 * @date 2023-01-08 16:23
 * @Description 作用域枚举
 */
public enum ScopeEnum {
    /**
     * 单例
     */
    SINGLETON("singleton"),
    /**
     * 原型
     */
    PROTOTYPE("prototype");

    private final String value;

    ScopeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

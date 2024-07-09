package com.gspring.mybaties.annotation.parameterhandle;

/**
 * @author xiangGang
 * @date 2023-01-27 13:42
 * @Description 参数映射
 */
public class ParameterMapping {

    /**
     * #{}中的属性
     */
    private String property;

    public ParameterMapping(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}

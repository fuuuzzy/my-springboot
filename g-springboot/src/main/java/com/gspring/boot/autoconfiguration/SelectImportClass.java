package com.gspring.boot.autoconfiguration;

/**
 * @author xiangGang
 * @date 2023-01-11 20:55
 * @Description 选择导入类接口
 */
public interface SelectImportClass {

    /**
     * 获取需要导入的类
     *
     * @return {@link Class} 类
     */
    Class<?>[] selectImportClass();
}

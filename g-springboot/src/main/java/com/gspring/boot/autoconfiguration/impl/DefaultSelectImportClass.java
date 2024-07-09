package com.gspring.boot.autoconfiguration.impl;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.gspring.boot.autoconfiguration.SelectImportClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author xiangGang
 * @date 2023-01-11 20:55
 * @Description 默认的导入类选择器
 */
public class DefaultSelectImportClass implements SelectImportClass {

    private static final Log LOG = LogFactory.getLog(DefaultSelectImportClass.class);

    /**
     * 获取需要导入的类
     *
     * @return {@link Class} 类
     */
    @Override
    public Class<?>[] selectImportClass() {

        InputStream inputStream = DefaultSelectImportClass.class.getClassLoader().getResourceAsStream("autoconfigurationProperties.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e + "autoconfigurationProperties.properties not found");
        }
        List<Class<?>> classList = new ArrayList<>();
        if (!properties.isEmpty()) {
            properties.forEach((k, v) -> {
                try {
                    classList.add(Class.forName(v.toString()));
                } catch (ClassNotFoundException e) {
                    LOG.info("过滤掉不需要的类");
                }
            });
        }
        return classList.toArray(new Class[0]);
    }
}

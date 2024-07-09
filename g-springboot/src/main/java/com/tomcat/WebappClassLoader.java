package com.tomcat;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author xiangGang
 * @date 2023-01-18 15:40
 * @Description 自定义类加载器
 */
public class WebappClassLoader extends URLClassLoader {

    public WebappClassLoader(URL[] urls) {
        super(urls);
    }
}

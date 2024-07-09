package com.gspring.boot;

import com.gspring.AnnotationApplicationContext;
import com.gspring.boot.webserver.WebServer;

import java.util.Map;

/**
 * @author xiangGang
 * @date 2023-01-09 0:10
 * @Description 启动类
 */
public class SpringBootApplication {

    private SpringBootApplication() {
    }

    /**
     * springBoot 启动类
     *
     * @param configClass 主键资源
     */
    public static void run(Class<?> configClass) {
        //创建容器
        AnnotationApplicationContext applicationContext = new AnnotationApplicationContext();
        applicationContext.register(configClass);
        applicationContext.refresh();
        WebServer webServer;
        try {
            webServer = getWebServer(applicationContext);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        webServer.start(applicationContext);
    }

    /**
     * 获取服务器
     *
     * @param applicationContext 配置类
     * @return {@link  WebServer}  服务器
     * @throws IllegalAccessException 异常
     */
    private static WebServer getWebServer(AnnotationApplicationContext applicationContext) throws IllegalAccessException {
        Map<String, WebServer> webServer = applicationContext.getBeanByType(WebServer.class);

        if (webServer == null || webServer.isEmpty()) {
            throw new RuntimeException("webServer is empty");
        }
        if (webServer.size() > 1) {
            throw new IllegalAccessException("webServer is more than one");
        }
        //返回唯一个webServer
        return webServer.values().iterator().next();
    }
}

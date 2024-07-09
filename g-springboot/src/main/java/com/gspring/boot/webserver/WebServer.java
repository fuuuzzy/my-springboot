package com.gspring.boot.webserver;

import com.gspring.AnnotationApplicationContext;

/**
 * @author xiangGang
 * @date 2023-01-09 19:52
 * @Description web服务器接口
 */
public interface WebServer {

    /**
     * 启动服务器
     *
     * @param annotationApplicationContext 上下文
     */
    void start(AnnotationApplicationContext annotationApplicationContext);

    /**
     * 停止服务器
     */
    void stop();

    /**
     * 获取端口
     *
     * @return 端口
     */
    int getPort();
}

package com.gspring.boot.webserver.impl;

import com.gspring.AnnotationApplicationContext;
import com.gspring.boot.webserver.WebServer;

/**
 * @author xiangGang
 * @date 2023-01-09 19:55
 * @Description jetty服务器
 */
public class JettyWebServer implements WebServer {
    @Override
    public void start(AnnotationApplicationContext annotationApplicationContext) {
        System.out.println("jetty start");
    }

    @Override
    public void stop() {
        System.out.println("jetty stop");
    }

    @Override
    public int getPort() {
        return 0;
    }
}

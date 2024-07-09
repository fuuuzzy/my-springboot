package com.gspring.boot.webserver.impl;

import com.gspring.AnnotationApplicationContext;
import com.gspring.boot.webserver.WebServer;
import com.gspring.mvc.MyDispatcherServlet;
import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;

/**
 * @author xiangGang
 * @date 2023-01-09 19:53
 * @Description Tomcat服务器
 */
public class TomcatWebServer implements WebServer {

    private Tomcat tomcat;

    /**
     * 启动
     */
    @Override
    public void start(AnnotationApplicationContext annotationApplicationContext) {
        tomcat = new Tomcat();
        Server server = tomcat.getServer();
        Service service = server.findService("Tomcat");
        Connector connector = new Connector();
        connector.setPort(8088);
        Engine engine = new StandardEngine();
        engine.setDefaultHost("localhost");
        StandardHost host = new StandardHost();
        host.setName("localhost");
        Context context = new StandardContext();
        String contextPath = "";
        context.setPath(contextPath);
        context.addLifecycleListener(new Tomcat.FixContextListener());

        host.addChild(context);
        engine.addChild(host);
        service.setContainer(engine);
        service.addConnector(connector);
        tomcat.addServlet(contextPath, "dispatcherServlet", new MyDispatcherServlet(annotationApplicationContext));
        context.addServletMappingDecoded("/*", "dispatcherServlet");
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPort() {
        return tomcat.getServer().getPort();
    }
}

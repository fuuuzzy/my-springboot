package com.gspring.boot.webserver;

import com.gspring.boot.webserver.impl.JettyWebServer;
import com.gspring.boot.webserver.impl.TomcatWebServer;
import com.gspring.gannotation.Bean;
import com.gspring.gannotation.Configuration;

/**
 * @author xiangGang
 * @date 2023-01-09 20:28
 * @Description web服务器自动配置类
 */
@Configuration
public class WebServerAutoConfiguration {

    @Bean
    public WebServer webServer() {
        return new JettyWebServer();
    }

    @Bean
    public WebServer tomcatServer() {
        return new TomcatWebServer();
    }

}

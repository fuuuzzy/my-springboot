package com.tomcat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiangGang
 * @date 2023-01-18 14:10
 * @Description Tomcat服务器
 */
public class Tomcat {

    /**
     * 线程池
     */
    private static final ExecutorService POOL = new ThreadPoolExecutor(200,
            300, 10,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(10),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                POOL.execute(new SocketProcess(serverSocket.accept()));
            }
        } catch (IOException e) {
            throw new RuntimeException("start tomcat error");
        }
    }

    public static void main(String[] args) {
        new Tomcat().start();
    }
}


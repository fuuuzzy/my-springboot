package com.tomcat;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * @author xiangGang
 * @date 2023-01-18 14:46
 * @Description 处理TCP请求
 */
public class SocketProcess implements Runnable {

    private final Socket socket;

    public SocketProcess(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        processSocket();
    }

    public void processSocket() {

        try (InputStream inputStream = socket.getInputStream()) {

            byte[] bytes = new byte[1024];
            inputStream.read(bytes);

        } catch (IOException e) {
            throw new RuntimeException("get inputStream error");
        }

    }
}

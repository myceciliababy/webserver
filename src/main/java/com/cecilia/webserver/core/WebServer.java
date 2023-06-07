package com.cecilia.webserver.core;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * WebServer主类
 * WebServer项目是一个网络容器,模拟开源的Tomcat服务器的功能
 * 其维护了若干个webapp(网络应用),并基于TCP协议与客户端(通常是浏览器)建立连接,并使用HTTP协议与客户端进行交互,使其可以访问维护的这些webapp下的资源
 * <p>
 * webapp网络应用通常包含一组页面、若干其他静态资源(图片,样式文件,脚本文件,其他素材)和业务逻辑代码.我们俗称的一个网站其实就是一个网络应用
 */
public class WebServer {

    private ServerSocket server;

    public WebServer() {
        try {
            System.out.println("正在启动服务端...");
            server = new ServerSocket(8088);
            System.out.println("服务端启动完毕！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            while (true) {
                System.out.println("等待客户端连接...");
                Socket socket = server.accept();
                System.out.println("一个客户端连接了！");

                // 启动一个线程处理该客户端的交互
                ClientHandler handler = new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.start();
    }

}

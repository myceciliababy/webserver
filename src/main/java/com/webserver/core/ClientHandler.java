package com.webserver.core;

import com.webserver.http.EmptyRequestException;
import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import com.webserver.service.LoginServlet;
import com.webserver.service.RegServlet;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

/**
 * 该线程任务负责与指定客户端交互
 * HTTP协议中交互方式分为一问一答,因此当前处理模式分为三步:
 * 1.解析请求
 * 2.处理请求
 * 3.响应客户端
 */
public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("\n开始处理交互...");

            // 1.解析请求
            System.out.println("\tClientHandler:第一步:开始解析请求...");
            // 实例化HttpRequest的过程就是解析请求的过程
            HttpRequest request = new HttpRequest(socket);
            System.out.println("\tClientHandler:第一步:完成解析请求...\n");

            // 2.处理请求
            System.out.println("\tClientHandler:第二步:开始处理请求...");

            // 2.1创建响应对象
            HttpResponse response = new HttpResponse(socket);

            // 2.2根据请求获取请求路径中抽象路径部分
            String path = request.getRequestURI();

            // 2.3根据抽象路径检查是否为请求业务
            if ("/myweb/reg".equals(path)) {
                // 处理注册操作
                RegServlet servlet = new RegServlet();
                servlet.service(request, response);
            } else if ("/myweb/login".equals(path)) {
                //处理登陆操作
                LoginServlet servlet = new LoginServlet();
                servlet.service(request, response);
            } else {
                // 不是则去webapps目录下寻找资源
                File file = new File("./webapps" + path);
                // 若响应的资源不存在,则响应通用的错误页面
                if (!file.exists()) {
                    file = new File("./webapps/root/404.html");
                    response.setStatusCode(404);
                    response.setStatusReson("NOT FOUND");
                }
                // 设置需要响应的内容
                response.setFile(file);
            }
            System.out.println("\tClientHandler:第二步:完成处理请求...\n");

            // 3.响应客户端
            System.out.println("\tClientHandler:第三步:开始响应客户端...");
            // 响应客户端
            response.flush();
            System.out.println("\tClientHandler:第三步:完成响应客户端...");

            System.out.println("本次交互完成...");
        } catch (EmptyRequestException e) {
            // 出现了空请求
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 处理完成后socket应该关闭,模拟HTTP1.0协议一次请求一次响应,响应完成后断开连接
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

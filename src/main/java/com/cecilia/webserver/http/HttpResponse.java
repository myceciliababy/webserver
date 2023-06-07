package com.cecilia.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 响应对象,该对象的每个实例用于处理客户端发送请求行服务端发送给客户端的HTTP响应
 * 一个响应内容由三部分构成:状态行、响应头、响应正文
 */
public class HttpResponse {

    // 状态行相关信息
    private int statusCode = 200; // 状态码,默认200
    private String statusReson = "OK"; // 状态描述,默认"OK"

    // 响应头相关信息
    private Map<String, String> headers = new HashMap<>();

    // 响应正文相关信息
    private File file;

    // 和连接相关的信息
    private Socket socket;
    private OutputStream out;

    /**
     * 实例化HttpResponse同时将Socket传入响应对象通过它将响应内容发送给客户端
     *
     * @param socket 本次响应与客户端进行通信的Socket对象
     */
    public HttpResponse(Socket socket) {
        this.socket = socket;
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将需要响应的内容以一个标准的HTTP响应格式发送给客户端
     */
    public void flush() {
        try {
            // 1.发送状态行
            sendStatusLine();

            // 2.发送响应头
            sendHeaders();

            // 3.发送响应正文部分
            sendContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送状态行
     */
    private void sendStatusLine() {
        try {
            String line = "HTTP/1.1" + " " + statusCode + " " + statusReson;
            out.write(line.getBytes("ISO8859-1"));
            out.write(13);
            out.write(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送响应头
     */
    private void sendHeaders() {
        try {
            // 遍历headers中的响应头和值
            Set<Entry<String, String>> entrySet = headers.entrySet();
            for (Entry entry : entrySet) {
                String line = entry.getKey() + ": " + entry.getValue();
                out.write(line.getBytes("ISO8859-1"));
                out.write(13);
                out.write(10);
            }
            // 单独发送CRLF表示响应头部分
            out.write(13);
            out.write(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送响应正文
     */
    private void sendContent() {
        if (file == null) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            int len = -1;
            byte[] data = new byte[1024 * 8];
            while ((len = fis.read(data)) != -1) {
                out.write(data, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }

    /**
     * 添加响应正文的实体文件
     * 添加的同时会根据该文件的名字分析类型,并设置需要发送的相应头Content-Type与Content-Length
     *
     * @param file
     */
    public void setFile(File file) {
        // 以下步骤用于获取文件的类型
        String fileName = file.getName();
        // 找到文件名中最后一个"."的位置
        int index = fileName.lastIndexOf('.');
        // 截取文件的后缀名
        String ext = fileName.substring(index + 1);

        // 根据文件类型获取对应的Contnet-Type值
        String type = HttpContext.getMimeType(ext);

        // 设置需要发送的响应头
        putHeader("Content-Type", type);
        putHeader("Content-Length", file.length() + "");

        this.file = file;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusReson(String statusReson) {
        this.statusReson = statusReson;
    }

    /**
     * 添加一个响应头
     *
     * @param name  响应头的名字
     * @param value 响应头的值
     */
    public void putHeader(String name, String value) {
        this.headers.put(name, value);
    }

}

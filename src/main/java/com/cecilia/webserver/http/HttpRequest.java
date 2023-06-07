package com.cecilia.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象
 * 该类的每一个实例用于处理客户端发送过来的Http请求内容
 * 每个请求由三部分构成:请求行、消息头、消息正文
 *
 * @author 黄先生
 * @create 2020/2/25 - 20:43
 */
public class HttpRequest {

    // 请求行相关信息
    private String method; //请求方式
    private String uri; //抽象路径
    private String requestURI; // uri中请求部分("?"左侧内容)
    private String queryString; // uri中参数部分("?"右侧内容)
    private Map<String, String> parameters = new HashMap<>(); // 每一个参数和其对应得值
    private String protocol; //协议版本

    // 消息头
    private Map<String, String> headers = new HashMap<>();

    // 消息正文

    //  和连接相关的信息
    private Socket socket;
    private InputStream in;

    /**
     * 构造方法,实例化的过程就是解析请求的过程
     *
     * @param socket 本次请求与客户端进行通信的Socket对象
     */
    public HttpRequest(Socket socket) throws EmptyRequestException {
        this.socket = socket;
        try {
            this.in = socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 1.解析请求行
        parseRequestLine();

        // 2.解析消息头
        parseHeaders();

        // 3.解析消息正文
        parseContent();
    }

    /**
     * 解析请求行
     */
    private void parseRequestLine() throws EmptyRequestException {
        System.out.println("\t\tHttpRequest:第一步:开始解析请求行...");
        try {
            // 1.读取第一行,获取请求行
            String line = readLine();

            // 解决浏览器自动发送空请求的问题
            if ("".equals(line)) {
                throw new EmptyRequestException();
            }

            // 2.将结果按照空格拆分为三部分,正则表达式中"\s"表示空格
            String[] data = line.split("\\s+");

            // 3.分别将三部分赋值给method、uri、protocol
            this.method = data[0];
            // 由于浏览器会自动发送空请求,所以不经过处理会出现下标越界异常
            this.uri = data[1];
            this.protocol = data[2];

            // 测试
            System.out.println("\t\t\tmethod=" + method);
            System.out.println("\t\t\turi=" + uri);
            System.out.println("\t\t\tprotocol=" + protocol);

            // 进一步解析uri
            parseURI();
        } catch (EmptyRequestException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\t\tHttpRequest:第一步:完成解析请求行...\n");
    }

    /**
     * 进一步解析uri
     * 因为uri可能存在有参数的情况,进一步解析获取其中的参数
     */
    private void parseURI() {
        System.out.println("\t\t\tHttpRequest:进一步解析uri...");
        /*
            由于uri有两种情况,所以处理需要进行区分:
            1.不含参数时:直接将uri的值赋值给requestURI即可
            2.含有参数时:
                1.按照"?"进行拆分uri,将"?"左侧内容赋值给requestURI,将"?"右侧内容赋值给queryString
                2.再将queryString按照"&"拆分出每一组参数名和参数值
                  并且再此基础上再按照"="拆分出名和值,将参数名作为key,参数值作为value保存到parameters中
         */
        if (!uri.contains("?")) {
            requestURI = uri;
        } else {
            String[] data = uri.split("\\?");
            requestURI = data[0];
            // 若仅仅有一个?,后面没有参数,则不执行下面操作
            if (data.length > 1) {
                // 将参数整个部分进行保存
                queryString = data[1];
                // 继续拆分出每一组参数名和参数值
                data = queryString.split("&");
                for (int i = 0; i < data.length; i++) {
                    String[] arr = data[i].split("=");
                    if (arr.length > 1) {
                        // 如果是正常情况,则将"="两边的内容进行保存
                        parameters.put(arr[0], arr[1]);
                    } else {
                        // 如果是"uername=&"这种情况,则参数值为null
                        parameters.put(arr[0], null);
                    }
                }
            }
        }

        // 测试
        System.out.println("\t\t\t\trequestURI=" + requestURI);
        System.out.println("\t\t\t\tqueryString=" + queryString);
        System.out.println("\t\t\t\tparameters=" + parameters);

        System.out.println("\t\t\tHttpRequest:完成进一步解析uri...");
    }

    /**
     * 解析消息头
     */
    private void parseHeaders() {
        System.out.println("\t\tHttpRequest:第二步:开始解析消息头...");
        try {
            while (true) {
                String line = readLine();
                if ("".equals(line)) {
                    break;
                }
                // 消息头中每一行都以": "(冒号空格)隔开
                String[] data = line.split(": ");

                // 将拆分后结果中消息头的名字作为key,消息头的值作为value放入headers中
                headers.put(data[0], data[1]);
            }

            // 测试
            for (Map.Entry entry : headers.entrySet()) {
                System.out.println("\t\t\t" + entry.getKey() + ": " + entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\t\tHttpRequest:第二步:完成解析消息头...\n");
    }

    /**
     * 解析消息正文
     */
    private void parseContent() {
        System.out.println("\t\tHttpRequest:第三步:开始解析消息正文...");
        System.out.println("\t\tHttpRequest:第三步:完成解析消息正文...");
    }

    /**
     * 通过输入流读取读取Http请求一行数据(以CRLF结尾),并返回
     */
    private String readLine() throws IOException {
        String line = "";
        int c1 = -1; //表示上次读取到的内容
        int c2 = -1; //表示本次读取到的内容
        StringBuilder builder = new StringBuilder();
        while ((c2 = in.read()) != -1) {
            // 如果上次读取到的是回车符,本次读取到的是换行符,则表示本行读取完毕
            if (c1 == 13 && c2 == 10) {
                break;
            }
            // 将读取到的一个字节转换为字符并加入拼接的字符串中
            builder.append((char) c2);
            c1 = c2;
        }
        // 第一行读取完毕后,末尾会有一个回车符
        line = builder.toString().trim();
        return line;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * 获取指定消息头对应的值
     *
     * @param name 消息头的名称
     * @return
     */
    public String getHeaders(String name) {
        return headers.get(name);
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getParameters(String name) {
        return parameters.get(name);
    }
}

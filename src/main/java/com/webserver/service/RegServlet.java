package com.webserver.service;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * 当前类用于处理用户注册业务
 *
 * @author 黄先生
 * @create 2020/3/5 - 16:21
 */
public class RegServlet {

    /**
     * 1.获取用户在注册页面上输入的信息(表单提交上来的数据)
     * 2.将注册信息写入文件user.dat中
     * 3.响应注册结果页面给客户端(注册成功或失败)
     *
     * @param req HTTP协议请求对象
     * @param res HTTP协议响应对象
     */
    public void service(HttpRequest req, HttpResponse res) {
        System.out.println("\t\tRegServlet:开始处理注册业务...");

        String username = req.getParameters("username");
        String password = req.getParameters("password");
        String nick = req.getParameters("nick");
        int age = Integer.parseInt(req.getParameters("age"));

        // 测试
        System.err.println("\t\t\tusername=" + username);
        System.err.println("\t\t\tpassword=" + password);
        System.err.println("\t\t\tnick=" + nick);
        System.err.println("\t\t\tage=" + age);

        try {
            RandomAccessFile raf = new RandomAccessFile("./src/main/resources/user.dat", "rw");

            // 如果用户名已存在,则直接响应注册失败页面
            if (queryUsername(raf, username)) {
                File file = new File("./webapps/myweb/reg_fail.html");
                res.setFile(file);
            } else {
                // 若用户名不存在,则执行插入操作,先将指针移动到文件末尾,以便追加新纪录
                raf.seek(raf.length());
                writeString(raf, username);
                writeString(raf, password);
                writeString(raf, nick);
                raf.writeInt(age);
                raf.close();
                File file = new File("./webapps/myweb/reg_success.html");
                res.setFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\t\tRegServlet:处理注册业务完毕...");
    }

    public void writeString(RandomAccessFile raf, String str) throws IOException {
        byte[] data = str.getBytes("utf-8");
        // 将字符串转换成字节,并进行扩充到32个字节
        data = Arrays.copyOf(data, 32);
        raf.write(data);
    }

    public boolean queryUsername(RandomAccessFile raf, String username) throws IOException {
        int length = (int) raf.length() / 100;
        if (length == 0) {
            return false;
        }

        byte[] data = new byte[96];
        int len = -1;
        int i = 0;
        while ((len = raf.read(data)) != -1) {
            String name = new String(data, 0, 32, "utf-8").trim();
            if (name.equals(username)) {
                return true;
            }
        }
        return false;
    }

}

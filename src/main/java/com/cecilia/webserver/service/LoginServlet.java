package com.cecilia.webserver.service;

import com.cecilia.webserver.http.HttpRequest;
import com.cecilia.webserver.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LoginServlet {

    public void service(HttpRequest req, HttpResponse res) {
        System.out.println("\t\tLoginServlet:开始处理注册业务...");

        String username = req.getParameters("username");
        System.err.println(username);
        String password = req.getParameters("password");
        System.err.println(password);

        try {
            RandomAccessFile raf = new RandomAccessFile("./src/main/resources/user.dat", "r");
            if (check(raf, username, password)) {
                res.setFile(new File("./webapps/myweb/login_success.html"));
            } else {
                res.setFile(new File("./webapps/myweb/login_fail.html"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\t\tLoginServlet:处理注册业务完毕...");
    }

    public boolean check(RandomAccessFile raf, String username, String password) throws IOException {
        int length = (int) raf.length() / 100;
        if (length == 0) {
            return false;
        }

        byte[] data = new byte[96];
        int len = -1;
        int i = 0;
        while ((len = raf.read(data)) != -1) {
            String name = new String(data, 0, 32, "utf-8").trim();
            String pwd = new String(data, 32, 32, "utf-8").trim();
            System.err.println(name);
            System.err.println(pwd);
            if (name.equals(username) && pwd.equals(password)) {
                return true;
            }
        }
        return false;
    }

}

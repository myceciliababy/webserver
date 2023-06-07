package com.cecilia.webserver.http;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类保存所有有关HTTP协议规定的内容
 */
public class HttpContext {

    private static Map<String, String> mimeMapping = new HashMap<>();

    // 初始化所有静态资源
    static {
        initMimeMapping();
    }

    /**
     * 初始化mimeMapping
     * 1.使用dom4j解析"conf/web.xml"文档
     * 2.将根标签下所有名为"mime-mapping"的子标签获取
     * 3.遍历这些"mime-mapping"标签,并将其子标签中"<extension>"中间的文本作为key,"mime-type"中间的文本作为value,放入到mimeMapping中
     */
    private static void initMimeMapping() {
        /*
        mimeMapping.put("html", "text/html");
        mimeMapping.put("css", "text/css");
        mimeMapping.put("js", "application/javascript");
        mimeMapping.put("png", "image/png");
        mimeMapping.put("gif", "image/gif");
        mimeMapping.put("jpg", "image/jpeg");
         */

        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(new File("./src/main/resources/web.xml"));
            Element root = doc.getRootElement();
            List<Element> list = root.elements("mime-mapping");
            for (Element e : list) {
                String key = e.elementTextTrim("extension");
                String value = e.elementTextTrim("mime-type");
                mimeMapping.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据资源后缀名获取对应的Content-Type值
     *
     * @param ext 文件后缀名
     * @return
     */
    public static String getMimeType(String ext) {
        return mimeMapping.get(ext);
    }

    public static void main(String[] args) {
        System.out.println(mimeMapping.size());
        String type = getMimeType("js");
        System.out.println(type);
    }
}

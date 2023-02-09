package top.yqingyu.httpserver.web.controller;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.httpserver.annotation.QyController;
import top.yqingyu.httpserver.compoment.*;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.LocalDateTimeUtil;

import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.DemoController
 * @description
 * @createTime 2022年09月15日 14:09:00
 */
@QyController(path = "qy_demo")
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
    static final AtomicLong l = new AtomicLong();

    @QyController(path = "testSessionAndCookie", method = {HttpMethod.GET})
    public String demo3(Request req, Response resp) {

        Session session = req.getSession();
        session.set("name", "yyj");
        session.set("age", "101");

        Cookie cookie = new Cookie("test", LocalDateTimeUtil.HTTP_FORMATTER.format(ZonedDateTime.now()));
        resp.addCookie(cookie);

        cookie.setMaxAge(60 * 60);

        return "SessionAndCookie-Test";
    }

    @QyController(path = "get_demo", method = {HttpMethod.GET})
    public String demo4(Request req) {

        Session session = req.getSession();
        String name = (String) session.get("name");
        String age = (String) session.get("age");

        String test = req.getCookie("test");
        test = "cookie: " + age + test + "<br>Session: " + name + LocalDateTimeUtil.HTTP_FORMATTER.format(ZonedDateTime.now());
        logger.info(test);
        return test;
    }


    @QyController(path = "demo1", method = {HttpMethod.GET})
    public String demo1(Request r, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(name).append(" Welcome to Qy Framework  ~").append("<br>");
        sb.append("demo1").append("<br>");
        sb.append(LocalDateTimeUtil.HTTP_FORMATTER.format(ZonedDateTime.now()));

        return sb.toString();
    }

    @QyController(path = "demo2", method = {HttpMethod.GET})
    public String demo2(String yyj, DataMap data, String aa) {
        System.out.println(yyj);
        System.out.println(data);
        System.out.println(aa);
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>").append("Hello~ Qy Framework  ~").append("</h1>");
        sb.append("<h2>").append("demo2").append("</h2>");
        sb.append(LocalDateTimeUtil.HTTP_FORMATTER.format(ZonedDateTime.now()));
        return sb.toString();
    }

    @QyController(path = "demo3", method = {HttpMethod.GET})
    public JSONObject demo2() {
        JSONObject object = new JSONObject();
        object.put("key", l.getAndIncrement());
        return object;
    }
}

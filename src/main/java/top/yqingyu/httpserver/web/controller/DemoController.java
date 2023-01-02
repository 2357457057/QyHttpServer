package top.yqingyu.httpserver.web.controller;

import top.yqingyu.httpserver.annotation.QyController;
import top.yqingyu.httpserver.compoment.*;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.LocalDateTimeUtil;

import java.time.ZonedDateTime;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.DemoController
 * @description
 * @createTime 2022年09月15日 14:09:00
 */
@QyController(path = "qy_demo")
public class DemoController {


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

        return "cookie: " + age + test + "<br>Session: " + name + LocalDateTimeUtil.HTTP_FORMATTER.format(ZonedDateTime.now());
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
}

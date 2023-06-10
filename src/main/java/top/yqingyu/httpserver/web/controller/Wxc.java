package top.yqingyu.httpserver.web.controller;

import com.alibaba.fastjson2.JSONObject;
import top.yqingyu.httpserver.annotation.QyController;
import top.yqingyu.httpserver.common.HttpMethod;
import top.yqingyu.httpserver.common.Request;

@QyController
public class Wxc {

    @QyController(path = "/wx", method = {HttpMethod.GET})
    public String wxToken(Request object) {
        String echostr = object.getUrlParam("echostr");
        return echostr;
    }
}

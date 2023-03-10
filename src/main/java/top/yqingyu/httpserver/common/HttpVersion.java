package top.yqingyu.httpserver.common;

import java.io.Serializable;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.entity.HttpVersion
 * @description
 * @createTime 2022年09月13日 22:13:00
 */
public enum HttpVersion implements Serializable {

    V_1_1("HTTP/1.1"),
    V_2("HTTP/2"),

    V_3("HTTP/3");
    private final String v;

    HttpVersion(String v) {
        this.v = v;
    }

    public String getV() {
        return v;
    }

    public static HttpVersion getVersion(String v) {
        if (V_1_1.v.equals(v)) return V_1_1;
        if (V_2.v.equals(v)) return V_2;
        if (V_3.v.equals(v)) return V_3;
        else return null;
    }
}

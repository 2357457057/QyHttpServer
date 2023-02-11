package top.yqingyu.httpserver.common;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.entity.HttpMethod
 * @description
 * @createTime 2022年09月14日 11:30:00
 */
public enum HttpMethod {
    GET,
    POST,

    /**
     * Supported Methods
     */
    OPTIONS,

    /**
     * return a demo head
     */
    HEAD,
    /**
     * Uploading resources
     */
    PUT,
    /**
     * Delete the resource
     */
    DELETE,

    /**
     * Echo-Request
     */
    TRACE,

    /**
     * I guess it's to support SSL, I don't know
     */
    CONNECT;

    public static HttpMethod getMethod(String m) {


        if (GET.name().equals(m)) return GET;
        if (POST.name().equals(m)) return POST;
        if (OPTIONS.name().equals(m)) return OPTIONS;
        if (HEAD.name().equals(m)) return HEAD;
        if (PUT.name().equals(m)) return PUT;
        if (DELETE.name().equals(m)) return DELETE;
        if (TRACE.name().equals(m)) return TRACE;
        if (CONNECT.name().equals(m)) return CONNECT;

        else return null;


    }
}
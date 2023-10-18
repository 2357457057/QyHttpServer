package top.yqingyu.httpserver.common;

import java.time.ZonedDateTime;

public enum HttpStatue {
    $100("100", "post continue", false),
    $200("200", "ok", false),
    $206("206", "partial content", false),
    $400("400", "bad request", true),
    $401("401", "upload mother fucker!", true),
    $404("404", "not found", true),
    $413("413", "413 request entity too large", true),
    $500("500", "呜呜，人家坏掉了", true),
    ;
    public final String code;
    public final String msg;
    public final boolean respBody;

    private final Response response;

    HttpStatue(String code, String msg, boolean respBody) {
        this.code = code;
        this.msg = msg;
        this.respBody = respBody;
        response = new Response();
        setResponse(response);
    }

    Response setResponse(Response response) {
        response.setStatue_code(code)
                .setHttpVersion(HttpVersion.V_1_1)
                .putHeaderContentType(ContentType.TEXT_PLAIN)
                .putHeaderDate(ZonedDateTime.now())
                .setAssemble(true);

        if (respBody) {
            response.setString_body(msg);
        }
        return response;
    }

    public Response Response() {
        return response.putHeaderDate(ZonedDateTime.now());
    }
}


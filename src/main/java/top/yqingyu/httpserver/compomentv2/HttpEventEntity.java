package top.yqingyu.httpserver.compomentv2;

import top.yqingyu.common.server$aio.Session;


/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.httpserver.compoment.HttpEventEntity
 * @description
 * @createTime 2022年09月19日 15:45:00
 */
class HttpEventEntity {
    private Request request;
    private Response response;
    private final Session session;

    private final boolean notEnd;

    public HttpEventEntity(Session session, boolean end) {
        this.session = session;
        this.notEnd = end;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Session getSession() {
        return session;
    }

    public boolean isNotEnd() {
        return notEnd;
    }
}

package top.yqingyu.httpserver.engine2;

import top.yqingyu.httpserver.common.Request;
import top.yqingyu.httpserver.common.Response;

public class HttpEventEntity {
    private Request request;
    private Response response;

    public HttpEventEntity(Request request, Response response) {
        this.request = request;
        this.response = response;
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

}

package top.yqingyu.httpserver.compoment;

import top.yqingyu.common.bean.NetChannel;


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
    private final NetChannel netChannel;

    private final boolean notEnd;

    public HttpEventEntity(NetChannel netChannel, boolean end) {
        this.netChannel = netChannel;
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

    public NetChannel getnetChannel() {
        return netChannel;
    }

    public boolean isNotEnd() {
        return notEnd;
    }
}

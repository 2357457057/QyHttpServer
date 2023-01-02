package top.yqingyu.httpserver.compoment;

import java.nio.channels.SocketChannel;

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
    private final SocketChannel socketChannel;

    private final boolean notEnd;

    public HttpEventEntity(SocketChannel socketChannel, boolean end) {
        this.socketChannel = socketChannel;
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

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public boolean isNotEnd() {
        return notEnd;
    }
}

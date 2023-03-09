package top.yqingyu.httpserver.engine2;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import top.yqingyu.common.qymsg.netty.ServerExceptionHandler;

/**
 * 异常处理类
 * 持有{@link top.yqingyu.common.qymsg.netty.ServerExceptionHandler} 具体实现异常处理
 */
public class HttpExceptionHandler extends ChannelDuplexHandler {
    private final HttpServerExceptionHandler serverExceptionHandler;

    public HttpExceptionHandler() {
        this.serverExceptionHandler = new HttpServerExceptionHandler() {
        };
    }

    public HttpExceptionHandler(HttpServerExceptionHandler serverExceptionHandler) {
        this.serverExceptionHandler = serverExceptionHandler;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        serverExceptionHandler.handle(ctx, cause);
    }
}

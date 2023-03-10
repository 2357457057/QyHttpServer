package top.yqingyu.httpserver.engine2;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 异常处理类
 * 持有{@link top.yqingyu.httpserver.engine2.ExceptionInterface} 具体实现异常处理
 */
public class ExceptionHandle extends ChannelDuplexHandler {
    private final ExceptionInterface serverExceptionHandler;

    public ExceptionHandle() {
        this.serverExceptionHandler = new ExceptionInterface() {
        };
    }

    public ExceptionHandle(ExceptionInterface serverExceptionHandler) {
        this.serverExceptionHandler = serverExceptionHandler;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        serverExceptionHandler.handle(ctx, cause);
    }
}

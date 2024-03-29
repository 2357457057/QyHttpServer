package top.yqingyu.httpserver.engine2;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.httpserver.exception.HttpException;


/**
 * 具体的异常消息实现 ，可重写default方法
 */
public interface ExceptionInterface {

    HttpResponseStatus NotAMultipartFileInterface = HttpResponseStatus.valueOf(400, "NotAMultipartFileInterface");
    FullHttpResponse METHOD_NOT_ALLOWED = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED, Unpooled.EMPTY_BUFFER);
    FullHttpResponse INTERNAL_SERVER_ERROR = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.EMPTY_BUFFER);
    FullHttpResponse NotAMultipartFileInterfaceResp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, NotAMultipartFileInterface, Unpooled.EMPTY_BUFFER);
    Logger logger = LoggerFactory.getLogger(ExceptionInterface.class);

    default void handle(ChannelHandlerContext ctx, Throwable cause) {
        String causeMessage = cause.getMessage();
        ChannelFuture channelFuture;
        if (cause instanceof HttpException.MethodNotSupposedException) {
            channelFuture = ctx.writeAndFlush(METHOD_NOT_ALLOWED.retainedDuplicate());
        } else if (cause instanceof HttpException.NotAMultipartFileInterfaceException) {
            channelFuture = ctx.writeAndFlush(NotAMultipartFileInterfaceResp.retainedDuplicate());
        } else if (StringUtil.isNotEmpty(causeMessage) && causeMessage.contains("NotSslRecordException")) {
            logger.warn("非https 请求");
            return;
        } else {
            channelFuture = ctx.writeAndFlush(INTERNAL_SERVER_ERROR.retainedDuplicate());
        }

        channelFuture.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                ctx.close();
            }
            logger.debug("{}", causeMessage, future.cause());
        });
    }
}

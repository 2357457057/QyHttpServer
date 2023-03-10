package top.yqingyu.httpserver.engine2;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.concurrent.atomic.AtomicInteger;

import static top.yqingyu.httpserver.common.ServerConfig.MAX_BODY_SIZE;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addLast("log", new LoggingHandler());
        pipeline.addLast("reqDecode", new HttpRequestDecoder());
        pipeline.addLast("MultipartFileDecode", new HttpMultipartFileHandler());
        pipeline.addLast("ChunkedWrite", new ChunkedWriteHandler());
        pipeline.addLast("respHandle", new HttpResponseHandler());
        pipeline.addLast("requestHandle", new HttpRequestHandler());
        pipeline.addLast("exceptionHandle", new HttpExceptionHandler());
    }
}

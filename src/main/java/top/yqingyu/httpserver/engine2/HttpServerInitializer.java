package top.yqingyu.httpserver.engine2;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;

import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import static top.yqingyu.httpserver.common.ServerConfig.MAX_BODY_SIZE;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addLast("log", new LoggingHandler());
        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("exception", new HttpExceptionHandler());
        pipeline.addLast(new HttpMultipartFileHandler());
        pipeline.addLast("Aggregator", new HttpObjectAggregator((int) MAX_BODY_SIZE));
        pipeline.addLast("Chunked", new ChunkedWriteHandler());
        pipeline.addLast(new HttpResponseHandler());
        pipeline.addLast(new HttpRequestHandler());
    }
}

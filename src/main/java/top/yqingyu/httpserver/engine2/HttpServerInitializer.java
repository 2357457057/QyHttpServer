package top.yqingyu.httpserver.engine2;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;

import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        /*把请求报文 解码*/
        pipeline.addLast("log",new LoggingHandler());
        pipeline.addLast("codec",new HttpServerCodec());
        pipeline.addLast("Aggregator",new HttpObjectAggregator(1024 * 1024 * 10));
        pipeline.addLast("Chunked",new ChunkedWriteHandler());
        pipeline.addLast(new HttpResponseHandler());
        pipeline.addLast(new HttpRequestHandler());
    }
}

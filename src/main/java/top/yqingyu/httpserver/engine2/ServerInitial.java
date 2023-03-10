package top.yqingyu.httpserver.engine2;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

import io.netty.handler.ssl.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import top.yqingyu.httpserver.common.ServerConfig;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.File;

public class ServerInitial extends ChannelInitializer<SocketChannel> {
    public ServerInitial() {
    }

    private static volatile boolean sslIsInit = false;
    private static final Object SSL_LOCK = new Object();
    private static volatile SslContext context;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addLast("log", new LoggingHandler());
        SSL(ch);
        pipeline.addLast("HttpRequestDecoder", new HttpRequestDecoder());
        pipeline.addLast("DoRequestPre", new DoRequestPre());
        pipeline.addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
        pipeline.addLast("DoResponse", new DoResponse());
        pipeline.addLast("DoRequest", new DoRequest());
        pipeline.addLast("ExceptionHandle", new ExceptionHandle());
    }

    private void SSL(SocketChannel ch) throws SSLException {
        if (ServerConfig.SSL_ENABLE) {
            if (!sslIsInit) {
                synchronized (SSL_LOCK) {
                    if (!sslIsInit) {
                        SslContextBuilder contextBuilder = SslContextBuilder.forServer(new File(ServerConfig.SSL_CERT_PATH), new File(ServerConfig.SSL_KEY_PATH));
                        contextBuilder.startTls(false);
                        context = contextBuilder.build();
                        sslIsInit = true;
                    }
                }
            }
            SSLEngine engine = context.newEngine(ch.alloc());
            engine.setUseClientMode(false);    // 设置SslEngine是client或者是server模式
            // 添加SslHandler到pipeline作为第一个处理器
            ch.pipeline().addFirst("ssl", new SslHandler(engine, false));

        }
    }
}

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MyNettyServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String httpObject) throws Exception {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Encoding: gzip\r\n" +
                "Vary: Accept-Encoding\r\n" +
                "Server: Microsoft-IIS/7.5\r\n" +
                "X-Powered-By: ASP.NET\r\n" +
                "Date: Tue, 07 Mar 2023 10:30:35 GMT\r\n" +
                "Content-Length: 0\r\n\r\n";
        Channel channel = ctx.channel();
        channel.writeAndFlush(response);
    }
}
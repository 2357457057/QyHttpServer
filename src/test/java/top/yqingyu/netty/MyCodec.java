package top.yqingyu.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MyCodec extends ByteToMessageCodec<ByteBuf> {
    Logger logger = LoggerFactory.getLogger(MyCodec.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        logger.info("encode");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {
        int i = in.readableBytes();
        byte[] bytes = new byte[i];
        in.readBytes(bytes);
        String s = new String(bytes, StandardCharsets.UTF_8);
        logger.info("\n{}", new String(bytes, StandardCharsets.UTF_8));
        logger.info("decode");
        ctx.fireChannelRead(s);
    }
}

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyEncoder extends MessageToByteEncoder<String> {

    private static final Logger log = LoggerFactory.getLogger(MyEncoder.class);

    @Override
    //获得了ctx等于拿到了这个channel相关的pipeline中的所有信息
    //1. channel
    //2. ByteBuf
    // String msg 编码器接受的 client输出的内容
    //ByteBuf out 真正往服务端写的ByteBuf的数据,细节  xxx
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        log.debug("encode method invoke  ");
        String[] messges = msg.split("-");
        for (String messge : messges) {
            long resultLong = Long.parseLong(messge);
            //每一个long类型的数据，在bytebuf中占用8个字节
            out.writeLong(resultLong);
        }
    }
}

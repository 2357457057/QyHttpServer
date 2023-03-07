import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import top.yqingyu.netty.MyCodec;

public class MyNettyServerInitializer extends ChannelInitializer<SocketChannel> {

    // 连接被注册到Channel后，立刻执行此方法
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        // 加入netty提供的处理器
        pipeline.addLast("MyCodec",new MyCodec());
        // 增加自定义处理器MyNettyServerHandler，用于实际处理请求，并给出响应
        pipeline.addLast("MyNettyServerHandler",new MyNettyServerHandler());
    }
}

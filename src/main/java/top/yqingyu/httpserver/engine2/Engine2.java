package top.yqingyu.httpserver.engine2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import top.yqingyu.common.utils.ThreadUtil;
import top.yqingyu.httpserver.common.ServerConfig;

public class Engine2 {
    public static void start(String SERVER_NAME, int port) throws InterruptedException {
        NioEventLoopGroup SERVER = new NioEventLoopGroup(1);
        NioEventLoopGroup CLIENT = new NioEventLoopGroup(ServerConfig.handlerNumber, ThreadUtil.createThFactoryC(SERVER_NAME, port + "-Handler"));
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(SERVER, CLIENT);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ServerInitial());
        ChannelFuture sync = bootstrap.bind(port).sync();
        sync.channel().closeFuture().sync();
    }
}

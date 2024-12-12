package a.netty.server;

import a.netty.common.MyMsgDecoder;
import a.netty.common.MyMsgEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 服务代理socket
 */
public class ServerSocket {
    private static EventLoopGroup boosGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static ChannelFuture channelFuture;

    public static void startServer() throws Exception {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new MyMsgDecoder(Integer.MAX_VALUE, 0,
                                    4, -4, 0));
                            pipeline.addLast(new MyMsgEncoder());
                            pipeline.addLast(new IdleStateHandler(40, 10, 0));
                            pipeline.addLast(new ClientHandler());
                        }
                    });
            channelFuture = bootstrap.bind(Constant.serverPort).sync();
            channelFuture.addListener(cf -> {
            });
            channelFuture.channel().closeFuture().sync();
        } finally {
            shutdown();
        }
    }

    public static void shutdown() {
        if (channelFuture != null) {
            channelFuture.channel().close().syncUninterruptibly();
        }
        if (boosGroup != null && !boosGroup.isShutdown()) {
            boosGroup.shutdownGracefully();
        }
        if (workerGroup != null && !workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }
    }
}

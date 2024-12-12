package a.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RealSocket {
    public static EventLoopGroup group = new NioEventLoopGroup();

    /**
     * 连接真实服务
     */
    public static Channel connectRealServer(String vid) {
        if (vid == null || vid.isEmpty()) {

            return null;
        }
        Channel channel = Constant.vrc.get(vid);
        if (channel == null) {
            newConnect(vid);
            channel = Constant.vrc.get(vid);
        }
        return channel;
    }

    public static void newConnect(String vid) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new RealHandler());
                        }
                    });
            bootstrap.connect("127.0.0.1", Constant.realPort).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        // 客户端连接真实服务成功
                        channelFuture.channel().config().setOption(ChannelOption.AUTO_READ, false);
                        channelFuture.channel().attr(Constant.VID).set(vid);
                        Constant.vrc.put(vid, channelFuture.channel());
                        ProxySocket.connectProxyServer(vid);

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

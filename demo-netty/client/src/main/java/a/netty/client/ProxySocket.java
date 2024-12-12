package a.netty.client;

import a.netty.common.MyMsg;
import a.netty.common.MyMsgDecoder;
import a.netty.common.MyMsgEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProxySocket {
    private static EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    /** 重连代理服务 */
    public static final ScheduledExecutorService reconnectExcutor = Executors.newSingleThreadScheduledExecutor();

    public static Channel connectProxyServer() throws Exception {
        reconnectExcutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    connectProxyServer(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3, 3, TimeUnit.SECONDS);
        return connectProxyServer(null);
    }

    public static Channel connectProxyServer(String vid) throws Exception {
        if (vid == null || vid.isEmpty()) {
            if (Constant.proxyChannel == null || !Constant.proxyChannel.isActive()) {
                newConnect(vid);
            }
            return null;
        } else {
            Channel channel = Constant.vpc.get(vid);
            if (null == channel) {
                newConnect(vid);
                channel = Constant.vpc.get(vid);
            }
            return channel;
        }
    }

    public static void newConnect(String vid) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new MyMsgDecoder(Integer.MAX_VALUE, 0, 4, -4, 0));
                        pipeline.addLast(new MyMsgEncoder());
                        pipeline.addLast(new IdleStateHandler(40, 8, 0));
                        pipeline.addLast(new ProxyHandler());
                    }
                });
        bootstrap.connect(Constant.serverIp, Constant.serverPort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    // 客户端连接代理服务器成功
                    Channel channel = channelFuture.channel();
                    if (vid == null || vid.isEmpty()) {
                        // 告诉服务端这条连接是client的连接
                        MyMsg myMsg = new MyMsg();
                        myMsg.setType(MyMsg.TYPE_CONNECT);
                        myMsg.setData("client".getBytes());
                        channel.writeAndFlush(myMsg);

                        Constant.proxyChannel = channel;
                    } else {
                        // 告诉服务端这条连接是vid的连接
                        MyMsg myMsg = new MyMsg();
                        myMsg.setType(MyMsg.TYPE_CONNECT);
                        myMsg.setData(vid.getBytes());
                        channel.writeAndFlush(myMsg);

                        // 客户端绑定通道关系
                        Constant.vpc.put(vid, channel);
                        channel.attr(Constant.VID).set(vid);

                        Channel realChannel = Constant.vrc.get(vid);
                        if (null != realChannel) {
                            realChannel.config().setOption(ChannelOption.AUTO_READ, true);
                        }
                    }
                }
            }
        });
    }
}

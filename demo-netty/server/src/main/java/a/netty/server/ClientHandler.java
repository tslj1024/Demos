package a.netty.server;

import a.netty.common.MyMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;

/**
 * 服务代理Handler
 */
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<MyMsg> {
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, MyMsg myMsg) throws Exception {
        // 代理服务器读取到客户端数据
        byte type = myMsg.getType();
        switch (type) {
            case MyMsg.TYPE_HEARTBEAT:
                MyMsg heartbeatMsg = new MyMsg();
                heartbeatMsg.setType(MyMsg.TYPE_HEARTBEAT);
                ctx.channel().writeAndFlush(heartbeatMsg);
                break;
            case MyMsg.TYPE_CONNECT:
                String vid = new String(myMsg.getData());
                if (vid.isEmpty() || vid.equals("client")) {

                    Constant.clientChannel = ctx.channel();
                } else {
                    // 绑定访客和客户端的连接
                    Channel visitorChannel = Constant.vvc.get(vid);
                    if (visitorChannel != null) {
                        ctx.channel().attr(Constant.VID).set(vid);
                        Constant.vcc.put(vid, ctx.channel());

                        // 通道绑定完成可以读取访客数据
                        visitorChannel.config().setOption(ChannelOption.AUTO_READ, true);
                    }
                }
                break;
            case MyMsg.TYPE_DISCONNECT:
                String disVid = new String(myMsg.getData());
                Constant.clearVccVvcAndClose(disVid);
                break;
            case MyMsg.TYPE_TRANSFER:
                // 把数据转到用户服务
                ByteBuf buf = ctx.alloc().buffer(myMsg.getData().length);
                buf.writeBytes(myMsg.getData());

                String visitorId = ctx.channel().attr(Constant.VID).get();
                Channel vchannel = Constant.vvc.get(visitorId);

                log.info("数据量: [{}B]", myMsg.getData().length);
//                System.out.println(Arrays.toString(myMsg.getData()));
//                System.out.println(new String(myMsg.getData()));

                if (vchannel != null) {
                    vchannel.writeAndFlush(buf);
                }
                break;
            default:
                // 操作有误
        }
        // 代理服务器发送数据到用户了
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        String vid = ctx.channel().attr(Constant.VID).get();
        if (null == vid || vid.isEmpty()) {
            super.channelWritabilityChanged(ctx);
            return;
        }
        Channel vchannel = Constant.vvc.get(vid);
        if (vchannel != null) {
            vchannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String vid = ctx.channel().attr(Constant.VID).get();
        if (null == vid || vid.isEmpty()) {
            super.channelInactive(ctx);
            return;
        }
        Channel vchannel = Constant.vvc.get(vid);
        if (vchannel != null && vchannel.isActive()) {
            // 数据发送完毕再关闭连接，解决http1.0数据传输问题
            vchannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            vchannel.close();
        } else {
            ctx.channel().close();
        }
        Constant.clearVccVvc(vid);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (Objects.requireNonNull(event.state()) == IdleState.READER_IDLE) {
                ctx.channel().close();
            }
        }
    }
}

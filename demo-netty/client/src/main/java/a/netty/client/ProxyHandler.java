package a.netty.client;

import a.netty.common.MyMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 客户端代理socket
 */
public class ProxyHandler extends SimpleChannelInboundHandler<MyMsg> {

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, MyMsg myMsg) throws Exception {
        // 客户端读取到代理过来的数据了
        byte type = myMsg.getType();
        String vid = new String(myMsg.getData());
        switch (type) {
            case MyMsg.TYPE_HEARTBEAT:
                break;
            case MyMsg.TYPE_CONNECT:
                RealSocket.connectRealServer(vid);
                break;
            case MyMsg.TYPE_DISCONNECT:
                Constant.clearVpcVrcAndClose(vid);
                break;
            case MyMsg.TYPE_TRANSFER:
                // 把数据转移到真实服务
                ByteBuf buf = ctx.alloc().buffer(myMsg.getData().length);
                buf.writeBytes(myMsg.getData());

                String visitorId = ctx.channel().attr(Constant.VID).get();
                Channel rchannel = Constant.vrc.get(visitorId);
                if (rchannel != null) {
                    rchannel.writeAndFlush(buf);
                }
                break;
            default:
                // 操作有错误
        }
        // 客户端发数据到真实服务了
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        String vid = ctx.channel().attr(Constant.VID).get();
        if (vid == null || vid.isEmpty()) {
            super.channelWritabilityChanged(ctx);
            return;
        }
        Channel realChannel = Constant.vrc.get(vid);
        if (realChannel != null) {
            realChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String vid = ctx.channel().attr(Constant.VID).get();
        if (vid == null || vid.isEmpty()) {
            super.channelInactive(ctx);
            return;
        }
        Channel realChannel = Constant.vrc.get(vid);
        if (realChannel != null && realChannel.isActive()) {
            realChannel.close();
        }

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    ctx.channel().close();
                    break;
                case WRITER_IDLE:
                    MyMsg myMsg = new MyMsg();
                    myMsg.setType(MyMsg.TYPE_HEARTBEAT);
                    ctx.channel().writeAndFlush(myMsg);
                    break;
                case ALL_IDLE:
                    break;
            }
        }
    }
}

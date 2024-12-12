package a.netty.client;

import a.netty.common.MyMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class RealHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        // 客户读取到真实服务数据了
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        MyMsg myMsg = new MyMsg();
        myMsg.setType(MyMsg.TYPE_TRANSFER);
        myMsg.setData(bytes);
        String vid = ctx.channel().attr(Constant.VID).get();
        if (vid == null || vid.isEmpty()) {
            return ;
        }
        log.info("访客 ID: {}", vid);
        Channel proxyChannel = Constant.vpc.get(vid);
        if (null != proxyChannel) {
            proxyChannel.writeAndFlush(myMsg);
        }
        // 客户端发送真实数据到代理端了
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String vid = ctx.channel().attr(Constant.VID).get();
        if (vid == null || vid.isEmpty()) {
            super.channelInactive(ctx);
            return ;
        }
        Channel proxyChannel = Constant.vpc.get(vid);
        if (null != proxyChannel) {
            MyMsg myMsg = new MyMsg();
            myMsg.setType(MyMsg.TYPE_DISCONNECT);
            myMsg.setData(vid.getBytes());
            proxyChannel.writeAndFlush(myMsg);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        String vid = ctx.channel().attr(Constant.VID).get();

        if (vid == null || vid.isEmpty()) {
            super.channelWritabilityChanged(ctx);
            return;
        }
        Channel proxyChannel = Constant.vpc.get(vid);
        if (null != proxyChannel) {
            proxyChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}

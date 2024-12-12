package a.netty.server;

import a.netty.common.MyMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
public class VisitorHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 访客链接上代理
        Channel vchannel = ctx.channel();
        // 先不读取访客数据
        vchannel.config().setOption(ChannelOption.AUTO_READ, false);

        // 生成访客ID
        String vid = UUID.randomUUID().toString();
        log.debug("访客 ID: {}", vid);
        log.info("访客 ID: {}", vid);
        log.warn("访客 ID: {}", vid);
        log.error("访客 ID: {}", vid);

        // 绑定访客通道
        // 用户 - 代理服务端 通道
        vchannel.attr(Constant.VID).set(vid);
        Constant.vvc.put(vid, vchannel);

        MyMsg mymsg = new MyMsg();
        mymsg.setType(MyMsg.TYPE_CONNECT);
        mymsg.setData(vid.getBytes());
        Constant.clientChannel.writeAndFlush(mymsg);

        super.channelActive(ctx);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        String vid = ctx.channel().attr(Constant.VID).get();
        if (vid == null || vid.isEmpty()) {
            return;
        }
        int len = buf.readableBytes();
        byte[] data = new byte[len];
        buf.readBytes(data);
        log.info("访客 ID: [{}]", vid);
        MyMsg mymsg = new MyMsg();
        mymsg.setData(data);
        mymsg.setType(MyMsg.TYPE_TRANSFER);
        // 代理服务器发送数据到客户端了
        Channel channel = Constant.vcc.get(vid);
        channel.writeAndFlush(mymsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String vid = ctx.channel().attr(Constant.VID).get();
        if (vid == null || vid.isEmpty()) {
            super.channelInactive(ctx);
            return;
        }
        Channel channel = Constant.vcc.get(vid);
        if (channel != null && channel.isActive()) {
            channel.config().setOption(ChannelOption.AUTO_READ, true);

            // 通知客户端，访客连接已经断开
            MyMsg mymsg = new MyMsg();
            mymsg.setType(MyMsg.TYPE_DISCONNECT);
            mymsg.setData(vid.getBytes());
            channel.writeAndFlush(mymsg);
        }
        Constant.clearVccVvc(vid);
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String vid = channel.attr(Constant.VID).get();
        if (vid != null && vid.isEmpty()) {
            Channel cchannel = Constant.vcc.get(vid);
            if (cchannel != null) {
                cchannel.config().setOption(ChannelOption.AUTO_READ, channel.isWritable());
            }
        }
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}

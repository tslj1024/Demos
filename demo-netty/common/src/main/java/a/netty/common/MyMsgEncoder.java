package a.netty.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息编码类
 */
public class MyMsgEncoder extends MessageToByteEncoder<MyMsg> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MyMsg msg, ByteBuf out) throws Exception {
        int bodyLength = 5;
        if (msg.getData() != null) {
            bodyLength += msg.getData().length;
        }
        out.writeInt(bodyLength);
        out.writeByte(msg.getType());

        if (msg.getData() != null) {
            out.writeBytes(msg.getData());
        }
    }
}

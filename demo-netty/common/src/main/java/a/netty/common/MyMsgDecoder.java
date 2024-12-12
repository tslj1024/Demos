package a.netty.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MyMsgDecoder extends LengthFieldBasedFrameDecoder {
    public MyMsgDecoder(int maxFrameLength,
                        int lengthFieldOffset,
                        int lengthFieldLength,
                        int lengthAdjustment,
                        int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    public MyMsgDecoder(int maxFrameLength,
                        int lengthFieldOffset,
                        int lengthFieldLength,
                        int lengthAdjustment,
                        int initialBytesToStrip,
                        boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    @Override
    protected MyMsg decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf in2 = (ByteBuf) super.decode(ctx, in);
        if (in2 == null) {
            return null;
        }
        if (in2.readableBytes() < 4) {
            return null;
        }

        MyMsg msg = new MyMsg();
        int dataLength = in2.readInt();
        byte type = in2.readByte();
        byte[] data = new byte[dataLength - 5];
        in2.readBytes(data);
        msg.setData(data);
        msg.setType(type);

        in2.release();

        return msg;
    }
}

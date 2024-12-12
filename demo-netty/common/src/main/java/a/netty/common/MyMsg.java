package a.netty.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * 消息类
 */
@Setter
@Getter
public class MyMsg {
    // 心跳
    public static final byte TYPE_HEARTBEAT = 0x00;

    // 连接成功
    public static final byte TYPE_CONNECT = 0x01;

    // 数据传输
    public static final byte TYPE_TRANSFER = 0x02;

    // 连接断开
    public static final byte TYPE_DISCONNECT = 0x09;

    // 数据类型
    private byte type;

    // 消息传输数据
    private byte[] data;

    @Override
    public String toString() {
        return "MyMsg [type=" + type + ", data=" + Arrays.toString(data) + "]";
    }
}

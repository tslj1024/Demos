package a.netty.server;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constant {
    /** 客户端服务channel */
    public static Channel clientChannel = null;

    /** 绑定channel_id */
    public static final AttributeKey<String> VID = AttributeKey.newInstance("vid");

    /** 访客，客户服务channel */
    public static Map<String, Channel> vcc = new ConcurrentHashMap<>();

    /** 访客，访客服务channel */
    public static Map<String, Channel> vvc = new ConcurrentHashMap<>();

    /** 服务端口 */
    public static int serverPort = 16001;

    /** 服务代理端口 */
    public static int visitorPort = 16002;

    /**
     * 清除连接
     */
    public static void clearVccVvc(String vid) {
        if (null == vid || vid.isEmpty()) {
            return;
        }
        Channel clientChannel = vcc.get(vid);
        if (null != clientChannel) {
            clientChannel.attr(VID).set(null);
            vcc.remove(vid);
        }
        Channel visitorChannel = vvc.get(vid);
        if (null != visitorChannel) {
            visitorChannel.attr(VID).set(null);
            vvc.remove(vid);
        }
    }

    /**
     * 清除关闭连接
     */
    public static void clearVccVvcAndClose(String vid) {
        if (null == vid || vid.isEmpty()) {
            return;
        }
        Channel clientChannel = vcc.get(vid);
        if (null != clientChannel) {
            clientChannel.attr(VID).set(null);
            vcc.remove(vid);
            clientChannel.close();
        }
        Channel visitorChannel = vvc.get(vid);
        if (null != visitorChannel) {
            visitorChannel.attr(VID).set(null);
            vvc.remove(vid);
            visitorChannel.close();
        }
    }
}

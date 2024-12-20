package a.netty.client;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constant {
    /** 代理服务channel */
    public static Channel proxyChannel = null;

    /** 绑定访客ID */
    public static final AttributeKey<String> VID = AttributeKey.newInstance("vid");

    /** 访客，代理服务channel */
    public static Map<String, Channel> vpc = new ConcurrentHashMap<>();

    /** 访客，真实服务channel*/
    public static Map<String, Channel> vrc = new ConcurrentHashMap<>();

    /** 真实服务端口 */
    public static int realPort = 80;

    /** 服务端口 */
    public static int serverPort = 16001;

    /** 服务IP */
    public static String serverIp = "127.0.0.1";

    /**
     * 清除连接
     * @param vid
     */
    public static void clearVpcVrc(String vid) {
        if (null == vid || vid.isEmpty()) {
            return;
        }
        Channel clientChannel = vpc.get(vid);
        if (null != clientChannel) {
            clientChannel.attr(VID).set(null);
            vpc.remove(vid);
        }
        Channel visitorChannel = vrc.get(vid);
        if (null != visitorChannel) {
            visitorChannel.attr(VID).set(null);
            vrc.remove(vid);
        }
    }

    /**
     * 清除关闭连接
     * @param vid
     */
    public static void clearVpcVrcAndClose(String vid) {
        if (null == vid || vid.isEmpty()) {
            return;
        }
        Channel clientChannel = vpc.get(vid);
        if (null != clientChannel) {
            clientChannel.attr(VID).set(null);
            vpc.remove(vid);
            clientChannel.close();
        }
        Channel visitorChannel = vrc.get(vid);
        if (null != visitorChannel) {
            visitorChannel.attr(VID).set(null);
            vrc.remove(vid);
            visitorChannel.close();
        }
    }
}

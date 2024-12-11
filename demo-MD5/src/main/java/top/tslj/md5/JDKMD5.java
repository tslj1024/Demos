package top.tslj.md5;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JDKMD5 {
    public static void main(String[] args) throws NoSuchAlgorithmException {

        /*
            他们有没有可能加入用户或设备特异性标识呢? 应该不会，否则怎么重用文件本体?

            MD5占据, 长度！！！
            大文件占据
            ZIP Boom

            A:5GB ---> B:5GB
            强制转存导致用户总空间:-10GB
            占用空间<5GB
            与其他文件MD5相同 => <5GB ---> >=20GB

            MD5 肯定是存在环，但是有没有二元环，或者自环
         */
        
        // 他们都叫我成语小王子 兔死狗烹 鸟尽弓藏 卸磨杀驴 过河拆桥 落井下石
        // Hello, World! - 65a8e27d8879283831b664bd8b7f0ad4
        // Hello, world! - 6cd3556deb0da54bca060b4c39479839
        // '1024' - 021bbc7ee20b71134d53e20206bd6feb
        // 1024 - 632686e3b7ad446aabcaac367ce7b0f6
        // 1 0 2 4 - db7cbf230db4da2b150074867d8bf588
        // 16 - 6b31bdfa7f9bfece263381ffa91bd6a9
        // 0 - 93b885adfe0da089cdf634904fd59f71
        byte[] md5s = MD5.digest("Hello, world!".getBytes(StandardCharsets.UTF_8));
//        byte[] md5s = MD5.digest("Hello, World!".getBytes(StandardCharsets.UTF_8));
//        byte[] md5s = MD5.digest(new byte[]{0});
//        byte[] md5s = MessageDigest.getInstance("MD5").digest("Hello, world!".getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte md5 : md5s) {
            sb.append((md5 & 0xFF) < 16 ? "0" + Integer.toHexString(md5 & 0xFF) : Integer.toHexString(md5 & 0xFF));
        }
        System.out.println(sb);
        long start = System.currentTimeMillis();
        volidate(1_0000_0000, 2_0000_0000);
        System.out.println(System.currentTimeMillis() - start);
    }
    
    public static void volidate(int offset, int len) throws NoSuchAlgorithmException {
        for (int i = offset; i < len; i++) {
            byte[] input = new byte[4];
            input[0] = (byte) ((i >>> 24) & 0xFF);
            input[1] = (byte) ((i >>> 16) & 0xFF);
            input[2] = (byte) ((i >>> 8) & 0xFF);
            input[3] = (byte) (i & 0xFF);
            byte[] md5s1 = MD5.digest(input);
            byte[] md5s2 = MessageDigest.getInstance("MD5").digest(input);
            for (int j = 0; j < 16; j++) {
                if (md5s1[j] != md5s2[j]) {
                    throw new RuntimeException("MD5 digest failed: " + i);
                }
            }
        }
        System.out.println("Completed!");
    }
}

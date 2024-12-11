package top.tslj.md5;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Objects;

public class MD5 {

    // 旋转常数
    private static final int S11 = 7;
    private static final int S12 = 12;
    private static final int S13 = 17;
    private static final int S14 = 22;
    private static final int S21 = 5;
    private static final int S22 = 9;
    private static final int S23 = 14;
    private static final int S24 = 20;
    private static final int S31 = 4;
    private static final int S32 = 11;
    private static final int S33 = 16;
    private static final int S34 = 23;
    private static final int S41 = 6;
    private static final int S42 = 10;
    private static final int S43 = 15;
    private static final int S44 = 21;

    // 对于消息块的填充
    private static final byte[] padding;
    static {
        padding = new byte[136];
        padding[0] = (byte) 0x80;
    }

    // 最终存储 MD5 值的数组, 初始的四个状态
    private final int[] state;

    // 输入到压缩函数的块大小，那 512bit
    private final int blockSize;
    // 缓冲区，用以缓存那 512bit
    private final byte[] buffer;
    // 缓冲区偏移指针
    private int bufOfs;

    // 到目前为止已处理的字节数
    private long bytesProcessed;

    private MD5() {
        this.blockSize = 64;
        this.buffer = new byte[blockSize];
        this.state = new int[4];
        reset();
    }

    /**
     * 初始化状态
     */
    private void reset() {
        state[0] = 0x67452301;
        state[1] = 0xefcdab89;
        state[2] = 0x98badcfe;
        state[3] = 0x10325476;
    }

    /**
     * 根据输入的数据生成 MD5 数组
     *
     * @param input 输入的数据
     * @return MD5 字节数组
     */
    public static byte[] digest(byte[] input) {
        return MD5.digest(input, 0, input.length);
    }
    public static byte[] digest(byte[] input, int offset, int len) {
        MD5 md5 = new MD5();
        return md5.implDigest(input, offset, len);
    }

    /**
     * 执行MD5构造
     * @param input 输入的需要计算MD5的数据
     * @param offset 开始位置
     * @param len 计算长度
     * @return MD5数组
     */
    private byte[] implDigest(byte[] input, int offset, int len) {

        // 填充更新原始消息
        engineUpdate(input, offset, len);

        byte[] out = new byte[16];
        int ofs = 0;

        long bitsProcessed = bytesProcessed << 3;

        int index = (int) bytesProcessed & 0x3f;
        int padLen = (index < 56) ? (56 - index) : (120 - index);
        // 填充消息
        engineUpdate(padding, 0, padLen);

        // 小端存储，把bitsProcessed存在buffer的最后8字节
        i2bLittle4((int) bitsProcessed, buffer, 56);
        i2bLittle4((int) (bitsProcessed >>> 32), buffer, 60);

        // 继续压缩填充区域
        implCompress(buffer, 0);

        // 这里的 state 已经是结果了，只不过是4字节的整数
        i2bLittle(state, 0, out, ofs, 16);

        return out;
    }

    /**
     * 更新缓冲区
     * @param b 输入的数据
     * @param ofs 更新起始位置
     * @param len 更新长度
     */
    private void engineUpdate(byte[] b, int ofs, int len) {
        if (len == 0) {
            return;
        }
        if ((ofs < 0) || (len < 0) || (ofs > b.length - len)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        bytesProcessed += len;
        // 如果 buffer 不为空，我们需要在继续之前填充它
        if (bufOfs != 0) {
            int n = Math.min(len, blockSize - bufOfs);
            System.arraycopy(b, ofs, buffer, bufOfs, n);
            bufOfs += n;
            ofs += n;
            len -= n;
            if (bufOfs >= blockSize) {
                // 缓冲区已经被填满，立即压缩已完成的块
                implCompress(buffer, 0);
                bufOfs = 0;
            }
        }
        // 压缩完整块
        if (len >= blockSize) {
            int limit = ofs + len;
            ofs = implCompressMultiBlock(b, ofs, limit - blockSize);
            len = limit - ofs;
        }
        // 将剩余部分复制到缓冲区
        if (len > 0) {
            System.arraycopy(b, ofs, buffer, 0, len);
            bufOfs = len;
        }
    }

    // 压缩完整块
    private int implCompressMultiBlock(byte[] b, int ofs, int limit) {
        Objects.requireNonNull(b);

        if (ofs < 0 || ofs >= b.length) {
            throw new ArrayIndexOutOfBoundsException(ofs);
        }

        int endIndex = (limit / blockSize) * blockSize + blockSize - 1;
        if (endIndex >= b.length) {
            throw new ArrayIndexOutOfBoundsException(endIndex);
        }
        for (; ofs <= limit; ofs += blockSize) {
            implCompress(b, ofs);
        }
        return ofs;
    }

    /**
     * 将一个 32 位值存储到 out[outOfs..outOfs+3] 以小端顺序排列。
     * ad85b893
     * 93 b8 85 ad
     */
    public static void i2bLittle4(int val, byte[] out, int outOfs) {
        LE.INT_ARRAY.set(out, outOfs, val);
    }

    /**
     * 将一个 64 位值存储到 out[outOfs..outOfs+3] 以小端顺序排列。
     * fe14651708dddf33
     * 33 df dd 08 17 65 14 fe
     */
    public static void l2bLittle4(long val, byte[] out, int outOfs) {
        LE.LONG_ARRAY.set(out, outOfs, val);
    }

    /**
     * int[] 到 byte[] 的转换，小端字节顺序。
     * ad85b893    89a00dfe    9034f6cd    719fd54f
     * 93 b8 85 ad fe 0d a0 89 cd f6 34 90 4f d5 9f 71
     */
    public static void i2bLittle(int[] in, int inOfs, byte[] out, int outOfs, int len) {
        len += outOfs;
        while (outOfs < len) {
            LE.INT_ARRAY.set(out, outOfs, in[inOfs++]);
            outOfs += 4;
        }
    }

    /* **********************************************************
     * The MD5 Functions. The results of this
     * implementation were checked against the RSADSI version.
     * **********************************************************
     */

    private static final class LE {
        static final VarHandle INT_ARRAY
                = MethodHandles.byteArrayViewVarHandle(int[].class,
                ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();

        static final VarHandle LONG_ARRAY
                = MethodHandles.byteArrayViewVarHandle(long[].class,
                ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    }

    /**
     * This is where the functions come together as the generic MD5
     * transformation operation. It consumes sixteen
     * bytes from the buffer, beginning at the specified offset.
     */
    private void implCompress(byte[] buf, int ofs) {
        Objects.requireNonNull(buf);

        // 越界检查，这些检查已经足够了
        if ((ofs < 0) || ((buf.length - ofs) < 64)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        implCompress0(buf, ofs);
    }

    // The method 'implCompress0 seems not to use its parameters.
    // The method can, however, be replaced with a compiler intrinsic
    // that operates directly on the array 'buf' (starting from
    // offset 'ofs') and not on array 'x', therefore 'buf' and 'ofs'
    // must be passed as parameter to the method.
    private void implCompress0(byte[] buf, int ofs) {
        int a = state[0];
        int b = state[1];
        int c = state[2];
        int d = state[3];

        int x0 = (int) LE.INT_ARRAY.get(buf, ofs);
        int x1 = (int) LE.INT_ARRAY.get(buf, ofs + 4);
        int x2 = (int) LE.INT_ARRAY.get(buf, ofs + 8);
        int x3 = (int) LE.INT_ARRAY.get(buf, ofs + 12);
        int x4 = (int) LE.INT_ARRAY.get(buf, ofs + 16);
        int x5 = (int) LE.INT_ARRAY.get(buf, ofs + 20);
        int x6 = (int) LE.INT_ARRAY.get(buf, ofs + 24);
        int x7 = (int) LE.INT_ARRAY.get(buf, ofs + 28);
        int x8 = (int) LE.INT_ARRAY.get(buf, ofs + 32);
        int x9 = (int) LE.INT_ARRAY.get(buf, ofs + 36);
        int x10 = (int) LE.INT_ARRAY.get(buf, ofs + 40);
        int x11 = (int) LE.INT_ARRAY.get(buf, ofs + 44);
        int x12 = (int) LE.INT_ARRAY.get(buf, ofs + 48);
        int x13 = (int) LE.INT_ARRAY.get(buf, ofs + 52);
        int x14 = (int) LE.INT_ARRAY.get(buf, ofs + 56);
        int x15 = (int) LE.INT_ARRAY.get(buf, ofs + 60);

        /* Round 1 */
        a = FF(a, b, c, d, x0, S11, 0xd76aa478); /* 1 */
        d = FF(d, a, b, c, x1, S12, 0xe8c7b756); /* 2 */
        c = FF(c, d, a, b, x2, S13, 0x242070db); /* 3 */
        b = FF(b, c, d, a, x3, S14, 0xc1bdceee); /* 4 */
        a = FF(a, b, c, d, x4, S11, 0xf57c0faf); /* 5 */
        d = FF(d, a, b, c, x5, S12, 0x4787c62a); /* 6 */
        c = FF(c, d, a, b, x6, S13, 0xa8304613); /* 7 */
        b = FF(b, c, d, a, x7, S14, 0xfd469501); /* 8 */
        a = FF(a, b, c, d, x8, S11, 0x698098d8); /* 9 */
        d = FF(d, a, b, c, x9, S12, 0x8b44f7af); /* 10 */
        c = FF(c, d, a, b, x10, S13, 0xffff5bb1); /* 11 */
        b = FF(b, c, d, a, x11, S14, 0x895cd7be); /* 12 */
        a = FF(a, b, c, d, x12, S11, 0x6b901122); /* 13 */
        d = FF(d, a, b, c, x13, S12, 0xfd987193); /* 14 */
        c = FF(c, d, a, b, x14, S13, 0xa679438e); /* 15 */
        b = FF(b, c, d, a, x15, S14, 0x49b40821); /* 16 */

        /* Round 2 */
        a = GG(a, b, c, d, x1, S21, 0xf61e2562); /* 17 */
        d = GG(d, a, b, c, x6, S22, 0xc040b340); /* 18 */
        c = GG(c, d, a, b, x11, S23, 0x265e5a51); /* 19 */
        b = GG(b, c, d, a, x0, S24, 0xe9b6c7aa); /* 20 */
        a = GG(a, b, c, d, x5, S21, 0xd62f105d); /* 21 */
        d = GG(d, a, b, c, x10, S22, 0x2441453); /* 22 */
        c = GG(c, d, a, b, x15, S23, 0xd8a1e681); /* 23 */
        b = GG(b, c, d, a, x4, S24, 0xe7d3fbc8); /* 24 */
        a = GG(a, b, c, d, x9, S21, 0x21e1cde6); /* 25 */
        d = GG(d, a, b, c, x14, S22, 0xc33707d6); /* 26 */
        c = GG(c, d, a, b, x3, S23, 0xf4d50d87); /* 27 */
        b = GG(b, c, d, a, x8, S24, 0x455a14ed); /* 28 */
        a = GG(a, b, c, d, x13, S21, 0xa9e3e905); /* 29 */
        d = GG(d, a, b, c, x2, S22, 0xfcefa3f8); /* 30 */
        c = GG(c, d, a, b, x7, S23, 0x676f02d9); /* 31 */
        b = GG(b, c, d, a, x12, S24, 0x8d2a4c8a); /* 32 */

        /* Round 3 */
        a = HH(a, b, c, d, x5, S31, 0xfffa3942); /* 33 */
        d = HH(d, a, b, c, x8, S32, 0x8771f681); /* 34 */
        c = HH(c, d, a, b, x11, S33, 0x6d9d6122); /* 35 */
        b = HH(b, c, d, a, x14, S34, 0xfde5380c); /* 36 */
        a = HH(a, b, c, d, x1, S31, 0xa4beea44); /* 37 */
        d = HH(d, a, b, c, x4, S32, 0x4bdecfa9); /* 38 */
        c = HH(c, d, a, b, x7, S33, 0xf6bb4b60); /* 39 */
        b = HH(b, c, d, a, x10, S34, 0xbebfbc70); /* 40 */
        a = HH(a, b, c, d, x13, S31, 0x289b7ec6); /* 41 */
        d = HH(d, a, b, c, x0, S32, 0xeaa127fa); /* 42 */
        c = HH(c, d, a, b, x3, S33, 0xd4ef3085); /* 43 */
        b = HH(b, c, d, a, x6, S34, 0x4881d05); /* 44 */
        a = HH(a, b, c, d, x9, S31, 0xd9d4d039); /* 45 */
        d = HH(d, a, b, c, x12, S32, 0xe6db99e5); /* 46 */
        c = HH(c, d, a, b, x15, S33, 0x1fa27cf8); /* 47 */
        b = HH(b, c, d, a, x2, S34, 0xc4ac5665); /* 48 */

        /* Round 4 */
        a = II(a, b, c, d, x0, S41, 0xf4292244); /* 49 */
        d = II(d, a, b, c, x7, S42, 0x432aff97); /* 50 */
        c = II(c, d, a, b, x14, S43, 0xab9423a7); /* 51 */
        b = II(b, c, d, a, x5, S44, 0xfc93a039); /* 52 */
        a = II(a, b, c, d, x12, S41, 0x655b59c3); /* 53 */
        d = II(d, a, b, c, x3, S42, 0x8f0ccc92); /* 54 */
        c = II(c, d, a, b, x10, S43, 0xffeff47d); /* 55 */
        b = II(b, c, d, a, x1, S44, 0x85845dd1); /* 56 */
        a = II(a, b, c, d, x8, S41, 0x6fa87e4f); /* 57 */
        d = II(d, a, b, c, x15, S42, 0xfe2ce6e0); /* 58 */
        c = II(c, d, a, b, x6, S43, 0xa3014314); /* 59 */
        b = II(b, c, d, a, x13, S44, 0x4e0811a1); /* 60 */
        a = II(a, b, c, d, x4, S41, 0xf7537e82); /* 61 */
        d = II(d, a, b, c, x11, S42, 0xbd3af235); /* 62 */
        c = II(c, d, a, b, x2, S43, 0x2ad7d2bb); /* 63 */
        b = II(b, c, d, a, x9, S44, 0xeb86d391); /* 64 */

        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
    }

    private static int FF(int a, int b, int c, int d, int x, int s, int ac) {
        a += ((b & c) | ((~b) & d)) + x + ac;
        return ((a << s) | (a >>> (32 - s))) + b;
    }

    private static int GG(int a, int b, int c, int d, int x, int s, int ac) {
        a += ((b & d) | (c & (~d))) + x + ac;
        return ((a << s) | (a >>> (32 - s))) + b;
    }

    private static int HH(int a, int b, int c, int d, int x, int s, int ac) {
        a += ((b ^ c) ^ d) + x + ac;
        return ((a << s) | (a >>> (32 - s))) + b;
    }

    private static int II(int a, int b, int c, int d, int x, int s, int ac) {
        a += (c ^ (b | (~d))) + x + ac;
        return ((a << s) | (a >>> (32 - s))) + b;
    }

}

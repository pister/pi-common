package com.github.pister.common.lang.codec;


import com.github.pister.common.io.FastByteArrayInputStream;
import com.github.pister.common.io.FastByteArrayOutputStream;
import com.github.pister.common.lang.charsets.DefaultCharsets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *  base65 是一种二进制的字符串表示格式，最终转换的字符串只会包含a-z,A-Z,9-0,-,_中的这些字符。
 *
 * 特点：
 * 1、encode后的大小稳定，永远是原数据的1.33倍+1
 * 2、除了大小写字母和数字这62个字符外，还有出现_和-两个字符
 *
 * <p>
 * 实现原理借鉴了base64的方案，由于a-z,A-Z,9-0只有62个字符，所以剩余的2个字符会用-和_字符来表示，
 * 末尾字符肯定是padding标识字符
 * A - P 标识 2个字节剩余的4bit padding
 * Q - T 标识 1字节剩余的2bit padding
 * U 表示3字节没有padding
 *
 *
 *
 * <p>
 * Created by songlihuang on 2020/6/23.
 */
public final class Base65 {

    private static final int BUF_SIZE = 4 * 1024;

    private static final int ENCODE_MODE_SIZE = 3;

    private static final int DECODE_MODE_SIZE = 4;

    private static final byte[] ENCODE_MAP = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_', '-',
    };

    private static final byte[] TAIL2_MAP = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P'
    };

    private static final byte[] TAIL1_MAP = {
            'Q', 'R', 'S', 'T'
    };

    private static final byte TAIL0 = 'U';


    private Base65() {
    }

    public static void encode(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        Base642Encoder encoder = new Base642Encoder(new OutputStreamBytesAppender(outputStream));
        for (; ; ) {
            int len = inputStream.read(buf, 0, BUF_SIZE);
            if (len < 0) {
                break;
            }
            encoder.doUpdate(buf, 0, len);
        }
        encoder.doFinal();
    }

    public static byte[] encode(byte[] bytes) {
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream(1024);
        try {
            encode(new FastByteArrayInputStream(bytes), outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void decode(InputStream inputStream, OutputStream outputStream) throws IOException {
        Base642Decoder base629Decoder = new Base642Decoder(new OutputStreamBytesAppender(outputStream), inputStream);
        base629Decoder.decode();
    }

    public static String encodeString(byte[] bytes) {
        return new String(encode(bytes), DefaultCharsets.UTF_8);
    }

    public static byte[] decodeString(String input) {
        return decode(input.getBytes(DefaultCharsets.UTF_8));
    }

    public static byte[] decode(byte[] bytes) {
        FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream(1024);
        try {
            decode(new FastByteArrayInputStream(bytes), outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CodecBase {

        protected BytesAppender bytesAppender;

        protected SizeLimitedBytesQueue bytesQueue;

        protected static final int QUEUE_SIZE = 1024 * 2;

        protected static final int BATCH_SIZE = QUEUE_SIZE / 4;

    }


    private static class Base642Decoder extends CodecBase {

        private BufferedByteReader bufferedByteReader;

        private byte[] bytesBuf = new byte[DECODE_MODE_SIZE];

        public Base642Decoder(BytesAppender bytesAppender, InputStream inputStream) {
            this.bytesAppender = bytesAppender;
            this.bytesQueue = new SizeLimitedBytesQueue(QUEUE_SIZE);
            this.bufferedByteReader = new BufferedByteReader(inputStream);
        }


        private void decodeBuf(byte[] inputBuf, int pos) {
            byte ib0 = decodeChar(inputBuf[pos]);
            byte ib1 = decodeChar(inputBuf[pos + 1]);
            byte ib2 = decodeChar(inputBuf[pos + 2]);
            byte ib3 = decodeChar(inputBuf[pos + 3]);

            // 111111, 110000, 000000, 000000
            // b0 = (c0(00111111) << 2 | c1(00110000))
            int b0 = ((ib0 & 0x3F) << 2) | ((ib1 & 0x30) >> 4);
            // 000000, 001111, 111100, 000000
            // b1 = (c1(00001111) << 4) | c2(00111100) >> 4
            int b1 = ((ib1 & 0x0F) << 4) | ((ib2 & 0x3C) >> 2);
            // 000000, 000000, 000011, 111111
            int b2 = ((ib2 & 0x03) << 6) | ib3;

            bytesAppender.append((byte) b0);
            bytesAppender.append((byte) b1);
            bytesAppender.append((byte) b2);
        }

        private void decodeBytes() {
            while (bytesQueue.size() >= DECODE_MODE_SIZE) {
                bytesQueue.removeFirst(bytesBuf);
                decodeBuf(bytesBuf, 0);
            }
        }

        private void decodeTail() {
            byte[] buf = new byte[3];
            byte ib0, ib1, p;
            int b1, b2;
            int size = bytesQueue.size();
            switch (size) {
                case 0:
                    throw new IllegalArgumentException("bad Base65 format, miss padding end char");
                case 1:
                    bytesQueue.removeFirst(buf);
                    if (buf[0] != TAIL0) {
                        throw new IllegalArgumentException("bad Base65 format, invalidate padding char");
                    }
                    // do nothing
                    break;
                case 2:
                    bytesQueue.removeFirst(buf);
                    ib0 = decodeChar(buf[0]);
                    p = decodeTailChar(buf[1]);
                    // 00111111 00000022 => 11111122
                    // 11111100 | 00000011
                    b1 = ((ib0 << 2) & 0xFC) | (p & 0x03);
                    bytesAppender.append((byte) b1);
                    break;
                case 3:
                    bytesQueue.removeFirst(buf);
                    ib0 = decodeChar(buf[0]);
                    ib1 = decodeChar(buf[1]);
                    p = decodeTailChar(buf[2]);
                    // 00111111 00223333 00004444 => 11111122, 33334444
                    b1 = ((ib0 << 2) & 0xFC) | ((ib1 >> 4) & 0x03);
                    b2 = ((ib1 << 4) & 0xF0) | (p & 0x0F);
                    bytesAppender.append((byte) b1);
                    bytesAppender.append((byte) b2);
                    break;
                default:
                    throw new IllegalArgumentException("bad Base65 format, invalidate padding size:" + size);
            }

        }

        private byte decodeTailChar(int b) {
            if (b >= 'A' && b <= 'P') {
                return (byte) (b - 'A');
            }
            if (b >= 'Q' && b <= 'T') {
                return (byte) (b - 'Q');
            }
            throw new IllegalArgumentException("bad Base65 format, not support tail char:" + (char) b);
        }

        private byte decodeChar(int b) {
            if (b >= 'A' && b <= 'Z') {
                return (byte) (b - 'A');
            }
            if (b >= 'a' && b <= 'z') {
                return (byte) (b - 'a' + 26);
            }
            if (b >= '0' && b <= '9') {
                return (byte) (b - '0' + 52);
            }
            if (b == '_') {
                return 62;
            }
            if (b == '-') {
                return 63;
            }
            throw new IllegalArgumentException("bad Base65 format, not support char:" + (char) b);
        }


        private static final int HAS_NOT_SET_NEXT = Integer.MAX_VALUE;

        public void decode() throws IOException {
            int currentByte = bufferedByteReader.nextByte();
            int nextByte = HAS_NOT_SET_NEXT;
            for (; ; ) {
                if (currentByte < 0) {
                    break;
                }
                if (nextByte == HAS_NOT_SET_NEXT) {
                    nextByte = bufferedByteReader.nextByte();
                }
                if (nextByte < 0) {
                    // the last char
                    bytesQueue.addLast((byte) currentByte);
                    decodeTail();
                    break;
                }
                bytesQueue.addLast((byte) currentByte);
                decodeBytes();

                currentByte = nextByte;
                nextByte = bufferedByteReader.nextByte();
            }
        }

    }

    private static class Base642Encoder extends CodecBase {

        private byte[] bytesBuf = new byte[ENCODE_MODE_SIZE];

        public Base642Encoder(BytesAppender bytesAppender) {
            this.bytesAppender = bytesAppender;
            this.bytesQueue = new SizeLimitedBytesQueue(QUEUE_SIZE);
        }

        private void appendChar(int b) {
            bytesAppender.append(ENCODE_MAP[b]);
        }


        private void encodeBuf(byte[] inputBuf, int pos) {
            // 0-7, 8-15, 16-23
            // ===============>
            // 0-5, 6-11, 12-17, 18-23
            // 11111100 = 0xFC
            int b0 = (inputBuf[pos] & 0xFC) >> 2;
            // byte0:00000011 = 0x03
            // byte1:11110000 = 0xF0
            int b1 = ((inputBuf[pos] & 0x03) << 4) | ((inputBuf[pos + 1] & 0xF0) >> 4);
            // byte1:00001111 = 0x0F
            // byte2:11000000 = 0xC0
            int b2 = ((inputBuf[pos + 1] & 0x0F) << 2) | ((inputBuf[pos + 2] & 0xC0) >> 6);
            // byte2:00111111 = 0x3F
            int b3 = (inputBuf[pos + 2] & 0x3F);
            appendChar(b0);
            appendChar(b1);
            appendChar(b2);
            appendChar(b3);
        }

        private void encodeTail1(byte[] inputBuf, int pos) {
            // 11111122 => 00111111, 00000022
            int b1 = (inputBuf[pos] >> 2) & 0x3F;
            int p = (inputBuf[pos] & 0x03);
            appendChar(b1);
            bytesAppender.append(TAIL1_MAP[p]);
        }

        private void encodeTail2(byte[] inputBuf, int pos) {
            // 11111122 33334444 => 00111111, 00223333, 00004444
            int b1 = (inputBuf[pos] >> 2) & 0x3F;
            int b2 = ((inputBuf[pos] & 0x03) << 4) | ((inputBuf[pos + 1] & 0xF0) >> 4);
            int p = (inputBuf[pos + 1] & 0x0F);
            appendChar(b1);
            appendChar(b2);
            bytesAppender.append(TAIL2_MAP[p]);
        }

        public void doUpdate(byte[] buf, int offset, int len) throws IOException {
            int i = 0;
            for (; ; ) {
                int size = Math.min(BATCH_SIZE, len - i);
                if (size <= 0) {
                    break;
                }
                bytesQueue.addLast(buf, offset + i, size);
                i += size;
                while (bytesQueue.size() >= 3) {
                    bytesQueue.removeFirst(bytesBuf);
                    encodeBuf(bytesBuf, 0);
                }
            }
        }

        public void doFinal() {
            int remainSize = bytesQueue.size();
            while (remainSize >= ENCODE_MODE_SIZE) {
                remainSize = bytesQueue.removeFirst(bytesBuf);
                encodeBuf(bytesBuf, 0);
            }
            bytesQueue.removeFirst(bytesBuf);
            switch (remainSize) {
                case 0:
                    bytesAppender.append(TAIL0);
                    return;
                case 1:
                    encodeTail1(bytesBuf, 0);
                    break;
                case 2:
                    encodeTail2(bytesBuf, 0);
                    break;
                default:
                    throw new IllegalStateException("must not be reach here");
            }
            bytesAppender.finish();
        }

    }

    public static abstract class BytesAppender {
        public abstract void append(byte b);

        public void append(byte[] buf) {
            for (byte b : buf) {
                append(b);
            }
        }

        public void append(byte[] buf, int pos, int len) {
            for (int i = 0; i < len; i++) {
                append(buf[pos + i]);
            }
        }

        public abstract void finish();
    }

    public static class BufferedByteReader {

        private static final int BUFFER_SIZE = 1024 * 2;

        private byte[] buffer;

        private int pos = 0;

        private int len = 0;

        private InputStream inputStream;

        public BufferedByteReader(InputStream inputStream) {
            this.inputStream = inputStream;
            this.buffer = new byte[BUFFER_SIZE];
        }

        public int nextByte() throws IOException {
            while (pos >= len) {
                len = inputStream.read(buffer);
                if (len < 0) {
                    return -1;
                }
                pos = 0;
            }
            return buffer[pos++];
        }

    }

    public static class OutputStreamBytesAppender extends BytesAppender {

        private OutputStream outputStream;

        public OutputStreamBytesAppender(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void append(byte b) {
            try {
                outputStream.write(b);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void append(byte[] buf, int pos, int len) {
            try {
                outputStream.write(buf, pos, len);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void finish() {
            try {
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class SizeLimitedBytesQueue {
        private byte[] data0;
        private byte[] data1;

        private byte[] currentData;

        private int first = 0;

        private int last = 0;

        private final int maxSize;

        public SizeLimitedBytesQueue(int maxSize) {
            this.maxSize = maxSize;
            data0 = new byte[maxSize];
            data1 = new byte[maxSize];
            currentData = data0;
        }

        public int size() {
            return last - first;
        }

        public void addLast(byte b) {
            if (size() + 1 > maxSize) {
                throw new IllegalStateException("byte queue is full");
            }
            while (last >= maxSize - 1) {
                flap();
            }
            currentData[last++] = b;
        }

        public void addLast(byte[] buf) {
            addLast(buf, 0, buf.length);
        }

        public void addLast(byte[] buf, int pos, int len) {
            if (size() + len > maxSize) {
                throw new IllegalStateException("byte queue is full");
            }
            while (last >= maxSize - len) {
                flap();
            }
            System.arraycopy(buf, pos, currentData, last, len);
            last += len;
        }

        public int removeFirst(byte[] buf) {
            return removeFirst(buf, 0, buf.length);
        }

        public int removeFirst(byte[] buf, int pos, int len) {
            int size = size();
            int copySize = Math.min(size, len);
            System.arraycopy(currentData, first, buf, pos, copySize);
            first += copySize;
            return copySize;
        }

        private void flap() {
            byte[] srcData, destData;
            if (currentData == data0) {
                srcData = data0;
                destData = data1;
                currentData = data1;
            } else {
                srcData = data1;
                destData = data0;
                currentData = data0;
            }
            int len = size();
            System.arraycopy(srcData, first, destData, 0, len);
            first = 0;
            last = len;
        }
    }

}

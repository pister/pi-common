package com.github.pister.common.lang.file;


import com.github.pister.common.io.FastByteArrayInputStream;
import com.github.pister.common.lang.util.IoUtil;

import java.io.*;
import java.util.List;

/**
 * Created by songlihuang on 2021/3/8.
 */
public class BlockFileUtil {

    private static final byte[] END_OF_BLOCK_LENGTH_BYTES = {0, 0, 0, 0};

    private static final int MAX_DATA_LENGTH = 1024 * 1024 * 4;

    /**
     * 把多个文件合并成文件块
     * @param inputFiles
     * @param outOutput
     * @return
     * @throws IOException
     */
    public static boolean makeFilesBlock(List<File> inputFiles, File outOutput) throws IOException {
        if (inputFiles == null || inputFiles.size() == 0 || outOutput == null) {
            return false;
        }
        FileOutputStream outputStream = new FileOutputStream(outOutput);
        try {
            for (File file : inputFiles) {
                int len = (int)file.length();
                if (len > MAX_DATA_LENGTH) {
                    throw new IllegalStateException("too big file length:" + len);
                }
                byte[] fileLength = new byte[]{(byte) (len >> 24), (byte) ((0xFF0000 & len) >> 16), (byte) ((0xFF00 & len) >> 8), (byte) (0xFF & len)};
                outputStream.write(fileLength);
                FileInputStream is = new FileInputStream(file);
                try {
                    IoUtil.copy(is, outputStream);
                } finally {
                    IoUtil.close(is);
                }
            }
            outputStream.write(END_OF_BLOCK_LENGTH_BYTES);
        } finally {
            IoUtil.close(outputStream);
        }
        return true;
    }

    public static boolean makeBlock(List<byte[]> dataList, File outOutput) throws IOException {
        if (dataList == null || dataList.size() == 0 || outOutput == null) {
            return false;
        }
        FileOutputStream outputStream = new FileOutputStream(outOutput);
        try {
            for (byte[] data : dataList) {
                int len = data.length;
                if (len > MAX_DATA_LENGTH) {
                    throw new IllegalStateException("too big file length:" + len);
                }
                byte[] fileLength = new byte[]{(byte) (len >> 24), (byte) ((0xFF0000 & len) >> 16), (byte) ((0xFF00 & len) >> 8), (byte) (0xFF & len)};
                outputStream.write(fileLength);
                FastByteArrayInputStream is = new FastByteArrayInputStream(data);
                try {
                    IoUtil.copy(is, outputStream);
                } finally {
                    IoUtil.close(is);
                }
            }
            outputStream.write(END_OF_BLOCK_LENGTH_BYTES);
        } finally {
            IoUtil.close(outputStream);
        }
        return true;
    }


    public interface  BlockExtractCallback {

        void onInit();

        void onFinish();

        void onData(byte[] data) throws Exception;

    }

    /**
     * 从一个文件块从释放出各个文件
     * @param inputStream
     * @param maxCount 最大释放多少个，-1代表不限制数量
     * @param callback
     * @return
     * @throws Exception
     */
    public static int extractFromBlock(InputStream inputStream, int maxCount, BlockExtractCallback callback) throws Exception {
        if (maxCount < 0) {
            maxCount = Integer.MAX_VALUE;
        }
        int count = 0;
        callback.onInit();
        try {
            while (count < maxCount) {
                int v0 = inputStream.read();
                if (v0 < 0) {
                    throw new IllegalStateException("EOF");
                }
                int v1 = inputStream.read();
                if (v1 < 0) {
                    throw new IllegalStateException("EOF");
                }
                int v2 = inputStream.read();
                if (v2 < 0) {
                    throw new IllegalStateException("EOF");
                }
                int v3 = inputStream.read();
                if (v3 < 0) {
                    throw new IllegalStateException("EOF");
                }

                int len = (v0 << 24) | (v1 << 16) | (v2 << 8) | v3;
                if (len > MAX_DATA_LENGTH) {
                    throw new IllegalStateException("too big file length:" + len);
                }
                if (len == 0) {
                    break;
                }
                byte[] data = new byte[len];
                readFully(inputStream, data);
                callback.onData(data);
                count++;
            }
        } finally {
            IoUtil.close(inputStream);
            callback.onFinish();
        }
        return count;
    }

    private static void readFully(InputStream inputStream, byte[] buffer) throws IOException {
        final int BUFF_LEN = buffer.length;
        int len;
        int offset = 0;
        while (offset < BUFF_LEN) {
            len = inputStream.read(buffer, offset, BUFF_LEN - offset);
            if (len < 0) {
                throw new IllegalStateException("read EOF");
            }
            offset += len;
        }
    }

}

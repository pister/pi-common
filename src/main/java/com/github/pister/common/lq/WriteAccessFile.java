package com.github.pister.common.lq;

import com.github.pister.common.lang.util.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * 4 - bytes length
 * data *---
 * 4 - bytes crc32 sum
 * <p>
 * User: huangsongli
 * Date: 16/12/21
 * Time: 上午9:48
 */
public class WriteAccessFile {

    private static final Logger log = LoggerFactory.getLogger(WriteAccessFile.class);

    private Index writeIndex;

    private File basePath;

    private int currentFileIndex;

    private OutputStream os;

    public WriteAccessFile(Index writeIndex, File basePath) {
        this.writeIndex = writeIndex;
        this.basePath = basePath;
    }

    public void prepare() throws IOException {
        int writeFileIndex = writeIndex.getFileIndex();
        ensureOpenFile(writeFileIndex);
        log.warn("prepare write for " + basePath + " done, prepare writer, file index: " + writeFileIndex + ", position index:" + writeIndex.getPositionIndex());
    }

    public synchronized void writeData(byte[] data) throws IOException {
        int dataLength = data.length;
        if (dataLength > Constants.MAX_DATA_SIZE) {
            throw new IllegalArgumentException("accept max data length: " + Constants.MAX_DATA_SIZE + ", but your: " + dataLength);
        }
        int deltaLength = dataLength + Constants.BASE_LENGTH;
        int writeFileIndex = writeIndex.getFileIndex();

        ensureOpenFile(writeFileIndex);

        ByteBuffer dataBuffer = ByteBuffer.allocate(deltaLength);
        dataBuffer.putInt(dataLength);
        dataBuffer.put(data);
        int sum = LqIoUtil.getDataSum(data);
        dataBuffer.putInt(sum);
        dataBuffer.flip();
        writeData(dataBuffer);
        os.flush();
        writeIndex.incrPosition();
    }

    private void writeData(ByteBuffer dataBuffer) throws IOException {
        os.write(dataBuffer.array());
    }

    private void ensureOpenFile(int writeFileIndex) throws IOException {
        if (this.os == null) {
            openNewFile(writeFileIndex, FileUtil.getDataFile(basePath, writeFileIndex));
            return;
        }
        if (currentFileIndex == writeFileIndex) {
            return;
        }
        openNewFile(writeFileIndex, FileUtil.getDataFile(basePath, writeFileIndex));
    }


    private void openNewFile(int writeFileIndex, File file) throws IOException {
        OutputStream newOs = new FileOutputStream(file, true);
        OutputStream oldOs = os;
        os = newOs;
        currentFileIndex = writeFileIndex;

        IoUtil.close(oldOs);
    }

}

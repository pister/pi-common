package com.github.pister.common.lq;

import com.github.pister.common.lang.util.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * User: huangsongli
 * Date: 16/12/21
 * Time: 上午11:10
 */
public class ReadAccessFile {

    private static final Logger log = LoggerFactory.getLogger(ReadAccessFile.class);

    private Index readIndex;

    private Index writeIndex;

    private File basePath;

    private int currentFileIndex;

    public ReadAccessFile(Index readIndex, Index writeIndex, File basePath) {
        this.readIndex = readIndex;
        this.writeIndex = writeIndex;
        this.basePath = basePath;
    }

    private RandomAccessFile readFile;

    public void prepare() throws IOException {
        // 读到上次读到的地方再继续，否则会重新读完整个文件导致重读
        int fileIndex = readIndex.getFileIndex();
        int positionIndex = readIndex.getPositionIndex();
        if (fileIndex == 0 && positionIndex == 0) {
            log.warn("prepare read for " + basePath + " done, prepare read pointer:" + 0 + ", file index: " + fileIndex + ", position index:" + 0);
            return;
        }
        ensureOpenFile(fileIndex);
        int count = 0;
        int position = positionIndex;
        while (count < position) {
            if (log.isDebugEnabled()) {
                log.debug("prepare for read: " + count + " : " + position);
            }
            readImpl();
            count++;
        }
        long pointer = readFile.getFilePointer();
        log.warn("prepare read for " + basePath + " done, prepare read pointer:" + pointer + ", file index: " + fileIndex + ", position index:" + position);
    }

    private byte[] readImpl() throws IOException {
        int len = readFile.readInt();

        if (len > Constants.MAX_DATA_SIZE) {
            throw new RuntimeException("invalidate len: " + len);
        }

        byte[] data = new byte[len];
        readFile.readFully(data);
        int sumFromFile = readFile.readInt();
        int sumFromData = LqIoUtil.getDataSum(data);
        if (sumFromData != sumFromFile) {
            throw new RuntimeException("invalidate sum, data: " + sumFromData + ", but file:" + sumFromFile);
        }
        return data;
    }

    public synchronized byte[] readData(long waitTimeMillis) throws InterruptedException, IOException {
        long ts = System.currentTimeMillis();
        while (!hasAvailable()) {
            long delta = System.currentTimeMillis() - ts;
            if (delta >= waitTimeMillis) {
                return null;
            }
            Thread.sleep(100);
        }

        int fileIndex, position;
        while (true) {
            fileIndex = readIndex.getFileIndex();
            position = readIndex.getPositionIndex();
            ensureOpenFile(fileIndex);
            // 这里处理如果碰到读到文件末尾的时候切换到下一个文件
            if (readFile.length() >= position + Constants.BASE_LENGTH) {
                break;
            }
            readIndex.tryTurnNextFile();
        }
        if (log.isDebugEnabled()) {
            log.debug("read file pointer:" + readFile.getFilePointer());
        }
        byte[] data = readImpl();
        readIndex.incrPosition();
        return data;
    }


    private void ensureOpenFile(int readFileIndex) throws IOException {
        if (this.readFile == null) {
            openNewFile(readFileIndex, FileUtil.getDataFile(basePath, readFileIndex), "r");
            return;
        }
        if (currentFileIndex == readFileIndex) {
            return;
        }
        openNewFile(readFileIndex, FileUtil.getDataFile(basePath, readFileIndex), "r");
    }


    private void openNewFile(int writeFileIndex, File file, String mode) throws IOException {
        RandomAccessFile newRandomAccessFile = new RandomAccessFile(file, mode);
        RandomAccessFile oldRandomAccessFile = readFile;
        readFile = newRandomAccessFile;
        currentFileIndex = writeFileIndex;
        IoUtil.close(oldRandomAccessFile);
    }

    private boolean hasAvailable() {
        int readFileIndex = readIndex.getFileIndex();
        int readPositionIndex = readIndex.getPositionIndex();
        int writeFileIndex = writeIndex.getFileIndex();
        int writePositionIndex = writeIndex.getPositionIndex();
        // log.warn("read: [" + readFileIndex + "," + readPositionIndex + "]" + " vs write: [" + writeFileIndex + "," + writePositionIndex + "]");
        if (readIndex.getFileIndex() < writeIndex.getFileIndex()) {
            return true;
        }
        if (readIndex.getFileIndex() == writeIndex.getFileIndex()) {
            return readIndex.getPositionIndex() < writeIndex.getPositionIndex();
        }
        return false;
    }


}

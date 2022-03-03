package com.github.pister.common.lq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: huangsongli
 * Date: 16/12/21
 * Time: 上午9:45
 */
public class Index {

    private static final Logger log = LoggerFactory.getLogger(Index.class);

    private AtomicInteger fileIndex;

    private AtomicInteger positionIndex;

    private MappedByteBuffer mappedByteBuffer;

    public Index(File metaFile) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(metaFile, "rwd");
        long len = randomAccessFile.length();
        mappedByteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 8);
        if (len == 0) {
            fileIndex = new AtomicInteger(0);
            positionIndex = new AtomicInteger(0);
        } else {
            fileIndex = new AtomicInteger(mappedByteBuffer.getInt(0));
            positionIndex = new AtomicInteger(mappedByteBuffer.getInt(4));
        }
        log.warn("index loaded:" + metaFile + " with fileIndex:" + fileIndex + ", positionIndex:" + positionIndex.get());
    }


    public int getFileIndex() {
        return fileIndex.get();
    }

    public int getPositionIndex() {
        return positionIndex.get();
    }

    public void incrPosition() {
        while (true) {
            int currentPosition = positionIndex.get();
            int newPosition = currentPosition + 1;
            if (positionIndex.compareAndSet(currentPosition, newPosition)) {
                mappedByteBuffer.putInt(4, newPosition);
                break;
            }
        }

        tryTurnNextFile();
    }

    private void tryIncrFileIndex() {
        while (true) {
            int positionValue = positionIndex.get();
            if (positionValue < Constants.MAX_POSITION_PER_FILE) {
                break;
            }
            int currentFile = fileIndex.get();
            int newFile = currentFile + 1;
            if (fileIndex.compareAndSet(currentFile, newFile)) {
                mappedByteBuffer.putInt(0, newFile);

                positionIndex.set(0);
                mappedByteBuffer.putInt(4, 0);
                break;
            }
        }
    }


    public boolean tryTurnNextFile() {
        if (positionIndex.get() < Constants.MAX_POSITION_PER_FILE) {
            return false;
        }
        tryIncrFileIndex();
        if (log.isDebugEnabled()) {
            log.debug("turn to next file:" + fileIndex.get() + ", " + positionIndex.get());
        }
        return true;
    }
}

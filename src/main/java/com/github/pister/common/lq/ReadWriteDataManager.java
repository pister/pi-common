package com.github.pister.common.lq;

import java.io.File;
import java.io.IOException;

/**
 * User: huangsongli
 * Date: 16/12/21
 * Time: 下午2:35
 */
public class ReadWriteDataManager {

    private Index readIndex;

    private Index writeIndex;

    private File path;

    private File dataPath;

    private File indexPath;

    private WriteAccessFile writeAccessFile;

    private ReadAccessFile readAccessFile;

    private Thread deleteThread;

    public ReadWriteDataManager(File path) {
        this.path = path;
    }

    public void init() throws IOException {
        if (!path.exists()) {
            path.mkdirs();
        }
        dataPath = new File(path, "data");
        if (!dataPath.exists()) {
            dataPath.mkdirs();
        }
        indexPath = new File(path, "index");
        if (!indexPath.exists()) {
            indexPath.mkdirs();
        }
        readIndex = loadIndex("read_index");
        writeIndex = loadIndex("write_index");

        writeAccessFile = new WriteAccessFile(writeIndex, dataPath);
        readAccessFile = new ReadAccessFile(readIndex, writeIndex, dataPath);

        writeAccessFile.prepare();
        readAccessFile.prepare();

        deleteThread = new DeleteThread();
        deleteThread.start();
    }

    private class DeleteThread extends Thread {

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            while (true) {
                int fileIndex = readIndex.getFileIndex();
                for (int i = 0; i < fileIndex; ++i) {
                    File file = FileUtil.getDataFile(dataPath, i);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void writeData(byte[] data) throws IOException {
        writeAccessFile.writeData(data);
    }

    public byte[] readData(long waitTimeMillis) throws IOException, InterruptedException {
        return readAccessFile.readData(waitTimeMillis);
    }

    private Index loadIndex(String name) throws IOException {
        File file = new File(indexPath, name);
        return new Index(file);
    }

}

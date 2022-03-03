package com.github.pister.common.archive;

import com.github.pister.common.lang.util.IoUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/1/21
 * Time: 下午3:30
 */
public class DirectoryExtractor {

    private static Map<String, Extractor> extractorMap = new HashMap<String, Extractor>();

    static {
        extractorMap.put("zip", new ZipExtractor());
        extractorMap.put("rar", new RarExtractor());
    }

    private String path;
    private String outFile;

    public DirectoryExtractor(String path, String outFile) {
        this.path = path;
        this.outFile = outFile;
    }

    private static void deleteFiles(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : dir.listFiles()) {
                    deleteFiles(file);
                }
            }

            dir.delete();
            return;
        }
        dir.delete();
    }

    public int extract() throws IOException {
        RecordCountCallback recordCountCallback = new RecordCountCallback();
        int total = extractImpl(new File(path), new File(outFile), false, recordCountCallback);
        return total;
    }

    public void deleteFix() {
        deleteFix(new File(outFile));
    }

    private void deleteFix(File file) {
        if (!file.isDirectory()) {
            return;
        }
        String name = file.getName();
        name = name.toLowerCase();
        if (name.endsWith(".zip") || name.endsWith(".rar")) {
            deleteFiles(file);
        }
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            deleteFix(f);
        }
    }

    private int extractImpl(File inputFile, File outBase, boolean deleteSrcFile, CountCallback countCallback) throws IOException {
        int count = 0;
        File childOutFile = new File(outBase, inputFile.getName());
        if (inputFile.isDirectory()) {
            File[] files = inputFile.listFiles();
            if (!childOutFile.exists()) {
                childOutFile.mkdirs();
            }
            for (File file : files) {
                count += extractImpl(file, childOutFile, deleteSrcFile, countCallback);
            }
            countCallback.onCount(count);
            return count;
        }

        String name = inputFile.getName();
        String ext = FileUtils.getExt(name);
        if (ext == null || ext.length() == 0) {
            copyFile(inputFile, childOutFile);
            count = 1;
            countCallback.onCount(count);
            return count;
        }
        Extractor extractor = extractorMap.get(ext);
        if (extractor == null) {
            copyFile(inputFile, childOutFile);
            count = 1;
            countCallback.onCount(count);
            return count;
        }
        try {
            extractor.extractTo(inputFile, childOutFile);
            // 如果解压出来还是存在压缩文件，则继续解压
            count = extractImpl(childOutFile, new File(outBase, FileUtils.getNormal(name)), true, countCallback);

            if (deleteSrcFile) {
                inputFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        countCallback.onCount(count);
        return count;
    }

    private void copyFile(File src, File dest) throws IOException {
        IoUtil.copyAndClose(new FileInputStream(src), new FileOutputStream(dest));
        // IOUtils.copy(src, dest);
    }

    public static interface CountCallback {
        void onCount(int count);
    }

    private static class RecordCountCallback implements CountCallback {

        private int total = 0;

        @Override
        public void onCount(int count) {
            total += count;
        }

        private int getTotal() {
            return total;
        }
    }

}

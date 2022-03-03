package com.github.pister.common.archive;

import com.github.pister.common.archive.zip.ZipEntry;
import com.github.pister.common.archive.zip.ZipFile;
import com.github.pister.common.lang.util.IoUtil;

import java.io.*;
import java.util.Enumeration;

/**
 * User: huangsongli
 * Date: 16/1/21
 * Time: 下午3:50
 */
public class ZipExtractor implements Extractor {

    private String charset = "utf-8";

    @Override
    public void extractTo(File inputFile, File outPath) throws IOException {
        byte[] buf = new byte[2048];
        ZipFile zipFile = new ZipFile(inputFile, charset);
        try {
            Enumeration enumeration = zipFile.getEntries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enumeration.nextElement();
                String zipEntryName = entry.getName();
                File targetFile = new File(outPath, zipEntryName);
                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                    continue;
                }
                File parentFile = targetFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }

                InputStream inputStream = zipFile.getInputStream(entry);
                OutputStream out = new FileOutputStream(targetFile);
                try {
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    IoUtil.close(inputStream);
                    IoUtil.close(out);
                }
            }
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }
    }
}

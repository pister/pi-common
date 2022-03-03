package com.github.pister.common.archive;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.github.pister.tson.utils.StringUtil;
import com.github.pister.common.lang.util.IoUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: huangsongli
 * Date: 16/1/21
 * Time: 下午3:48
 */
public class RarExtractor implements Extractor {

    @Override
    public void extractTo(File inputFile, File outFile) throws IOException {
        Archive archive = null;
        try {
            archive = new Archive(inputFile);
            while (true) {
                FileHeader fileHeader = archive.nextFileHeader();
                if (fileHeader == null) {
                    break;
                }
                String name = fileHeader.getFileNameW();
                if (StringUtil.isBlank(name)) {
                    continue;
                }
                File path = new File(outFile, name);
                if (fileHeader.isDirectory()) {
                    path.mkdirs();
                    continue;
                }
                File parentFile = path.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path);
                try {
                    archive.extractFile(fileHeader, fos);
                } finally {
                    IoUtil.close(fos);
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IoUtil.close(archive);
        }
    }
}

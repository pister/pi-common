package com.github.pister.common.lang.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * User: huangsongli
 * Date: 16/2/26
 * Time: 下午5:15
 */
public class CompressUtil {

    private static final boolean ENABLED = true;

    public static InputStream unzipInputStream(InputStream is) throws IOException {
        if (!ENABLED) {
            return is;
        }
        ZipInputStream zipInputStream = new ZipInputStream(is);
        if (zipInputStream.getNextEntry() != null) {
            return zipInputStream;
        }
        return null;
    }

    public static OutputStream zipOutputStream(OutputStream outputStream) throws IOException {
        if (!ENABLED) {
            return outputStream;
        }
        ZipOutputStream zipOs = new ZipOutputStream(outputStream);
        ZipEntry entry = new ZipEntry("data");
        zipOs.putNextEntry(entry);
        return zipOs;
    }

    public static byte[] unzip(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IoUtil.copyAndClose(unzipInputStream(is), bos);
        return bos.toByteArray();
    }

    public static byte[] zip(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IoUtil.copyAndClose(is, zipOutputStream(bos));
        return bos.toByteArray();
    }


}

package com.github.pister.common.archive;

import java.io.File;

/**
 * User: huangsongli
 * Date: 16/1/22
 * Time: 上午10:54
 */
public class FileUtils {

    public static void deleteDirectory(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (!dir.isDirectory()) {
            dir.delete();
            return;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : dir.listFiles()) {
                deleteDirectory(file);
            }
        }
        dir.delete();
    }

    public static String getExt(String name) {
        if (name == null) {
            return null;
        }
        int pos = name.lastIndexOf(".");
        if (pos < 0) {
            return null;
        }
        String x = name.substring(pos + 1);
        return x.toLowerCase();
    }

    public static String getNormal(String name) {
        if (name == null) {
            return null;
        }
        int pos = name.lastIndexOf(".");
        if (pos < 0) {
            return name;
        }
        String x = name.substring(0, pos);
        return x.toLowerCase();
    }


}

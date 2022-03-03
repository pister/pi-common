package com.github.pister.common.lq;

import java.io.File;

/**
 * User: huangsongli
 * Date: 16/12/21
 * Time: 下午1:58
 */
public class FileUtil {

    public static File getDataFile(File basePath, int fileIndex) {
        return new File(basePath, "data_" + fileIndex);
    }

}

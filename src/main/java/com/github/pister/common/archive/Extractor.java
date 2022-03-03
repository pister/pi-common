package com.github.pister.common.archive;

import java.io.File;
import java.io.IOException;

/**
 * User: huangsongli
 * Date: 16/1/21
 * Time: 下午3:47
 */
public interface Extractor {

    void extractTo(File inputFile, File outPath) throws IOException;

}

package com.github.pister.common.lq;

/**
 * User: huangsongli
 * Date: 16/12/21
 * Time: 上午10:05
 */
public interface Constants {

    int MAX_POSITION_PER_FILE = 1 * 10000; // 20万个对象

    int BASE_LENGTH = 4 + 4;

    int MAX_DATA_SIZE = Integer.MAX_VALUE - BASE_LENGTH;



}

package com.github.pister.common.lang.util;

/**
 * Created by songlihuang on 2022/3/3.
 */
public interface Transformer<S, T> {

    T transform(S object);

}

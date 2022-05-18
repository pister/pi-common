package com.github.pister.common.ddl.runtime;

/**
 * Created by songlihuang on 2019/3/22.
 */
public interface InputProperties {

    Object getProperty(String name);

    Object getPropertyValues();
}

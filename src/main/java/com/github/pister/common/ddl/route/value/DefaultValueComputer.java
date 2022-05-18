package com.github.pister.common.ddl.route.value;


import com.github.pister.common.ddl.route.RouteUtil;

/**
 * Created by songlihuang on 2017/6/15.
 */
public class DefaultValueComputer implements ValueComputer {
    @Override
    public long compute(Object input) {
        return RouteUtil.getLongValue(input);
    }

}

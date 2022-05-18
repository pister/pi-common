package com.github.pister.common.ddl.route;

import wint.lang.utils.MapUtil;

import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 上午10:09
 */
public final class Routers {

    private Routers() {}

    private static Map<String, Class<? extends Router>> namedRouterClasses = MapUtil.newHashMap();

    static {
        namedRouterClasses.put("mod", FieldModRouter.class);
        namedRouterClasses.put("parentChildMod", ParentChildPropertyModRouter.class);
    }

    public static Class<? extends Router> getRouterType(String name) {
        return namedRouterClasses.get(name);
    }


}

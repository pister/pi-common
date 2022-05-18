package com.github.pister.common.ddl.route;

import com.github.pister.common.ddl.config.TableConfig;
import wint.lang.magic.MagicClass;
import wint.lang.magic.MagicObject;
import wint.lang.utils.MapUtil;

import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 上午10:02
 */
public class DefaultRouterManger implements RouterManger {

    private Map<String, TableConfig> tableConfigMap = MapUtil.newHashMap();

    private Map<String, Router> routerMap = MapUtil.newHashMap();

    public void init() {
        for (Map.Entry<String, TableConfig> entry : tableConfigMap.entrySet()) {
            String name = entry.getKey();
            TableConfig tableConfig = entry.getValue();
            Router router = initRouter(tableConfig);
            routerMap.put(name, router);
        }
    }

    public void setTableConfigMap(Map<String, TableConfig> tableConfigMap) {
        this.tableConfigMap = tableConfigMap;
    }

    private Router initRouter(TableConfig tableConfig) {
        String type = tableConfig.getRouteType();
        Class<? extends Router> clazz = Routers.getRouterType(type);
        MagicClass magicClass;
        if (clazz == null) {
            magicClass = MagicClass.forName(tableConfig.getRouteType());
        } else {
            magicClass = MagicClass.wrap(clazz);
        }
        if (!magicClass.isAssignableTo(Router.class)) {
            throw new RuntimeException(tableConfig.getRouteType() + " is not a subType of Router.");
        }
        MagicObject magicObject = magicClass.newInstance();
        Map<String, String> params = tableConfig.getParams();
        if (!MapUtil.isEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                magicObject.setPropertyValueExt(entry.getKey(), entry.getValue());
            }
        }
        return (Router)magicObject.getObject();
    }

    @Override
    public TableConfig getTableConfig(String name) {
        return tableConfigMap.get(name);
    }

    @Override
    public Router getRouter(String name) {
        Router router = routerMap.get(name);
        if (router == null) {
            throw new RuntimeException("no router exist for: " + name);
        }
        return router;
    }
}

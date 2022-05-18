package com.github.pister.common.ddl.config;

import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/27
 * Time: 下午5:55
 */
public class TableConfig {

    private String routeType;

    private Map<String, String> params;

    private String idName = "id";

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

}
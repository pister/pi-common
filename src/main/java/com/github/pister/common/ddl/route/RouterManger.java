package com.github.pister.common.ddl.route;


import com.github.pister.common.ddl.config.TableConfig;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 上午10:01
 */
public interface RouterManger {

    Router getRouter(String name);

    TableConfig getTableConfig(String name);
}

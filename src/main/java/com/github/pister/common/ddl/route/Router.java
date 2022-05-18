package com.github.pister.common.ddl.route;


import com.github.pister.common.ddl.runtime.InputProperties;
import com.github.pister.common.ddl.shard.ShardInfo;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 上午9:57
 */
public interface Router {

    ShardInfo route(InputProperties inputProperties);

}

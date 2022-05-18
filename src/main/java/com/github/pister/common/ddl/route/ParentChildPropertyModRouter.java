package com.github.pister.common.ddl.route;

import com.github.pister.common.ddl.route.value.DefaultValueComputer;
import com.github.pister.common.ddl.route.value.ValueComputer;
import com.github.pister.common.ddl.runtime.InputProperties;
import com.github.pister.common.ddl.shard.ShardInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 父子字段路由
 * 先根据父字段找到库索引，再根据子字段找到该库中的表索引。
 * 使用父子字段路由的场景是，根据父字段shard可能会出现不均匀，而再根据子字段分表可以避免这种情况
 *
 * parentTableTotalCount = dbCount * parentTableCount;
 * parentTableIndex = parentValue % parentTableTotalCount;
 * int dbIndex = parentTableIndex / parentTableCount;
 * int childTableIndex = (childValue % childTableCount) + dbIndex * childTableCount
 *
 * User: huangsongli
 * Date: 16/4/29
 * Time: 上午9:21
 */
public class ParentChildPropertyModRouter implements Router {

    private static final Logger log = LoggerFactory.getLogger(ParentChildPropertyModRouter.class);

    private String parentProperty;

    private String childProperty;

    /**
     * 数据库总数量
     */
    private int dbCount = 1;

    /**
     * 每个数据库中父表的数量
     */
    private int parentTableCount = 1;
    /**
     * 每个数据库中子表的数量
     */
    private int childTableCount = 1;

    private ValueComputer valueComputer1 = new DefaultValueComputer();

    private ValueComputer valueComputer2 = new DefaultValueComputer();

    @Override
    public ShardInfo route(InputProperties inputProperties) {
        Object v1 = inputProperties.getProperty(parentProperty);
        if (v1 == null) {
            throw new RuntimeException("miss property: " + parentProperty);
        }
        Object v2 = inputProperties.getProperty(childProperty);
        if (v2 == null) {
            throw new RuntimeException("miss property: " + childProperty);
        }
        long parentValue = valueComputer1.compute(v1); //RouteUtil.getLongValue(v1);
        long childValue = valueComputer2.compute(v2); //RouteUtil.getLongValue(v2);
        int parentTableTotalCount = dbCount * parentTableCount;
        if (parentTableTotalCount == 0) {
            throw new RuntimeException("parentTableTotalCount can not be zero!");
        }
        if (childTableCount == 0) {
            throw new RuntimeException("childTableCount can not be zero!");
        }
        int parentTableIndex = (int) (parentValue % parentTableTotalCount);
        int dbIndex = parentTableIndex / parentTableCount;
        int childTableIndex = (int) (childValue % childTableCount) + dbIndex * childTableCount;
        if (log.isDebugEnabled()) {
            log.warn(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            log.debug("parentValue: " + parentValue);
            log.debug("childValue: " + childValue);
            log.debug("dbCount: " + dbCount);
            log.debug("parentTableCount: " + parentTableCount);
            log.debug("childTableCount: " + childTableCount);
            log.debug("childTableIndex: " + childTableIndex);
            log.debug("dbIndex: " + dbIndex);
            log.warn("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }
        ShardInfo shardInfo = new ShardInfo();
        shardInfo.setDatabaseIndex(dbIndex);
        shardInfo.setTableIndex(childTableIndex);
        return shardInfo;
    }

    public void setParentProperty(String parentProperty) {
        this.parentProperty = parentProperty;
    }

    public void setChildProperty(String childProperty) {
        this.childProperty = childProperty;
    }

    public void setDbCount(int dbCount) {
        this.dbCount = dbCount;
    }

    public void setParentTableCount(int parentTableCount) {
        this.parentTableCount = parentTableCount;
    }

    public void setChildTableCount(int childTableCount) {
        this.childTableCount = childTableCount;
    }

}
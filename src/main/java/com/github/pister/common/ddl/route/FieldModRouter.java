package com.github.pister.common.ddl.route;


import com.github.pister.common.ddl.route.value.DefaultValueComputer;
import com.github.pister.common.ddl.route.value.ValueComputer;
import com.github.pister.common.ddl.runtime.InputProperties;
import com.github.pister.common.ddl.shard.ShardInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wint.lang.magic.MagicClass;
import wint.lang.magic.MagicObject;

/**
 * 单个字段路由
 * <p/>
 * totalTableCount =  dbCount * tableCount;
 * tableIndex = value % totalTableCount;
 * dbIndex = tableIndex / tableCount;
 * <p/>
 * User: huangsongli
 * Date: 16/4/28
 * Time: 下午2:45
 */
public class FieldModRouter implements Router {

    private static final Logger log = LoggerFactory.getLogger(FieldModRouter.class);
    private String property;
    /**
     * 数据库总数量
     */
    private int dbCount = 1;
    /**
     * 每个数据库中的表数量
     */
    private int tableCount = 1;

    private ValueComputer valueComputer = new DefaultValueComputer();

    @Override
    public ShardInfo route(InputProperties inputProperties) {
        Object value = inputProperties.getProperty(property);
        if (value == null) {
            throw new RuntimeException("miss property: " + property);
        }
        long longValue = valueComputer.compute(value); // RouteUtil.getLongValue(value);
        int totalTableCount = dbCount * tableCount;
        if (totalTableCount == 0) {
            throw new RuntimeException("totalTableCount can not be zero!");
        }
        int tableIndex = (int) (longValue % totalTableCount);
        int dbIndex = tableIndex / tableCount;
        if (log.isDebugEnabled()) {
            log.debug("value: " + value);
            log.debug("longValue: " + longValue);
            log.debug("dbCount: " + dbCount);
            log.debug("tableCount: " + tableCount);
            log.debug("totalTableCount: " + totalTableCount);
            log.debug("tableIndex: " + tableIndex);
            log.debug("dbIndex: " + dbIndex);
        }
        ShardInfo shardInfo = new ShardInfo();
        shardInfo.setDatabaseIndex(dbIndex);
        shardInfo.setTableIndex(tableIndex);
        return shardInfo;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setDbCount(int dbCount) {
        this.dbCount = dbCount;
    }

    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }

    public void setValueComputerClass(String valueComputerClass) {
        MagicClass magicClass = MagicClass.forName(valueComputerClass);
        MagicObject magicObject = magicClass.newInstance();
        if (!magicClass.isAssignableTo(ValueComputer.class)) {
            throw new RuntimeException(valueComputerClass + " is not instanceof ValueComputer.");
        }
        this.valueComputer = (ValueComputer)magicObject.get();
    }
}

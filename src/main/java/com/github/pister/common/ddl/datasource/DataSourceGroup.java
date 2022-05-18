package com.github.pister.common.ddl.datasource;

import wint.lang.utils.CollectionUtil;

import javax.sql.DataSource;
import java.util.List;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 下午2:09
 */
public class DataSourceGroup {

    private DataSource master;

    private List<DataSource> slavers = CollectionUtil.newArrayList(2);

    public DataSource getMaster() {
        return master;
    }

    public void setMaster(DataSource master) {
        this.master = master;
    }

    public List<DataSource> getSlavers() {
        return slavers;
    }

    public void setSlavers(List<DataSource> slavers) {
        this.slavers = slavers;
    }

    public void addSlaver(DataSource slaver) {
        this.slavers.add(slaver);
    }

}

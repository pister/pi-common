package com.github.pister.common.ddl.datasource;

import wint.lang.magic.MagicClass;
import wint.lang.magic.MagicObject;
import wint.lang.utils.CollectionUtil;
import wint.lang.utils.MapUtil;
import wint.lang.utils.StringUtil;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/29
 * Time: 下午4:12
 */
public class DataSourcePoolMatrix {

    private String dataSourceDriverClass;
    private String urlProperty = "url";
    private String initMethod;
    private String destroyMethod;
    private String urlPattern = "jdbc:mysql://%s?useUnicode=true&amp;characterEncoding=utf8&amp;zeroDateTimeBehavior=convertToNull&amp;transformedBitIsBoolean=true";
    private Map<String, String> properties = MapUtil.newHashMap();
    private Map<Integer, DataSourceGroup> shardDataSourceGroup;
    /**
     * 表达式格式：
     * <p/>
     * dsName=database，每个配置用;隔开
     * dsName格式：由数字加上字母m或s，前几个数字代表不同shard， 后面字母表示该shard加入的是(m)master还是(s)slaver
     * <p/>
     * database的格式是url[:port]/dbName
     *
     * 举例：
     *  "0m=127.0.0.1:3306/db_00;0s=01:127.0.0.1:3306/db_0b;0s=127.0.0.1/db_0b;1m=127.0.0.1:3306/db_0a";
     */
    private String matrixExpress;

    public void init() {
        if (StringUtil.isEmpty(matrixExpress)) {
            throw new RuntimeException("matrixExpress property can not be empty!");
        }
        Map<Integer, DataSourceGroup> shardDataSourceGroup = MapUtil.newHashMap();
        List<String> parts = StringUtil.splitTrim(matrixExpress, ";");
        for (String part : parts) {
            String dsName = StringUtil.getFirstBefore(part, "=");
            String url = StringUtil.getFirstAfter(part, "=");
            if (!dsName.matches("\\d+[m|s]")) {
                throw new RuntimeException("invalidate format dsName: " + dsName);
            }
            String shardIndex = dsName.substring(0, dsName.length()-1);
            int shardIndexInt = Integer.parseInt(shardIndex);
            char c = dsName.charAt(dsName.length()-1);
            DataSourceGroup dataSourceGroup = shardDataSourceGroup.get(shardIndexInt);
            if (dataSourceGroup == null) {
                dataSourceGroup = new DataSourceGroup();
                shardDataSourceGroup.put(shardIndexInt, dataSourceGroup);
            }
            switch (c) {
                case 'm':
                    dataSourceGroup.setMaster(initDataSource(url));
                    break;
                case 's':
                    dataSourceGroup.addSlaver(initDataSource(url));
                    break;
            }
        }
        this.shardDataSourceGroup = Collections.unmodifiableMap(shardDataSourceGroup);
    }

    public void destroy() {
        if (MapUtil.isEmpty(shardDataSourceGroup) || StringUtil.isEmpty(destroyMethod)) {
            return;
        }
        for (Map.Entry<Integer, DataSourceGroup> entry : shardDataSourceGroup.entrySet()) {
            DataSourceGroup dataSourceGroup = entry.getValue();
            if (dataSourceGroup == null) {
                continue;
            }
            DataSource dataSource = dataSourceGroup.getMaster();
            destroyDataSource(dataSource);

            List<DataSource> slavers = dataSourceGroup.getSlavers();
            if (CollectionUtil.isEmpty(slavers)) {
                continue;
            }
            for (DataSource ds : slavers) {
                destroyDataSource(ds);
            }

        }
    }

    public Map<Integer, DataSourceGroup> getShardDataSourceGroup() {
        return shardDataSourceGroup;
    }

    private void destroyDataSource(DataSource dataSource) {
        if (dataSource == null) {
            return;
        }
        MagicObject magicObject = MagicObject.wrap(dataSource);
        magicObject.invoke(destroyMethod);
    }

    private String makeUrl(String url) {
        return String.format(urlPattern, url);
    }

    private DataSource initDataSource(String url) {
        if (StringUtil.isEmpty(dataSourceDriverClass)) {
            throw new RuntimeException(dataSourceDriverClass + " can not be empty!");
        }
        MagicClass magicClass = MagicClass.forName(dataSourceDriverClass);
        MagicObject magicObject = magicClass.newInstance();

        Map<String, String> newProperties = MapUtil.newHashMap();
        newProperties.putAll(properties);
        newProperties.put(urlProperty, makeUrl(url));

        for (Map.Entry<String, String> entry : newProperties.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            magicObject.setPropertyValueExt(name, value);
        }

        if (initMethod != null) {
            magicObject.invoke(initMethod);
        }

        return (DataSource)magicObject.getObject();
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getUrlProperty() {
        return urlProperty;
    }

    public void setUrlProperty(String urlProperty) {
        this.urlProperty = urlProperty;
    }

    public String getDataSourceDriverClass() {
        return dataSourceDriverClass;
    }

    public void setDataSourceDriverClass(String dataSourceDriverClass) {
        this.dataSourceDriverClass = dataSourceDriverClass;
    }

    public String getInitMethod() {
        return initMethod;
    }

    public void setInitMethod(String initMethod) {
        this.initMethod = initMethod;
    }

    public String getDestroyMethod() {
        return destroyMethod;
    }

    public void setDestroyMethod(String destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    public String getMatrixExpress() {
        return matrixExpress;
    }

    public void setMatrixExpress(String matrixExpress) {
        this.matrixExpress = matrixExpress;
    }
}

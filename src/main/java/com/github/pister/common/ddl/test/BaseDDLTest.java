package com.github.pister.common.ddl.test;

import junit.framework.TestCase;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * User: huangsongli
 * Date: 16/5/3
 * Time: 下午1:54
 */
public abstract class BaseDDLTest extends TestCase {

    protected ApplicationContext applicationContext;

    @Override
    protected final void setUp() throws Exception {
        super.setUp();
        String[] locations = getConfigLocations();
        initApplicationContext(locations);
        onSetup();
    }

    @Override
    protected final void tearDown() throws Exception {

        onTearDown();
    }

    private void initApplicationContext(String[] locations) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(locations);
        AutowireCapableBeanFactory autowireCapableBeanFactory = classPathXmlApplicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
        this.applicationContext = classPathXmlApplicationContext;
    }

    protected void onSetup() {
    }

    protected void onTearDown() {
    }

    protected abstract String[] getConfigLocations();
}

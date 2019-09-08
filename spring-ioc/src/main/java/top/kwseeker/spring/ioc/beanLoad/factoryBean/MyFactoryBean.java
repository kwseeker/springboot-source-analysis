package top.kwseeker.spring.ioc.beanLoad.factoryBean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO：FactoryBean 这种创建Bean的方式的意义？
 */
@Component
public class MyFactoryBean implements FactoryBean {

    @Override
    public Object getObject() throws Exception {
        return new ReentrantLock();
    }

    @Override
    public Class<?> getObjectType() {
        return ReentrantLock.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

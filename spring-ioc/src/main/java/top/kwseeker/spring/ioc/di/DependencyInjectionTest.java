package top.kwseeker.spring.ioc.di;

import top.kwseeker.spring.ioc.bean.BaseBean;
import top.kwseeker.spring.ioc.bean.MyBean;

import java.lang.reflect.Constructor;

/**
 * 模拟依赖注入
 */
public class DependencyInjectionTest {

    public static void main(String[] args) throws Exception {
        Class MyBeanClass = Class.forName("top.kwseeker.spring.ioc.bean.MyBean");

        //检测依赖并创建引用
        //...
        BaseBean baseBean = new BaseBean();     //Spring处理的过程比这复杂的多，最终的结果是生成一个BaseBean的引用

        //BeanUtils.instantiateClass(ctor, args);
        Constructor<MyBean> constructor = MyBeanClass.getConstructor(String.class, BaseBean.class);
        MyBean myBean = constructor.newInstance("test", baseBean);

        System.out.println(myBean.getContent());
    }
}

package top.kwseeker.spring.ioc;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import top.kwseeker.spring.ioc.bean.MyBean;

/**
 * Spring提供的最基本的IoC容器 XmlBeanFactory 的工作原理
 *
 * 这个类已经被废弃
 *
 */
public class XmlBeanFactoryTest {

    public static void main(String[] args) {
        //Resource加载过程：
        // 1）路径清理：使用StringUtils替换window目录分割符"\\"为"/"，清理不规范的路径格式等，细节先不管了
        // 2）path 和 classLoader赋值（没有手动指定classLoader则使用默认的ClassLoader）
        ClassPathResource resource = new ClassPathResource("application-context.xml");

        //XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(resource);
        //Object bean = xmlBeanFactory.getBean("myBean");

        //上面这行代码可分为下面几个流程
        //初始化对象静态属性（包括继承来的），比较重要的是几个容器类用来存放beans的别名，已经被初始化的bean
        //  被忽略的依赖类型，被忽略的接口类型等等（比如实现BeanNameAware接口的类会被添加到这里，然后不会新建这个类的实例而是
        //  创建指向setBeanName中传参name对应的bean的引用）
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        //创建解析XML Bean定义的Reader
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        //使用BeanDefinitionReader从Resource中读取要加载的Bean的BeanDefinition，存放到BeanFactory的beanDefinitionMap中
        reader.loadBeanDefinitions(resource);
        //校验是否已经创建这个bean，没有的话创建
        MyBean bean1 = (MyBean) factory.getBean("myBean");
        System.out.println(bean1.getContent());
    }
}

package top.kwseeker.hello.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = HelloProperties.HELLO_PREFIX)
public class HelloProperties {

    public static final String HELLO_PREFIX = "hello";

    private String name;

    //服务是否开启
    private String enable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }
}

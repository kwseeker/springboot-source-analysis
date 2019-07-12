package top.kwseeker.spring.ioc.bean;

public class MyBean {

    private String content;

    private BaseBean baseBean;

    public MyBean(String content, BaseBean baseBean) {
        this.content = content;
        this.baseBean = baseBean;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BaseBean getBaseBean() {
        return baseBean;
    }

    public void setBaseBean(BaseBean baseBean) {
        this.baseBean = baseBean;
    }
}

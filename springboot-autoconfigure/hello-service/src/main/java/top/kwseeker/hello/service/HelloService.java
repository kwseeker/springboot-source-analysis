package top.kwseeker.hello.service;

public class HelloService {

    private String someone;

    public HelloService(String someone) {
        this.someone = someone;
    }

    public String sayHello() {
        return sayHello(null);
    }

    public String sayHello(String someone) {
        if(someone != null) {
            return "Hello " + someone;
        }
        return "Hello " + this.someone;
    }
}

package top.kwseeker.spring.ioc.beanLoad.xml;

public class User {

    private String name;
    private int age;

    public User() {
        System.out.println("调用无参构造方法");
    }

    public User(String name) {
        System.out.println("调用带参数的构造方法 User(String name)");
        this.name = name;
    }

    public User(String name, int age) {
        System.out.println("调用带参数的构造方法 User(String name, int age)");
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String contentString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

package top.kwseeker.spring.ioc.beanLoad.autowireMode;

public class UserService {

    private UserDao userDao;

    public UserService() {}

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }


}

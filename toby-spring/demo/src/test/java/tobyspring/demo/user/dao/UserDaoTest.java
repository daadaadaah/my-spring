package tobyspring.demo.user.dao;

import java.sql.SQLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tobyspring.demo.user.domain.User;

public class UserDaoTest {

    @Test
    public void addAndGet() throws SQLException, ClassNotFoundException {
        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao userDao = ac.getBean("userDao", UserDao.class);

        User user = new User();
        user.setId("daadaadaa4");
        user.setName("네네임");
        user.setPassword("test1!");
        userDao.add(user);

        User user2 = userDao.get(user.getId());
        Assertions.assertEquals(user2.getName(), user.getName());
        Assertions.assertEquals(user2.getPassword(), user.getPassword());
    }
}

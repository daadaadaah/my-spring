package tobyspring.demo.user.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        userDao.deleteAll();
        assertEquals(userDao.getCount(), 0);

        User user = new User();
        user.setId("daadaadaa4");
        user.setName("네네임");
        user.setPassword("test1!");
        userDao.add(user);

        assertEquals(userDao.getCount(), 1);

        User user2 = userDao.get(user.getId());
        assertEquals(user2.getName(), user.getName());
        assertEquals(user2.getPassword(), user.getPassword());
    }
}

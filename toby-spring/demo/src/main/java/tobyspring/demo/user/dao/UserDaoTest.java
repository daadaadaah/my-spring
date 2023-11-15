package tobyspring.demo.user.dao;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tobyspring.demo.user.domain.User;

public class UserDaoTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        User user = new User();
        user.setId("daadaadaah");
        user.setName("");
        user.setPassword("test1!");

        // 본격적으로 Spring 사용한다.
        ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao userDao = ac.getBean("userDao", UserDao.class);

        userDao.add(user);
        System.out.println(user.getId() + " 등록 성공");

        User user2 = userDao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());
        System.out.println(user2.getId() + " 조회 성공");
    }
}

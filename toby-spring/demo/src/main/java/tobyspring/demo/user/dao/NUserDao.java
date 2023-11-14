package tobyspring.demo.user.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import tobyspring.demo.user.domain.User;

public class NUserDao extends UserDao {

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");

        Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/toby-spring", "sa", "");

        return c;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        User user = new User();
        user.setId("[D사] daadaadaah");
        user.setName("");
        user.setPassword("test1!");

        UserDao userDao = new NUserDao();
        userDao.add(user);
        System.out.println(user.getId() + " 등록 성공");

        User user2 = userDao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());
        System.out.println(user2.getId() + " 조회 성공");
    }
}

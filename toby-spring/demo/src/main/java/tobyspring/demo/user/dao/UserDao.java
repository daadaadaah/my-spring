package tobyspring.demo.user.dao;

import javax.sql.DataSource;
import java.sql.*;

import org.springframework.boot.SpringApplication;
import tobyspring.demo.DemoApplication;
import tobyspring.demo.user.domain.User;

public class UserDao {

    public void add(User user) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");

        Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/toby-spring", "sa", "");

        PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");

        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        ps.close();
        c.close();
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");

        Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/toby-spring", "sa", "");

        PreparedStatement ps = c.prepareStatement("select * from users where id = ?");

        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();

        rs.next();

        User user = new User();

        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));

        rs.close();
        ps.close();
        c.close();

        return user;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        User user = new User();
        user.setId("daadaadaah");
        user.setName("");
        user.setPassword("secret1!");

        UserDao userDao = new UserDao();
        userDao.add(user);

        System.out.println(user.getId() + " 등록 성공");

        User user2 = userDao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());
        System.out.println(user2.getId() + " 조회 성공");

        SpringApplication.run(DemoApplication.class, args);
    }
}
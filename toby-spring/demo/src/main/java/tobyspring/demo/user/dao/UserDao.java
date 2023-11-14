package tobyspring.demo.user.dao;

import java.sql.*;

import tobyspring.demo.user.domain.User;

public class UserDao {

    private ConnectionMaker connectionMaker;

    public UserDao() {
//        this.connectionMaker = new NConnectionMaker(); // N사의 경우
        this.connectionMaker = new DConnectionMaker(); // D사의 경우
    }

    public void add(User user) throws ClassNotFoundException, SQLException {
        // 관심 1 : DB와 연결을 위한 커넥션을 어떻게 가져올까라는 관심
        Connection c = connectionMaker.makeConnection();

        // 관심 2 : 사용자 등록을 위해 DB에 보낼 SQL 문장을 담을 Statement를 만들고 실행하는 관심
        PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");

        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        // 관심 3 : 작업이 끝나면, 사용한 리소스인 Statement와 Connection 오브젝트를 닫아줘서 소중한 공유 리소스를 시스템에 돌려주는 것
        ps.close();
        c.close();
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Connection c = connectionMaker.makeConnection();

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
        user.setId("[D사] daadaadaah");
        user.setName("");
        user.setPassword("test1!");

        UserDao userDao = new UserDao();
        userDao.add(user);
        System.out.println(user.getId() + " 등록 성공");

        User user2 = userDao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());
        System.out.println(user2.getId() + " 조회 성공");
    }
}

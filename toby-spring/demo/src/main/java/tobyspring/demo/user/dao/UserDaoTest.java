package tobyspring.demo.user.dao;

import java.sql.SQLException;

import tobyspring.demo.user.domain.User;

public class UserDaoTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        User user = new User();
        user.setId("[D사] daadaadaah");
        user.setName("");
        user.setPassword("test1!");

        // 관계 설정 코드가 클라이언트 class(UserDaoTest) 에서 하도록 변경되었다.
        // 이제야 서로가 완전히 분리되었다.
//        ConnectionMaker connectionMaker = new NConnectionMaker(); // N사
//        UserDao userDao = new UserDao(connectionMaker);

        ConnectionMaker connectionMaker = new DConnectionMaker(); // D사
        UserDao userDao = new UserDao(connectionMaker);

        userDao.add(user);
        System.out.println(user.getId() + " 등록 성공");

        User user2 = userDao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());
        System.out.println(user2.getId() + " 조회 성공");
    }
}

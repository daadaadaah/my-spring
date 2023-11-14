package tobyspring.demo.user.dao;

import java.sql.SQLException;

import tobyspring.demo.user.domain.User;

public class UserDaoTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        User user = new User();
        user.setId("[D사] daadaadaah");
        user.setName("");
        user.setPassword("test1!");

        // UserDao 와 ConnectionMaker 생성 및 관계 설정 역할을 DaoFactory 로 분리시켰다.
        // 그래서, UserDaoTest 역할은 기능이 잘 동작하는지 Test하는 역할 1개로  줄어들었다.
        UserDao userDao = new DaoFactory().userDao();

        userDao.add(user);
        System.out.println(user.getId() + " 등록 성공");

        User user2 = userDao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());
        System.out.println(user2.getId() + " 조회 성공");
    }
}

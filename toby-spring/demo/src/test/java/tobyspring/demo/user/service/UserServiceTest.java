package tobyspring.demo.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.transaction.PlatformTransactionManager;
import tobyspring.demo.user.dao.UserDao;
import tobyspring.demo.user.domain.Level;
import tobyspring.demo.user.domain.User;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static tobyspring.demo.user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static tobyspring.demo.user.service.UserService.MIN_RECCOMEND_FOR_GOLD;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserDao userDao;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    MailSender mailSender;

    List<User> users;

    @BeforeEach
    public void setUp() {
        users = Arrays.asList(
                new User("bumjin", "박범진", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0, "bumjin@gmail.com"),
                new User("joytouch", "강명성", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "joytouch@gmail.com"),
                new User("erwins", "신승한", "p3", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD - 1, "erwins@gmail.com"),
                new User("madnite1", "이상호", "p4", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD, "madnite1@gmail.com"),
                new User("green", "오민규", "p5", Level.GOLD, 100, Integer.MAX_VALUE, "green@gmail.com")
        );
    }

    @Test
    public void bean() {
        assertNotNull(this.userService);
    }

    @Test
    public void upgradeLevels() throws Exception {
        this.userDao.deleteAll();

        for(User user: users) userDao.add(user);

//        try {
            userService.upgradeLevels();

            checkLevelUpgraded(users.get(0), false);
            checkLevelUpgraded(users.get(1), true);
            checkLevelUpgraded(users.get(2), false);
            checkLevelUpgraded(users.get(3), true);
            checkLevelUpgraded(users.get(4), false);
//        } catch (Exception e) {
//
//        }
    }

    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);	  // GOLD 레벨
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertEquals(userWithLevelRead.getLevel(), userWithLevel.getLevel());
        assertEquals(userWithoutLevelRead.getLevel(), Level.BASIC);
    }

    @Test
    public void upgradeAllOrNothing() {
        UserService testUserService = new TestUserService(users.get(3).getId()); // 예외를 발생시킬 네 번째 사용자의 id를 넣어서 생성한다.
        testUserService.setUserDao(this.userDao); // userDao 수동 DI
        testUserService.setTransactionManager(this.transactionManager);
        testUserService.setMailSender(this.mailSender);

        userDao.deleteAll();
        for(User user : users) {
            userDao.add(user);
        }

        try {
            // testUserService 는 업그레이드 작업 중에 예외가 발생해야 한다. 정상 종료라면 문제가 있으니 실패
            testUserService.upgradeLevels();
            fail("TestUserServiceException excepted"); // 테스트가 의도한 대로 동작하는지를 확인하기 위해 넣은 것
        } catch(Exception e) { // TestUserService가 던져주는 예외를 잡아서 계속 진행되도록 한다. 그 외의 예외라면 테스트 실패

        }
        // 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 변경되었는지 확인
        checkLevelUpgraded(users.get(1), false);
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertEquals(userUpdate.getLevel(), user.getLevel().nextLevel());
        }
        else {
            assertEquals(userUpdate.getLevel(), user.getLevel());
        }
    }

    // 테스트용으로 특별히 만든 UserService의 대역
    static class TestUserService extends UserService {
        private String id;

        private TestUserService(String id) {
            this.id = id;
        }

        protected void upgradeLevel(User user) {
            if(user.getId().equals(this.id)) {
                throw new TestUserServiceException();
            }
            super.upgradeLevel(user);
        }

        static class TestUserServiceException extends RuntimeException {

        }
    }
}
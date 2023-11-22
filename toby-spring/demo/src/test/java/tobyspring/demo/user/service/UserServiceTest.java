package tobyspring.demo.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.PlatformTransactionManager;
import tobyspring.demo.user.dao.UserDao;
import tobyspring.demo.user.domain.Level;
import tobyspring.demo.user.domain.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static tobyspring.demo.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static tobyspring.demo.user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserServiceImpl userServiceImpl;

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
        assertNotNull(this.userServiceImpl);
    }

    @Test
    public void upgradeLevels() {
        this.userDao.deleteAll();

        for(User user: users) userDao.add(user);

        UserServiceImpl userServiceImpl = new UserServiceImpl();

        MockUserDao mockUserDao = new MockUserDao(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated();
        assertEquals(updated.size(), 2);
        checkUserAndLevel(updated.get(0), "joytouch", Level.SILVER);
        checkUserAndLevel(updated.get(1), "madnite1", Level.GOLD);

        List<String> request = mockMailSender.getRequests();
        assertEquals(request.size(), 2);
        assertEquals(request.get(0), users.get(1).getEmail());
        assertEquals(request.get(1), users.get(3).getEmail());
    }

    @Test
    public void mockUpgradeLevels() throws Exception {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MailSender mockMailSender = mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertEquals(users.get(1).getLevel(), Level.SILVER);
        verify(mockUserDao).update(users.get(3));
        assertEquals(users.get(3).getLevel(), Level.GOLD);

        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertEquals(mailMessages.get(0).getTo()[0], users.get(1).getEmail());
        assertEquals(mailMessages.get(1).getTo()[0], users.get(3).getEmail());
    }

    private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
        assertEquals(updated.getId(), expectedId);
        assertEquals(updated.getLevel(), expectedLevel);
    }

    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);	  // GOLD 레벨
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userServiceImpl.add(userWithLevel);
        userServiceImpl.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertEquals(userWithLevelRead.getLevel(), userWithLevel.getLevel());
        assertEquals(userWithoutLevelRead.getLevel(), Level.BASIC);
    }

    @Test
    public void upgradeAllOrNothing() {
        TestUserService testUserService = new TestUserService(users.get(3).getId()); // 예외를 발생시킬 네 번째 사용자의 id를 넣어서 생성한다.
        testUserService.setUserDao(this.userDao); // userDao 수동 DI
        testUserService.setMailSender(this.mailSender);

        UserServiceTx txUserService = new UserServiceTx();
        txUserService.setPlatformTransactionManager(this.transactionManager);
        txUserService.setUserService(testUserService);

        userDao.deleteAll();
        for(User user : users) {
            userDao.add(user);
        }

        try {
            // testUserService 는 업그레이드 작업 중에 예외가 발생해야 한다. 정상 종료라면 문제가 있으니 실패
            txUserService.upgradeLevels();
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
    static class TestUserService extends UserServiceImpl {
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

    static class MockMailSender implements MailSender {

        private List<String> requests = new ArrayList<>();

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public void send(SimpleMailMessage... mailMessage) throws MailException {
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            requests.add(simpleMessage.getTo()[0]); // 전송 요청을 받은 이메일 주소를 저장해둔다. 첫번쨰 수신자 메일이다
        }
    }

    static class MockUserDao implements UserDao {
        private List<User> users;
        private List<User> updated = new ArrayList();

        private MockUserDao(List<User> users) {
            this.users = users;
        }


        public List<User> getUpdated() {
            return this.updated;
        }

        public List<User> getAll() { // 스텁 기능 제공
            return this.users;
        }

        public void update(User user) {
            updated.add(user); // 목 오브젝트 기능 제공
        }

        // 테스트에서 사용되지 않는 메서드들
        public void add(User user) { throw new UnsupportedOperationException(); }
        public void deleteAll() { throw new UnsupportedOperationException(); }
        public User get(String id) { throw new UnsupportedOperationException(); }
        public int getCount() { throw new UnsupportedOperationException(); }
    }
}
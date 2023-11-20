package tobyspring.demo.user.dao;

    import static org.hamcrest.CoreMatchers.is;
    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertThrows;

    import java.sql.SQLException;
    import java.util.List;
    import javax.sql.DataSource;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.dao.DataAccessException;
    import org.springframework.dao.DuplicateKeyException;
    import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
    import org.springframework.jdbc.support.SQLExceptionTranslator;
    import tobyspring.demo.user.domain.Level;
    import tobyspring.demo.user.domain.User;

@SpringBootTest
public class UserDaoTest {
    @Autowired
    private UserDao dao;

    @Autowired
    private DataSource dataSource;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() {
        this.user1 = new User("pilhwankim", "김필환", "secret2@", Level.BASIC, 1, 0);
        this.user2 = new User("leegm700", "이길원", "springno1", Level.SILVER, 55, 10);
        this.user3 = new User("bumjin", "박범진", "springno2", Level.GOLD, 100, 40);
    }

    @Test
    public void addAndGet() throws SQLException, ClassNotFoundException {
        dao.deleteAll();
        assertEquals(dao.getCount(), 0);

        dao.add(user1);
        dao.add(user2);

        assertEquals(dao.getCount(), 2);

        User userget1 = dao.get(user1.getId());
        checkSameUser(userget1, user1);

        User userget2 = dao.get(user2.getId());
        checkSameUser(userget2, user2);
    }

    @Test
    public void count() throws SQLException, ClassNotFoundException {
        dao.deleteAll();
        assertEquals(dao.getCount(), 0);

        dao.add(user1);
        assertEquals(dao.getCount(), 1);

        dao.add(user2);
        assertEquals(dao.getCount(), 2);

        dao.add(user3);
        assertEquals(dao.getCount(), 3);
    }

    @Test
    public void getUserFailure() throws SQLException, ClassNotFoundException {
        dao.deleteAll();
        assertEquals(dao.getCount(), 0);

        assertThrows(RuntimeException.class, () -> dao.get("unknown_id"));
    }

    @Test
    public void duplicateKey() {
        dao.deleteAll();

        dao.add(user1);
        assertThrows(DataAccessException.class, () -> dao.add(user1));

    }

    @Test
    public void getAll() {
        dao.deleteAll();

        List<User> users0 = dao.getAll();
        assertEquals(users0.size(), 0);

        dao.add(user1);
        List<User> users1 = dao.getAll();
        assertEquals(users1.size(), 1);
        checkSameUser(user1, users1.get(0));

        dao.add(user2);
        List<User> users2 = dao.getAll();
        assertEquals(users2.size(), 2);
        checkSameUser(user1, users2.get(1));
        checkSameUser(user2, users2.get(0));


        dao.add(user3);
        List<User> users3 = dao.getAll();
        assertEquals(users3.size(), 3);
        checkSameUser(user1, users3.get(2));
        checkSameUser(user2, users3.get(1));
        checkSameUser(user3, users3.get(0));
    }

    @Test
    public void sqlExceptionTranslate() {
        dao.deleteAll();

        try {
            dao.add(user1);
            dao.add(user1);
        } catch(DuplicateKeyException ex) {
            SQLException sqlEx = (SQLException) ex.getRootCause();
            SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
            assertEquals(set.translate(null, null, sqlEx).getClass(), DuplicateKeyException.class);
        }
    }

    @Test
    public void update() {
        dao.deleteAll();

        dao.add(user1); // 수정되어야 하는 사용자
        dao.add(user2); // 1개만 수정되었는지 확인하기 위해 수정되지 않아야 하는 사용자도 테스트에 추가함.

        user1.setName("오민규");
        user1.setPassword("springno6");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);
        dao.update(user1);

        User user1update = dao.get(user1.getId());
        checkSameUser(user1, user1update);
        User user2update = dao.get(user2.getId());
        checkSameUser(user2, user2update);
    }

    private void checkSameUser(User user1, User user2) {
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getName(), user2.getName());
        assertEquals(user1.getPassword(), user2.getPassword());
        assertEquals(user1.getLevel(), user2.getLevel());
        assertEquals(user1.getLogin(), user2.getLogin());
        assertEquals(user1.getRecommend(), user2.getRecommend());
    }
}
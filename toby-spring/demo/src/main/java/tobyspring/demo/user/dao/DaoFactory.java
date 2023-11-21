package tobyspring.demo.user.dao;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.mail.MailSender;
import org.springframework.transaction.PlatformTransactionManager;
import tobyspring.demo.mail.DummyMailSender;
import tobyspring.demo.user.service.UserService;

@Configuration
public class DaoFactory {

    @Bean
    public UserDao userDao() {
        UserDaoJdbc userDao = new UserDaoJdbc();
        userDao.setDataSource(dataSource());
        return userDao;
    }

    @Bean
    public UserService userService() {
        UserService userService = new UserService(userDao(), platformTransactionManager(), mailSender());
        return userService;
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager(){
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public MailSender mailSender() {
        // TODO : 운영용이랑 테스트옹 DI 다르도록 변경해야됨
        // new JavaMailSenderImpl()
        return new DummyMailSender();
    }

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.h2.Driver.class);
        dataSource.setUrl("jdbc:h2:~/toby-spring");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
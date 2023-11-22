package tobyspring.demo.user.service;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import tobyspring.demo.user.domain.User;

public class UserServiceTx implements UserService {

    UserService userService;
    PlatformTransactionManager platformTransactionManager;

    public UserServiceTx(UserService userService, PlatformTransactionManager platformTransactionManager) {
        this.userService = userService;
        this.platformTransactionManager = platformTransactionManager;
    }

    public UserServiceTx() {
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    public void add(User user) {
        userService.add(user);
    }

    public void upgradeLevels() {
        TransactionStatus transactionStatus = this.platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            userService.upgradeLevels();

            this.platformTransactionManager.commit(transactionStatus);
        } catch (Exception e) {
            this.platformTransactionManager.rollback(transactionStatus);
            throw e;
        }
    }
}

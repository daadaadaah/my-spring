package tobyspring.demo.user.service;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import tobyspring.demo.user.dao.UserDao;
import tobyspring.demo.user.domain.Level;
import tobyspring.demo.user.domain.User;

import java.util.List;

public class UserService {
    UserDao userDao;

    public static final int MIN_LOGCOUNT_FOR_SILVER = 50;

    public static final int MIN_RECCOMEND_FOR_GOLD = 30;

    private PlatformTransactionManager transactionManager;

    private MailSender mailSender;

    public UserService(UserDao userDao, PlatformTransactionManager transactionManager, MailSender mailSender) {
        this.userDao = userDao;
        this.transactionManager = transactionManager;
        this.mailSender = mailSender;
    }

    public UserService() {
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void upgradeLevels() {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            List<User> users = userDao.getAll();
            for (User user: users) {
                if(canUpgradeLevel(user)) {
                    upgradeLevel(user);
                }
            }
            transactionManager.commit(status); // 정상적으로 작업을 마치면 트랜잭션 커밋
        } catch (RuntimeException e) {
            transactionManager.rollback(status);; // 예외가 발생할 경우 롤백
            throw e;
        }
    }

    private boolean canUpgradeLevel(User user) {
        Level currentLevel = user.getLevel();

        switch(currentLevel) {
            case BASIC: return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
            case SILVER: return (user.getRecommend() >= MIN_RECCOMEND_FOR_GOLD);
            case GOLD: return false; // GOLD는 항상 업그레드가 불가능 하니 false
            default: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
        }
    }

    protected void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
        sendUpgradeEMail(user);
    }

    private void sendUpgradeEMail(User user) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setFrom("useradmin@ksug.org");
        mailMessage.setSubject("Upgrade 안내");
        mailMessage.setText("사용자님의 등급이 " + user.getLevel().name());

        this.mailSender.send(mailMessage);
    }

    public void add(User user) {
        if (user.getLevel() == null) {
            user.setLevel(Level.BASIC);
        }
        userDao.add(user);
    }
}

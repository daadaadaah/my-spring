package tobyspring.demo.user.service;

import tobyspring.demo.user.dao.UserDao;
import tobyspring.demo.user.domain.Level;
import tobyspring.demo.user.domain.User;

import java.util.List;

public class UserService {
    UserDao userDao;

    public static final int MIN_LOGCOUNT_FOR_SILVER = 50;

    public static final int MIN_RECCOMEND_FOR_GOLD = 30;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void upgradeLevels() {
        List<User> users = userDao.getAll();
        for(User user : users) {
            if (canUpgradeLevel(user)) {
                upgradeLevel(user);
            }
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

    private void upgradeLevel(User user) {
        user.upgradeLevel();
        userDao.update(user);
    }


    public void add(User user) {
        if (user.getLevel() == null) {
            user.setLevel(Level.BASIC);
        }
        userDao.add(user);
    }
}

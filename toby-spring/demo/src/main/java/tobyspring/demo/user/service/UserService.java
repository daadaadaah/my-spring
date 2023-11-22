package tobyspring.demo.user.service;

import tobyspring.demo.user.domain.User;

public interface UserService {
    void add(User user);
    void upgradeLevels();
}
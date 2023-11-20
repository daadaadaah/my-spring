package tobyspring.demo.user.dao;

import java.sql.*;

import java.util.List;
import tobyspring.demo.user.domain.User;

public interface UserDao {
    void add(User user);
    User get(String id);
    List<User> getAll();
    void deleteAll();
    int getCount();
    void update(User user);
}

package tobyspring.demo.user.dao;

import java.sql.*;

import tobyspring.demo.user.domain.User;

public class UserDao {

    private ConnectionMaker connectionMaker;

    public UserDao(ConnectionMaker connectionMaker) {
        this.connectionMaker = connectionMaker;
    }

    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = connectionMaker.makeConnection();

            ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");
            ps.setString(1, user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getPassword());

            ps.executeUpdate();
        } catch(SQLException e) {
            throw e;
        } finally {
            if(ps != null) {
                try {
                    ps.close();
                } catch (SQLException e){
                    /*
                    ps.close() 메소드에도 SQLException이 발생할 수 있기 때문에 이를 잡아줘야 한다.
                    그렇지 않으면 Connection close() 하지 못하고 메소드를 빠져나가게 된다.
                    */
                }

            }
            if(c != null) {
                try {
                    c.close();
                } catch (SQLException e){ }
            }
        }
    }

    public void deleteAll() throws SQLException, ClassNotFoundException {
        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = connectionMaker.makeConnection();
            ps = c.prepareStatement("delete from users");
            ps.executeUpdate();
        } catch (SQLException e){
            throw e;
        } finally {
            if(ps != null) {
                try {
                    ps.close();
                } catch (SQLException e){
                    /*
                    ps.close() 메소드에도 SQLException이 발생할 수 있기 때문에 이를 잡아줘야 한다.
                    그렇지 않으면 Connection close() 하지 못하고 메소드를 빠져나가게 된다.
                    */
                }

            }
            if(c != null) {
                try {
                    c.close();
                } catch (SQLException e){ }
            }
        }
    }

    public int getCount() throws SQLException, ClassNotFoundException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            c = connectionMaker.makeConnection();

            ps = c.prepareStatement("select count(*) from users");

            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw e;
        } finally {
            if(rs != null) {
                try {
                    rs.close();
                } catch (SQLException e){ }
                /*
                rs.close() 메소드에도 SQLException이 발생할 수 있기 때문에 이를 잡아줘야 한다.
                그렇지 않으면 Connection close() 하지 못하고 메소드를 빠져나가게 된다.
                */
            }
            if(ps != null) {
                try {
                    ps.close();
                } catch (SQLException e){ }
            }
            if(c != null) {
                try {
                    c.close();
                } catch (SQLException e){ }
            }
        }
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            c = connectionMaker.makeConnection();

            ps = c.prepareStatement("select * from users where id = ?");
            ps.setString(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password"));
            }

            if(user == null) throw new RuntimeException();

            return user;
        } catch (SQLException e) {
            throw e;
        } finally {
            if(rs != null) {
                try {
                    rs.close();
                } catch (SQLException e){
                    /*
                    rs.close() 메소드에도 SQLException이 발생할 수 있기 때문에 이를 잡아줘야 한다.
                    그렇지 않으면 Connection close() 하지 못하고 메소드를 빠져나가게 된다.
                    */
                }
            }
            if(ps != null) {
                try {
                    ps.close();
                } catch (SQLException e){ }
            }
            if(c != null) {
                try {
                    c.close();
                } catch (SQLException e){ }
            }
        }
    }
}

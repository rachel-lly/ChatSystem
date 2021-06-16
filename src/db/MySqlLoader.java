package db;

import model.GroupChat;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

public class MySqlLoader {

    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/chatsystem?serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "root";

    public static final String STANDARD_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS `user`(" +
                    "`id` VARCHAR(20) NOT NULL, " +
                    "`password` VARCHAR(100) , " +
                    "`nickName` VARCHAR(20) , " +
                    "PRIMARY KEY ( `id` )" +
                    ");";


    public static final String STANDARD_GROUP_CHAT_TABLE =
            "CREATE TABLE IF NOT EXISTS `groupChatList`(" +
                    "`id` VARCHAR(20) NOT NULL, " +
                    "`groupChatName` VARCHAR(20) NOT NULL, " +
                    "PRIMARY KEY ( `groupChatName` )" +
                    ");";


    public static final String STANDARD_FRIEND_TABLE =
            "CREATE TABLE IF NOT EXISTS `friendList`(" +
                    "`id` VARCHAR(20) NOT NULL, " +
                    "`friendId` VARCHAR(20) NOT NULL " +
                    ");";

    public static final String GET_GROUP_NAME = "SELECT * FROM groupChatList;";
    public static final String SET_GROUP_NAME = "insert into groupChatList values(\"%s\",\"%s\");";

    public static final String STANDARD_INSERT_FRIEND_STRING = "insert into friendList values(\"%s\",\"%s\");";

    public static final String STANDARD_SEARCH_FRIEND_STRING = "SELECT distinct f1.friendId FROM friendList as f1 INNER JOIN friendList as f2 ON f1.id = f2.friendId AND f2.id = f1.friendId WHERE f1.id = \"%s\";";

    public static final String STANDARD_SEARCH_APPLY_FRIEND_STRING = "SELECT distinct  * from (select friendList.id, friendList.friendId FROM friendList LEFT JOIN " +
            "(SELECT f1.id, f1.friendId FROM friendList as f1 INNER JOIN friendList as f2 ON f1.id = f2.friendId AND f2.id = f1.friendId) as t1 " +
            "ON friendList.friendId = t1.friendId AND friendList.id = t1.id where t1.id IS NULL) as t2 where t2.friendId = \"%s\";";

    public static final String STANDARD_DELETE_FRIEND_STRING = "DELETE FROM friendList WHERE friendId = \"%s\" and id = \"%s\";";

    public Connection connection = null;
    public Statement statement = null;

    public MySqlLoader() throws ClassNotFoundException, SQLException {
        this.init();
    }

    public void init() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        this.connection = DriverManager.getConnection(DB_URL, USER, PASS);
        this.statement = this.connection.createStatement();
        this.selfCheck();
    }

    public void selfCheck() {
        boolean isExist;
        isExist = true;

        try {
            this.statement.executeQuery("use chatsystem;");
        } catch (SQLException se) {
            if (se.getErrorCode() == 1049) {
                isExist = false;
            }
        }

        if (!isExist) {
            try {
                this.statement.executeQuery("create database chatsystem;");
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        try {
            this.statement.executeQuery("use chatsystem;");
            this.statement.execute(STANDARD_USER_TABLE);
            this.statement.execute(STANDARD_FRIEND_TABLE);
            this.statement.execute(STANDARD_GROUP_CHAT_TABLE);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void loadUsers(Map<String, User> dst) {
        try {
            ResultSet rs = this.statement.executeQuery("SELECT * FROM user;");

            while (rs.next()) {
                dst.put(rs.getString("id"), new User(rs.getString("id"), rs.getString("password"), rs.getString("nickName")));
            }
            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public ArrayList<GroupChat> getGroupName(){
        ArrayList<GroupChat> res = new ArrayList<>();

        try {
            ResultSet rs = this.statement.executeQuery(GET_GROUP_NAME);

            while (rs.next()){

                res.add(new GroupChat(rs.getString("id"),rs.getString("groupChatName"),1));
            }
            rs.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return res;
    }

    public void setGroupName(String groupName){


        try {
            this.statement.execute(String.format(SET_GROUP_NAME,"500"+(int)(100*Math.random()),groupName));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }



    public void connectFriend(String id, String friendId) {
        try {
            this.statement.execute(String.format(STANDARD_INSERT_FRIEND_STRING, id, friendId));
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void deleteFriend(String id, String friendId) {
        try {
            this.statement.execute(String.format(STANDARD_DELETE_FRIEND_STRING, id, friendId));
            this.statement.execute(String.format(STANDARD_DELETE_FRIEND_STRING, friendId, id));
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public ArrayList<String> searchFriend(String id) {
        ArrayList<String> res = new ArrayList<>();


        try {
            ResultSet rs = this.statement.executeQuery(String.format(STANDARD_SEARCH_FRIEND_STRING, id));
            while (rs.next()) {

                res.add(rs.getString("friendId"));
            }
            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }

        return res;
    }

    public ArrayList<String> searchApplyFriend(String id) {
        ArrayList<String> res = new ArrayList<>();
        try {
            ResultSet rs = this.statement.executeQuery(String.format(STANDARD_SEARCH_APPLY_FRIEND_STRING, id));
            while (rs.next()) {
                res.add(rs.getString("id"));
            }
            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return res;
    }

    public void close() throws SQLException {
        this.statement.close();
        this.connection.close();
    }
}

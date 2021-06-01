package server.user;

import model.GroupChat;
import model.User;
import server.database.MySqlLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UsersContainer {
    public Map<String, User> users = null;
    public MySqlLoader mysqlLoader = null;
    public static final UsersContainer INSTANCE = new UsersContainer();

    private UsersContainer() {
        this.init();
    }

    private void init() {
        try {
            mysqlLoader = new MySqlLoader();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        this.clear();
        this.loadData();
    }

    public void clear() {
        if (users == null) {
            users = new HashMap<>();
        }
        users.clear();
    }

    public boolean connectFriend(String srcId, String dstId) {
        this.mysqlLoader.connectFriend(srcId, dstId);
        return true;
    }

    public boolean deleteFriend(String srcId, String dstId) {
        this.mysqlLoader.deleteFriend(srcId, dstId);
        return true;
    }

    public ArrayList<String> searchFriend(String srcId) {
        return this.mysqlLoader.searchFriend(srcId);
    }

    public ArrayList<User> searchFriendAsUser(String srcId) {
        ArrayList<User> res = new ArrayList<>();

        for (String id : this.mysqlLoader.searchFriend(srcId)) {
            res.add(this.users.get(id));
        }
        return res;
    }

    public ArrayList<User> searchApplyFriendAsUser(String srcId) {
        ArrayList<User> res = new ArrayList<>();

        for (String id : this.mysqlLoader.searchApplyFriend(srcId)) {
            res.add(this.users.get(id));
        }
        return res;
    }

    public void loadData() {
        this.mysqlLoader.loadUsers(this.users);
    }

    public ArrayList<GroupChat> getGroupNameList(){
        return this.mysqlLoader.getGroupName();
    }

    public void setGroupNameList(String groupNameList){
        this.mysqlLoader.setGroupName(groupNameList);
    }
}

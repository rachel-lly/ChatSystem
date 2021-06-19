package db;

import model.GroupChat;
import model.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DBImpl {
    public Map<String, User> users = null;
    public DBLoader dbLoader = null;
    public static final DBImpl INSTANCE = new DBImpl();

    private DBImpl() {
        this.init();
    }

    private void init() {
        try {
            dbLoader = new DBLoader();
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
        this.dbLoader.connectFriend(srcId, dstId);
        return true;
    }

    public boolean deleteFriend(String srcId, String dstId) {
        this.dbLoader.deleteFriend(srcId, dstId);
        return true;
    }

    public ArrayList<String> searchFriend(String srcId) {
        return this.dbLoader.searchFriend(srcId);
    }

    public ArrayList<User> searchFriendAsUser(String srcId) {
        ArrayList<User> res = new ArrayList<>();

        for (String id : this.dbLoader.searchFriend(srcId)) {
            res.add(this.users.get(id));
        }
        return res;
    }

    public ArrayList<User> searchApplyFriendAsUser(String srcId) {
        ArrayList<User> res = new ArrayList<>();

        for (String id : this.dbLoader.searchApplyFriend(srcId)) {
            res.add(this.users.get(id));
        }
        return res;
    }

    public void loadData() {
        this.dbLoader.loadUsers(this.users);
    }

    public ArrayList<GroupChat> getGroupNameList(){
        return this.dbLoader.getGroupName();
    }

    public void setGroupNameList(String groupNameList){
        this.dbLoader.setGroupName(groupNameList);
    }
}

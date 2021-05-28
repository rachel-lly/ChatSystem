package client;


import GUI.Login;
import GUI.chat.ChattGUI;
import GUI.friend.Friend;
import GUI.friend.FriendGUI;
import GUI.utils.Utils;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class UserController {
    public ClientControl client;
    public Login loggingLogin;
    public FriendGUI friendGUI;
    public ArrayList<Friend> friendList;
    public ArrayList<Friend> applyFriendList;
    public HashMap<String, ChattGUI> chattingPanel;
    public String id;

    public boolean login(String id, String password) throws Exception {
        client.login(id, password);
        this.id = id;
        this.friendGUI.frame.setVisible(true);
        return true;
    }

    public boolean sendFile(String id, String name, byte[] file) {
        try {
            this.client.sendFile(id, name, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void openChattingPanel(Friend friend) {
        if (chattingPanel.get(friend.id) != null) {
        } else {
            chattingPanel.put(friend.id, new ChattGUI(this, friend));
        }
    }

    public void updateFriendList(ArrayList<Friend> friendList) {
        this.friendGUI.friendsList = friendList;

        for (Friend friend : friendList) {
            if (this.chattingPanel.get(friend.id) != null) {
                this.chattingPanel.get(friend.id).friend = friend;
                this.chattingPanel.get(friend.id).update();
            }
        }
        this.friendGUI.updateFriend();
    }

    public void updateApplyFriendList(ArrayList<Friend> applyFriendsList) {
        this.friendGUI.applyFriendsList = applyFriendsList;
    }

    public boolean closeChattingPanel(Friend friend) {
        boolean res = true;

        if (chattingPanel.get(friend.id) == null) {
            res = false;
        } else {
            chattingPanel.remove(friend.id);
        }

        return res;
    }

    public void receivedMsg(String id, String msg, int type) {
        if (chattingPanel.get(id) == null) {
            Friend sender = new Friend(id, "匿名");
            for (Friend fri : this.friendGUI.friendsList) {
                if (fri.id.equals(id)) {
                    sender.nickName = fri.nickName;
                    sender.state = 1;
                    break;
                }
            }
            chattingPanel.put(id, new ChattGUI(this, sender));
        }
        chattingPanel.get(id).receiveMsg(msg, type);
    }

    public boolean sendMsg(String id, String msg, int type) throws Exception {
        client.sendMsg(id, msg, type);

        return true;
    }

    public boolean addFriends(ArrayList<Friend> friends) {
        try {
            this.client.addFriends(friends);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public void deleteFriends(ArrayList<Friend> friends) {
        try {
            this.client.deleteFriends(friends);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        try {
            this.client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("退出");
        System.exit(0);
    }

    public UserController() throws IOException {
        init();
        this.loggingLogin = new Login(this);
    }

    public void init() throws IOException {
        this.friendList = new ArrayList<>();
        this.applyFriendList = new ArrayList<>();
        this.client = new ClientControl(this);
        this.friendGUI = new FriendGUI(this.friendList, this.applyFriendList, this);
        this.chattingPanel = new HashMap<>();
    }

    public void errorOccupy(final String msg) {
        new Thread(() -> Utils.showErrorMsg(msg, "错误", friendGUI.frame)).start();
    }
}

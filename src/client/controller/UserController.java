package client.controller;

import UI.login.LoginUI;
import UI.chat.ChatUI;
import model.Friend;
import UI.friend.FriendUI;
import UI.utils.Utils;
import model.GroupChat;
import db.UsersContainer;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class UserController {

    public ArrayList<Friend> applyFriendList;
    public ArrayList<Friend> friendList;


    public ClientController client;
    public LoginUI loggingLoginUI;
    public FriendUI friendUI;

    public HashMap<String, ChatUI> chattingPanel;
    public String id;


    public boolean login(String id, String password) throws Exception {
        client.login(id, password);
        this.id = id;
        this.friendUI.frame.setVisible(true);
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

    public void openChatPanel(Friend friend) {
        if (chattingPanel.get(friend.id) != null) {
        } else {
            chattingPanel.put(friend.id, new ChatUI(this, friend));
        }
    }

    public void openGroupChatPanel(String groupName) {

        chattingPanel.put(groupName, new ChatUI(this, groupName));

    }


    public void updateFriendList(ArrayList<Friend> friendList) {
        this.friendUI.friendsList = friendList;

        for (Friend friend : friendList) {
            if (this.chattingPanel.get(friend.id) != null) {
                this.chattingPanel.get(friend.id).friend = friend;
                this.chattingPanel.get(friend.id).update();
            }
        }
        this.friendUI.updateInformation();
    }



    public void updateApplyFriendList(ArrayList<Friend> applyFriendsList) {
        this.friendUI.applyFriendsList = applyFriendsList;
    }

    public boolean closeChatPanel(Friend friend) {
        boolean res = true;

        if (chattingPanel.get(friend.id) == null) {
            res = false;
        } else {
            chattingPanel.remove(friend.id);
        }

        return res;
    }

    public void receivedMsg(String id, String msg, int type) {

        boolean isGroup = false;

        if (chattingPanel.get(id) == null) {

           ArrayList<GroupChat> groupChats = UsersContainer.INSTANCE.getGroupNameList();

            for (GroupChat groupChat : groupChats) {

                if (id.equals(groupChat.groupId)) {
                    isGroup = true;
                    Friend sender = new Friend(id, groupChat.groupName);
                    sender.state = 1;
                    chattingPanel.put(id, new ChatUI(this, sender));
                    break;
                }
            }


           if(!isGroup){

               Friend sender = new Friend(id, "anonymous");
               for (Friend fri : this.friendUI.friendsList) {
                   if (fri.id.equals(id)) {
                       sender.nickName = fri.nickName;
                       sender.state = 1;
                       break;
                   }
               }
               chattingPanel.put(id, new ChatUI(this, sender));
           }

        }


        chattingPanel.get(id).receiveMsg(msg, type);
    }

    public boolean sendMsg(String id, String msg, int type) throws Exception {
        client.sendMsg(id, msg, type);
        return true;
    }

    public boolean sendGroupMsg(String groupName, String msg, int type) throws Exception {
        client.sendGroupMsg(groupName, msg, type);
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

    public boolean addGroupChat(String groupChatName) {
        try {
            this.client.addGroupChat(groupChatName);
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
        System.out.println("Exit");
        System.exit(0);
    }

    public UserController() throws IOException {
        init();
        this.loggingLoginUI = new LoginUI(this);
    }

    public void init() throws IOException {
        this.friendList = new ArrayList<>();
        this.applyFriendList = new ArrayList<>();
        this.client = new ClientController(this);
        this.friendUI = new FriendUI(this.friendList, this.applyFriendList,this);
        this.chattingPanel = new HashMap<>();
    }

    public void errorOccupy(final String msg) {
        new Thread(() -> Utils.showErrorMsg(msg, "error", friendUI.frame)).start();
    }
}

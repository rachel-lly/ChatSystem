package client.controller;

import UI.login.LoginUI;
import UI.chat.ChatUI;
import model.Friend;
import UI.chatList.ChatListUI;
import UI.util.DesignUtil;
import model.GroupChat;
import db.UsersContainer;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class UserController {


    public ArrayList<Friend> friendList;


    public ClientController client;
    public LoginUI loginUI;
    public ChatListUI chatListUI;

    public HashMap<String, ChatUI> chatPanel;
    public String id;


    public boolean login(String id, String password) throws Exception {
        client.login(id, password);
        this.id = id;
        this.chatListUI.setUserTitle();
        this.chatListUI.frame.setVisible(true);

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
        if (chatPanel.get(friend.getId()) != null) {
        } else {
            chatPanel.put(friend.getId(), new ChatUI(this, friend));
        }
    }

    public void openGroupChatPanel(String groupName) {

        chatPanel.put(groupName, new ChatUI(this, groupName));

    }


    public void updateFriendList(ArrayList<Friend> friendList) {
        this.chatListUI.friendsList = friendList;

        for (Friend friend : friendList) {
            if (this.chatPanel.get(friend.getId()) != null) {
                this.chatPanel.get(friend.getId()).friend = friend;
                this.chatPanel.get(friend.getId()).updateMessage();
            }
        }
        this.chatListUI.updateAllList();
    }





    public boolean closeChatPanel(Friend friend) {
        boolean res = true;

        if (chatPanel.get(friend.getId()) == null) {
            res = false;
        } else {
            chatPanel.remove(friend.getId());
        }

        return res;
    }

    public void receivedMsg(String id, String msg, int type) {

        boolean isGroup = false;

        if (chatPanel.get(id) == null) {

           ArrayList<GroupChat> groupChats = UsersContainer.INSTANCE.getGroupNameList();

            for (GroupChat groupChat : groupChats) {

                if (id.equals(groupChat.getGroupId())) {
                    isGroup = true;
                    Friend sender = new Friend(id, groupChat.getGroupName());
                    sender.setState(1);
                    chatPanel.put(id, new ChatUI(this, sender));
                    break;
                }
            }


           if(!isGroup){

               Friend sender = new Friend(id, "anonymous");
               for (Friend fri : this.chatListUI.friendsList) {
                   if (fri.getId().equals(id)) {
                       sender.setNickName(fri.getNickName());
                       sender.setState(1);
                       break;
                   }
               }
               chatPanel.put(id, new ChatUI(this, sender));
           }

        }


        chatPanel.get(id).receiveMessage(msg,type);
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
        this.loginUI = new LoginUI(this);
    }

    public void init() throws IOException {
        this.friendList = new ArrayList<>();
        this.client = new ClientController(this);
        this.chatListUI = new ChatListUI(this.friendList,this);
        this.chatPanel = new HashMap<>();
    }

    public void errorOccupy(final String msg) {
        new Thread(() -> DesignUtil.showErrorMsg(msg, "error", chatListUI.frame)).start();
    }
}

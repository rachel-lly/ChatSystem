package UI.chatList;

import UI.login.LoginUI;
import UI.util.DesignUtil;
import client.controller.UserController;
import model.Friend;
import model.GroupChat;
import db.DBImpl;
import model.User;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Objects;

public class ChatListUI {

    public boolean isFirst = true;

    static String iconURL = "/UI/assets/chat_icon.png";

    public JFrame frame;

    public ArrayList<Friend> friendsList;

    public ArrayList<String> groupNameList = new ArrayList<>();

    public DefaultMutableTreeNode treeNode;

    public DefaultMutableTreeNode rootNode, onLine, offLine;

    public DefaultMutableTreeNode groupChat;


    public UserController callback;
    public JButton addFriendsButton, deleteFriendButton;

    public JButton addGroupChatButton;

    public boolean isDelete = false;
    public JTree tree;


    public ChatListUI(UserController callback) {
        this(new ArrayList<>(),callback);
    }

    public ChatListUI(ArrayList<Friend> friendsList,
                      UserController callback) {

        ArrayList<GroupChat> list = DBImpl.INSTANCE.getGroupNameList();
        for(int i=0;i<list.size();i++){
            this.groupNameList.add(list.get(i).getGroupName());
        }

        this.friendsList = friendsList;

        this.callback = callback;

        initView();
        updateAllList();
    }

    public void updateAllList() {
        this.onLine.removeAllChildren();
        this.offLine.removeAllChildren();

        this.groupChat.removeAllChildren();

        for (Friend friend : this.friendsList) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(friend);
            if (friend.getState() == 1) {
                this.onLine.add(node);
            } else if (friend.getState() == 0) {
                this.offLine.add(node);
            }
        }

        ArrayList<GroupChat> list = DBImpl.INSTANCE.getGroupNameList();
        ArrayList<String> slist = new ArrayList<>();
        for(int i=0;i<list.size();i++){
            slist.add(list.get(i).getGroupName());
        }

        groupNameList = slist;
        for(String groupName : groupNameList){
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(groupName);
            this.groupChat.add(node);
        }


        this.tree.updateUI();
    }

    public void initView() {



        this.frame = new JFrame("????????????");

        this.frame.setSize(400, 450);
        this.frame.setIconImage(new ImageIcon(Objects.requireNonNull(LoginUI.class.getResource(iconURL))).getImage());
        this.frame.setLocationRelativeTo(null);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(initToolBar(), BorderLayout.NORTH);

        treeNode = new DefaultMutableTreeNode("??????");

        rootNode = new DefaultMutableTreeNode("??????");
        treeNode.add(rootNode);

        onLine = new DefaultMutableTreeNode("????????????");
        rootNode.add(onLine);
        offLine = new DefaultMutableTreeNode("????????????");
        rootNode.add(offLine);

        groupChat = new DefaultMutableTreeNode("??????");
        treeNode.add(groupChat);

        this.tree = new JTree(treeNode);
        this.tree.setShowsRootHandles(true);


        this.tree.addTreeSelectionListener(e -> {
            try {
                ArrayList<GroupChat> grouplist = DBImpl.INSTANCE.getGroupNameList();
                ArrayList<String> groupNameList = new ArrayList<>();
                for(int i=0;i<grouplist.size();i++){
                    groupNameList.add(grouplist.get(i).getGroupName());
                }
                Object node =((DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent()).getUserObject();

                if(groupNameList.contains(node.toString())){

                    callback.openGroupChatPanel(node.toString());

                }else{

                    Friend friend = (Friend)node;

                    if (isDelete) {
                        ArrayList<Friend> tempArrayList = new ArrayList<>();
                        tempArrayList.add(new Friend(friend.getId(), friend.getNickName()));
                        if (JOptionPane.showConfirmDialog(frame,
                                "?????????????????????????",
                                "??????",
                                JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                            callback.deleteFriends(tempArrayList);

                            DesignUtil.showInformationMsg("??????????????????","??????",null);
                        }
                    } else {
                        callback.openChatPanel(friend);
                    }

                }


            } catch (Exception e1) {

            }
        });

        panel.add(new JScrollPane(tree), BorderLayout.CENTER);

        this.frame.setContentPane(panel);
    }

    public JPanel initToolBar() {
        JPanel jp = new JPanel();
        jp.setBackground(new Color(196, 232, 188));
        jp.setLayout(new GridLayout(1, 0));
        jp.setPreferredSize(new Dimension(0, 30));

        addFriendsButton = DesignUtil.createButton("????????????");
        addFriendsButton.addActionListener(e -> addFriend());

        jp.add(addFriendsButton);

        deleteFriendButton = DesignUtil.createButton("????????????");
        deleteFriendButton.addActionListener(new ActionListener() {

            public Color switchColor = new Color(0.5f, 0.0f, 0.0f);

            @Override
            public void actionPerformed(ActionEvent e) {

                if(isFirst){
                    DesignUtil.showWarningMsg("????????????????????????\n??????????????????????????????????????????", "??????",null);
                    isFirst = false;
                }

                isDelete = !isDelete;
                Color tempColor = deleteFriendButton.getForeground();
                deleteFriendButton.setForeground(switchColor);
                switchColor = tempColor;
            }
        });
        jp.add(deleteFriendButton);

        addGroupChatButton = DesignUtil.createButton("????????????");
        addGroupChatButton.addActionListener( e -> addGroupChat());
        jp.add(addGroupChatButton);


        return jp;
    }

    private void addGroupChat() {
        JDialog dialog = new JDialog(this.frame, "???????????????????????????:", true);
        dialog.setBounds(400, 300, 300, 100);
        dialog.setLayout(new BorderLayout());

        JButton confirm = DesignUtil.createButton("??????");

        JTextField idFieldArea = new JTextField();

        confirm.addActionListener(e -> {
            if ("".equals(idFieldArea.getText())) {
                return;
            }


            callback.addGroupChat(idFieldArea.getText());
            DesignUtil.showInformationMsg("??????????????????","??????",null);
            updateAllList();
            dialog.setVisible(false);

        });

        idFieldArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    confirm.doClick();
                }
            }
        });

        idFieldArea.setFont(new Font("menlo", Font.BOLD, 17));
        dialog.add(idFieldArea, BorderLayout.NORTH);
        dialog.add(confirm, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this.frame);
        dialog.setVisible(true);


    }
    public void addFriend() {
        JDialog dialog = new JDialog(this.frame, "????????????????????????ID:", true);
        dialog.setBounds(400, 300, 300, 100);
        dialog.setLayout(new BorderLayout());


        JButton confirm = DesignUtil.createButton("??????");

        JTextField idFieldArea = new JTextField();

        confirm.addActionListener(e -> {
            if ("".equals(idFieldArea.getText())) {
                return;
            }


            ArrayList<Friend> tempArrayList = new ArrayList<>();
            tempArrayList.add(new Friend(idFieldArea.getText(), ""));
            callback.addFriends(tempArrayList);
            dialog.setVisible(false);
        });

        idFieldArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    confirm.doClick();
                }
            }
        });
        idFieldArea.setFont(new Font("menlo", Font.BOLD, 17));
        dialog.add(idFieldArea, BorderLayout.NORTH);
        dialog.add(confirm, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this.frame);
        dialog.setVisible(true);
    }


    public void setUserTitle(){
        User user = DBImpl.INSTANCE.users.get(callback.id);
        frame.setTitle("???????????? "+user.nickName+"@"+user.id);
    }

}

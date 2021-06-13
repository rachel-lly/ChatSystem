package UI.chat;


import UI.util.DesignUtil;
import model.Friend;
import client.controller.UserController;
import model.ChatRecord;
import util.ChatRecordManager;
import client.util.ClientUtil;
import model.GroupChat;
import db.UsersContainer;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class ChatUI {


    public JFrame frame;
    public JButton sendButton;
    public JButton toolImageButton, toolFileButton;
    public JTextArea msgArea;
    public JTextArea editArea;
    public JScrollPane jScrollPane;
    public UserController callback;
    public ChatPanel chatPanel;
    public Friend friend;
    public ArrayList<ChatRecord> chatRecords;

    boolean isGroup = false;
    String groupName = "";

    static String chatIconURL = "/UI/assets/chat_icon.png";

    public void update() {

       ArrayList<GroupChat> list = UsersContainer.INSTANCE.getGroupNameList();

       for(int i=0;i<list.size();i++){
           if(groupName.equals(list.get(i).groupName)){
               isGroup = true;
               this.frame.setTitle(list.get(i).toString());
               this.chatPanel.friend = new Friend(list.get(i).groupId,list.get(i).groupName,1);
           }
       }

       if(!isGroup){
           this.frame.setTitle(friend.toString());
           this.chatPanel.friend = friend;
       }



        if (friend.state == 1) {
            this.sendButton.setEnabled(true);
            this.sendButton.setText("Send");
            this.toolImageButton.setEnabled(true);
            this.toolFileButton.setEnabled(true);
            this.editArea.setEnabled(true);
        } else {
            this.sendButton.setEnabled(false);
            this.sendButton.setText("This friend isn't online at the moment");
            this.toolImageButton.setEnabled(false);
            this.toolFileButton.setEnabled(false);
            this.editArea.setEnabled(false);
        }
    }

    public ChatUI(UserController callback, Friend friend) {
        this.friend = friend;
        this.callback = callback;
        this.isGroup = false;

        try {
            this.chatRecords = ChatRecordManager.readChatRecord(callback.id, friend.id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        frame = new JFrame();
        frame.setBackground(DesignUtil.Theme.ThemeColor);

        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();

        frame.setBounds((d.width - d.width / 3) / 2, (d.height - d.height / 3) / 2, 700, 700);
        frame.setIconImage(new ImageIcon(Objects.requireNonNull(ChatUI.class.getResource(chatIconURL))).getImage());
        frame.setResizable(true);
        frame.setLayout(new BorderLayout());
        frame.add(creatSouth(), BorderLayout.SOUTH);
        frame.add(creatCenter(), BorderLayout.CENTER);
        frame.setVisible(true);

        this.callback = callback;
        this.frame.addWindowListener(new CloseWindow());
        this.loadChatRecord();
        this.update();
    }

    public ChatUI(UserController callback, String name) {

        this.callback = callback;
        this.isGroup = true;
        this.groupName = name;

        frame = new JFrame();
        frame.setBackground(DesignUtil.Theme.ThemeColor);

        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();

        frame.setBounds((d.width - d.width / 3) / 2, (d.height - d.height / 3) / 2, 700, 700);
        frame.setIconImage(new ImageIcon(Objects.requireNonNull(ChatUI.class.getResource(chatIconURL))).getImage());
        frame.setResizable(true);
        frame.setLayout(new BorderLayout());
        frame.add(creatSouth(), BorderLayout.SOUTH);
        frame.add(creatCenter(), BorderLayout.CENTER);
        frame.setVisible(true);

        this.callback = callback;
        this.frame.addWindowListener(new CloseWindow());

        this.update();
    }

    public void loadChatRecord() {
        if (this.chatRecords == null) {
            return;
        }

        for (ChatRecord chatRecord : this.chatRecords) {
            if (chatRecord.state == 0) {
                this.chatPanel.addSent(chatRecord.msg, 1);
            } else if (chatRecord.state == 1) {
                this.chatPanel.addReceived(chatRecord.msg, 1);
            } else if (chatRecord.state == 2) {
                this.chatPanel.addSent(chatRecord.msg, 2);
            } else if (chatRecord.state == 3) {
                this.chatPanel.addReceived(chatRecord.msg, 2);
            } else if (chatRecord.state == 4) {
                this.chatPanel.addSent(chatRecord.msg, 3);
            } else if (chatRecord.state == 5) {
                this.chatPanel.addReceived(chatRecord.msg, 3);
            }
        }
    }

    public JScrollPane creatCenter() {
        msgArea = new JTextArea();
        msgArea.setFont(new Font("menlo", Font.BOLD, 17));

        chatPanel = new ChatPanel(new Friend(callback.id, ""), this.friend);
        chatPanel.setBackground(new Color(232, 232, 232));

        jScrollPane = new JScrollPane(chatPanel);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setBackground(new Color(255, 255, 255));

        return jScrollPane;
    }

    public JPanel creatSouth() {
        JPanel jp = new JPanel();
        jp.setBackground(new Color(255, 255, 255));
        jp.setLayout(new BorderLayout());
        jp.setPreferredSize(new Dimension(0, 200));

        editArea = new JTextArea();
        editArea.setFont(new Font("menlo", Font.BOLD, 17));

        sendButton = new JButton("Send");
        sendButton.setBounds(0, 0, 260, 30);
        sendButton.setFont(new Font("menlo", Font.BOLD, 17));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.setContentAreaFilled(false);
        sendButton.setFocusPainted(false);

        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!"".equals(editArea.getText())) {
                    sendMsg(editArea.getText(), 1);
                }
                editArea.setText("");
            }
        };
        sendButton.addActionListener(sendAction);
        editArea.getInputMap().put(KeyStroke.getKeyStroke('\n', InputEvent.CTRL_DOWN_MASK), "send");
        editArea.getActionMap().put("send", sendAction);

        jp.add(createTool(), BorderLayout.NORTH);
        jp.add(new JScrollPane(editArea), BorderLayout.CENTER);
        jp.add(sendButton, BorderLayout.SOUTH);

        return jp;
    }

    public JPanel createTool() {
        JPanel jp = new JPanel();
        jp.setBackground(new Color(255, 255, 255));
        jp.setLayout(new GridLayout(1, 0));
        jp.setPreferredSize(new Dimension(0, 30));

        toolImageButton = new JButton("Picture");
        toolImageButton.setFont(new Font("menlo", Font.BOLD, 17));
        toolImageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toolImageButton.setContentAreaFilled(false);
        toolImageButton.setFocusPainted(false);

        Action imageAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fd = new JFileChooser();

                fd.setAcceptAllFileFilterUsed(false);
                fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fd.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith(".bmp") || f.getName().endsWith(".gif") || f.getName().endsWith(".jpg") || f.getName().endsWith(".png") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Picture(*.png/jpg/gif/bmp)";
                    }
                });

                if (fd.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fd.getSelectedFile();

                    try {
                        if (file != null) {
                            sendMsg(ClientUtil.Base64Utils.readBase64(file), 2);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        toolImageButton.addActionListener(imageAction);
        jp.add(toolImageButton);
        toolFileButton = new JButton("File");
        toolFileButton.setFont(new Font("menlo", Font.BOLD, 17));
        toolFileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toolFileButton.setContentAreaFilled(false);
        toolFileButton.setFocusPainted(false);

        Action fileAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fd = new JFileChooser();
                fd.setFileSelectionMode(JFileChooser.FILES_ONLY);

                if (fd.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fd.getSelectedFile();
                    if (file.length() > 1024 * 1024 * 10) {
                        callback.errorOccupy("The file size is larger than 10MB!");
                    } else {
                        sendMsg(file.getName(), 3);
                        new Thread(() -> {
                            byte[] data;
                            try {
                                data = ClientUtil.FileUtils.readFile(file);

                                callback.sendFile(friend.id, file.getName(), data);
                            } catch (Exception e12) {
                                e12.printStackTrace();
                            }


                        }).start();
                    }
                }
            }
        };
        toolFileButton.addActionListener(fileAction);
        jp.add(toolFileButton);

        return jp;
    }

    public void sendMsg(String msg, int type) {


        try {

            if (type != 3) {

                if(isGroup){

                    callback.sendGroupMsg(groupName,msg,type);

                }else{
                    callback.sendMsg(this.friend.id, msg, type);

                    if (type == 1) {
                        chatRecords.add(new ChatRecord(callback.id, "", msg, (byte) 0));
                    } else {
                        if (type == 2) {
                            chatRecords.add(new ChatRecord(callback.id, "", msg, (byte) 2));
                        } else if (type == 3) {
                            chatRecords.add(new ChatRecord(callback.id, "", msg, (byte) 4));
                        }
                    }
                }

            }
            this.chatPanel.addSent(msg, type);

        } catch (Exception e) {
            e.printStackTrace();
        }


        jScrollPane.getViewport().setViewPosition(new Point(0, jScrollPane.getVerticalScrollBar().getMaximum()));
    }

    public void receiveMsg(String msg, int type) {
        this.chatPanel.addReceived(msg, type);

        if (type == 1) {
            chatRecords.add(new ChatRecord(callback.id, "", msg, (byte) 1));
        } else {
            if (type == 2) {
                chatRecords.add(new ChatRecord(callback.id, "", msg, (byte) 3));
            } else if (type == 3) {
                chatRecords.add(new ChatRecord(callback.id, "", msg, (byte) 5));
            }
        }
        jScrollPane.getViewport().setViewPosition(new Point(0, jScrollPane.getVerticalScrollBar().getMaximum()));
    }

    class CloseWindow extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {

            if(!isGroup){
                try {

                    ChatRecordManager.saveChatRecord(callback.id, friend.id, chatRecords);

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                callback.closeChatPanel(friend);
            }

            dispose();
        }
    }
    public void dispose() {
        this.frame.dispose();
    }
}

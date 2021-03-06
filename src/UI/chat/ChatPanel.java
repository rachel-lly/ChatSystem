package UI.chat;

import model.Friend;
import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;


public class ChatPanel extends JPanel {

    public ParallelGroup seGroup1;
    public SequentialGroup seGroup2;
    public GroupLayout layout;
    public Friend friend, self;

    public ChatPanel(Friend friend, Friend self) {
        layout = new GroupLayout(this);
        this.setLayout(layout);
        this.friend = friend;
        this.self = self;

        seGroup1 = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        seGroup2 = layout.createSequentialGroup();
        seGroup2.addContainerGap();

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup().addContainerGap().addGroup(seGroup1).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(seGroup2));
    }

    public void addReceiveMessage(String text, int type) {
        JPanel msgPanel = null;

        if (type == 1) {
            msgPanel = LeftBubble.create(text);
        } else if (type == 2) {
            msgPanel = LeftBubble.createImage(text);
        } else if (type == 3) {
            msgPanel = LeftBubble.createFile(text, this.self.getId());
        }
        seGroup1.addComponent(msgPanel);
        seGroup2.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(msgPanel,
                GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18);
        this.updateUI();
    }

    public void addSendMessage(String text, int type) {
        JPanel msgPanel = null;

        if (type == 1) {
            msgPanel = RightBubble.create(text);
        } else if (type == 2) {
            msgPanel = RightBubble.createImage(text);
        } else if (type == 3) {
            msgPanel = RightBubble.createFile(text);
        }
        seGroup1.addComponent(msgPanel);
        seGroup2.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(msgPanel,
                GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18);
        this.updateUI();
    }
}

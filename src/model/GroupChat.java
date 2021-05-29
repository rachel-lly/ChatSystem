package model;

public class GroupChat {

    public String groupId;
    public String groupName;
    public int state;//0--在线 1--离线


    public GroupChat(String groupId, String groupName, int state) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.state = state;
    }



    @Override
    public String toString() {
        return this.groupName + "@" + this.groupId;
    }
}

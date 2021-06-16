package model;

public class GroupChat {

    private String groupId;
    private String groupName;
    private int state;//0--在线 1--离线

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

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

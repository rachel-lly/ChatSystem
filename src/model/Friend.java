package model;


public class Friend {
    private String id;
    private String nickName;
    private int state;//0--离线 1--在线

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Friend(String id, String nickName) {
        this(id, nickName, -1);
    }

    public Friend(String id, String nickName, int state) {
        this.id = id;
        this.nickName = nickName;
        this.state = state;
    }

    @Override
    public String toString() {
        if (this.state == -1) {
            return this.nickName + "@" + this.id;
        } else if (this.state == 1) {
            return this.nickName + "@" + this.id + " [在线]";
        } else if (this.state == 0) {
            return this.nickName + "@" + this.id + " [离线]";
        }

        return this.nickName + "@" + this.id + " [" + this.state + "]";
    }

}

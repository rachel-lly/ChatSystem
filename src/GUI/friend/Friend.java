package GUI.friend;


public class Friend {
    public String id;
    public String nickName;
    public int state;//0--离线 1--在线

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
            return this.nickName + "@" + this.id + " (状态: 在线)";
        } else if (this.state == 0) {
            return this.nickName + "@" + this.id + " (状态: 离线)";
        }

        return this.nickName + "@" + this.id + " (" + this.state + ")";
    }



//    public String toString() {
//        if (this.state == -1) {
//            return this.nickName + " ( ID: " + this.id+" ) ";
//        } else if (this.state == 1) {
//            return this.nickName + " ( ID: " + this.id+" ) "+ " [在线]";
//        } else if (this.state == 0) {
//            return this.nickName +" ( ID: " + this.id+" ) " + " [离线]";
//        }
//
//        return this.nickName +" ( ID: " + this.id+" ) " + " (" + this.state + ")";
//    }
}

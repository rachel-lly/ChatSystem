package model;


public class User {
    public String id;
    public String password;
    public String nickName;

    public User(String id, String password, String nickName) {
        this.id = id;
        this.password = password;
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return this.nickName + "@" + this.id;
    }
}

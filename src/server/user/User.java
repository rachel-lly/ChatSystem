package server.user;


public class User {
    public String id;
    public String passwordMd5;
    public String nickName;

    public User(String id, String passwordMd5, String nickName) {
        this.id = id;
        this.passwordMd5 = passwordMd5;
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return this.nickName + " ( ID: " + this.id +" )";
    }
}

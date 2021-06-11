package model;

import java.nio.channels.AsynchronousSocketChannel;


public class OnlineUser {
    public final User user;
    public AsynchronousSocketChannel sc;

    public String publicKey;

    public String privateKey;

    public OnlineUser(AsynchronousSocketChannel sc, String publicKey, String privateKey, User user) {
        this.sc = sc;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.user = user;
    }
}

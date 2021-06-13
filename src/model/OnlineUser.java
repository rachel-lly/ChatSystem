package model;

import java.nio.channels.AsynchronousSocketChannel;


public class OnlineUser {
    public final User user;
    public AsynchronousSocketChannel socketChannel;

    public String publicKey;

    public String privateKey;

    public OnlineUser(AsynchronousSocketChannel socketChannel, String publicKey, String privateKey, User user) {
        this.socketChannel = socketChannel;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.user = user;
    }
}

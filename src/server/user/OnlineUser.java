package server.user;

import java.nio.channels.AsynchronousSocketChannel;


public class OnlineUser {
    public final User user;
    public AsynchronousSocketChannel sc;
    /**
     * For receiving
     */
    public String publicKey;
    /**
     * For broadcasting
     */
    public String privateKey;

    public OnlineUser(AsynchronousSocketChannel sc, String publicKey, String privateKey, User user) {
        this.sc = sc;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.user = user;
    }
}

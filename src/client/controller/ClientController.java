package client.controller;

import client.util.ClientUtil;
import model.Friend;
import UI.util.DesignUtil;
import model.GroupChat;
import org.apache.commons.codec.binary.Hex;
import db.DBImpl;
import util.FileFolder;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ClientController {

    public static String ADDRESS_DEFAULT = "127.0.0.1";

    public static int PORT_DEFAULT = 9023;

    public static final int BANDWIDTH_DEFAULT = 1024 * 8;

    public int port;

    public String address;
    public String publicKey = null, privateKey = null;

    public ExecutorService threadPool;
    public AsynchronousChannelGroup channelGroup;
    public AsynchronousSocketChannel clientChannel = null;
    public UserController userController;
    public Map<String, ArrayList<byte[]>> packageBuffer;

    public ArrayList<Friend> friendList = new ArrayList<>();

   public void sendGroupMsg(String groupName, String msg, int type) throws MsgException {
        if (!this.clientChannel.isOpen()) {
            throw new MsgException(1, "Unconnected to server");
        }


        ArrayList<GroupChat> groupChats = DBImpl.INSTANCE.getGroupNameList();

        for(int i=0;i<groupChats.size();i++){
           if(groupChats.get(i).getGroupName().equals(groupName)){
               String id = groupChats.get(i).getGroupId();
               byte[] msgData = ClientUtil.PackageUtils.messageGroupPack(id, (byte) type, msg, privateKey);
               try {
                   secondaryPackAndSent(this.clientChannel, msgData, BANDWIDTH_DEFAULT);
               } catch (InterruptedException | ExecutionException e) {
                   e.printStackTrace();
               }
           }

        }


    }


    public void sendMsg(String id, String msg, int type) throws MsgException {
        if (!this.clientChannel.isOpen()) {
            throw new MsgException(1, "Unconnected to server");
        }


        byte[] msgData = ClientUtil.PackageUtils.messagePack(id, (byte) type, msg, privateKey);

        try {
            secondaryPackAndSent(this.clientChannel, msgData, BANDWIDTH_DEFAULT);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }



    public ClientController(UserController userController) throws IOException {
        this(userController, ADDRESS_DEFAULT, PORT_DEFAULT);
    }

    public ClientController(UserController userController, String address, int port) throws IOException {
        this.packageBuffer = new HashMap<>();
        this.userController = userController;
        this.address = address;
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(80);
        this.channelGroup = AsynchronousChannelGroup.withThreadPool(threadPool);
    }


    public void addFriends(ArrayList<Friend> friends) throws MsgException {
        if (!this.clientChannel.isOpen()) {
            throw new MsgException(1, "Unconnected to server");
        }
        byte[] friendsData = ClientUtil.PackageUtils.friendListPack((byte) 1, friends, privateKey);

        try {
            secondaryPackAndSent(this.clientChannel, friendsData, BANDWIDTH_DEFAULT);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    public void addGroupChat(String groupChatName) throws MsgException {
        if (!this.clientChannel.isOpen()) {
            throw new MsgException(1, "Unconnected to server");
        }

        DBImpl.INSTANCE.setGroupNameList(groupChatName);

    }



    public void deleteFriends(ArrayList<Friend> friends) throws MsgException {
        if (!this.clientChannel.isOpen()) {
            throw new MsgException(1, "Unconnected to server");
        }
        byte[] friendsData = ClientUtil.PackageUtils.friendListPack((byte) 2, friends, privateKey);

        try {
            secondaryPackAndSent(this.clientChannel, friendsData, BANDWIDTH_DEFAULT);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String id, String name, byte[] file) throws MsgException {
        if (!this.clientChannel.isOpen()) {
            throw new MsgException(1, "Unconnected to server");
        }
        byte[] msgData = ClientUtil.PackageUtils.filePack(id, name, file, privateKey);

        try {
            secondaryPackAndSent(this.clientChannel, msgData, BANDWIDTH_DEFAULT);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void login(String id, String password) throws InterruptedException, ExecutionException, LoginException, IOException {
        ByteBuffer signInMsg = ByteBuffer.allocate(4096);

        if (this.clientChannel != null && this.clientChannel.isOpen()) {
            this.clientChannel.close();
        }
        this.clientChannel = AsynchronousSocketChannel.open(channelGroup);
        this.clientChannel.connect(new InetSocketAddress(this.address, this.port)).get();
        this.clientChannel.write(ByteBuffer.wrap(
                ClientUtil.PackageUtils.loginPack(id, password)));
        this.clientChannel.read(signInMsg).get();

        if (signInMsg.get(0) == 1) {
            Map<String, String> keyPair = ClientUtil.PackageUtils.loginSucessUnPack(signInMsg);
            publicKey = keyPair.get("publicKey");
            privateKey = keyPair.get("privateKey");
            LoopingMessageHandler handler = new LoopingMessageHandler(this.clientChannel);
            this.clientChannel.read(handler.msgData, null, handler);
        } else if (signInMsg.get(0) == 4) {
            Map<String, String> keyPair = ClientUtil.PackageUtils.errorUnPack(signInMsg);
            String errorType = keyPair.get("subStatus");
            String errorMsg = keyPair.get("message");

            try {
                this.clientChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw new LoginException(Integer.parseInt(errorType), errorMsg);
        }
    }

    public void close() throws Exception {
        this.clientChannel.close();
    }

    public void enQueue(byte[] data) {
        String md5 = Hex.encodeHexString(ClientUtil.Convertion.BytesCapture(data, 0, 16));
        ArrayList<byte[]> queue = this.packageBuffer.computeIfAbsent(md5, k -> new ArrayList<>());

        System.out.println(Hex.encodeHexString(data));
        queue.add(data);
    }

    public void process(String md5) {
        if (this.packageBuffer.get(md5) == null) {
            return;
        }
        ByteBuffer msgData = ByteBuffer.wrap(ClientUtil.PackageUtils.SecondaryUnPack(this.packageBuffer.get(md5), BANDWIDTH_DEFAULT));
        this.packageBuffer.remove(md5);

        try {
            if (msgData.get(0) == 3) {
                Map<String, String> resMap = ClientUtil.PackageUtils.messageUnPack(msgData, publicKey);
                System.out.println("data type???" + resMap.get("msgType"));
                userController.receivedMsg(resMap.get("id"), resMap.get("message"), Integer.parseInt(resMap.get("msgType")));
            } else if (msgData.get(0) == 4) {
                System.out.println("receive error");
                Map<String, String> resMap = ClientUtil.PackageUtils.errorUnPack(msgData);
                userController.errorOccupy(resMap.get("message"));
            } else if (msgData.get(0) == 5) {
                ArrayList<Friend> resArray = ClientUtil.PackageUtils.friendListUnPack(msgData, publicKey);
                
                if (msgData.get(1) == 1) {
                    friendList = resArray;
                    userController.updateFriendList(resArray);
                    
                }
                
            } else if (msgData.get(0) == 6) {
                Map<String, Object> resMap;
                resMap = ClientUtil.PackageUtils.fileUnPack(msgData, publicKey);
                File file = new File(FileFolder.getDefaultDirectory() + "/" + resMap.get("id") + "/files/" + resMap.get("name"));
                ClientUtil.FileUtils.saveFile(file, (byte[]) resMap.get("file"));
                userController.receivedMsg((String) resMap.get("id"), (String) resMap.get("name"), 3);
                System.out.println("Documents received");
                System.out.println("From???" + resMap.get("id"));
                System.out.println("Name???" + resMap.get("name"));
                System.out.println("Save in???" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void secondaryPackAndSent(AsynchronousSocketChannel sc, byte[] data, int blockSize) throws InterruptedException, ExecutionException {
        ArrayList<byte[]> dataSeriesArrayList = ClientUtil.PackageUtils.SecondaryPack(data, blockSize);

        assert dataSeriesArrayList != null;
        for (byte[] datas : dataSeriesArrayList) {
            sc.write(ByteBuffer.wrap(datas)).get();
        }
    }



    class LoopingMessageHandler implements CompletionHandler<Integer, Object> {
        public AsynchronousSocketChannel sc;
        public ByteBuffer msgData = ByteBuffer.allocate(BANDWIDTH_DEFAULT);
        public LoopingMessageHandler(AsynchronousSocketChannel sc) {
            this.sc = sc;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            msgData.flip();
            Map<String, String> dataMap = ClientUtil.PackageUtils.readMD5andState(msgData);

            if ("1".equals(dataMap.get("info"))) {
                enQueue(msgData.array().clone());
            } else {
                if (!"0".equals(dataMap.get("info"))) {
                } else {
                    process(dataMap.get("MD5"));
                }
            }
            msgData.clear();
            this.sc.read(this.msgData, null, this);
        }

        @Override
        public void failed(Throwable ex, Object attachment) {
            DesignUtil.showErrorMsg("Disconnected from the Server!???", "ERROR", userController.chatListUI.frame);
            userController.exit();
            ex.printStackTrace();
        }
    }

    static class UniqueClientException extends Exception {
        int type;

        public UniqueClientException(int errorType, String message) {
            super(message);
            this.type = errorType;
        }
    }

    /**
     * ErrorType
     * 301 User does not connect to the message server
     */
    static class MsgException extends UniqueClientException {
        public MsgException(int errorType, String message) {
            super(300 + errorType, message);
        }
    }

    /**
     * ErrorType
     * 101 User's id does not exist
     * 102 User's password wrong
     */
    static class LoginException extends UniqueClientException {
        public LoginException(int errorType, String message) {
            super(100 + errorType, message);
        }
    }
}

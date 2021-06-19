package server.controller;

import client.controller.ClientController;
import model.GroupChat;
import org.apache.commons.codec.binary.Hex;
import model.OnlineUser;
import model.User;
import db.DBImpl;
import server.util.ServerUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ServerController {

    public int port;

    public static final int BANDWIDTH = 1024 * 8;
    public HashMap<String, OnlineUser> onlineUserList;
    public ExecutorService threadPool;
    public AsynchronousChannelGroup channelGroup ;
    public AsynchronousServerSocketChannel serverChannel;
    public Map<String, Map<String, ArrayList<byte[]>>> bufferDataMap = new HashMap<>();


    public ServerController() throws IOException {
        this(ClientController.PORT_DEFAULT);
    }

    public ServerController(int port) throws IOException {
        this.port = port;
        this.onlineUserList = new HashMap<>();
        this.threadPool = Executors.newFixedThreadPool(20);
        this.channelGroup = AsynchronousChannelGroup.withThreadPool(threadPool);
        this.serverChannel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(this.port));
    }

    public void start() {
        this.serverChannel.accept(null, new LoginHandler(this.serverChannel, this.onlineUserList));
        try {
            System.out.println("Server started successfully!");
            while (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class LoopingMessageHandler implements CompletionHandler<Integer, Object> {
        public AsynchronousSocketChannel sc;
        public OnlineUser user;
        public ByteBuffer msgData = ByteBuffer.allocate(BANDWIDTH);

        public LoopingMessageHandler(AsynchronousSocketChannel sc, OnlineUser user) {
            this.sc = sc;
            this.user = user;
        }

        public void process(String md5) {

            if (bufferDataMap.get(user.user.id).get(md5) == null) {
                return;
            }
            ByteBuffer msgData = ByteBuffer.wrap(ServerUtil.PackageUtils.secondaryUnPack(bufferDataMap.get(user.user.id).get(md5), BANDWIDTH));
            bufferDataMap.get(user.user.id).remove(md5);

            boolean isGroup = false;

            try {


                if (msgData.get(0) == 3) {

                    Map<String, String> resMap = ServerUtil.PackageUtils.messageUnPack(msgData, user.publicKey);

                    ArrayList<GroupChat> groupChats = DBImpl.INSTANCE.getGroupNameList();

                    ArrayList<String> groupidList = new ArrayList<>();

                    for(int i=0;i<groupChats.size();i++){
                        groupidList.add(groupChats.get(i).getGroupId());
                    }
                    for(String id:groupidList){
                        if(id.equals(resMap.get("id"))){
                            isGroup = true;
                            for(String onlineId:onlineUserList.keySet()){
                                if(!onlineId.equals(user.user.id)){
                                    sendMsg(resMap.get("id"), onlineId, resMap.get("message"), Integer.parseInt(resMap.get("msgType")));
                                }
                            }

                        }
                    }

                    if(!isGroup){
                        for (String id : DBImpl.INSTANCE.searchFriend(user.user.id)) {
                            if (id.equals(resMap.get("id"))) {
                                sendMsg(user.user.id, resMap.get("id"), resMap.get("message"), Integer.parseInt(resMap.get("msgType")));
                            }
                        }

                    }

                } else if (msgData.get(0) == 5) {
                    ArrayList<String> FriendStrings = ServerUtil.PackageUtils.friendListUnPack(msgData, user.publicKey);
                    if (msgData.get(1) == 1) {
                        for (String id : FriendStrings) {
                            if (DBImpl.INSTANCE.users.get(id) == null) {
                                secondaryPackAndSent(sc, ServerUtil.PackageUtils.errorPack((byte) 4, "Id:" + id + "not exist！"), BANDWIDTH);
                                break;
                            }
                            DBImpl.INSTANCE.connectFriend(user.user.id, id);

                            try {
                                if (onlineUserList.get(id) != null) {
                                    updateFriendList(onlineUserList.get(id));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (msgData.get(1) == 2) {
                        for (String id : FriendStrings) {
                            if (DBImpl.INSTANCE.users.get(id) == null) {
                                secondaryPackAndSent(sc, ServerUtil.PackageUtils.errorPack((byte) 4, "Id:" + id + "not exist！"), BANDWIDTH);
                                break;
                            }
                            DBImpl.INSTANCE.deleteFriend(user.user.id, id);

                            try {
                                if (onlineUserList.get(id) != null) {
                                    updateFriendList(onlineUserList.get(id));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    updateFriendList(user);
                }

                if (msgData.get(0) == 6) {
                    Map<String, Object> resMap = ServerUtil.PackageUtils.fileUnPack(msgData, user.publicKey);
                    System.out.println("receive file");
                    System.out.println("To：" + resMap.get("id"));
                    System.out.println("name：" + resMap.get("name"));


                    for (String id : DBImpl.INSTANCE.searchFriend(user.user.id)) {
                        if (id.equals(resMap.get("id")) && onlineUserList.get(resMap.get("id")) != null) {

                            byte[] data = ServerUtil.PackageUtils.filePack(user.user.id, (String) resMap.get("name"), (byte[]) resMap.get("file"), onlineUserList.get(id).privateKey);
                            new Thread(
                                    () -> {
                                        try {
                                            secondaryPackAndSentWithLimit(onlineUserList.get(id).socketChannel, data, BANDWIDTH);
                                        } catch (InterruptedException | ExecutionException e) {
                                            e.printStackTrace();
                                        }
                                    }
                            ).start();
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void enQueue(byte[] data) {
            String md5 = Hex.encodeHexString(ServerUtil.Convertion.BytesCapture(data, 0, 16));
            ArrayList<byte[]> queue = bufferDataMap.get(this.user.user.id).computeIfAbsent(md5, k -> new ArrayList<>());
            queue.add(data);
        }

        @Override
        public void completed(Integer result, Object attachment) {
            msgData.flip();
            Map<String, String> dataMap = ServerUtil.PackageUtils.readMD5andState(msgData);
            System.out.println(bufferDataMap.get(this.user.user.id).get(dataMap.get("MD5")));

            if ("1".equals(dataMap.get("info"))) {
                enQueue(msgData.array().clone());
            } else if ("0".equals(dataMap.get("info"))) {
                process(dataMap.get("MD5"));
            }
            msgData.clear();
            this.sc.read(this.msgData, null, this);
        }

        @Override
        public void failed(Throwable ex, Object attachment) {
            onlineUserList.remove(this.user.user.id);
            bufferDataMap.remove(this.user.user.id);
            userLogOut(user);
            System.out.println("User:" + this.user.user.toString() + " Offline" + "!Current number of online users：" + onlineUserList.size());
        }
    }


    public static void secondaryPackAndSent(AsynchronousSocketChannel sc, byte[] data, int blockSize) throws InterruptedException, ExecutionException {
        ArrayList<byte[]> dataSeriesArrayList;
        dataSeriesArrayList = ServerUtil.PackageUtils.SecondaryPack(data, blockSize);

        assert dataSeriesArrayList != null;
        for (byte[] datas : dataSeriesArrayList) {
            sc.write(ByteBuffer.wrap(datas)).get();
        }
    }

    public void secondaryPackAndSentWithLimit(AsynchronousSocketChannel sc, byte[] data, int blockSize) throws InterruptedException, ExecutionException {
        ArrayList<byte[]> dataSeriesArrayList = ServerUtil.PackageUtils.SecondaryPack(data, blockSize);

        assert dataSeriesArrayList != null;
        for (byte[] datas : dataSeriesArrayList) {
            sc.write(ByteBuffer.wrap(datas)).get();
        }
    }


    public boolean sendMsg(String id, String toId, String msg, int type) {
        boolean res = false;

        if (toId != null) {
            OnlineUser user = this.onlineUserList.get(toId);
            if (user == null) {
                res = true;
            } else {
                try {
                    secondaryPackAndSent(user.socketChannel, ServerUtil.PackageUtils.messagePack(id, (byte) type, msg, user.privateKey), BANDWIDTH);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (String onLineid : this.onlineUserList.keySet()) {
                OnlineUser user = this.onlineUserList.get(onLineid);
                try {
                    secondaryPackAndSent(user.socketChannel, ServerUtil.PackageUtils.messagePack(id, (byte) type, msg, user.privateKey), BANDWIDTH);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        return res;
    }

    public void updateFriendList(OnlineUser user) {
        try {
            ArrayList<User> friendList = DBImpl.INSTANCE.searchFriendAsUser(user.user.id);
            boolean[] onLine = new boolean[friendList.size()];


            for (int i = 0; i < onLine.length; ++i) {
                onLine[i] = onlineUserList.get(friendList.get(i).id) != null;

            }
            secondaryPackAndSent(user.socketChannel, ServerUtil.PackageUtils.friendListPack((byte) 1,
                    friendList, onLine,
                    user.privateKey), BANDWIDTH);
            friendList = DBImpl.INSTANCE.searchApplyFriendAsUser(user.user.id);
            onLine = new boolean[friendList.size()];
            secondaryPackAndSent(user.socketChannel, ServerUtil.PackageUtils.friendListPack((byte) 2,
                    friendList, onLine,
                    user.privateKey), BANDWIDTH);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }



    public void userLogOut(OnlineUser user) {
        for (String friendId : DBImpl.INSTANCE.searchFriend(user.user.id)) {
            if (onlineUserList.get(friendId) != null) {
                updateFriendList(onlineUserList.get(friendId));
            }
        }
    }

    class LoginHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
        public AsynchronousServerSocketChannel serverChannel;
        public HashMap<String, OnlineUser> channelList;

        public LoginHandler(AsynchronousServerSocketChannel sc, HashMap<String, OnlineUser> cl) {
            this.serverChannel = sc;
            this.channelList = cl;
        }

        @Override
        public void completed(final AsynchronousSocketChannel sc, Object attachment) {
            this.serverChannel.accept(null, this);
            ByteBuffer signInMsg = ByteBuffer.allocate(BANDWIDTH);
            final Map<String, OnlineUser> channel = this.channelList;
            sc.read(signInMsg, 1000, TimeUnit.MILLISECONDS, this.channelList,
                    new CompletionHandler<Integer, Object>() {

                        @Override
                        public void completed(Integer result, Object attachment) {

                            signInMsg.flip();
                            if (signInMsg.get(0) == 1) {
                                Map<String, String> infoMap = ServerUtil.PackageUtils.loginUnPack(signInMsg);
                                User targetUser = DBImpl.INSTANCE.users.get(infoMap.get("id"));

                                if (targetUser == null) {
                                    try {
                                        sc.write(ByteBuffer.wrap(ServerUtil.PackageUtils.errorPack((byte) 1, "id doesn't exist")))
                                                .get();
                                    } catch (InterruptedException | ExecutionException e1) {
                                        e1.printStackTrace();
                                    }
                                    System.out.println("id doesn't exist!");

                                    try {
                                        sc.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return;

                                } else if (!ServerUtil.LoginUtils.verifyPassword(infoMap.get("password"),
                                        targetUser.password)) {
                                    try {
                                        sc.write(ByteBuffer.wrap(ServerUtil.PackageUtils.errorPack((byte) 2, "password error!"))).get();
                                    } catch (InterruptedException | ExecutionException e1) {
                                        e1.printStackTrace();
                                    }
                                    System.out.println("password error!");
                                    try {
                                        sc.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    return;
                                } else if (onlineUserList.get(infoMap.get("id")) != null) {
                                    try {
                                        sc.write(ByteBuffer.wrap(ServerUtil.PackageUtils.errorPack((byte) 3, "Your account has been logged in elsewhere!"))).get();
                                    } catch (InterruptedException | ExecutionException e1) {
                                        e1.printStackTrace();
                                    }
                                    System.out.println("Repeat login!");

                                    try {
                                        sc.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    return;
                                }

                                Map<String, String> keyPair1 = ServerUtil.RSAUtils.createKeys(1024);  // For client
                                Map<String, String> keyPair2 = ServerUtil.RSAUtils.createKeys(1024);  // For server
                                OnlineUser targetOnlineUser = new OnlineUser(sc, keyPair2.get("publicKey"),
                                        keyPair1.get("privateKey"), targetUser);
                                channel.put(infoMap.get("id"), targetOnlineUser);
                                bufferDataMap.put(infoMap.get("id"), new HashMap<>());

                                try {
                                    sc.write(ByteBuffer.wrap(ServerUtil.PackageUtils.loginSuccessPack(keyPair1.get("publicKey"),
                                            keyPair2.get("privateKey")))).get();
                                    updateFriendList(targetOnlineUser);
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                                LoopingMessageHandler msgHandler = new LoopingMessageHandler(sc, targetOnlineUser);
                                sc.read(msgHandler.msgData, null, msgHandler);

                                for (String friendId : DBImpl.INSTANCE.searchFriend(targetOnlineUser.user.id)) {
                                    if (onlineUserList.get(friendId) != null) {
                                        updateFriendList(onlineUserList.get(friendId));
                                    }
                                }
                                System.out.println("User:" + targetUser.toString() + "online!Current number of online users：" + channel.size());
                            } else {
                                try {
                                    sc.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void failed(Throwable ex, Object attachment) {
                            System.out.println("login timeout!");
                            try {
                                sc.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

        @Override
        public void failed(Throwable ex, Object attachment) {
            System.out.println("connection failed: " + ex);
        }
    }
}

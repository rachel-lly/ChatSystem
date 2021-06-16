package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ChatRecord {
    private String id;
    private String nickName;
    private String message;
    private byte state;//1---text 2---Image 3---File

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public ChatRecord(String id, String nickName, String message, byte state) {
        this.id = id;
        this.nickName = nickName;
        this.message = message;
        this.state = state;
    }

    public ChatRecord() {
        this("", "", "", (byte) 0);
    }

    public void writeToFile(DataOutputStream stream) throws IOException {
        stream.writeByte(this.state);
        stream.writeInt(this.message.getBytes(StandardCharsets.UTF_8).length);
        stream.write(this.message.getBytes(StandardCharsets.UTF_8));
    }

    public static ChatRecord writeToData(DataInputStream stream) throws IOException {
        ChatRecord resChatRecord;
        resChatRecord = new ChatRecord();
        resChatRecord.state = stream.readByte();
        byte[] message = new byte[stream.readInt()];
        stream.read(message);
        resChatRecord.message = new String(message, StandardCharsets.UTF_8);

        return resChatRecord;
    }
}

package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ChatRecord {
    public String id;
    public String nickName;
    public String msg;
    public byte state;//1---text 2---Image 3---File

    public ChatRecord(String id, String nickName, String msg, byte state) {
        this.id = id;
        this.nickName = nickName;
        this.msg = msg;
        this.state = state;
    }

    public ChatRecord() {
        this("", "", "", (byte) 0);
    }

    public void toFile(DataOutputStream stream) throws IOException {
        stream.writeByte(this.state);
        stream.writeInt(this.msg.getBytes(StandardCharsets.UTF_8).length);
        stream.write(this.msg.getBytes(StandardCharsets.UTF_8));
    }

    public static ChatRecord toData(DataInputStream stream) throws IOException {
        ChatRecord resChatRecord;
        resChatRecord = new ChatRecord();
        resChatRecord.state = stream.readByte();
        byte[] msg = new byte[stream.readInt()];
        stream.read(msg);
        resChatRecord.msg = new String(msg, StandardCharsets.UTF_8);

        return resChatRecord;
    }
}

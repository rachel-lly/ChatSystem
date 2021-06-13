package client.controller;

import model.ChatRecord;
import java.io.*;
import java.util.ArrayList;

public class ChatRecordManager {

    public static void saveChatRecord(String srcId, String dstId, ArrayList<ChatRecord> record) throws IOException {

        File file = new File(FileFolder.getDefaultDirectory() + "/" + srcId + "/" + dstId + ".txt");

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));
        outputStream.writeInt(record.size());

        for (ChatRecord chatRecord : record) {
            chatRecord.writeToFile(outputStream);
        }

        outputStream.close();

    }

    public static ArrayList<ChatRecord> readChatRecord(String srcId, String dstId) throws IOException {

        File file = new File(FileFolder.getDefaultDirectory() + "/" + srcId + "/" + dstId + ".txt");
        ArrayList<ChatRecord> resArrayList = new ArrayList<>();

        if (!file.exists() || file.isDirectory()) {
            return resArrayList;
        }

        DataInputStream inputStream = new DataInputStream(new FileInputStream(file));

        try {

            int size = inputStream.readInt();
            System.out.println(size);
            for (int i = 0; i < size; ++i) {
                resArrayList.add(ChatRecord.writeToData(inputStream));
            }
            inputStream.close();

        } catch (IOException e) {
            inputStream.close();
            e.printStackTrace();
        }

        return resArrayList;
    }
}

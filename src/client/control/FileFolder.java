package client.control;

import java.io.File;


public class FileFolder {

    public static String getDefaultDirectory() {
        return "./src/chatRecordFile";
    }
    static {
        init();
    }

    public static void init() {
        File file = new File(getDefaultDirectory());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        file.mkdirs();
    }
}

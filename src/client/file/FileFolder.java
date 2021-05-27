package client.file;

import java.io.File;


public class FileFolder {

    public static String getDefaultDirectory() {
        return "./src/client/chattingRecord";
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

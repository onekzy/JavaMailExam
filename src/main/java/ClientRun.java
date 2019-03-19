import client.release.ClientConsole;
import utils.nativeUtils.NativeUtils;

import java.io.IOException;

public class ClientRun {
    static {
        try {
            NativeUtils.loadLibraryFromJar("/native/JIntellitype.dll");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ClientConsole();
    }
}

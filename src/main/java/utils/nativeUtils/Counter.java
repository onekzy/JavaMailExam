package utils.nativeUtils;

public class Counter {
    public static int count;

    static {
        count = (int) (Math.random()*1000000);
    }

    public static String getCount() {
        return String.valueOf(count);
    }
}

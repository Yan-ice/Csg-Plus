package org.csg.group.task.toolkit;

public class JSUtils {
    public static String[] split(String origin, String regex) {
        return origin.split(regex);
    }

    public static String[] split(String origin, String regex, int limit) {
        return origin.split(regex,limit);
    }
}

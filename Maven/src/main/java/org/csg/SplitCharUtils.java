package org.csg;

public class SplitCharUtils {
    public static String getSplitChar(String osName) {
        if (osName.contains("win")) {
            return ";";
        } else if (osName.contains("nix") || osName.contains("nux")) {
            return ":";
        } else {
            return null;
        }
    }
}

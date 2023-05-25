package org.csg.Utils;

import java.util.regex.Pattern;

public class CommonUtils {

    /**
     * 判断字符串是否为数字
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[0-9]+(\\.[0-9]+)?$");
        return pattern.matcher(str).matches();
    }

}

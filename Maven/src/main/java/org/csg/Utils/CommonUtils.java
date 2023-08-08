package org.csg.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.csg.Fwmain;

import java.util.Random;
import java.util.regex.Pattern;

public class CommonUtils {

    public CommonUtils() { }


    public static Random random = new Random();

    /**
     * 判断字符串是否为数字
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[0-9]+(\\.[0-9]+)?$");
        return pattern.matcher(str).matches();
    }

    /**
     * 控制台输出常规信息
     * @param info
     */
    public static void ConsoleInfoMsg(String info) {
        Fwmain.getInstance().getLogger().info(ChatColor.translateAlternateColorCodes('&',"&a[信息]&r " + info));
    }

    /**
     * 控制台输出警告信息
     * @param info
     */
    public static void ConsoleWarnMsg(String info) {
        Fwmain.getInstance().getLogger().info(ChatColor.translateAlternateColorCodes('&',"&e[警告]&r " + info));
    }

    public static void ConsoleErrorMsg(String info) {
        Fwmain.getInstance().getLogger().info(ChatColor.translateAlternateColorCodes('&',"&c[错误]&r " + info));
    }
    public static void ConsoleDebugMsg(String str){
        if(Fwmain.getInstance().isDebug()){
            Fwmain.getInstance().getLogger().info(ChatColor.translateAlternateColorCodes('&',"&e[调试]&r " + str));
        }
    }

    /**
     * 随机取件的整数
     * @param min 最小值
     * @param max 最大值
     * @return 随机的值
     */
    public static int Random(int min, int max) {
        int s;
        int length;
        if (min > max) {
            length = min - max;
            s = random.nextInt(length) + max;
        } else if (min < max) {
            length = max - min;
            s = random.nextInt(length) + min;
        } else {
            return min;
        }
        return s;
    }


    public static void ConsoleCommand(String command) {
        Bukkit.dispatchCommand(Fwmain.getInstance().getServer().getConsoleSender(), command);
    }
}

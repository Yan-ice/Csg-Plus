package org.csg.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.csg.Fwmain;
import org.csg.group.Lobby;

import java.io.File;
import java.util.Objects;

public class OSUtils {

    // 私有化构造器
    private OSUtils() { }

    /**
     * 获取操作系统类型
     * @return 操作系统类型
     */
    public static String analyseOs() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) {
            return "win";
        } else {
           return "linux";
        }
    }

    /**
     * 加载配置文件
     * @param file 文件对象
     * @return 配置文件
     */
    public static FileConfiguration loadFileConfiguration(File file) {
        // 判断文件是否存在，不存在则从 jar 包中读取
        if (!file.exists()) {
            Fwmain.getInstance().saveResource(file.getName(),false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * 检查文件夹是否存在
     * @param lobbyFolder Lobby 文件夹
     * @return 是否存在
     */
    public static boolean checkFolder(File lobbyFolder) {
        boolean flg = false;
        // 判断是否存在插件文件夹
        if (!Fwmain.getInstance().getDataFolder().exists()) {
            flg = Fwmain.getInstance().getDataFolder().mkdir();
        }
        // 判断是否存在Lobby文件夹
        if (!lobbyFolder.exists()) {
            flg = lobbyFolder.mkdir();
        }

        return flg;
    }

    public static void loadAllLobby(File lobbyFolder) {
        for(File file : Objects.requireNonNull(lobbyFolder.listFiles())){
            if(file.isDirectory()){
                new Lobby(file).addToList();
            }
        }
    }
}

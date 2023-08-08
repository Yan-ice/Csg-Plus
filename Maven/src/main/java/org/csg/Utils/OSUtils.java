package org.csg.Utils;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.csg.Fwmain;
import org.csg.group.Lobby;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

    /**
     * 加载所有 Lobby游戏文件
     * @param lobbyFolder
     */
    public static void loadAllLobby(File lobbyFolder) {
        for(File file : Objects.requireNonNull(lobbyFolder.listFiles())){
            if(file.isDirectory()){
                new Lobby(file).addToList();
            }
        }
    }


    /**
     * 加载世界路径
     */
    public static String loadWorldPath(){
        boolean in_world = true;
        String default_worldname = "world";
        File csgt = new File("./");

        for(World w : Fwmain.getInstance().getServer().getWorlds()){
            boolean pass = false;

            if(w.getName().contains("_nether")){
                default_worldname = w.getName().split("_nether")[0];

                for(File f : csgt.listFiles()){
                    if(f.getName().equals(w.getName())){
                        in_world = false;
                    }
                }
            }
        }

        if(in_world){
            CommonUtils.ConsoleInfoMsg("CustomGo认为你的世界应该安装在./"+default_worldname+"中！");
            return "./"+default_worldname+"/";
        }else{
            CommonUtils.ConsoleInfoMsg("CustomGo认为你的世界应该安装在根目录中！");
            return "./";
        }
    }

    public static void loadBukkitCore(File root, boolean isPaper) {
        if(!isPaper){
            Arrays.stream(System.getProperty("java.class.path").split(";")).filter(e -> e.endsWith(".jar")).forEach(e -> {
                File file = new File(e);
                try {
                    JarFile jar = new JarFile(file);
                    JarEntry entry = jar.getJarEntry("version.json");

                    if(jar.getJarEntry("version.json") != null || jar.getJarEntry("mohist_libraries.txt") != null){
                        CommonUtils.ConsoleInfoMsg("识别到核心端 " + file.getAbsolutePath());
                        Fwmain.getInstance().getBukkitCoreList().add(file);
                    }
                    if(entry != null){
                        CommonUtils.ConsoleInfoMsg("识别到核心端 " + file.getAbsolutePath());
                        Fwmain.getInstance().getBukkitCoreList().add(file);
                    }
                }catch (Exception err){
                    err.printStackTrace();
                }
            });

        }else{
            File[] files = root.listFiles();
            if (files != null) {
                for(File f : files){
                    if(f.isDirectory()){
                        loadBukkitCore(f,isPaper);
                    }else{
                        if(f.getName().endsWith(".jar")){
                            if(Fwmain.getInstance().isDebug()) {
                                CommonUtils.ConsoleInfoMsg("识别到API "+f.getName());
                            }
                            Fwmain.getInstance().bukkitCoreList.add(f);
                        }
                    }
                }
            }
        }
    }

    /**
     * 保存配置文件
     * @param configuration 配置文件对象
     * @param file 文件对象
     */
    public static void save(FileConfiguration configuration, File file) {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

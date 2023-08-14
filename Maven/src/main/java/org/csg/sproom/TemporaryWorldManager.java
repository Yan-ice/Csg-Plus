package org.csg.sproom;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.csg.Fwmain;
import org.csg.Utils.CommonUtils;
import org.csg.Utils.OSUtils;
import org.csg.group.Lobby;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TemporaryWorldManager implements WorldManager {

    private List<World> temporaryWorlds = new ArrayList<>();

    private final Fwmain plugin;

    public TemporaryWorldManager(Fwmain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void createTemporaryWorldFromSource(String sourceWorldName, Lobby lobby, List<Player> senderList) {
        World sourceWorld = Bukkit.getWorld(sourceWorldName);
        if (sourceWorld == null) {
            CommonUtils.ConsoleInfoMsg("&7目标世界不存在: &c" + sourceWorldName);
            return;
        }

        // 生成新世界的名称和文件夹路径
        String newWorldName = sourceWorldName + "_TEMP_" + System.currentTimeMillis();
        File sourceWorldFolder = sourceWorld.getWorldFolder();
        File newWorldFolder = new File(plugin.getWorldpath(), newWorldName);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // 复制世界文件夹
            try {
                CommonUtils.ConsoleInfoMsg("&7正在复制世界文件夹...");
                OSUtils.copyWorld(sourceWorldFolder.toPath(), newWorldFolder.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 在主线程中加载世界
            Bukkit.getScheduler().runTask(plugin, () -> {
                WorldCreator creator = new WorldCreator(newWorldName);
                creator.environment(sourceWorld.getEnvironment());
                World newWorld = Bukkit.createWorld(creator);
                temporaryWorlds.add(newWorld);
                // 世界加载完成后触发事件
                Bukkit.getPluginManager().callEvent(new TemporaryWorldEvent(newWorld, lobby, senderList));
            });
        });
    }

    @Override
    public void createTemporaryWorldFromZip(File zipFile) {

    }

    @Override
    public void createTemporaryWorldFromWorldEdit(File schematicFile) {
    }

    @Override
    public void unloadTemporaryWorld(World world) {

    }
}

package org.csg.sproom;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.csg.group.Lobby;

import java.io.File;
import java.util.List;

public interface WorldManager {

    /**
     * 从现有世界复制创建临时世界
     * @param sourceWorldName 源世界名称
     * @param lobby 游戏Lobby对象
     * @return 临时世界
     */
    void createTemporaryWorldFromSource(String sourceWorldName, Lobby lobby, List<Player> senderList);

    /**
     * 从ZIP文件解压创建临时世界
     * @param zipFile ZIP文件
     * @return 临时世界
     */
    void createTemporaryWorldFromZip(File zipFile);

    /**
     * 从WorldEdit文件创建临时世界
     * @param schematicFile WorldEdit文件
     * @return 临时世界
     */
    void createTemporaryWorldFromWorldEdit(File schematicFile);

    /**
     * 卸载临时世界
     * @param world 临时世界
     */
    void unloadTemporaryWorld(World world);
}

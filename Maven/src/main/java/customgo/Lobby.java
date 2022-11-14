package customgo;

import customgo.event.PlayerJoinLobbyEvent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.csg.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface Lobby {
    /**
     * 获得该大厅内的所有玩家。
     * @return
     */
    public List<UUID> getPlayerList();
    /**
     * 获得该大厅内是否有该玩家。
     * @return
     */
    public boolean hasPlayer(Player p);

    /**
     * 触发脚本中的指定名字的function，触发的玩家自己指定。
     * @param name function名字
     * @param p 触发者（可为null）
     * @param para 参数，如果参数列表不对应会报错。
     */
    public Object callFunction(String name, Player p, Object... para);

    /**
     * 设置大厅是否允许加入。
     * 在大厅内没人时，自动切换为允许加入。
     * @param canJoin 是否允许加入
     */
    public void setCanJoin(boolean canJoin);

    /**
     * 触发脚本中的指定名字的listener。由玩家所在队列触发。
     * @param name listener名字
     * @param p 触发者，不为null则必须在lobby中。
     * @param para 参数，如果参数列表不对应会报错。
     */
    public void callListener(String name, Player p, Object... para);


    /**
     * 重新命名此游戏。
     * @param name
     */
    public void rename(String name);

    /**
     * 获得大厅的名字。
     * @return
     */
    public String getName();


    public Lobby clone();

    /**
     * 获得大厅的初始队伍。
     * @return
     */
    public Group getDefaultGroup();

    /**
     * 获得大厅的队伍列表。
     * @return
     */
    public Set<Group> getGroupList();

    /**
     * 获得大厅的人数。
     * @return
     */
    public int getPlayerAmount();

    /**
     * 获取大厅内的变量，可以是玩家分数，宏，或局部变量。
     * @param p 玩家
     * @param key 变量占位符
     * @return
     */
    public String getVariable(Player p, String key);

    /**
     * 使玩家转移到游戏内的另一队伍。
     * @return
     */
    public void ChangeGroup(Player player,String groupname);


    /**
     * 为该游戏大厅创建一个新的工作文件(或读取一个已存在的文件)。
     * @param name 文件名
     * @return YamlFileConfiguration
     */
    public FileConfiguration loadWorkFile(String name);

    /**
     * 保存一个被修改过的工作文件。
     * @param name 文件名
     */
    public void saveWorkFile(String name);

    /**
     * 删除一个工作文件。
     * 注意，删除后所有文件内信息将会丢失！
     * @param name 文件名
     */
    public void deleteWorkFile(String name);

    /**
     * 令一名玩家加入该副本
     * @param player
     */
    public void Join(Player player);

    /**
     * 令一名玩家离开该副本
     * @param player
     */
    public void Leave(Player player);
}

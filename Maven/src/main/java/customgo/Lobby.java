package customgo;

import customgo.event.PlayerJoinLobbyEvent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.csg.Data;

import java.util.List;
import java.util.Map;
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
     * 查找一名游戏内玩家所处的队伍。
     * @param p 游戏内玩家
     * @return 返回玩家所处的队伍，如果玩家为null或不在游戏中，返回null。
     */
    public Group findGroupOfPlayer(Player p);
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
     * 自动字符串化。
     * @param p 玩家
     * @param key 变量占位符
     * @return
     */
    public String getVariable(Player p, String key);

    /**
     * 获取大厅内的宏，不会字符串化。
     * 自动字符串化。
     * @param key 变量占位符
     * @return
     */
    public Object getMacro(String key);

    /**
     * 获取一个宏，需指定需要的类型
     * @param key
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T getMacro(String key, Class<T> clazz);

    /**
     * 获取宏转换为整数
     * @param key
     * @return
     */
    public int getMacroInt(String key);

    /**
     * 获取宏转换为浮点数
     * @param key
     * @return
     */
    public double getMacroDouble(String key);

    /**
     * 获取宏转换为字符串
     * @param key
     * @return
     */
    public String getMacroString(String key);

    /**
     * 获取宏转换为布尔值
     * @param key
     * @return
     */
    public boolean getMacroBoolean(String key);

    /**
     * 获取宏转换为列表
     * @param key
     * @return
     */
    public List<String> getMacroList(String key);


    /**
     * 添加一个全局变量宏
     * @param key
     * @param value
     */
    public void addMacro(String key, Object value);

    /**
     * 获取全局/玩家得分。
     * @param p
     * @param key
     * @return
     */
    public double getScore(Player p, String key);

    /**
     * 添加全局/玩家得分。
     * @param player
     * @param key
     * @param value
     */
    public void addScore(Player player, String key, Double value);

    /**
     * 添加玩家键值对
     * @param key 键
     * @param player 玩家
     * @param value 值
     */
    public void addValues(String key, Player player, String value);

    /**
     * 移除玩家键值对
     * @param key 键
     * @param player 玩家
     */
    public void removeValues(String key, Player player);

    /**
     * 获取玩家键值对
     * @param key 键
     * @param player 玩家
     */
    public void getValues(String key, Player player);

    /**
     * 模糊的查询是否存在Macro键
     * @param key
     * @return
     */
    public boolean hasMacroForLike(String key);

    /**
     * 模糊的查询包含Macro键的所有键值
     * @param key
     * @return
     */
    public Map<String, Object> getMacroForLike(String key);

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

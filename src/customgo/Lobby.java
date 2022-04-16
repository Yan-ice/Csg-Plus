package customgo;

import org.bukkit.configuration.file.FileConfiguration;
import org.csg.group.Group;
import org.csg.group.task.toolkit.PlayerValueBoard;
import org.csg.group.task.toolkit.ValueBoard;
import org.bukkit.entity.Player;

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
     * 触发脚本中的指定名字的listener，触发的队列与玩家均自己指定。
     * @param name listener名字
     * @param g 所在队伍
     * @param p 触发者（可为null）
     * @param para 参数，如果参数列表不对应会报错。
     */
    public void callListener(String name, Group g, Player p, Object[] para);

    /**
     * 触发脚本中的指定名字的listener。由玩家所在队列触发。
     * @param name listener名字
     * @param p 触发者，不可为null且必须在lobby中。
     * @param para 参数，如果参数列表不对应会报错。
     */
    public void callListener(String name, Player p, Object[] para);

    /**
     * 触发脚本中的指定名字的listener。由默认队列触发，且无触发玩家。
     * @param para 参数，如果参数列表不对应会报错。
     */
    public void callListener(String name, Object[] para);


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
     * 使玩家转移到游戏内的另一队伍。
     * @return
     */
    public void ChangeGroup(Player player,String groupname);

    /**
     * 获得游戏的全局计分板对象
     * @return
     */
    public ValueBoard ValueBoard();

    /**
     * 获得游戏的玩家计分板对象
     * @return
     */
    public PlayerValueBoard PlayerValueBoard();

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
}

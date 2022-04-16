package customgo;

import java.util.Set;

import org.bukkit.entity.Player;

public interface PlayerValueBoard {

    /**
     * 在计分板上给一个玩家记录新的变量。
     * 如果变量名已存在，则覆盖它。
     * @param Name 变量名
     * @param Value 值
     * @param player 被记录的玩家
     */
    public void Value(String Name,Double Value,Player player);

    /**
     * 在计分板上获取一个玩家的变量。
     * 如果不存在该玩家，则获取"[空玩家变量]"
     * 如果存在该玩家而不存在该变量，则获取"[未知变量]"
     * @param Name 变量名
     * @param player 被获取的玩家
     * @return 获取到的值
     */
    public double getValue(String Name,Player player);

    /**
     * 移除计分板上指定玩家的指定变量。
     * @param Name 变量名
     * @param player 被移除变量的玩家
     */
    public void removeValue(String Name,Player player);

    /**
     * 获得计分板上一个玩家的所有变量。
     * @param player 玩家
     * @return 变量列表
     */
    public Set<String> ValueList(Player player);
}

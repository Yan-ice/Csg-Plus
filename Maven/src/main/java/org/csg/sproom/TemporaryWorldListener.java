package org.csg.sproom;


import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.csg.Fwmain;
import org.csg.Utils.CommonUtils;
import org.csg.group.Lobby;

public class TemporaryWorldListener implements Listener {

    @EventHandler
    public void onTemporaryWorld(TemporaryWorldEvent event) {
        World world = event.getWorld();

        CommonUtils.ConsoleInfoMsg("&7临时世界 &d" + world.getName() + " &7已创建成功");
        Lobby newLobby = event.getSourceLobby().clone(true, world.getName());
        Fwmain.lobbyList.add(newLobby);
        for (Player player : event.getWaitPlayer()) {
            newLobby.Join(player);
            CommonUtils.ConsoleInfoMsg("&7玩家 &a" + player.getName() + " &7已加入镜像游戏 &d" + newLobby.getName());
        }
    }
}

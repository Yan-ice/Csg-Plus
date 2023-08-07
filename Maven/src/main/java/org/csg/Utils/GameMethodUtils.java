package org.csg.Utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.csg.group.Lobby;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameMethodUtils {

    Lobby lobby;

    Map<UUID,Location> spawnpoint = new HashMap<>();

    public GameMethodUtils(Lobby lobby) {
        this.lobby = lobby;
    }

    public void setSpawn(Player p, Location loc){
        // 若参数均为空，则不做任何操作
        if(loc==null || p==null){
            return;
        }

        // 若玩家不在Lobby游戏中，则不做任何操作
        if(!lobby.hasPlayer(p)){
            return;
        }

        // 若玩家已经设置过出生点，则替换
        if(spawnpoint.containsKey(p.getUniqueId())){
            spawnpoint.replace(p.getUniqueId(),loc);
        }else{ // 否则添加新的出生点
            spawnpoint.put(p.getUniqueId(),loc);
        }
    }

    public Location getSpawn(Player p){

        // 若参数为空或者玩家不在Lobby游戏中，则不做任何操作
        if(p==null || !lobby.hasPlayer(p)){
            return null;
        }
        return spawnpoint.get(p.getUniqueId());
    }

}

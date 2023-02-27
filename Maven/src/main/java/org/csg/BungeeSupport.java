package org.csg;

import customgo.Lobby;
import customgo.event.PlayerLeaveLobbyEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BungeeSupport implements Listener {
    private static BungeeSupport singleton;

    Lobby lb;
    public BungeeSupport(Lobby lobby){
        if(singleton!=null){
            HandlerList.unregisterAll(singleton);
            singleton = this;
        }
        Bukkit.getPluginManager().registerEvents(this,Data.fmain);
        lb = lobby;
    }
    @EventHandler
    public void jlisten(PlayerJoinEvent e){
        lb.Join(e.getPlayer());
    }
    @EventHandler
    public void llisten(PlayerLeaveLobbyEvent e){
        e.getPlayer().kickPlayer("您已离开游戏！");
    }
}

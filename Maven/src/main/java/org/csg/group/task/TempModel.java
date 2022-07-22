package org.csg.group.task;

import customgo.Group;
import customgo.Lobby;

import customgo.PlayerValueBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempModel {

            JavaPlugin plugin;
            Lobby lobby = null;
           Group group = null;
           Player striker = null;
           Player player = null;


    LobbyPlaceholder lobbyPlaceholderExp;
    public void initPlaceholder(){
        lobbyPlaceholderExp = new LobbyPlaceholder(lobby);
        boolean success = lobbyPlaceholderExp.register();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"");
        if(success){
            System.out.println("PAPI连接成功！");
        }else{
            System.out.println("PAPI连接失败！");
        }

    }

    public String replaceHolder(String s){
        return PlaceholderAPI.setPlaceholders(player,s);
    }
    public void destroyPlaceholder(){
        lobbyPlaceholderExp.p_unregister();
    }
    public void addPlaceHolder(String k,String v){
        if(lobbyPlaceholderExp!=null){
            lobbyPlaceholderExp.addPlaceHolder(k,v);
        }else{
            System.out.println("尝试添加占位符，但并未成功连接到PAPI！");
        }
    }

    class LobbyPlaceholder extends PlaceholderExpansion {
        Lobby lobby;
        public LobbyPlaceholder(Lobby l) {
            lobby = l;
        }

        public void p_unregister(){
            PlaceholderAPI.unregisterPlaceholderHook(getIdentifier());
        }
        public void addPlaceHolder(String key,String value){
            if(m.containsKey(key)){
                m.replace(key, value);
            }else{
                m.put(key, value);
            }
        }

        Map<String,String> m = new HashMap<>();

        @Override
        public String onPlaceholderRequest(Player arg0, String arg1) {

            if(arg1.startsWith("score_")){
                String score = arg1.substring(6);
                for(String v : lobby.PlayerValueBoard().ValueList(arg0)){
                    if(v.equals(score)){
                        return String.valueOf(lobby.PlayerValueBoard().getValue(v,arg0));
                    }
                }
                for(String v : lobby.ValueBoard().ValueList()){
                    if(v.equals(score)){
                        return String.valueOf(lobby.ValueBoard().getValue(v));
                    }
                }
                return "未知分数";
            }
            for(String s : m.keySet()){
                if (arg1.equals(s)) {
                    return m.get(s);
                }
            }
            return "未知变量";
        }
        @Override
        public String getAuthor() {
            return "Yan_ice";
        }
        @Override
        public String getIdentifier() {
            return "fw"+lobby.getName();
        }
        @Override
        public String getVersion() {
            return "V1.1";
        }
        @Override
        public String getPlugin() {
            return "Csg-Plus";
        }

    }



}
package org.csg.sproom;


import com.grinderwolf.swm.api.world.SlimeWorld;

import customgo.event.PlayerLeaveLobbyEvent;
import net.minecraft.server.v1_12_R1.GameRules;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import org.bukkit.scheduler.BukkitRunnable;
import org.csg.Data;
import org.csg.FileMng;
import org.csg.group.Lobby;

import java.io.*;
import java.util.List;
import java.util.Map;

public class Reflect implements Listener {

	protected ReflectStatu statu = ReflectStatu.PREPARING;
	//protected World model;

	protected SlimeWorld world_template;

	protected String worldName;
	protected int id = 0;
	protected Lobby lobby;
	protected Room byRoom;


	public Reflect(Room room, final int id) {
		this.byRoom = room;
		worldName = "FW_" + byRoom.name + "_" + id;
		world_template = room.loadWorld().clone(worldName);
		//
		this.id = id;

		Data.fmain.getServer().getPluginManager().registerEvents(Reflect.this, Data.fmain);

		lobby = byRoom.lobby_model.clone();
		lobby.macros.AddMacro("world",worldName);
		lobby.rename(lobby.getName()+"_"+id);
		lobby.addToList();
		if(lobby.isComplete()){
			statu = ReflectStatu.WAITING;
		}else{
			statu = ReflectStatu.WRONG;
		}
		loadWorld();
	}

	public ReflectStatu getStatu(){
		if(lobby==null){
			return ReflectStatu.WRONG;
		}
		if(statu==ReflectStatu.WAITING){
			if("true".equals(lobby.macros.macros.get("isGaming"))){
				statu = ReflectStatu.STARTED;
			}
		}
		return statu;
	}

	public int getId(){
		return id;
	}
	public Lobby getLobby(){
		return lobby;
	}

	public void renew() {
		if(statu==ReflectStatu.ENDED) {

			statu = ReflectStatu.PREPARING;
			loadWorld();
			statu = ReflectStatu.WAITING;
		}
	}
	private void loadWorld(){
		World model = Bukkit.getWorld(worldName);
		if(model!=null){
			for(Player p : model.getPlayers()){
				p.teleport(Data.defaultLocation);
			}
			Bukkit.unloadWorld(model, false);
		}

		world_template = byRoom.loadWorld().clone(worldName);
		Room.plugin.generateWorld(world_template);

		model = Bukkit.getWorld(worldName);
		for(Map.Entry<String,String> kv: byRoom.roomSetting.gamerule.entrySet()){
			model.setGameRuleValue(kv.getKey(),kv.getValue());
		}

	}
	public boolean Join(Player p){
		if(statu!=ReflectStatu.WAITING){
			p.sendMessage(ChatColor.RED+"房间正在准备中！请您稍等片刻...");
			return false;
		}
		lobby.Join(p);
		return true;
	}
	
	public boolean hasPlayer(Player p){
		if(lobby!=null){
			return lobby.getPlayerList().contains(p);
		}else{
			return false;
		}
	}

	public void emergencyUnload() {
		if(lobby!=null && lobby.isComplete()){
			lobby.unLoad();
		}
		lobby=null;
		World model = Bukkit.getWorld(worldName);
		if(model!=null){
			for(Player p :model.getPlayers()){
				p.teleport(Data.defaultLocation);
			}
			Data.fmain.getServer().unloadWorld(model, false);
		}
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void st(PlayerRespawnEvent evt){
		if(lobby!=null && lobby.hasPlayer(evt.getPlayer())){
			Location loc = evt.getRespawnLocation();
			loc.setWorld(Bukkit.getWorld(worldName));
			evt.setRespawnLocation(loc);
		}
	}

	@EventHandler
	public void bd(BlockBreakEvent evt){
		if(!byRoom.roomSetting.allowBuilding && lobby.hasPlayer(evt.getPlayer())){
			if(evt.getBlock().getWorld().getName().equals(worldName)){
				evt.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void bd(BlockPlaceEvent evt){
		if(!byRoom.roomSetting.allowBuilding && lobby.hasPlayer(evt.getPlayer())){
			if(evt.getBlock().getWorld().getName().equals(worldName)){
				evt.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void st(PlayerLeaveLobbyEvent evt){
		if(evt.getLobby()!=this.lobby){
			return;
		}
		if(evt.getLobby().getPlayerAmount()<=1 && statu!=ReflectStatu.UNLOADING){
			statu = ReflectStatu.UNLOADING;
			new BukkitRunnable() {
				int wait = 10;
				@Override
				public void run() {
					World model = Bukkit.getWorld(worldName);
					if(model!=null) {
						if(model.getPlayers().size()>0 && wait>0){
							wait--;
							return;
						}
						for(Player p : model.getPlayers()){
							p.teleport(Data.defaultLocation);
						}
						Bukkit.unloadWorld(model,false);

					}
					statu = ReflectStatu.ENDED;
					cancel();
					byRoom.autoCreateReflect();
				}
			}.runTaskTimer(Data.fmain, 60,60);
		}
	}

}

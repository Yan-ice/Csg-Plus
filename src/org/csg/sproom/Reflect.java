package org.csg.sproom;

import customgo.event.PlayerLeaveLobbyEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.csg.Data;
import org.csg.FileMng;
import org.csg.group.Lobby;

import java.io.File;

public class Reflect implements Listener {

	protected ReflectStatu statu = ReflectStatu.PREPARING;
	protected World model;
	protected String worldName;
	protected int id = 0;
	protected Lobby lobby;
	protected Room byRoom;

	public Reflect(Room room, final int id) {
		this.byRoom = room;
		worldName = "FW_" + byRoom.name + "_" + id;
		this.id = id;
		loadFile();
		loadWorld();
	}

	public ReflectStatu getStatu(){
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
			statu = ReflectStatu.WAITING;
		}
	}
	
	protected void loadWorld() {
		new BukkitRunnable() {

			@Override
			public void run() {
				World w = Bukkit.getWorld(worldName);
				if(w!=null) {
					w.setKeepSpawnInMemory(false);
					w.setAutoSave(false);
					Data.fmain.getServer().unloadWorld(w, false);
					Data.fmain.getLogger().info("在尝试加载世界"+worldName+"时出现异常：该世界未被卸载？");
					Data.fmain.getLogger().info("已自动卸载原世界！");
				}
				
				model = Data.fmain.getServer().createWorld(WorldCreator.name(worldName));
				
				model.setAutoSave(false);
				model.setKeepSpawnInMemory(false);
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


			}
			
		}.runTask(Data.fmain);
		
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

		if(model!=null){
			for(Player p :model.getPlayers()){
				p.teleport(Data.defaultLocation);
			}
			Data.fmain.getServer().unloadWorld(model, false);
			model = null;
		}
		unloadFile();
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void st(PlayerRespawnEvent evt){
		if(lobby!=null && lobby.hasPlayer(evt.getPlayer())){
			Location loc = evt.getRespawnLocation();
			loc.setWorld(model);
			evt.setRespawnLocation(loc);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void st(ChunkUnloadEvent evt){
		if(evt.getWorld()==model){
			evt.setSaveChunk(false);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void st(PlayerLeaveLobbyEvent evt){
		if(evt.getLobby()!=this.lobby){
			return;
		}
		Data.fmain.getLogger().info("LISTENED: you leave "+lobby.getName()+" "+evt.getLobby().getPlayerAmount());
		evt.getPlayer().sendMessage("LISTENED: you leave "+lobby.getName()+" "+evt.getLobby().getPlayerAmount());
		if(evt.getLobby().getPlayerAmount()<=1){

			statu = ReflectStatu.UNLOADING;
			new BukkitRunnable() {
				@Override
				public void run() {
					if(model!=null) {
						for(Player p : model.getPlayers()){
							p.teleport(Data.defaultLocation);
						}
						if(model.getPlayers().size()>0){
							return;
						}
						for(Chunk chu : model.getLoadedChunks()){
							chu.unload(false);
						}
						statu = ReflectStatu.ENDED;
						cancel();
						byRoom.autoCreateReflect();

					}else {
						statu = ReflectStatu.ENDED;
						cancel();
						byRoom.autoCreateReflect();
					}
				}
			}.runTaskTimer(Data.fmain, 60,60);
		}
	}

	boolean fileProcessing = false;

	void loadFile() {
		fileProcessing = true;
		File file = new File(byRoom.lobby_model.getTempFolder(), "sproom_world");
		File target = new File(Data.worldpath+worldName);

		if(target.exists()){
			FileMng.deleteDir(target);
		}
		FileMng.copyDir(file,target);
		fileProcessing = false;
	}

	void unloadFile() {
		fileProcessing = true;
		FileMng.deleteDir(new File(Data.worldpath + worldName));
		model = null;
		fileProcessing = false;
	}

}

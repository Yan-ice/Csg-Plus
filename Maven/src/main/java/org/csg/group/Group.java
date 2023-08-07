package org.csg.group;

import java.util.*;

import org.bukkit.entity.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


public class Group implements customgo.Group{
	public static Group SearchPlayerInGroup(Player pl){
		for(Lobby l : Lobby.getLobbyList()){
			for(Group g : l.getGroupListI()){
				if(g.hasPlayer(pl)){
					return g;
				}
			}
		}
		return null;
	}

	private String Name;

	protected List<UUID> playerList = new ArrayList<>();

	public Group(Lobby byLobby, String name) {
		Name = name;
		this.byLobby = byLobby;
		byLobby.callListener("onGroupLoaded",null,this);
	}

	protected Lobby byLobby;


	public String getName() {
		return Name;
	}

	public int getPlayerAmount() {
		return playerList.size();
	}

	public List<UUID> getPlayerList(){
		return playerList;
	}


	public void UnLoad() {

		while(playerList.size()>0){
			Player p = Bukkit.getPlayer(playerList.get(0));
			if(p!=null){
				getLobby().Leave(p);
			}
		}
		byLobby.callListener("onGroupUnloaded",null,this);
	}


	public boolean hasPlayer(Player player) {
		if(player==null){
			return false;
		}
		return playerList.contains(player.getUniqueId());
	}


	public Lobby getLobby() {
		return byLobby;
	}


	public void JoinGroup(Player player) {
		UUID uid = player.getUniqueId();
		playerList.add(uid);
		byLobby.callListener("onPlayerJoinGroup",player, this);
	}

	public void LeaveGroup(Player pl) {
		for (UUID plu : playerList) {
			if (pl.getUniqueId().equals(plu)) {
				byLobby.callListener("onPlayerLeaveGroup",pl, this);
				playerList.remove(plu);

				return;
			}
		}
	}

	public void sendNotice(String str){
		for(UUID pl : playerList){
			Bukkit.getPlayer(pl).sendMessage(str);
		}
	}

	public void state(CommandSender sender) {
		sender.sendMessage(ChatColor.AQUA + getName()+ ":");

		String pl = "";
		if (playerList.size() > 0) {
			for (int a = 0; a < playerList.size(); a++) {
				pl = pl + Bukkit.getPlayer(playerList.get(a)).getName() + " ";
			}
		}
		if (pl.equals("")) {
			pl = "无";
		}
		sender.sendMessage(ChatColor.YELLOW + "玩家:");
		sender.sendMessage(pl);
	}

	public boolean isClear() {
		return this.getPlayerAmount() == 0;
	}
}

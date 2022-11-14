package org.csg.group;

import java.util.*;

import org.bukkit.entity.*;
import org.csg.group.task.csgtask.FunctionTask;
import org.csg.group.task.toolkit.TaskExecuter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import org.csg.Data;
import org.csg.group.hologram.FwHologram;
import org.csg.location.Teleporter;

public class Group implements customgo.Group{

	static Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

	List<String> active_field = new ArrayList<>();


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

	private FileConfiguration file;
	private PlayerRule rule = new PlayerRule();


	private String Name;
	private String Display;


	protected List<UUID> playerList = new ArrayList<>();

	public boolean localRespawn = false;

	Team t;

	public Group(String Name, FileConfiguration f) {
		this.Name = Name;
		this.file = f;
	}

	public Group(Lobby byLobby,FileConfiguration file,String name) {
		Name = name;
		this.file = file;
		setLobby(byLobby);
		Load();
	}
	public Group(Lobby byLobby,String name,String display) {
		Name = name;
		Display = display;
		this.file = null;
		setLobby(byLobby);
		Load();
	}

	public void setLobby(Lobby byLobby){
		this.byLobby = byLobby;
	}

	protected Lobby byLobby;


	public String GetDisplay() {
		return Display;
	}


	public String getName() {
		return Name;
	}

	public int getPlayerAmount() {
		return playerList.size();
	}

	public List<UUID> getPlayerList(){
		return playerList;
	}

	public FileConfiguration getFileConf() {
		return file;
	}

	public void UnLoad() {

		while(playerList.size()>0){
			Player p = Bukkit.getPlayer(playerList.get(0));
			if(p!=null){
				getLobby().Leave(p);
			}
		}
		try{
			if(t!=null){
				t.unregister();
				t = null;
			}
		}catch(Exception e){
			Data.ConsoleInfo("尝试卸载未注册的Team。(debug)");
		}
	}

	public void Load() {

//		if(this.byLobby.Name.length()>3){
//			if(Name.length()>4){
//				teamname = "F"+this.byLobby.Name.substring(0, 4)+Name.substring(0, 4);
//			}else{
//				teamname = "F"+this.byLobby.Name.substring(0, 4)+Name;
//			}
//
//		}else{
//			if(Name.length()>4){
//				teamname = "F"+this.byLobby.Name+Name.substring(0, 4);
//			}else{
//				teamname = "F"+this.byLobby.Name+Name;
//			}
//		}

		if(getFileConf()==null){
			teamload();
			Data.ConsoleInfo("队伍 "+Name+" 加载成功!");
			onGroupLoaded();
			return;
		}

		try{
			if(getFileConf().contains("Display")){
				Display = getFileConf().getString("Display");
			}
			if(getFileConf().contains("ListenerScript")) {
				active_field = getFileConf().getStringList("ListenerScript");
			}

			rule.Load(this);

			teamload();
			Data.ConsoleInfo("队伍 "+Name+" 加载成功!");
			onGroupLoaded();

		}catch(NullPointerException | NumberFormatException e){
			Data.ConsoleInfo("队伍 "+Name+" 加载出现错误：");
			e.printStackTrace();
		}
	}

	private String teamname = "Fdefault";

	private void teamload(){
		if(!rule.useTeam){
			t = null;
			return;
		}
		do{
			teamname = "F"+Data.random.nextInt(899999)+100000;
		}while(board.getTeam(teamname)!=null);
		t = board.registerNewTeam(teamname);

		if(Data.HighMCVersion){
			switch(rule.NameInv()){
			case 0:
				t.setOption(Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
				break;
			case 1:
				t.setOption(Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS);
				break;
			case 2:
				t.setOption(Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
				break;
			case 3:
				t.setOption(Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.FOR_OWN_TEAM);
				break;
			}
		}

		if(rule.Prefix()!=null){
			t.setPrefix(rule.Prefix().replace("&", "§"));
		}
	}

	public boolean hasPlayer(Player player) {
		if(player==null){
			return false;
		}
		return playerList.contains(player.getUniqueId());
	}

	public boolean hasScriptField(String field){
		if(active_field.size()==0){
			return true;
		}
		for(String s : active_field){
			if(field.contains(s)){
				return true;
			}
		}
		return false;
	}
	public Lobby getLobby() {
		return byLobby;
	}

	public PlayerRule getRule(){
		return rule;
	}

	public void JoinGroup(Player player,String from) {
		UUID uid = player.getUniqueId();

		playerList.add(uid);
		onPlayerJoin(player,from);
	}

	public void LeaveGroup(UUID pl,String to) {
		for (UUID plu : playerList) {
			if (pl.equals(plu)) {
				Player p = Bukkit.getPlayer(pl);
				onPlayerLeave(p,to);

				playerList.remove(pl);
				onPlayerRest();

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
		sender.sendMessage(ChatColor.AQUA + Display + ChatColor.WHITE + "("+Name+")");

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

	private void onGroupLoaded() {
		byLobby.callListener("onGroupLoaded",null,this);
	}

	private void onPlayerJoin(Player player,String from) {

		byLobby.callListener("onPlayerJoin",player,from);
	}
	private void onPlayerLeave(Player player,String to) {

		byLobby.callListener("onPlayerLeave",player,to);

		player.setCustomName(player.getName());
		player.setCustomNameVisible(true);
	}

	private void onPlayerRest() {
		byLobby.callListener("onPlayerRest",null,playerList.size());
	}

}

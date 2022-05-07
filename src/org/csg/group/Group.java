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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import org.csg.Data;
import org.csg.group.hologram.FwHologram;
import org.csg.location.Teleporter;

public class Group implements customgo.Group{

	static Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

	List<String> active_field = new ArrayList<>();
	boolean back = false;

	public static Group SearchPlayerInGroup(Player pl){
		for(Lobby l : Lobby.getLobbyList()){
			for(Group g : l.getGroupList()){
				if(g.hasPlayer(pl)){
					return g;
				}
			}
		}
		return null;
	}


	private FileConfiguration file;
	private PlayerRule rule = new PlayerRule();

	private GListener listener;

	private String Name;
	private String Display;
	public FwHologram hd = new FwHologram();

	protected List<UUID> playerList = new ArrayList<>();

	public List<Location> LeaveLoc = new ArrayList<>();
	public List<Location> RespawnLoc = new ArrayList<>();
	public List<Location> GroupLoc = new ArrayList<>();

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

		HandlerList.unregisterAll(listener);

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
			refreshListener();
			Data.ConsoleInfo("队伍 "+Name+" 加载成功!");
			onGroupLoaded();
			return;
		}

		try{
			if(getFileConf().contains("Display")){
				Display = getFileConf().getString("Display");
			}
			if(getFileConf().contains("ListenerScript")){
				active_field = getFileConf().getStringList("ListenerScript");
			}
			if(getFileConf().contains("Locations.Leaves")){
				if(getFileConf().get("Locations.Leaves") instanceof String){
					if("back".equals(getFileConf().get("Locations.Leaves"))) {
						back = true;
					}else {
						LeaveLoc.add(Teleporter.stringToLoc(getFileConf().getString("Locations.Leaves"),null));
					}

				}else{

					List<String> Loclist = getFileConf().getStringList("Locations.Leaves");
					if(Loclist.get(0).equals("back")) {
						back = true;
					}else {
						for (String s : Loclist) {
							Location l = Teleporter.stringToLoc(s);
							if(l!=null){
								LeaveLoc.add(l);
							}
						}
					}
				}
			}

			if(getFileConf().contains("Locations.Respawn")){
				List<String> Loclist = getFileConf().getStringList("Locations.Respawn");
				if(Loclist.get(0).equals("back")) {
					localRespawn = true;
				}else {
					for (int a = 0; a < Loclist.size(); a++) {
						Location l = Teleporter.stringToLoc(Loclist.get(a));
						if(l!=null){
							RespawnLoc.add(l);
						}
					}
				}
			}
			if(getFileConf().contains("Locations.Arena")){
				List<String> Loclist = getFileConf().getStringList("Locations.Arena");

				for (String s : Loclist) {
					Location l = Teleporter.stringToLoc(s);
					if(l!=null){
						GroupLoc.add(l);
					}
				}
			}
			rule.Load(this);

			teamload();
			refreshListener();
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

	protected void refreshListener(){
		if(listener!=null){
			HandlerList.unregisterAll(listener);
		}
		listener = new GListener(this);
		Data.fmain.getServer().getPluginManager().registerEvents(listener, Data.fmain);

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

		if(Group.SearchPlayerInGroup(player)!=null){
			//player.sendMessage(ChatColor.RED+"您已经在一个大厅中了！");
			return;
		}

		playerList.add(uid);
		if(board.getTeam(teamname)==null||t==null){
			teamload();
		}
		if(t!=null && !t.hasPlayer(player)){
			t.addPlayer(player);
		}

		Teleporter tel = new Teleporter(player);
		tel.TeleportRandom(GroupLoc);
		onPlayerJoin(player,from);
		return;
	}

	public void LeaveGroup(UUID pl,String to) {
		for (UUID plu : playerList) {
			if (pl.equals(plu)) {
				Player p = Bukkit.getPlayer(pl);
				if(t!=null && board.getTeam(teamname)!=null &&  t.hasPlayer(p)){
					t.removePlayer(p);
				}

				onPlayerLeave(p,to);

				playerList.remove(pl);
				Teleporter tel = new Teleporter(p);
				tel.TeleportRandom(LeaveLoc);
				onPlayerRest();

				return;
			}
		}
	}


	public static void AutoLeaveGroup(Player player,String to){
		if(Group.SearchPlayerInGroup(player)!=null){
			Group.SearchPlayerInGroup(player).LeaveGroup(player.getUniqueId(),to);
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
		String statu = "";
		if (playerList.size() > 0) {
			for (int a = 0; a < playerList.size(); a++) {
				pl = pl + Bukkit.getPlayer(playerList.get(a)).getName() + " ";
			}
		}
		if (pl == "") {
			pl = "无";
		}
		sender.sendMessage(ChatColor.YELLOW + "玩家:");
		sender.sendMessage(pl);
		sender.sendMessage(ChatColor.YELLOW + "游戏状态: " + statu);
	}

	public boolean isClear() {
		return this.getPlayerAmount() == 0;
	}

	public void runTask(FunctionTask task, UUID striker, Object[] param){
		TaskExecuter executer = new TaskExecuter(task,this);
		task.loadArgs(executer,param);
		executer.execute(striker);
	}

	private void onGroupLoaded() {
		byLobby.callListener("onGroupLoaded",this,null,new Object[0]);
	}

	private void onPlayerJoin(Player player,String from) {

		byLobby.callListener("onPlayerJoin",this,player,new Object[]{from});
	}
	private void onPlayerLeave(Player player,String to) {

		byLobby.callListener("onPlayerLeave",this,player,new Object[]{to});

		player.setCustomName(player.getName());
		player.setCustomNameVisible(true);
	}

	private void onPlayerRest() {
		byLobby.callListener("onPlayerRest",this,null,new Object[]{playerList.size()});
	}

}

class GListener implements Listener {
	Group g;
	PlayerRule rule;
	public GListener(Group g){
		this.g = g;
		rule = g.getRule();
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	private void PVPListen(EntityDamageByEntityEvent evt) {
		if (evt.getEntity() instanceof Player) {
			Player damaged = (Player) evt.getEntity();
			Player damager;
			if (evt.getDamager() instanceof Player) {
				damager = (Player) evt.getDamager();
				if (g.hasPlayer(damaged) && g.hasPlayer(damager)) {
					if (!rule.PvP()) {
						evt.setCancelled(true);
						if(rule.PvPMessage()!="none"){
							damager.sendMessage(rule.PvPMessage());
						}

					}else{
						if(rule.HighPriority()){
							evt.setCancelled(false);
						}
					}
				}
			}else
			if(evt.getDamager() instanceof Projectile){
				if(((Projectile)evt.getDamager()).getShooter() != null){
					if((((Projectile)evt.getDamager()).getShooter()) instanceof Player){
						damager = (Player)(((Projectile)evt.getDamager()).getShooter());
						if(g.hasPlayer(damaged) && g.hasPlayer(damager)){
							if (!rule.Projectile()) {
								evt.setCancelled(true);
								if(rule.ProjectileMessage()!="none"){
									damager.sendMessage(rule.ProjectileMessage());
								}

							}else{
								if(rule.HighPriority()){
									evt.setCancelled(false);
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGH)
	private void PotionListen(PotionSplashEvent evt2) {
		ThrownPotion pot = evt2.getPotion();
		if (pot.getShooter() instanceof Player) {
			Player shooter = (Player) pot.getShooter();
			if(g.hasPlayer(shooter)){
				if(!rule.Potionhit()){
					evt2.setCancelled(true);
					List<Entity> damageds = pot.getNearbyEntities(3.0, 3.0, 3.0);
					for (Entity d : damageds) {
						if (d instanceof Player) {
							Player damaged = (Player)d;
								if ((shooter != damaged) && g.hasPlayer(damaged) ) {
									if(rule.PotionhitMessage()!="none"){
										shooter.sendMessage(rule.PotionhitMessage());
									}
								} else {
									damaged.addPotionEffects(pot.getEffects());
								}
						} else if (d instanceof Creature) {
							((Creature)d).addPotionEffects(pot.getEffects());
						}
					}
				}else{
					if(rule.HighPriority()){
						evt2.setCancelled(false);
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	private void CommandListen(PlayerCommandPreprocessEvent evt) {
		if(evt.isCancelled() || evt.getPlayer().isOp()){
			return;
		}
		if (g.hasPlayer(evt.getPlayer())) {
			String Command = evt.getMessage().split(" ")[0];
			if (Command.equalsIgnoreCase("/csg")) {
				return;
			}
			for (int a = 0; a < rule.WhiteListCommand().size(); a++) {
				if (Command.equalsIgnoreCase("/" + rule.WhiteListCommand().get(a))) {
					return;
				}
			}
			evt.setCancelled(true);
			evt.getPlayer().sendMessage(ChatColor.RED + "队列禁止使用本指令。");
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	private void ChatListen(PlayerChatEvent evt) {
		if(evt.isCancelled() || evt.getMessage().startsWith("/")){
			return;
		}
		if(rule.chatInGroup() && g.hasPlayer(evt.getPlayer())){
			evt.setCancelled(true);
			String message = rule.ChatFormat();
			message = Data.ColorChange(message);
			message = message.replace("%player%", evt.getPlayer().getName());

			message = message.replace("%group%", g.GetDisplay());
			if(evt.getMessage().startsWith("!")){
				message = message.replace("%type%", "[所有人]");
				message = message.replace("%message%", evt.getMessage().substring(1));
				for(Group g : g.byLobby.getGroupList()){
					g.sendNotice(message);
				}
			}else{
				message = message.replace("%message%", evt.getMessage());
				message = message.replace("%type%", "[队伍内]");
				g.sendNotice(message);
			}
			
		}
	}
	Map<UUID, Location> dloc = new HashMap<>();

	@EventHandler
	private void LListen(PlayerQuitEvent evt){
		if(g.hasPlayer(evt.getPlayer())){
			g.getLobby().callListener("onPlayerOffline",g,evt.getPlayer(),new Object[0]);
			g.getLobby().Leave(evt.getPlayer());
		}
	}
	
	@EventHandler
	private void LListen(EntityDamageEvent evt){
		if(evt.getEntity() instanceof ArmorStand){
			if(g.hd.Holograms().containsValue((ArmorStand) evt.getEntity())){
				evt.setCancelled(true);
			}
		}

	}
}

package org.csg;

import java.io.File;

import java.util.*;

//import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.csg.group.Group;
import org.csg.group.Lobby;

import org.csg.group.task.ValueData;
import org.csg.location.Teleporter;
import org.csg.sproom.Reflect;
import org.csg.sproom.Room;

public class Fwmain extends JavaPlugin implements Listener {


	public FileConfiguration load(File file) {
		if (!file.exists()) {
			saveResource(file.getName(), true);
		}
		return YamlConfiguration.loadConfiguration(file);

	}
	Set<Player> plist = new HashSet<>();
	Set<Player> vexlist = new HashSet<>();

	private void Fwcommands(CommandSender sender, String args[]) {
		if(args[0].equals("skip")){
			if(args.length<2){
				return;
			}
			if(args.length<3){
				if(sender instanceof Player){
					Group g = Group.SearchPlayerInGroup((Player)sender);
					if(g!=null){
						Lobby l = g.getLobby();
						if (sender.hasPermission("csg.skip." + args[1]) || CheckPerm(sender, "csg.skip")) {
							l.ChangeGroup((Player)sender, args[1]);
						}
					}

				}else{
					sender.sendMessage("控制台不能这么做！");
				}
			}else{
				if(sender.isOp()){
					for(Player p : Bukkit.getOnlinePlayers()){
						if(p.getName().equals(args[2])){
							Group g = Group.SearchPlayerInGroup(p);
							if(g!=null){
								Lobby l = g.getLobby();
								l.ChangeGroup(p, args[1]);
							}
							return;
						}
					}
					sender.sendMessage("无效的玩家名！");
				}
			}
		}else if(args[0].equals("item")){
			if(args.length<2){
				return;
			}

			return;
		}
		if(args.length < 2){
			switch(args[0]){
				case "unloadtec":
					Data.ConsoleInfo("已卸载CustomGoTec");
					Bukkit.unloadWorld("CustomGoTec",false);
					return;
				case "killall":
					if(!(sender instanceof Player)){
						return;
					}
					for(Entity e : ((Player)sender).getNearbyEntities(300, 150, 300)){
						Data.ConsoleInfo("Find : "+e.getName()+" in "+ Teleporter.locToString(e.getLocation()));
						if(!(e instanceof LivingEntity)){
							continue;
						}
						LivingEntity en = (LivingEntity) e;
						if(!en.isDead()){
							Data.ConsoleInfo("kill");
							en.remove();
						}

					}
					break;
				case "list":
					if (CheckPerm(sender, "csg.list")) {
						showList(sender);
					}
					break;
				case "uuid":
					if (CheckPerm(sender, "csg.uuid")) {
						if(plist.contains((Player)sender)) {
							plist.remove((Player)sender);
							sender.sendMessage("您退出了UUID查看模式。");
						}else {
							sender.sendMessage("您正在UUID查看模式：攻击一个生物来查看它的UUID。");

							plist.add((Player)sender);
						}
					}
					break;
				case "debug":
					if (CheckPerm(sender, "csg.admin")) {
						if(Data.debug){
							Data.debug = false;
							sender.sendMessage("测试模式已关闭。");
						}else{
							Data.debug = true;
							sender.sendMessage("测试模式已开启。");
						}

					}
					break;
				case "reload":
					if (CheckPerm(sender, "csg.reload")) {
						Reload(sender);
					}
					break;

				case "leave":
					if(sender instanceof Player){
						if (CheckPerm(sender, "csg.leave")) {
							Lobby.AutoLeave((Player)sender,false);
						}
					}
					break;
				case "help":
					if (CheckPerm(sender, "csg.help")) {
						Help.MainHelp(sender);
						Help.LobbyHelp(sender);
						if(Bukkit.getWorld("CustomGoTec")==null){
							sender.sendMessage("§a*************************************");
							sender.sendMessage("§a检测到可以进行教程模板加载/更新！");
							sender.sendMessage("§a如果你是第一次使用插件，教程会对你有很大帮助哦~！");
							sender.sendMessage("§a");
							sender.sendMessage("想要加载/更新教程，请输入 §e/fw teach");
							sender.sendMessage("§a");
							sender.sendMessage("§7(附加提示：更新教程会重载插件~)");
							sender.sendMessage("§a*************************************");
						}

					}
					break;
				case "teach":
					sender.sendMessage("§d正在进行教程模板加载/更新！请耐心等待...");
					this.LoadTec();
					this.Reload(sender);
					sender.sendMessage("§d更新完成！你可以选择以下一个队列进行游戏：");
					sender.sendMessage("§e/csg CustomGoTec join §d[推荐！]");
					sender.sendMessage("§e/csg Dungeon join");
					sender.sendMessage("§e/csg 7sec_run join");
					sender.sendMessage("§e/csg Battle join §d[需要多个玩家]");
					sender.sendMessage("§e/csg MoneyGame join §d[需要前置Vault，PlaceHolderAPI]");
					sender.sendMessage("§d一边游戏的同时，可以一边看看它们的配置文件噢！");

					break;
				case "stop":
					if (CheckPerm(sender, "csg.stop")) {
						sender.sendMessage(ChatColor.BLUE+"安全关闭所有游戏！");
						for(Lobby l : Lobby.getLobbyList()){
							l.Clear();
						}
						Lobby.getLobbyList().clear();
					}

					break;
				default:
					String Name = args[0];
					if(Lobby.getLobby(Name)!=null){
						Help.LobbyHelp(sender);
					}else{
						Help.MainHelp(sender);
					}


			}
		}else{
			if(args[0].equals("leave")){
				if(sender.isOp()){
					for(Player p : Bukkit.getOnlinePlayers()){
						if(p.getName().equals(args[1])){
							Lobby.AutoLeave(p,false);
							return;
						}
					}
					sender.sendMessage("无效的玩家名！");
					return;
				}else{
					return;
				}
			}

			String Name = args[0];
			Lobby lobby = Lobby.getLobby(Name);
			if (args.length < 2) {
				Help.LobbyHelp(sender);
			} else {
				if (lobby != null) {
					switch (args[1]) {
						case "load":
							if (CheckPerm(sender, "csg.load")) {
								sender.sendMessage("重载游戏 "+args[0]+" ！");
								lobby.load();
							}
							break;
						case "unload":
							if (CheckPerm(sender, "csg.unload")) {
								sender.sendMessage("卸载游戏 "+args[0]+" ！");
								lobby.unLoad();
							}
							break;
						case "join":
							if(args.length<3){
								if(sender instanceof Player){
									if (sender.hasPermission("csg.join." + args[0]) || CheckPerm(sender, "csg.join")) {
										if(Room.searchRoom(args[0])!=null){
											Room.searchRoom(args[0]).JoinRoom((Player)sender);
										}else{
											lobby.Join((Player)sender);
										}

									}
								}else{
									sender.sendMessage("控制台不能这么做！");
								}
							}else{
								if(sender.isOp()){
									for(Player p : Bukkit.getOnlinePlayers()){
										if(p.getName().equals(args[2])){
											if(Room.searchRoom(args[0])!=null){
												Room.searchRoom(args[0]).JoinRoom(p);
											}else{
												lobby.Join(p);
											}
											return;
										}
									}
									sender.sendMessage("无效的玩家名！");
								}

							}

							break;
						case "statu":
							if (CheckPerm(sender, "csg.statu")) {
								if(Room.searchRoom(args[0])!=null){
									Room r = Room.searchRoom(args[0]);
									sender.sendMessage(ChatColor.YELLOW+"房间名： "+ChatColor.AQUA+r.getName()+ChatColor.YELLOW+"  游戏进行个数： "+r.allreflects.size()+"/"+r.getMaxReflect());
									for(Reflect rf : r.allreflects){
										if(rf!=null){
											sender.sendMessage(ChatColor.YELLOW+"  副本镜像"+rf.getId()+"： ");
											String pli = "";
											switch(rf.getStatu()){
												case WAITING:
													sender.sendMessage("    游戏状态： "+ChatColor.GREEN+"等待中！");
													for(UUID p : rf.getLobby().getPlayerList()){
														pli = pli+Bukkit.getPlayer(p).getName()+" ";
													}
													sender.sendMessage("    游玩玩家："+pli);
													break;
												case STARTED:
													sender.sendMessage("    游戏状态： "+ChatColor.RED+"游戏中");
													for(UUID p : rf.getLobby().getPlayerList()){
														pli = pli+Bukkit.getPlayer(p).getName()+" ";
													}
													sender.sendMessage("    游玩玩家："+pli);
													break;
												case PREPARING:
													sender.sendMessage("    游戏状态： "+ChatColor.RED+"正在加载");
													break;
												case ENDED:
													sender.sendMessage("    处于已卸载状态，随时等待重新启用。");
													break;
												case UNLOADING:
													sender.sendMessage("    游戏已结束，等待所有人离开世界将卸载。");
													break;
											}
										}

									}
								}else{
									sender.sendMessage(ChatColor.BLUE+lobby.getName()+" :");
									sender.sendMessage(ChatColor.GREEN+"默认队列："+lobby.getDefaultGroup().GetDisplay());
									for(Group gro : lobby.getGroupList()){
										gro.state(sender);
									}
								}

							}

							break;
						case "trigger":
							if(args.length>2) {
								Player striker = null;
								if(args.length>3){
									striker = Bukkit.getPlayer(args[3]);
								}
								if(striker==null && sender instanceof Player){
									striker = (Player)sender;
								}
								if(striker!=null){
									lobby.callListener(args[2],striker,new Object[0]);
								}else{
									lobby.callListener(args[2],lobby.getDefaultGroup(),null,new Object[0]);
								}

							}else {
								sender.sendMessage("/fw <房间名> function <函数名> [触发者]");
							}
							break;
						default:
							Help.LobbyHelp(sender);
					}

				} else {
					if (args[1].equals("load") && CheckPerm(sender, "csg.load")) {

						for(File f : Data.lobbyDir.listFiles()){
							if(f.getName().equals(args[0])){
								Lobby l = new Lobby(f);
								l.addToList();
								sender.sendMessage("已加载游戏 "+args[0]+" !");
								return;
							}
						}
						sender.sendMessage("未找到可加载的文件夹 "+args[0]+" !");
					}
					sender.sendMessage("队列不存在！");
				}
			}
		}
	}

	/**
	 * 发送大厅列表。
	 * @param player 要发送的玩家
	 */
	public void showList(CommandSender player) {
		String Glist = "";
		Set<Lobby> Lobbylist = Lobby.getLobbyList();
		for (Lobby l : Lobbylist) {
			String Name;
			if (l.isComplete()) {
				Name = ChatColor.GREEN + l.getName() + ChatColor.AQUA;
			} else {
				Name = ChatColor.RED + l.getName() + ChatColor.AQUA;
			}
			if (Glist != "") {
				Glist = Glist + "、" + Name;
			} else {
				Glist = Name;
			}
		}
		player.sendMessage(ChatColor.AQUA + "当前游戏列表:");
		player.sendMessage(Glist);
	}
	/**
	 * 检查一个玩家的一项权限。
	 * csg.admin可以直接通过检查而无需验证是否有需要权限。
	 * @param sender 被检查的玩家
	 * @param Permission 需要检查的权限
	 * @return 是否通过检查
	 */
	public static boolean CheckPerm(CommandSender sender, String Permission) {
		if (sender.hasPermission("csg.admin") || sender.hasPermission(Permission)) {
			return true;
		} else {
			sender.sendMessage(ChatColor.RED+"您没有权限这样做！缺少权限："+Permission);
			return false;
		}
	}

	public void onEnable() {
		Data.fmain = this;
		getServer().getPluginManager().registerEvents(this, this);
		try{

			SendToData();
			loadWorldPath();
			if(Data.isPaper){
				File root = new File("./libraries");
				LoadBukkitCore(root,true);
			}else{
				File root = new File("./");
				LoadBukkitCore(root,false);
			}



			getLogger().info("插件启动成功！ [Csg-Plus " + Data.Version + " ]");

			new BukkitRunnable(){

				@Override
				public void run() {
					getLogger().info("正在准备读取队列...");
					Lobby.LoadAll(lobby);

				}


			}.runTaskLater(this, 80);
		}catch(LinkageError e){
			getLogger().info("=====[出现链接错误！]=====");
			getLogger().info("请检查是否有以下任何情况发生：");
			getLogger().info("【1】用了plugman/YUM重载插件(重启解决)");
			getLogger().info("===========================");

		}

	}

	public void onDisable() {
		Data.onDisable=true;

		getLogger().info("正在无触发器退出所有玩家...");
		Lobby.UnLoadAll();

		Data.data.Save();
		HandlerList.unregisterAll((Plugin)this);

		getLogger().info("插件关闭成功！");
	}
	/**
	 * 重载插件。
	 * @param sender 发送重载信息的对象
	 */
	public void Reload(CommandSender sender) {

		Lobby.UnLoadAll();

		HandlerList.unregisterAll((Plugin)this);
		Data.data.Save();

		SendToData();
		loadWorldPath();

		File root = new File("./libraries");
		if(root.exists()){
			LoadBukkitCore(root,true);
		}
		root = new File("./");
		LoadBukkitCore(root,false);

//		if(Data.isPaper){
//			File root = new File("./libraries");
//			LoadBukkitCore(root,true);
//		}else{
//			File root = new File("./");
//			LoadBukkitCore(root,false);
//		}

		Lobby.LoadAll(lobby);

		getServer().getPluginManager().registerEvents(this, this);

		if(sender != null){
			sender.sendMessage("插件重载成功！ [CustomGo-Plus " + Data.Version + " ]");
		}

	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length==0) {
			Help.MainHelp(sender);
			return false;
		}
		if(Data.debug){
			if(CheckPerm(sender,"csg.debug")){
				switch(label){
					case "csg":
						Fwcommands(sender, args);
						break;
				}

			}else{
				sender.sendMessage("管理员开启了测试模式！暂时无法使用指令...");
			}
		}else{
			switch(label){
				case "csg":
					Fwcommands(sender, args);
					break;
				case "seril":
					if(args.length<2){
						sender.sendMessage("参数不足！使用方式：/seril <游戏名> <世界名>");
						break;
					}
					Room.serilizeLobby(sender,args[0],Bukkit.getWorld(args[1]));
					break;
			}
		}
		return false;
	}

	protected File lobby = new File(getDataFolder(), "lobby");
	protected File itemd = new File(getDataFolder(), "itemtask");
	protected File func = new File(getDataFolder(), "function");
	public File data;
	protected File option;
	protected static FileConfiguration optionfile;
	protected static ValueData d;

	private void loadWorldPath(){
		boolean in_world = false;
		String default_worldname = "world";
		File csgt = new File("./");

		for(World w : getServer().getWorlds()){
			boolean pass = false;
			for(File f : csgt.listFiles()){
				if(f.getName().equals(w.getName())){
					pass = true;
				}
			}
			if(w.getName().contains("_nether")){
				default_worldname = w.getName().split("_nether")[0];
			}
			if(!pass){
				in_world = true;
			}
		}

		if(in_world){
			Data.ConsoleInfo("CustomGo认为你的世界应该安装在./"+default_worldname+"中！");
			Data.worldpath = ("./"+default_worldname+"/");
		}else{
			Data.ConsoleInfo("CustomGo认为你的世界应该安装在根目录中！");
			Data.worldpath = ("./");
		}
	}
	private void LoadTec(){

		this.saveResource("Csg-Plus.zip", true);
		File zip = new File(getDataFolder(), "Csg-Plus.zip");
		FileMng.unZip(zip,"plugins");
		zip.delete();

		this.saveResource("CustomGoTec.zip", true);
		zip = new File(getDataFolder(), "CustomGoTec.zip");

		File csgt = new File(Data.worldpath);
		FileMng.unZip(zip,csgt.getAbsolutePath());

		zip.delete();

		getServer().createWorld(WorldCreator.name("CustomGoTec"));
	}
	private void LoadBukkitCore(File root,boolean isPaper) {

		if(!isPaper){
			for(File f : root.listFiles()){
				if(f.getName().endsWith(".jar") && f.getTotalSpace()>4*1024*1024){
					Data.ConsoleInfo("识别到核心端 "+f.getName());
					Data.bukkit_core.add(f);
					break;
				}
			}
		}else{
			for(File f : root.listFiles()){
				if(f.isDirectory()){
					LoadBukkitCore(f,isPaper);
				}else{
					if(f.getName().endsWith(".jar")){
						Data.ConsoleInfo("识别到API "+f.getName());
						Data.bukkit_core.add(f);
						break;
					}
				}
			}
		}


	}

	private void SendToData() {

		Data.fmain = this;

		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		if (!lobby.exists()) {
			lobby.mkdir();
		}

		data = new File(getDataFolder(), "Data.yml");
		option = new File(getDataFolder(), "Option.yml");
		optionfile = load(option);
		d = new ValueData(load(data));

		Data.optionFile = option;
		Data.lobbyDir = lobby;
		Data.optionFileConf = optionfile;

		Data.data = d;
		Data.LoadOption();
	}

	@EventHandler
	private void PlayerJoinTip(PlayerJoinEvent evt){
		final Player sender = evt.getPlayer();
		if(evt.getPlayer().isOp()) {
			if(Lobby.getLobby("CustomGoTec")==null){

				new BukkitRunnable() {

					@Override
					public void run() {

						if(sender.isOp()) {
							sender.sendMessage("§a*************************************");
							sender.sendMessage("§a检测到可以进行教程模板加载/更新！");
							sender.sendMessage("§a如果你是第一次使用插件，教程会对你有很大帮助哦~！");
							sender.sendMessage("§a");
							sender.sendMessage("想要加载/更新教程，请输入 §d/csg teach");
							sender.sendMessage("§a");
							sender.sendMessage("§7(附加提示：更新教程会重载插件~)");
							sender.sendMessage("§a*************************************");
						}

					}

				}.runTaskLater(this, 100);

			}
		}
	}

	@EventHandler
	private void Playeruuid(EntityDamageByEntityEvent evt){
		if(evt.getDamager() instanceof Player && evt.getEntity() instanceof LivingEntity){
			Player pl = (Player)evt.getDamager();
			if(plist.contains(pl)){
				plist.remove(pl);
				pl.sendMessage("UUID: "+evt.getEntity().getUniqueId().toString());
			}
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> ls = new ArrayList<>();
		if(args.length==1){
			for(Lobby l : Lobby.getLobbyList()){
				if(l.getName().startsWith(args[0])){
					ls.add(l.getName());
				}
			}
		}
		return ls;
	}
}

package org.csg;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.csg.Utils.OSUtils;
import org.csg.cmd.CsgCmd;
import org.csg.group.Lobby;
import org.csg.group.task.toolkit.ListenerFactory;

import java.io.File;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Fwmain extends JavaPlugin implements Listener {

	// 实例
	private static Fwmain instance;

	public static Fwmain getInstance() {
		return instance;
	}

	// 系统名称
	@Getter
	private static String osName;

	// 配置文件
	protected static FileConfiguration optionFile;

	// Lobby 游戏列表
	private static List<Lobby> LobbyList = new ArrayList<>();

	// 获取 Lobby 目录
	protected File lobbyFolder = new File(getDataFolder(), "lobby");

	// 命令依赖
	private static CsgCmd csgCmd;

	// 插件启动
	public void onEnable() {
		// 初始化
		instance = this;

		ListenerFactory.enable();
		// 注册监听器
		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		//分析操作系统
		osName =  OSUtils.analyseOs();

		// 输出调试信息
		if(Data.debug) {
			System.out.print("-------------Properties------------");
			System.getProperties().forEach((k,v)->System.out.printf("%s: %s%n",k,v));
			System.out.print("-----------------------------------");
		}


		try{
			// 检查插件文件夹是否存在
			OSUtils.checkFolder(lobbyFolder);
			// 读取Option.yml文件
			optionFile = OSUtils.loadFileConfiguration(new File(getDataFolder(), "Option.yml"));

			loadWorldPath();
			if(Data.isPaper){
				File root = new File("./libraries");
				LoadBukkitCore(root,true);
			}else{
				File root = new File("./");
				LoadBukkitCore(root,false);
			}

			csgCmd = new CsgCmd(Bukkit.getPluginCommand("csg"));

			// 加载所有Lobby游戏列表
			OSUtils.loadAllLobby(lobbyFolder);

			getLogger().info("插件启动成功！ [Csg-Plus " + Data.Version + " ]");

		}catch(LinkageError e){
			getLogger().info("=====[出现链接错误！]=====");
			getLogger().info("请检查是否有以下任何情况发生：");
			getLogger().info("【1】用了plugman/YUM重载插件(重启解决)");
			getLogger().info("===========================");

		}

	}

	public static List<String> getOptionDepends() {
		return Fwmain.optionDepends;
	}


	public static void setOptionDepends(List<String> optionDepends) {
		Fwmain.optionDepends = optionDepends;
	}


	public FileConfiguration loadFileConfiguration(File file) {
		if (!file.exists()) {
			saveResource(file.getName(), true);
		}
		return YamlConfiguration.loadConfiguration(file);
	}

	Set<Player> plist = new HashSet<>();
	Set<Player> vexlist = new HashSet<>();

	/**
	 * 发送大厅列表。
	 * @param player 要发送的玩家
	 */
	public void showList(CommandSender player) {
		String Glist = "";
		List<Lobby> Lobbylist = Lobby.getLobbyList();
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



	public void onDisable() {
		Data.onDisable=true;
		ListenerFactory.disable();
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

		Lobby.LoadAll(lobby);

		getServer().getPluginManager().registerEvents(this, this);

		if(sender != null){
			sender.sendMessage("插件重载成功！ [CustomGo-Plus " + Data.Version + " ]");
		}

	}



	private static List<String> optionDepends;

	private void loadWorldPath(){
		boolean in_world = true;
		String default_worldname = "world";
		File csgt = new File("./");

		for(World w : getServer().getWorlds()){
			boolean pass = false;

			if(w.getName().contains("_nether")){
				default_worldname = w.getName().split("_nether")[0];

				for(File f : csgt.listFiles()){
					if(f.getName().equals(w.getName())){
						in_world = false;
					}
				}
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
	private void LoadBukkitCore(File root, boolean isPaper) {
		if(!isPaper){
			Arrays.stream(System.getProperty("java.class.path").split(";")).filter(e -> e.endsWith(".jar")).forEach(e -> {
				File file = new File(e);
				try {
					JarFile jar = new JarFile(file);
					JarEntry entry = jar.getJarEntry("version.json");

					if(jar.getJarEntry("version.json") != null || jar.getJarEntry("mohist_libraries.txt") != null){
						Data.ConsoleInfo("识别到核心端 " + file.getAbsolutePath());
						Data.bukkit_core.add(file);
					}
					if(entry != null){
						Data.ConsoleInfo("识别到核心端 " + file.getAbsolutePath());
						Data.bukkit_core.add(file);
					}
				}catch (Exception err){
					err.printStackTrace();
				}
			});

		}else{
			for(File f : root.listFiles()){
				if(f.isDirectory()){
					LoadBukkitCore(f,isPaper);
				}else{
					if(f.getName().endsWith(".jar")){
						if(Data.debug) {
							Data.ConsoleInfo("识别到API "+f.getName());
						}
						Data.bukkit_core.add(f);
					}
				}
			}
		}
	}


	private void SendToData() {

		Data.fmain = this;


	}
	@EventHandler
	private void LListen(PlayerQuitEvent evt){
		Lobby.AutoLeave(evt.getPlayer(),false);
		Data.ConsoleInfo("玩家"+evt.getPlayer()+"因离开游戏而离开队列。");
	}

	@EventHandler
	private void LListen(EntityDamageEvent evt){
		if(evt.getEntity() instanceof ArmorStand){
			for(Lobby l : Lobby.getLobbyList()){
				if(l.hd.Holograms().containsValue((ArmorStand) evt.getEntity())){
					evt.setCancelled(true);
				}
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

	@Deprecated
	public List<String> DonTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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

	public Set<Player> getVexlist() {
		return this.vexlist;
	}

	public File getLobby() {
		return this.lobby;
	}

	public File getItemd() {
		return this.itemd;
	}

	public File getFunc() {
		return this.func;
	}

	public File getData() {
		return this.data;
	}

	public File getOption() {
		return this.option;
	}

	public Set<Player> getPlist() {
		return this.plist;
	}
}

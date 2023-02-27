package org.csg;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.csg.cmd.CsgCmd;
import org.csg.group.Group;
import org.csg.group.Lobby;
import org.csg.group.task.ValueData;
import org.csg.group.task.toolkit.ListenerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Fwmain extends JavaPlugin implements Listener {

	static CsgCmd csgCmd;

	public static String getOsName() {
		return Fwmain.osName;
	}

	public static List<String> getOptionDepends() {
		return Fwmain.optionDepends;
	}

	public static void setOsName(String osName) {
		Fwmain.osName = osName;
	}

	public static void setOptionDepends(List<String> optionDepends) {
		Fwmain.optionDepends = optionDepends;
	}


	public FileConfiguration load(File file) {
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

	public void onEnable() {
		Data.fmain = this;
		ListenerFactory.enable();
		getServer().getPluginManager().registerEvents(this, this);
		//分析操作系统
		analyseOs();
		System.out.println("-------------Properties------------");
		System.getProperties().forEach((k,v)->System.out.printf("%s: %s%n",k,v));
		System.out.println("-----------------------------------");
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

			csgCmd = new CsgCmd(Bukkit.getPluginCommand("csg"));

			Lobby.LoadAll(lobby);

			getLogger().info("插件启动成功！ [Csg-Plus " + Data.Version + " ]");

		}catch(LinkageError e){
			getLogger().info("=====[出现链接错误！]=====");
			getLogger().info("请检查是否有以下任何情况发生：");
			getLogger().info("【1】用了plugman/YUM重载插件(重启解决)");
			getLogger().info("===========================");

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


	protected File lobby = new File(getDataFolder(), "lobby");
	protected File itemd = new File(getDataFolder(), "itemtask");
	protected File func = new File(getDataFolder(), "function");
	public File data;
	protected File option;
	protected static FileConfiguration optionfile;
	protected static ValueData d;

	private static String osName;

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
	public void LoadTec() throws IOException {

		this.saveResource("Csg-Plus.zip", true);
		File zip = new File(getDataFolder(), "Csg-Plus.zip");
		ZipUtils.decompress("./plugins",zip.getAbsolutePath());
		zip.delete();

		this.saveResource("CustomGoTec.zip", true);
		zip = new File(getDataFolder(), "CustomGoTec.zip");

		File csgt = new File(Data.worldpath);
		ZipUtils.decompress(csgt.getAbsolutePath(),zip.getAbsolutePath());
		zip.delete();

		getServer().createWorld(WorldCreator.name("CustomGoTec"));
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
						Data.ConsoleInfo("识别到API "+f.getName());
						Data.bukkit_core.add(f);
						break;
					}
				}
			}
		}
	}

	public static void analyseOs() {
		String os = System.getProperty("os.name");
		if(os.toLowerCase().contains("windows")) setOsName("win");
		else setOsName("linux");
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

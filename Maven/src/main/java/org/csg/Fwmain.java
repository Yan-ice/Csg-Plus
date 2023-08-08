package org.csg;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.csg.Utils.CommonUtils;
import org.csg.Utils.OSUtils;
import org.csg.cmd.CsgCmd;
import org.csg.group.Lobby;
import org.csg.group.task.toolkit.ListenerFactory;

import java.io.File;
import java.util.*;

public class Fwmain extends JavaPlugin implements Listener {

	// 实例
	private static Fwmain instance;

	public static Fwmain getInstance() {
		return instance;
	}

	// 系统名称
	@Getter
	private String osName;

	@Getter
	private String worldpath;

	// 核心JAR文件列表
	@Getter
	public List<File> bukkitCoreList = new ArrayList<>();

	// 调试模式
	@Getter
	private Boolean debug;

	// 配置文件
	protected static FileConfiguration optionFileConfiguration;

	// Lobby 游戏列表
	@Getter
	private List<Lobby> lobbyList = new ArrayList<>();

	// 获取 Lobby 目录
	protected File lobbyFolder = new File(getDataFolder(), "lobby");

	// 命令依赖
	private static CsgCmd csgCmd;

	// 插件启动
	public void onEnable() {
		// 初始化
		instance = this;

		// 注册监听器工厂
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


		Set<Player> playerList = new HashSet<>();

		try{
			// 检查插件文件夹是否存在
			OSUtils.checkFolder(lobbyFolder);
			// 读取Option.yml文件
			optionFileConfiguration = OSUtils.loadFileConfiguration(new File(getDataFolder(), "Option.yml"));
			// 读取世界路径
			worldpath = OSUtils.loadWorldPath();

			// 判断是否为Paper
			if(Bukkit.getVersion().contains("Paper")){
				File root = new File("./libraries");
				OSUtils.loadBukkitCore(root,true);
			}else{
				File root = new File("./");
				OSUtils.loadBukkitCore(root,false);
			}

			// 加载命令
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

	public void onDisable() {

		// 注销监听器工厂
		ListenerFactory.disable();

		CommonUtils.ConsoleInfoMsg("正在退出所有玩家");

		// 注销所有 Lobby游戏列表
		unloadAllLobby();

		// 注销监听器
		HandlerList.unregisterAll((Plugin)this);

		getLogger().info("插件关闭成功！");
	}
	/**
	 * 重载插件。
	 * @param sender 发送重载信息的对象
	 */
	public void Reload(CommandSender sender) {

		// 注销所有 Lobby游戏列表
		unloadAllLobby();

		// 注销监听器
		HandlerList.unregisterAll((Plugin)this);

		// 读取Option.yml文件
		optionFileConfiguration = OSUtils.loadFileConfiguration(new File(getDataFolder(), "Option.yml"));

		// 检查插件文件夹是否存在
		OSUtils.checkFolder(lobbyFolder);

		// 读取世界路径
		worldpath = OSUtils.loadWorldPath();

		// 判断是否为Paper
		if(Bukkit.getVersion().contains("Paper")){
			File root = new File("./libraries");
			OSUtils.loadBukkitCore(root,true);
		}else{
			File root = new File("./");
			OSUtils.loadBukkitCore(root,false);
		}

		// 加载所有Lobby游戏列表
		OSUtils.loadAllLobby(lobbyFolder);

		// 注册所有监听器
		getServer().getPluginManager().registerEvents(this, this);

		if(sender != null){
			sender.sendMessage("插件重载成功！");
		}

	}

	@EventHandler
	private void LeaveServerListen(PlayerQuitEvent evt){
		Lobby.AutoLeave(evt.getPlayer(),false);
		CommonUtils.ConsoleInfoMsg("玩家"+evt.getPlayer()+"因离开游戏而离开队列。");
	}

	/**
	 * 注销所有Lobby游戏
	 */
	public void unloadAllLobby() {
		for (Lobby lobby : lobbyList) {
			lobby.unLoad();
		}
	}
}

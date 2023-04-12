package org.csg;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
/**
 * 用于编辑插件指令帮助的类。
 *
 */
public class Help {
	private static String title(String str) {
		return ChatColor.RED + "-------[" + ChatColor.YELLOW + str + ChatColor.RED + "]-------";
	}
	/**
	 * 发送插件总指令帮助。
	 * @param sender 要发送的对象。
	 */
	public static void MainHelp(CommandSender sender) {
		sender.sendMessage(title("Csg-Plus "+Data.Version+" 帮助"));
		sender.sendMessage("/csg reload              重载插件");
		sender.sendMessage("/csg list                查看所有游戏");
		sender.sendMessage("/csg leave               离开一个大厅");
		sender.sendMessage("/csg leave <玩家>         令指定玩家离开一个游戏");
		sender.sendMessage("/csg <游戏名> <参数>   游戏相关指令[详细帮助见下一栏]");
	}
	/**
	 * 发送插件lobby参数帮助。
	 * @param sender 要发送的对象。
	 */
	public static void LobbyHelp(CommandSender sender) {
		sender.sendMessage(title("大厅指令参数帮助"));
		sender.sendMessage("join             加入一个已有大厅");
		sender.sendMessage("join <玩家>       令指定玩家加入一个已有大厅");
		sender.sendMessage("statu            查看大厅及其队列的状态");
		sender.sendMessage("load             读取/重载一个大厅的配置");
		sender.sendMessage("unload           卸载一个大厅(不会删除文件)");
	}
}

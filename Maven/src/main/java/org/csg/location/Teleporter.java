package org.csg.location;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Teleporter {
	Player player;

	/**
	 * 为一个玩家创建传送器。
	 * 传送器可以便捷地传送该玩家。
	 * @param player 传送器对应的玩家
	 */
	public Teleporter(Player player) {
		this.player = player;
	}

	/**
	 * 自动检查坐标的存在性。如果存在将传送玩家。
	 * 
	 * @param Loc
	 *            将要传送的地点
	 * @param SafeTp
	 *            如果地点不安全，是否传送到附近安全区
	 */
	public void Teleport(Location Loc, boolean SafeTp) {
		player.teleport(Loc);
	}

	/**
	 * 自动检查坐标的存在性,并传送到随机一个坐标点。
	 * @param Loc 需要检查并随机选择传送的坐标列表。
	 */
	public void TeleportRandom(List<Location> Loc) {
		if(Loc != null && Loc.size()>0){
			Location loc = Loc.get(Data.Random(0,Loc.size()));
			if(player!=null){
				player.teleport(loc);
			}
			
		}
		
		return;
	}

	public static String locToString(Location loc){
		return String.format("%d %d %d %s %f %f",
				loc.getBlockX(),loc.getBlockY(),loc.getBlockZ(),loc.getWorld().getName()
				,loc.getYaw(),loc.getPitch());
	}
	public static Location stringToLoc(String l){
		if(l.equals("none")){
			return null;
		}
		try{
			String[] s = l.split(" ");
			double x,y,z;
			float yaw=0,pitch=0;
			World world;
			if(s.length==4){
				x = Double.parseDouble(s[0]);
				y = Double.parseDouble(s[1]);
				z = Double.parseDouble(s[2]);
				world = Bukkit.getWorld(s[3]);
			}else
			if(s.length==6){
				x = Double.parseDouble(s[0]);
				y = Double.parseDouble(s[1]);
				z = Double.parseDouble(s[2]);
				yaw = Float.parseFloat(s[4]);
				pitch = Float.parseFloat(s[5]);
				world = Bukkit.getWorld(s[3]);
			}else{
				CommonUtils.ConsoleInfoMsg(l+"似乎不是有效的坐标（参数个数错误）。");
				return null;
			}
			if(world==null){
				CommonUtils.ConsoleInfoMsg(l+"坐标中的世界不存在！");
			}
			return new Location(world,x,y,z,yaw,pitch);
		}catch(Exception e){
			CommonUtils.ConsoleInfoMsg(l+"似乎不是有效的坐标（数据类型错误）。");
			return null;
		}
	}

	public static Location stringToLoc(String l,World defaultWorld){
		try{
			String[] s = l.split(" ");
			double x,y,z;
			float yaw=0,pitch=0;
			World world;
			if(s.length==4){
				x = Double.parseDouble(s[0]);
				y = Double.parseDouble(s[1]);
				z = Double.parseDouble(s[2]);
				world = Bukkit.getWorld(s[3]);
			}else
			if(s.length==6){
				x = Double.parseDouble(s[0]);
				y = Double.parseDouble(s[1]);
				z = Double.parseDouble(s[2]);
				yaw = Float.parseFloat(s[4]);
				pitch = Float.parseFloat(s[5]);
				world = Bukkit.getWorld(s[3]);
			}else{
				CommonUtils.ConsoleInfoMsg(l+"似乎不是有效的坐标（参数个数错误）。");
				return null;
			}
			if(world==null){
				world = defaultWorld;
			}
			return new Location(world,x,y,z,yaw,pitch);
		}catch(Exception e){
			CommonUtils.ConsoleInfoMsg(l+"似乎不是有效的坐标（数据类型错误）。");
			return null;
		}


	}
}

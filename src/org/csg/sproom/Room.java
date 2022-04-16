package org.csg.sproom;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.csg.Data;
import org.csg.FileMng;
import org.csg.group.Lobby;
import org.csg.location.Teleporter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Room {
	public static Set<Room> rooms = new HashSet<Room>();

	public void delete(){
		config.set("RoomList."+name,null);
	}

	public void loadConfig(){
		if(config.contains("maxReflect")){
			maxreflect = config.getInt("maxReflect");
		}

		if(config.contains("allowBuilding")){
			allowBuilding = config.getBoolean("allowBuilding");
		}
		if(config.contains("leaveLoc")){
			leaveLoc = (Location)config.get("leaveLoc");
		}
	}

	public void saveConfig(){
		config.set("maxReflect",maxreflect);
		config.set("allowBuilding",allowBuilding);
		config.set("leaveLoc",leaveLoc);

		try {
			config.save(config_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Room searchRoom(String Name){
		for(Room r : rooms){
			if(r.name.equals(Name)){
				return r;
			}
		}
		return null;
	}

	//状态码： 0成功 1找不到队列 2找不到世界
	public static int serilizeLobby(CommandSender sender, String lobbyname, World world){
		if(world==null){
			sender.sendMessage(ChatColor.RED+"错误：无法找到您指定的世界。请确认其是否被加载。");
			return 2;
		}
		sender.sendMessage(ChatColor.YELLOW+"正在尝试生成游戏 "+lobbyname+" 的独立副本...");

		Lobby lobby = Lobby.getLobby(lobbyname);
		if(lobby==null){
			sender.sendMessage(ChatColor.RED+"错误: 未找到指定游戏。");
			return 1;
		}

		for(Room r : rooms){
			if(r.name.equals(lobbyname)){
				sender.sendMessage("检测到独立副本已存在。正在卸载旧独立副本...");
				r.unLoad();
				break;
			}
		}

		sender.sendMessage("已读取到游戏世界 "+ChatColor.YELLOW+world.getName()+ChatColor.WHITE+" 。");
		world.save();
		File targetWorld = new File(lobby.getTempFolder(), "sproom_world");
		if(targetWorld.exists()){
			FileMng.deleteDir(targetWorld);
		}
		targetWorld.mkdir();
		sender.sendMessage("正在拷贝游戏 "+lobbyname+" 的世界存档...");
		File sourceWorld = new File(Data.worldpath+world.getName());

		FileMng.copyDir(sourceWorld,targetWorld);
		//以上在复制世界文件
		sender.sendMessage("正在将配置中的 "+world.getName()+" 替换为 $world$ 占位符...");

		try{
			replaceWorldStr(lobby.getFolder(),world.getName(),"$world$");

		}catch(IOException e){

		}
		//以上在替换世界名字
		new Room(lobby,5);
		sender.sendMessage(ChatColor.AQUA+"生成独立副本"+lobbyname+"成功!");
		sender.sendMessage(ChatColor.AQUA+"输入 "+ChatColor.YELLOW+"/fwroom "+lobbyname+" join "+ChatColor.AQUA+"即可开始体验游戏了！");
		sender.sendMessage(ChatColor.AQUA+"啊对了，你可以随时继续使用 /seril "+lobbyname+" 更新这个独立副本噢~");

		return 0;
	}

	public static void replaceWorldStr(File dir,String old_s,String new_s) throws IOException {
		for(File f : dir.listFiles()){
			f.setWritable(true);
			if(f.isFile()){
				FileMng.autoReplaceStr(f.getAbsolutePath(),old_s,new_s);
			}
			if(f.isDirectory() && !f.getName().equals("temp")){
				replaceWorldStr(f,old_s,new_s);
			}
		}
	}
//	public World loadWorld(){
//		File wo =  new File(lobby_model.getTempFolder(), name);
//
//		File dir = new File(Data.worldpath+"FW_"+name);
//
//		if(dir.exists()){
//			dir.delete();
//		}
//		dir.mkdir();
//		FileMng.copyDir(wo,dir);
//		WorldCreator w = WorldCreator.name("FW_" +name);
//		w.type(WorldType.FLAT);
//		return FwSpMain.main.getServer().createWorld(w);
//	}

	public Room(Lobby model, int max){
		lobby_model = model;
		this.name = model.getName();
		rooms.add(this);
		maxreflect = max;
		joining = createReflect();
		config_file = new File(lobby_model.getTempFolder(),"sproom_config.yml");
		try {
			if(!config_file.exists()){
				config_file.createNewFile();
			}
			config = Data.fmain.load(config_file);
			loadConfig();

			this.maxreflect = max;
			saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public Room(Lobby model, File config_f){
		lobby_model = model;
		this.name = model.getName();
		rooms.add(this);
		try {
			config_file = config_f;
			if(!config_file.exists()){
				config_file.createNewFile();
			}
			config = Data.fmain.load(config_file);

			loadConfig();

		} catch (IOException e) {
			e.printStackTrace();
		}


		joining = createReflect();

	}

	Lobby lobby_model;
	String name;
	public Set<Reflect> allreflects = new HashSet<Reflect>();
	int id = 0;
	int maxreflect = 5;
	Location leaveLoc = null;
	boolean allowBuilding = false;
	Reflect joining = null;
	Reflect waiting = null;

	File config_file;
	FileConfiguration config;
	
	public void JoinRoom(Player p){
		if(joining==null){
			joining = createReflect();
			if(joining==null){
				p.sendMessage(ChatColor.RED+"镜像状态：");
				return;
			}
		}
		
		switch(joining.getStatu()){
		case WAITING:
			if(!joining.Join(p)){
				if(waiting!=null){
					joining = waiting;
				}else{
					joining = createReflect();
				}
				waiting = createReflect();
				return;
			}
			if(waiting==null){
				waiting = createReflect();
			}
			break;
		case STARTED:
		case ENDED:
		case UNLOADING:
			if(waiting!=null){
				joining = waiting;
				waiting=null;
				joining.Join(p);
			}else{
				p.sendMessage(ChatColor.RED+"队列已满，暂时无法加入...");
			}
			waiting = this.createReflect();
			break;
		case PREPARING:
			p.sendMessage(ChatColor.RED+"游戏正在准备中，稍等片刻~");
			return;
		case WRONG:
			p.sendMessage(ChatColor.RED+"游戏状态错误，无法查看！");
			break;
		default:
			break;
		}

	}
	
	public void autoCreateReflect() {
		for(Reflect rf : allreflects){
			if(rf.getStatu()==ReflectStatu.WAITING){
				return;
			}
		}
		joining = createReflect();
	}
	
	public Reflect createReflect(){
		for(Reflect rf : allreflects){
			if(rf.getStatu()==ReflectStatu.ENDED){
				rf.renew();
				return rf;
			}
		}
		if(allreflects.size()<maxreflect){
			id++;
			Reflect r = new Reflect(this,id);
			allreflects.add(r);
			return r;
		}else{
			return null;
		}
		
	}

	public String getName(){
		return name;
	}
	public void unLoad(){
		Set<Reflect> allrc = new HashSet<Reflect>();
		allrc.addAll(allreflects);
		for(Reflect r : allrc){
			r.emergencyUnload();
		}
		rooms.remove(this);
	}
	public int getMaxReflect(){
		return maxreflect;
	}
	public void setMaxReflect(int m){
		maxreflect = m;
	}
	public void setAllowBuilding(boolean allow){
		allowBuilding = allow;
	}
	
	public static void autoLeave(Player p){
		for(Room r: rooms){
			for(Reflect rf : r.allreflects){
				if(rf.hasPlayer(p)){
					rf.getLobby().Leave(p);
				}
			}
		}
	}
}

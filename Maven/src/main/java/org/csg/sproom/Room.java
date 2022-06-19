package org.csg.sproom;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimeProperty;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.csg.Data;
import org.csg.FileMng;
import org.csg.group.Lobby;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Room {

	public static SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

	public static Set<Room> rooms = new HashSet<Room>();

	public Settings roomSetting;

	
	public static Room searchRoom(String Name){
		for(Room r : rooms){
			if(r.name.equals(Name)){
				return r;
			}
		}
		return null;
	}

	//状态码： 0成功 1找不到队列 2找不到世界
	public static int serilizeLobby(CommandSender sender, String lobbyname, String worldName){
		if(plugin==null){
			sender.sendMessage("未找到前置插件SlimeWorldManager，无法使用独立副本功能！");
			return 2;
		}
		sender.sendMessage("正在获取指定游戏房间...");
		Lobby lobby = Lobby.getLobby(lobbyname);
		if(lobby==null){
			sender.sendMessage(ChatColor.RED+"错误: 未找到指定游戏。");
			return 1;
		}

		sender.sendMessage("正在获取指定游戏世界...");
		File sourceWorld = new File(Data.worldpath+worldName);
		if(!sourceWorld.exists()){
			sender.sendMessage(ChatColor.RED+"错误：无法找到您指定的世界"+Data.worldpath+"("+sourceWorld.getAbsolutePath()+"。");
			if(Bukkit.getWorld(worldName)!=null){
				sender.sendMessage(ChatColor.YELLOW+"插件检测到您似乎在尝试指定一个SlimeWorldManager加载的世界。");
				sender.sendMessage(ChatColor.YELLOW+"由于特殊格式要求，您必须指定非SlimeWorldManager格式的世界，但你可以用特殊方式实现它");
			}
			return 2;

		}
		World world = Bukkit.getWorld(worldName);

		if(Data.defaultLocation.getWorld()==world){
			sender.sendMessage(ChatColor.RED+"错误：无法使用默认世界作为独立副本世界！");
			return 2;
		}
		sender.sendMessage("正在尝试保存游戏世界...");
		if(world!=null){
			for(Player p : world.getPlayers()){
				p.sendMessage("由于该世界正在被服务器导入，您被移出了该世界。");
				p.teleport(Data.defaultLocation);
			}
			Bukkit.unloadWorld(world,true);
		}

		for(Room r : rooms){
			if(r.name.equals(lobbyname)){
				sender.sendMessage("检测到独立副本已存在。正在卸载旧独立副本...");
				r.unLoad();
				break;
			}
		}
		sender.sendMessage("正在将配置中的 "+worldName+" 替换为 $world$ 占位符...");
		try{
			replaceWorldStr(lobby.getFolder(),worldName,"$world$");

		}catch(IOException e){
			e.printStackTrace();
		}

		sender.sendMessage(ChatColor.YELLOW+"正在尝试生成游戏 "+lobbyname+" 的独立副本...");

		new Room(lobby,5, sourceWorld);
		if(sourceWorld.exists() && world!=null){
			Bukkit.createWorld(WorldCreator.name(worldName));
		}

		sender.sendMessage(ChatColor.AQUA+"生成独立副本"+lobbyname+"成功!");
		sender.sendMessage(ChatColor.AQUA+"输入 "+ChatColor.YELLOW+ "/csg " +lobbyname+" join "+ChatColor.AQUA+"即可开始体验游戏了！");
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

	private CsgSlimeLoader loader = null;
	public CsgSlimeLoader getLoader(){
		if(loader==null){
			File f = new File(lobby_model.getTempFolder(),"world.slime");
			loader = new CsgSlimeLoader(f);

		}
		return loader;
	}
	private SlimePropertyMap getProperty(){

		SlimePropertyMap properties = new SlimePropertyMap();
		properties.setString(SlimeProperties.DIFFICULTY, roomSetting.difficulty);
		properties.setBoolean(SlimeProperties.PVP,roomSetting.PvP);
		return properties;
	}
	private SlimeWorld world_model;
	public SlimeWorld loadWorld(){
		if(world_model==null){
			try {
				world_model = plugin.loadWorld(getLoader(),lobby_model.getName(),true,getProperty());
			} catch (UnknownWorldException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CorruptedWorldException e) {
				e.printStackTrace();
			} catch (NewerFormatException e) {
				e.printStackTrace();
			} catch (WorldInUseException e) {
				e.printStackTrace();
			}
		}
		return world_model;
	}
	public Room(Lobby model, int max, File targetWorld){

		lobby_model = model;
		this.name = model.getName();
		try {
			plugin.importWorld(targetWorld,name,getLoader());
		} catch (WorldAlreadyExistsException e) {
			e.printStackTrace();
		} catch (InvalidWorldException e) {
			e.printStackTrace();
		} catch (WorldLoadedException e) {
			e.printStackTrace();
		} catch (WorldTooBigException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		rooms.add(this);
		joining = createReflect();
		config_file = new File(lobby_model.getTempFolder(),"sproom_config.yml");
		try {
			if(!config_file.exists()){
				config_file.createNewFile();
				roomSetting = new Settings();
				roomSetting.saveConfig(config_file);
			}else{
				roomSetting = new Settings(config_file);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Room(Lobby model, File config_f){
		if(plugin==null){
			Data.ConsoleInfo("未找到前置插件SlimeWorldManager，无法使用独立副本功能！");
			return;
		}
		lobby_model = model;
		this.name = model.getName();
		rooms.add(this);
		try {
			config_file = config_f;
			if(!config_file.exists()){
				config_file.createNewFile();
			}
			roomSetting = new Settings(config_file);

		} catch (IOException e) {
			e.printStackTrace();
		}
		joining = createReflect();
	}

	Lobby lobby_model;
	String name;
	public Set<Reflect> allreflects = new HashSet<Reflect>();
	int id = 0;

	Reflect joining = null;
	Reflect waiting = null;

	File config_file;

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
		if(allreflects.size()<roomSetting.maxReflect){
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
		allreflects.clear();
		rooms.remove(this);
	}
	public int getMaxReflect(){
		return roomSetting.maxReflect;
	}
	public void setAllowBuilding(boolean allow){
		roomSetting.allowBuilding = allow;
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

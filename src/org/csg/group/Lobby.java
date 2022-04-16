package org.csg.group;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import customgo.event.ListenerCalledEvent;
import customgo.event.PlayerJoinLobbyEvent;
import customgo.event.PlayerLeaveLobbyEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.csg.group.task.Macro;
import org.csg.group.task.csgtask.FunctionTask;
import org.csg.group.task.csgtask.ListenerTask;
import org.bukkit.entity.*;
import org.bukkit.*;

import org.csg.Data;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.csg.group.task.toolkit.*;
import org.csg.sproom.Room;
import org.csg.update.CycleUpdate;
import org.csg.update.MainCycle;
import org.csg.update.SecondCycle;

public class Lobby implements customgo.Lobby, CycleUpdate {
	private static Set<Lobby> LobbyList = new HashSet<>();

	public static Set<Lobby> getLobbyList(){
		return LobbyList;
	}

	Map<UUID,Location> spawnpoint = new HashMap<>();
	public void setSpawn(Player p,Location loc){
		if(loc==null||p==null){
			return;
		}
		if(!hasPlayer(p)){
			return;
		}
		if(spawnpoint.containsKey(p.getUniqueId())){
			spawnpoint.replace(p.getUniqueId(),loc);
		}else{
			spawnpoint.put(p.getUniqueId(),loc);
		}
	}

	public Location getSpawn(Player p){
		if(p==null || !hasPlayer(p)){
			return null;
		}
		return spawnpoint.get(p.getUniqueId());
	}

	public boolean requireMacro(String s,String annotation){
		int state = macros.HasMacro(s);
		if(state!=2){
			Data.ConsoleError("该大厅并未满足脚本需求的宏"+s+"！");
			Data.ConsoleError("请在macro.yml文件设置相关内容，并重载该大厅。在此之前，相关脚本将无法使用！");
		}
		if(state==0){
			try{
				FileOutputStream fos = null;
				try {
					//true不覆盖已有内容
					fos = new FileOutputStream(default_macro_file.getPath(), true);
					//写入
					fos.write(String.format("\n%s: null",s).getBytes(StandardCharsets.UTF_8));
					if(annotation!=null){
						fos.write(String.format("\n%s\n",annotation).getBytes(StandardCharsets.UTF_8));
					}

				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					if(fos != null){
						try {
							fos.flush();
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}catch(Exception e){

			}
		}
		return state==2;
	}
	public Macro macros = new Macro();
	File default_macro_file;

	private Set<FunctionTask> functions = new HashSet<>();
	private Set<ListenerTask> listener = new HashSet<>();
	private Class<?> javaFunction;
	private Object instance;

	private Set<Group> grouplist = new HashSet<>();
	private Trigger trigger = new Trigger(this);
	Group Default;

	File Folder;
	File tempFolder;
	String Name;
	
	private ValueBoard Board = new ValueBoard();
	private ValueBoard dataBoard = new ValueBoard();
	private PlayerValueBoard PlayerBoard = new PlayerValueBoard();

	public File getTempFolder(){
		return tempFolder;
	}
	public File getFolder(){
		return Folder;
	}

	public ValueBoard DataBoard(){
		return dataBoard;
	}

	public ValueBoard ValueBoard(){
		return Board;
	}

	public PlayerValueBoard PlayerValueBoard(){
		return PlayerBoard;
	}

	public List<UUID> getPlayerList(){
		List<UUID> pl = new ArrayList<>();
		for(Group g : getGroupList()){
			for(UUID p : g.getPlayerList()){
				pl.add(p);
			}
			
		}
		return pl;
	}

	public boolean hasPlayer(Player p){
		for(Group g : getGroupList()){
			if(g.hasPlayer(p)){
				return true;
			}
		}
		return false;
	}

	public Object callFunction(String name, TaskExecuter executer, Player p, Object[] para){
		for(FunctionTask task : functions){
			if(task.getName().equals(name)){
				executer.group.runTask(task,p!=null ? p.getUniqueId() : null,para);
			}
		}
		if(javaFunction==null){
			return null;
		}
		try{
			for(Method meth : javaFunction.getMethods()){
				if(meth.getName().equals("_setMember")){
					meth.invoke(instance,this,executer.group,executer.striker!=null ? Bukkit.getPlayer(executer.striker) : null,p);
				}
			}
			for(Method meth : javaFunction.getMethods()){
				if(meth.getName().equals(name)){
					return meth.invoke(instance,para);
				}
			}
		}catch(IllegalAccessException | InvocationTargetException e){
			e.printStackTrace();
			Data.ConsoleInfo("尝试调取java函数"+name+"失败！");
		}
		return null;
	}
	public void callListener(String name, Group g, Player p, Object[] para){
		for(Group gro : grouplist){
			if(gro==g){
				for(ListenerTask task : listener){
					if(task.getName().equals(name)){
						if(!task.checkTarget(gro) || !gro.hasScriptField(task.getField())){
							continue;
						}
						ListenerCalledEvent call = new ListenerCalledEvent(name,this,p,para);
						Data.fmain.getServer().getPluginManager().callEvent(call);
						if(p!=null){
							gro.runTask(task,p.getUniqueId(),para);
						}else{
							gro.runTask(task,null,para);
						}

					}
				}
			}
		}
	}
	public void callListener(String name, Player p, Object[] para){
		for(Group gro : grouplist){
			if(gro.hasPlayer(p)){
				for(ListenerTask task : listener){
					if(task.getName().equals(name)){
						if(!task.checkTarget(gro) || !gro.hasScriptField(task.getField())){
							continue;
						}
						ListenerCalledEvent call = new ListenerCalledEvent(name,this,p,para);
						Data.fmain.getServer().getPluginManager().callEvent(call);

						gro.runTask(task,p.getUniqueId(),para);
					}
				}

			}
		}
	}
	public void callListener(String name, Object[] para){
		Group gro = getDefaultGroup();
		for(ListenerTask task : listener){
			if(task.getName().equals(name)){
				if(!task.checkTarget(gro) || !gro.hasScriptField(task.getField())){
					continue;
				}
				ListenerCalledEvent call = new ListenerCalledEvent(name,this,null,para);
				Data.fmain.getServer().getPluginManager().callEvent(call);
				gro.runTask(task,null,para);
			}
		}
	}

	public void rename(String name){
		this.Name = name;
	}

	Map<String,FileConfiguration> fc = new HashMap<>();

	public FileConfiguration loadWorkFile(String name){
		if(fc.containsKey(name)){
			return (FileConfiguration)fc.get(name);
		}else{
			File f = new File(tempFolder,name+".yml");
			if(!f.exists()){
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fc.put(name,Data.fmain.load(f));
			return fc.get(name);
		}
	}
	public void saveWorkFile(String name){
		if(fc.containsKey(name)){
			File f = new File(tempFolder,name+".yml");
			try {
				if(!f.exists()){
					f.createNewFile();
				}
				fc.get(name).save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void deleteWorkFile(String name){
		File f = new File(tempFolder,name+".yml");
		if(f.exists()){
			f.delete();
		}
		fc.remove(name);
	}

	private boolean sproom_control = false;
	public Lobby(File folder){
		Folder = folder;
		tempFolder = new File(folder,"temp");
		if(!tempFolder.exists()){
			tempFolder.mkdir();
		}
		this.Name = folder.getName();
		load();
	}
	private Lobby(File folder,int p){
		sproom_control = true;

		Folder = folder;
		tempFolder = new File(folder,"temp");
		if(!tempFolder.exists()){
			tempFolder.mkdir();
		}
		this.Name = folder.getName();
		load();
	}
	public Lobby clone(){
		return new Lobby(Folder,1);
	}
	
	
	public void addToList(){
		LobbyList.add(this);
	}

	private void loadFileRecurse(File folder, JavaTaskCompiler jcompiler) throws IOException {
		for(File f : folder.listFiles()){
			if(f.isDirectory()){
				if(f.getName().equals("temp")){
					continue;
				}
				loadFileRecurse(f,jcompiler);
				continue;
			}

			if(f.getName().endsWith("yml")){
				if(f.getName().equals("macro.yml")){
					continue;
				}
				String groName = f.getName().split("\\.")[0];
				if(getGroup(groName)==null){
					Data.ConsoleInfo("正在加载队列 "+groName);
					Group gro = new Group(this, Data.fmain.load(f),groName);
					grouplist.add(gro);
					if(groName.equals("Main")){
						setDefaultGroup(gro);
					}
				}else {
					Data.ConsoleError(String.format("多个队列出现相同名字%s！", this.Name, groName));
				}
			}
			if(f.getName().endsWith("csgtask")){

				TaskCompiler tcompiler = new TaskCompiler();
				tcompiler.compile(this,f);
				tcompiler.getFunctions(functions);
				tcompiler.getListeners(listener);
				tcompiler.destroy();
			}
			if(f.getName().endsWith("javatask")){
				jcompiler.read(f);
			}
			if(f.getName().endsWith("class")){
				jcompiler.addDepend(f);
			}
		}
	}
	private void loadMacro(File folder,boolean direct){
		for(File f : folder.listFiles()) {
			if(f.isDirectory()){
				loadMacro(f,false);
				continue;
			}
			if(f.getName().equals("macro.yml")){
				if(direct){
					default_macro_file = f;
				}else{
					Data.ConsoleInfo("正在加载宏列表 "+f.getPath());
					macros.LoadMacro(Data.fmain.load(f));
				}
			}
		}
	}
	private void loadFile(File folder) throws IOException {
		JavaTaskCompiler jcompiler = new JavaTaskCompiler(this);

		loadMacro(folder,true);

		if(default_macro_file!=null){
			Data.ConsoleInfo("正在加载用户宏列表 macro.yml");
			macros.LoadMacro(Data.fmain.load(default_macro_file));
		}else{
			default_macro_file = new File(folder.getPath()+"/macro.yml");
			default_macro_file.mkdir();
		}

		loadFileRecurse(folder,jcompiler);

		javaFunction = jcompiler.compile();
		if(javaFunction==null){
			return;
		}
		try {
			instance = javaFunction.getConstructor().newInstance();
			javaFunction.getMethod("_setPlugin",JavaPlugin.class).invoke(instance,Data.fmain);
		} catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 加载大厅
	 */
	public void load(){
		Clear();
		for(Group g : grouplist){
			g.UnLoad();
		}
		fc.clear();
		grouplist.clear();
		functions.clear();
		listener.clear();
		Data.ConsoleInfo("=====[正在加载大厅"+Name+"]=====");
		try{
			loadFile(Folder);
		}catch(IOException e){
			e.printStackTrace();
			Data.ConsoleInfo("文件读取失败！");
		}
		if(this.getDefaultGroup()==null){
			Data.ConsoleInfo("该游戏缺少初始队列，已自动生成默认初始队列。");
			Group gro = new Group(this, "Main","主队列");
			grouplist.add(gro);
			setDefaultGroup(gro);
		}

		Data.fmain.getServer().getPluginManager().registerEvents(trigger,Data.fmain);
		MainCycle.registerCall(trigger);
		if(isComplete()){
			callListener("onLobbyLoaded",getDefaultGroup(),null,new Object[0]);

			for(File f : tempFolder.listFiles()){
				if(!sproom_control && f.getName().equals("sproom_config.yml")){
					Data.ConsoleInfo("检测到大厅"+Name+"具有独立副本配置，已生成独立副本！");
					new Room(this,f);
					break;
				}
			}

			Data.ConsoleInfo("=====[大厅"+Name+"加载成功]=====");
			SecondCycle.registerCall(this);

		}else{
			Data.ConsoleInfo("=====[大厅"+Name+"加载失败]=====");
		}
	}

	public void unLoad() {
		Clear();
		for(Group g : grouplist){
			if(getDefaultGroup()==g){
				continue;
			}
			g.UnLoad();
		}
		if(getDefaultGroup()!=null){
			getDefaultGroup().UnLoad();
		}

		if(LobbyList.contains(this)){
			LobbyList.remove(this);
		}

		callListener("onLobbyUnloaded",null,null,new Object[0]);
		HandlerList.unregisterAll(trigger);
		MainCycle.unRegisterCall(trigger);
		SecondCycle.unRegisterCall(this);
	}

	public boolean isComplete(){
		if(Default==null){
			return false;
		}
		return true;
	}

	public String getName(){
		return Name;
	}
	

	protected void setDefaultGroup(Group gro){
		if(setted){
			Data.ConsoleInfo("警告：该游戏存在多个初始队列(Main.yml)！");
			gro.UnLoad();
			return;
		}
		setted = true;
		this.Default = gro;
		if(!grouplist.contains(gro)){
			grouplist.add(gro);
		}
		
	}
	
	private boolean setted = false;


	public Group getDefaultGroup(){
		return Default;
	}

	public Set<Group> getGroupList(){
		return grouplist;
	}
	

	public int getPlayerAmount(){
		int a = 0;
		for(Group g : grouplist){
			a = a+g.getPlayerAmount();
		}
		return a;
	}

	public Group getGroup(String Name){
		for(Group l : grouplist){
			if(l.getName().equals(Name)){
				return l;
			}
		}
		return null;
	}

	public void Clear(){

		for(UUID p : this.getPlayerList()){
			this.Leave(Bukkit.getPlayer(p));
		}
		for(Group l : grouplist){
			l.hd.ClearHologram();
		}
	}

	public static Lobby getLobby(String Name){
		for(Lobby l : LobbyList){
			if(l.Name.equalsIgnoreCase(Name)){
				return l;
			}
		}
		return null;
	}
	

	public void Join(Player player){
		if(Default==null){
			player.sendMessage(ChatColor.RED+"该游戏缺少主队列！");
			return;
		}
		PlayerJoinLobbyEvent e = new PlayerJoinLobbyEvent(player,this);
		Data.fmain.getServer().getPluginManager().callEvent(e);
		if(!e.isCancelled()){
			Default.JoinGroup(player,"_outside_");
		}

	}

	public void Leave(Player player){
		if(player==null || !hasPlayer(player)){
			return;
		}

		this.ChangeGroup(player,"Main");
		System.out.println("leave call start");
		PlayerLeaveLobbyEvent e = new PlayerLeaveLobbyEvent(player,this);
		Data.fmain.getServer().getPluginManager().callEvent(e);
		System.out.println("leave call end");
		getDefaultGroup().LeaveGroup(player.getUniqueId(),"_outside_");
	}

	public void ChangeGroup(Player player,String groupname){
		if(player==null || !hasPlayer(player)){
			return;
		}
		Group from = Group.SearchPlayerInGroup(player);
		if(from==null){
			return;
		}

		if(this.getGroup(groupname)!=null){
			Group to = this.getGroup(groupname);
			if(from==to){
				return;
			}
			from.LeaveGroup(player.getUniqueId(),to.getName());
			to.JoinGroup(player,from.getName());
		}else{
			player.sendMessage(ChatColor.RED+"目标队列不存在！");
		}
		
	}
	

	public static void AutoLeave(Player player,boolean noTel){
		for(Lobby l : LobbyList){
			if(l.getPlayerList().contains(player.getUniqueId())){
				l.Leave(player);
				return;
			}
		}
		player.sendMessage("您不在任何游戏中！");
	}

	public static void LoadAll(File lobby) {

		for(File file : lobby.listFiles()){
			if(file.isDirectory()){
				new Lobby(file).addToList();
			}
		}

	}
	
	public static void UnLoadAll() {
		for(Lobby l : LobbyList){
			for(Group g : l.grouplist){
				g.UnLoad();
			}
			//l.ListenerRespond(new EventOnLobbyUnloaded(l,false));
		}
		LobbyList.clear();
	}

	@Override
	public void onUpdate() {
		callListener("onEverySecond",getDefaultGroup(),null,new Object[0]);
	}
//
//	static List<LobbyListener> llist = new ArrayList<>();
//
//	public static void RegisterListener(LobbyListener l){
//		llist.add(l);
//	}
//	public static void UnRegisterListener(LobbyListener l){
//		llist.remove(l);
//	}

//	protected void ListenerRespond(EventLobby evt){
//		for(int a = llist.size()-1;a>=0;a--){
//			LobbyListener listener = llist.get(a);
//			Method[] mlist = listener.getClass().getMethods();
//			List<Method> runninglist = new ArrayList<>();
//			for(Method meth : mlist){
//				if(meth.isAnnotationPresent(ListenerTag.class)){
//					runninglist.add(meth);
//				}
//			}
//			for(int dtime = -5;dtime <= 5;dtime++){
//				for(Method run : runninglist){
//					if(run.getAnnotation(ListenerTag.class).runDelay() == dtime){
//						try {
//							if(run.getParameterTypes().length==1 && run.getParameterTypes()[0].equals(evt.getClass())){
//								run.invoke(listener,evt);
//							}
//						} catch (IllegalAccessException e) {
//							e.printStackTrace();
//						} catch (IllegalArgumentException e) {
//							e.printStackTrace();
//						} catch (InvocationTargetException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//
//		}
//	}
}

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
import org.csg.group.hologram.FwHologram;
import org.csg.group.task.VarTable;
import customgo.CsgTaskListener;
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

	public FwHologram hd = new FwHologram();
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
	public boolean open = true;

	public Location getSpawn(Player p){
		if(p==null || !hasPlayer(p)){
			return null;
		}
		return spawnpoint.get(p.getUniqueId());
	}

	public void setCanJoin(boolean open){
		this.open = open;
	}

	public Object getMacro(String key){
		return macros.macros.get(key);
	}
	public boolean requireMacro(String line){
		String annotation;
		String s;
		String default_value;
		if(line.contains("#")){
			String[] ano = line.split("#");
			annotation = "# "+ano[1];
			if(ano[0].contains(" ")){
				s = ano[0].split(" ",2)[0];
				default_value = ano[0].split(" ",2)[1];
			}else{
				s = ano[0];
				default_value = "'null'";
			}
		}else{
			annotation = null;
			if(line.contains(" ")){
				s = line.split(" ",2)[0];
				default_value = line.split(" ",2)[1];
			}else{
				s = line;
				default_value = "'null'";
			}
		}

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
					fos.write(String.format("\n%s: %s",s,default_value).getBytes(StandardCharsets.UTF_8));
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
			}catch(Exception ignored){

			}
		}
		return state==2;
	}
	public VarTable macros = new VarTable();
	File default_macro_file;

	private final Set<FunctionTask> functions = new HashSet<>();
	private final Set<ListenerTask> listener = new HashSet<>();
	private Class<?> javaFunctionClass;
	private Object javaTaskInstance;
	private JsTaskCompiler jsTaskCompiler;

	private Set<Group> grouplist = new HashSet<>();
	private Trigger trigger = new Trigger(this);
	Group Default;

	File Folder;
	File tempFolder;
	String Name;

	private boolean isSproom_control = false;

	public boolean isSpRoom(){
		return isSproom_control;
	}
	public File getTempFolder(){
		return tempFolder;
	}
	public File getFolder(){
		return Folder;
	}

	public List<UUID> getPlayerList(){
		List<UUID> pl = new ArrayList<>();
		for(Group g : getGroupListI()){
			pl.addAll(g.getPlayerList());
			
		}
		return pl;
	}

	public boolean hasPlayer(Player p){
		for(Group g : getGroupListI()){
			if(g.hasPlayer(p)){
				return true;
			}
		}
		return false;
	}
	public Object callFunction(String name, Player p, Object... para){
		for(FunctionTask task : functions){
			if(task.getName().equals(name)){
				runTask(task,p,para);
			}
		}
		if (jsTaskCompiler != null) {
			jsTaskCompiler._setMember(this,null,p,p);
			if (name.startsWith("js"))
				return jsTaskCompiler.callFunction(name.substring(2),para);
		}
		if(javaFunctionClass !=null){
			try{
				for(Method meth : javaFunctionClass.getMethods()) {
					if (meth.getName().equals("_setMember")) {
						meth.invoke(javaTaskInstance, p, p);
					}
				}
				for(Method meth : javaFunctionClass.getMethods()){
					if(meth.getName().equals(name)){
						return meth.invoke(javaTaskInstance,para);
					}
				}
			}catch(IllegalAccessException | InvocationTargetException e){
				e.printStackTrace();
				Data.ConsoleInfo("尝试调取java函数"+name+"失败！");
			}
		}
		return null;
	}

	public Object callFunction(String name, TaskExecuter executer, Player p, Object... para){
		for(FunctionTask task : functions){
			if(task.getName().equals(name)){
				executer.lobby.runTask(task,p, para);
			}
		}
		if(jsTaskCompiler != null && name.toLowerCase().startsWith("js")) {
			jsTaskCompiler._setMember(this,null,executer.striker!=null ? Bukkit.getPlayer(executer.striker) : null,p);
			return jsTaskCompiler.callFunction(name.substring(2),para);
		}
		if(javaFunctionClass !=null){
			try{
				for(Method meth : javaFunctionClass.getMethods()){
					if(meth.getName().equals("_setMember")){
						meth.invoke(javaTaskInstance,this, executer.striker!=null ? Bukkit.getPlayer(executer.striker) : null,p);
						break;
					}
				}
				for(Method meth : javaFunctionClass.getMethods()){
					if(meth.getName().equals(name)){
						return meth.invoke(javaTaskInstance,para);
					}
				}
			}catch(IllegalAccessException | InvocationTargetException e){
				e.printStackTrace();
				Data.ConsoleInfo("尝试调取java函数"+name+"失败！");
			}
		}


		return null;
	}

	public Group findGroupOfPlayer(Player p){
		for(Group g : grouplist){
			if(g.hasPlayer(p)){
				return g;
			}
		}
		return null;
	}

	public void runTask(FunctionTask task, Player p, Object... para){
		TaskExecuter executer = new TaskExecuter(task,this);
		task.loadArgs(executer,para);

		executer.execute(p!=null ? p.getUniqueId() : null);
	}
	public void callListener(String name, Player p, Object... para){
		for(ListenerTask task : listener){
			if(task.getName().equals(name)){
				ListenerCalledEvent call = new ListenerCalledEvent(name,this,p,para);
				Data.fmain.getServer().getPluginManager().callEvent(call);

				TaskExecuter executer = new TaskExecuter(task,this);
				task.loadArgs(executer,para);
				executer.execute(p!=null ? p.getUniqueId() : null);
			}
		}
		if(javaFunctionClass !=null){
			try{
				for(Method meth : javaFunctionClass.getMethods()){
					if(meth.getName().equals("_setMember")){
						meth.invoke(javaTaskInstance,this, p, p);
						break;
					}
				}
				for(Method meth : javaFunctionClass.getMethods()){
					if(meth.isAnnotationPresent(CsgTaskListener.class)){
						CsgTaskListener ls = meth.getAnnotation(CsgTaskListener.class);
						if(ls.name().equals(name)){
							meth.invoke(javaTaskInstance,para);
						}
					}
				}
			}catch(IllegalAccessException | InvocationTargetException e){
				e.printStackTrace();
				Data.ConsoleInfo("尝试调取java监听器"+name+"失败！");
			}
		}
	}

	public void rename(String name){
		this.Name = name;
	}

	Map<String,FileConfiguration> fc = new HashMap<>();

	public FileConfiguration loadWorkFile(String name){
		if (!fc.containsKey(name)) {
			File f = new File(tempFolder, name + ".yml");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fc.put(name, Data.fmain.load(f));
		}
		return fc.get(name);
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

	private void loadFileRecurse(File folder, JavaTaskCompiler jcompiler, JsTaskCompiler jsTaskCompiler) throws IOException {
		for(File f : folder.listFiles()){
			if(f.isDirectory()){
				if(f.getName().equals("temp")){
					continue;
				}
				loadFileRecurse(f,jcompiler,jsTaskCompiler);
				continue;
			}

			if(f.getName().endsWith("yml")){
				continue;
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
			if(f.getName().endsWith("js")){
				jsTaskCompiler.read(f);
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
					macros.LoadMacro(Data.fmain.load(f));
				}
			}
		}
	}
	private void loadFile(File folder) throws IOException {
		JavaTaskCompiler jcompiler = new JavaTaskCompiler(this);
		jsTaskCompiler = new JsTaskCompiler(this);

		loadMacro(folder,true);

		if(default_macro_file!=null){
			Data.ConsoleInfo("正在加载用户宏列表 macro.yml");
			macros.LoadMacro(Data.fmain.load(default_macro_file));
		}else{
			default_macro_file = new File(folder.getPath()+"/macro.yml");
			default_macro_file.createNewFile();
		}

		loadFileRecurse(folder,jcompiler,jsTaskCompiler);

		javaFunctionClass = jcompiler.compile();
		if(javaFunctionClass !=null){
			try {
				javaTaskInstance = javaFunctionClass.getConstructor().newInstance();
				javaFunctionClass.getMethod("_setPlugin",JavaPlugin.class).invoke(javaTaskInstance,Data.fmain);
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		if (jsTaskCompiler.isHasContent()) {
			jsTaskCompiler._setPlugin(Data.fmain);
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

		Default = new Group(this, "Main");
		grouplist.add(Default);

		Data.fmain.getServer().getPluginManager().registerEvents(trigger,Data.fmain);
		MainCycle.registerCall(trigger);
		if(isComplete()){
			callListener("onLobbyLoaded",null);

			for(File f : tempFolder.listFiles()){
				if(!sproom_control && f.getName().equals("sproom_config.yml")){
					Data.ConsoleInfo("检测到大厅"+Name+"具有独立副本配置，已生成独立副本！");
					isSproom_control = true;
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

		jsTaskCompiler.getTasks().values().forEach(e -> e.getListeners().values().forEach(HandlerList::unregisterAll));
		if(LobbyList.contains(this)){
			LobbyList.remove(this);
		}

		callListener("onLobbyUnloaded",null);
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
	
	private boolean setted = false;


	public Group getDefaultGroup(){
		return Default;
	}

	public Set<Group> getGroupListI(){
		return grouplist;
	}

	public Set<customgo.Group> getGroupList(){
		Set<customgo.Group> g = new HashSet<>(grouplist);
		return g;
	}

	public int getPlayerAmount(){
		int a = 0;
		for(Group g : grouplist){
			a = a+g.getPlayerAmount();
		}
		return a;
	}

	public String getVariable(Player p, String key){
		return VarTable.objToString(macros.getValue(p,key));
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
		hd.ClearHologram();
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
		if(getPlayerAmount()==0){
			open = true;
		}
		if(!open){
			player.sendMessage(ChatColor.RED+"该游戏正在进行中，无法加入！");
			return;
		}
		PlayerJoinLobbyEvent e = new PlayerJoinLobbyEvent(player,this);
		Data.fmain.getServer().getPluginManager().callEvent(e);
		if(!e.isCancelled()){
			Default.JoinGroup(player);
			callListener("onPlayerJoinLobby", player);
		}

	}

	public void Leave(Player player){
		if(player==null || !hasPlayer(player)){
			return;
		}

		callListener("onPlayerLeaveLobby", player);

		Group g = this.findGroupOfPlayer(player);
		g.LeaveGroup(player);


		PlayerLeaveLobbyEvent e = new PlayerLeaveLobbyEvent(player,this);
		Data.fmain.getServer().getPluginManager().callEvent(e);

	}

	public void ChangeGroup(Player player,String groupname){
		if(player==null || !hasPlayer(player)){
			return;
		}
		Group from = Group.SearchPlayerInGroup(player);
		if(from==null){
			return;
		}

		if(getGroup(groupname)==null){
			grouplist.add(new Group(this,groupname));
		}

		Group to = this.getGroup(groupname);
		if(from==to){
			return;
		}
		from.LeaveGroup(player);
		to.JoinGroup(player);
		
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
			l.unLoad();
			//l.ListenerRespond(new EventOnLobbyUnloaded(l,false));
		}
		LobbyList.clear();
	}

	@Override
	public void onUpdate() {
		callListener("onEverySecond",null);
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

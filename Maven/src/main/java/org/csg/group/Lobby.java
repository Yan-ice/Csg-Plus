package org.csg.group;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import customgo.LobbyAPI;
import customgo.event.ListenerCalledEvent;
import customgo.event.PlayerJoinLobbyEvent;
import customgo.event.PlayerLeaveLobbyEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.csg.Fwmain;
import org.csg.Utils.CommonUtils;
import org.csg.Utils.GameMethodUtils;
import org.csg.Utils.OSUtils;
import org.csg.group.hologram.FwHologram;
import org.csg.group.task.VarTable;
import customgo.CsgTaskListener;
import org.csg.group.task.cast.TypeCastFactory;
import org.csg.group.task.csgtask.FunctionTask;
import org.csg.group.task.csgtask.ListenerTask;
import org.bukkit.entity.*;
import org.bukkit.*;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.csg.group.task.toolkit.*;
import org.csg.update.CycleUpdate;
import org.csg.update.MainCycle;
import org.csg.update.SecondCycle;

public class Lobby implements CycleUpdate, LobbyAPI {



	// 全息显示
	public FwHologram hd = new FwHologram();

	// 游戏方法工具类
	public GameMethodUtils gameMethodUtils = new GameMethodUtils(this);

	public boolean open = true;


	public void setCanJoin(boolean open){
		this.open = open;
	}

	public boolean hasMacroForLike(String key) {
		if(macros.macros.containsKey(key)) { // 存在key
			return true;
		}
		// 模糊查询是否有key的值等于查询key
		for (String str : macros.macros.keySet()) {
			if (str.startsWith(key+".")) { // 如果有说明查询的节点是父级Key
				return true;
			}
		}
		return false;
	}

	public Map<String, Object> getMacroForLike(String key) {
		Map<String, Object> macroList = new HashMap<>();
		// 模糊查询是否有key的值等于查询key
		for (String str : macros.macros.keySet()) {
			if (str.startsWith(key+".")) { // 如果有说明查询的节点是父级Key
				macroList.put(str, macros.macros.get(str));
			}
		}
		return macroList;
	}

	public Object getMacro(String key){
		return macros.macros.get(key);
	}



	public <T> T getMacro(String key, Class<T> clazz) {
		return clazz.cast(macros.macros.get(key));
	}

	/**
	 * 获取宏的值转换为int
	 * @param key
	 * @return
	 */
	@Override
	public int getMacroInt(String key) {
		return (int) macros.macros.get(key);
	}

	/**
	 * 获取宏的值转换为double
	 * @param key
	 * @return
	 */
	@Override
	public double getMacroDouble(String key) {
		return (double) macros.macros.get(key);
	}

	/**
	 * 获取宏的值转换为String
	 * @param key
	 * @return
	 */
	@Override
	public String getMacroString(String key) {
		return (String) macros.macros.get(key);
	}

	/**
	 * 获取宏的值转换为boolean
	 * @param key
	 * @return
	 */
	@Override
	public boolean getMacroBoolean(String key) {
		return (boolean) macros.macros.get(key);
	}

	/**
	 * 获取宏的值转换为List<String>
	 * @param key
	 * @return
	 */
	@Override
	public List<String> getMacroList(String key) {
		return (List<String>) macros.macros.get(key);
	}

	public void addMacro(String key, Object value) {
		macros.AddMacro(key, value);
	}

	public void addScore(Player player, String key, Double value) {
		macros.AddScore(player, key, value);
	}


	public double getScore(Player player, String key) {
		return macros.getScore(player, key);
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
		s = s.trim();
		int state = macros.HasMacro(s);
		if(state!=2){
			CommonUtils.ConsoleErrorMsg("该大厅并未满足脚本需求的宏"+s+"！");
			CommonUtils.ConsoleErrorMsg("请在macro.yml文件设置相关内容，并重载该大厅。在此之前，相关脚本将无法使用！");
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
		if(javaFunctionClass !=null){
			try{
				for(Method meth : javaFunctionClass.getMethods()) {
					if (meth.getName().equals("_setMember")) {
						meth.invoke(javaTaskInstance, p, p);
					}
				}
				for(Method meth : javaFunctionClass.getMethods()){
					if(meth.getName().equals(name) && meth.getGenericParameterTypes().length == para.length){
						safeCallJavaFunction(meth,para);
					}
				}
			}catch(IllegalAccessException | InvocationTargetException e){
				e.printStackTrace();
				CommonUtils.ConsoleInfoMsg("尝试调取java函数"+name+"失败！");
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
		if(javaFunctionClass !=null){
			try{
				for(Method meth : javaFunctionClass.getMethods()){
					if(meth.getName().equals("_setMember")){
						meth.invoke(javaTaskInstance,this, executer.striker!=null ? Bukkit.getPlayer(executer.striker) : null,p);
						break;
					}
				}
				for(Method meth : javaFunctionClass.getMethods()){
					if(meth.getName().equals(name) && meth.getGenericParameterTypes().length == para.length){
						return safeCallJavaFunction(meth,para);
					}
				}
			}catch(IllegalAccessException | InvocationTargetException e){
				e.printStackTrace();
				CommonUtils.ConsoleInfoMsg("尝试调取java函数"+name+"失败！");
			}
		}

		return null;
	}

	/**
	 * "安全地"进行java函数调用。
	 * 在调用时如果出现类型不匹配，会先尝试用TypeCastFactory进行类型转化。
	 * @param meth
	 * @param para
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private Object safeCallJavaFunction(Method meth, Object... para) throws InvocationTargetException, IllegalAccessException {
		Type[] require_list = meth.getGenericParameterTypes();
		Object[] cast_list = new Object[require_list.length];
		for(int a = 0;a<require_list.length;a++){
			cast_list[a] = TypeCastFactory.castObject(para[a],require_list[a]);
		}
		return meth.invoke(javaTaskInstance,cast_list);
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
		if(p!=null && !hasPlayer(p)){
			return;
		}
		for(ListenerTask task : listener){


			if(task.getName().equals(name)){
				List<String> canGroup = new ArrayList<>(Arrays.asList(task.getField().split(",,")));
				Group groupOfPlayer = null;
				if(p != null) {
					groupOfPlayer = this.findGroupOfPlayer(p);
				}
				if (groupOfPlayer == null ||
						canGroup.size() > 0
								&& StringUtils.isNotBlank(canGroup.get(0))
								&& canGroup.contains(groupOfPlayer.getName())) {
					ListenerCalledEvent call = new ListenerCalledEvent(name, this, p, para);
					Fwmain.getInstance().getServer().getPluginManager().callEvent(call);

					TaskExecuter executer = new TaskExecuter(task, this);
					task.loadArgs(executer, para);
					executer.execute(p != null ? p.getUniqueId() : null);
				}
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
						if(ls.name().equals(name) && meth.getGenericParameterTypes().length == para.length){
							safeCallJavaFunction(meth, para);
						}
					}
				}
			}catch(IllegalAccessException | InvocationTargetException e){
				e.printStackTrace();
				CommonUtils.ConsoleInfoMsg("尝试调取java监听器"+name+"失败！");
			}
		}
	}

	public void rename(String name){
		this.Name = name;
	}

	Map<String,FileConfiguration> fileConfigMap = new HashMap<>();

	public FileConfiguration loadWorkFile(String name){
		if (!fileConfigMap.containsKey(name)) {
			File f = new File(tempFolder, name + ".yml");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fileConfigMap.put(name, OSUtils.loadFileConfiguration(f));
		}
		return fileConfigMap.get(name);
	}
	public void saveWorkFile(String name){
		if(fileConfigMap.containsKey(name)){
			File f = new File(tempFolder,name+".yml");
			try {
				if(!f.exists()){
					f.createNewFile();
				}
				fileConfigMap.get(name).save(f);
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
		fileConfigMap.remove(name);
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
	private Lobby(File folder, int p){
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
		Fwmain.getInstance().getLobbyList().add(this);
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
					macros.LoadMacro(OSUtils.loadFileConfiguration(f), null);
				}
			}
		}
	}
	private void loadFile(File folder) throws IOException {
		JavaTaskCompiler jcompiler = new JavaTaskCompiler(this);

		loadMacro(folder,true);

		if(default_macro_file!=null){
			//CommonUtils.ConsoleInfoMsg("正在加载Macro预设宏");
			macros.LoadMacro(OSUtils.loadFileConfiguration(default_macro_file), null);
		}else{
			default_macro_file = new File(folder.getPath()+"/macro.yml");
			default_macro_file.createNewFile();
		}

		loadFileRecurse(folder,jcompiler);

		javaFunctionClass = jcompiler.compile();
		if(javaFunctionClass !=null){
			try {
				javaTaskInstance = javaFunctionClass.getConstructor().newInstance();
				javaFunctionClass.getMethod("_setPlugin",JavaPlugin.class).invoke(javaTaskInstance,Fwmain.getInstance());
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
				e.printStackTrace();
			}
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
		fileConfigMap.clear();
		grouplist.clear();
		functions.clear();
		listener.clear();
		CommonUtils.ConsoleErrorMsg("===== | &b正在加载大厅 " + Name + " &r| =====");

//		if(Data.isBungee){
//			new BungeeSupport((this));
//		}

		try{
			loadFile(Folder);
		}catch(IOException e){
			e.printStackTrace();
			CommonUtils.ConsoleInfoMsg("文件读取失败！");
		}

		Default = new Group(this, "Main");
		grouplist.add(Default);

		Fwmain.getInstance().getServer().getPluginManager().registerEvents(trigger, Fwmain.getInstance());
		MainCycle.registerCall(trigger);

		if(isComplete()){
			callListener("onLobbyLoaded",null);

			CommonUtils.ConsoleInfoMsg("===== | &a大厅 "+Name+"加载成功 &r| =====");
			SecondCycle.registerCall(this);

		}else{
			CommonUtils.ConsoleInfoMsg("===== | &c大厅 "+Name+"加载失败 &r| =====");
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

		if(Fwmain.getInstance().getLobbyList().contains(this)){
			Fwmain.getInstance().getLobbyList().remove(this);
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
		for(Lobby l : Fwmain.getInstance().getLobbyList()){
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
		if(Group.SearchPlayerInGroup(player)!=null){
			player.sendMessage(ChatColor.RED+"你已经在一个游戏中了！");
			return;
		}
		PlayerJoinLobbyEvent e = new PlayerJoinLobbyEvent(player,this);
		Fwmain.getInstance().getServer().getPluginManager().callEvent(e);
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
		Fwmain.getInstance().getServer().getPluginManager().callEvent(e);
		callListener("onPlayerRest",null,getPlayerAmount());
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
		for(Lobby l : Fwmain.getInstance().getLobbyList()){
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
		while(Fwmain.getInstance().getLobbyList().size()>0){
			Fwmain.getInstance().getLobbyList().get(0).unLoad();
			//l.ListenerRespond(new EventOnLobbyUnloaded(l,false));
		}
	}

	@Override
	public void onUpdate() {
		callListener("onEverySecond",null);
	}

	/**
	 * 添加玩家键值对
	 * @param key 键
	 * @param player 玩家
	 * @param value 值
	 */
	public void addValues(String key, Player player, String value) {
		macros.addValues(key,player.getUniqueId(),value);
	}

	/**
	 * 移除玩家键值对
	 * @param key 键
	 * @param player 玩家
	 */
	public boolean removeValues(String key, Player player) {
		return macros.removeValues(key,player.getUniqueId());
	}

	/**
	 * 获取玩家键值对
	 * @param key 键
	 * @param player 玩家
	 */
	public String getValues(String key, Player player) {
		return macros.getValues(key,player.getUniqueId());
	}

}

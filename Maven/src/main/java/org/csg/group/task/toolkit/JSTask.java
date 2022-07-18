package org.csg.group.task.toolkit;

import customgo.Group;
import customgo.Lobby;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.csg.Data;

import javax.script.*;
import java.util.*;

public class JSTask {

    ScriptEngine engine;
    String name;
    String path;

    String script;
    boolean loaded = false;
    boolean stop = false;

    private JsTaskCompiler compiler;
    private Plugin plugin;
    private Group group;
    private Lobby lobby;
    private Player striker;
    private Player player;
    private Map<String, Listener> listeners;


    public JSTask(String name, String script, JsTaskCompiler compiler, Lobby lobby, String path) {
        this.compiler = compiler;
        this.path = path;
        this.name = name;
        this.lobby = lobby;
        this.listeners = new HashMap<>();
        engine = new NashornScriptEngineFactory().getScriptEngine(Data.fmain.getClass().getClassLoader());

        this.script = script;
    }

    public boolean load(){
        if (loaded) {
            return true;
        }
        try {
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("thisJS",this);
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("plugin",plugin);
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("lobby",lobby);
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("group",group);
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("striker",striker);
            engine.getBindings(ScriptContext.ENGINE_SCOPE).put("player",player);
            engine.eval(this.script);
            loaded = true;
            this.script = "";
            return true;
        } catch (Exception e) {
            Data.Debug(String.format("加载JSTask[%s]失败",name));
            Data.Debug(e.getMessage());
            return false;
        }
    }

    public Object callFunction(String name, Object... paras) {
        try {
            return ((Invocable) engine).invokeFunction(name,paras);
        } catch (ScriptException | NoSuchMethodException e) {
            Data.Debug(e.getMessage());
            return "null";
        }
    }

    public void _setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public void _setMember(Lobby lobby, Group group, Player striker, Player player) {
        this.group = group;
        this.lobby=lobby;
        this.striker = striker;
        this.player = player;
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("group", group);
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("lobby", lobby);
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("striker", striker);
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("player", player);
    }

    public void setMacro(String key, String value) {
        ((org.csg.group.Lobby)lobby).macros.macros.put(key,value);
    }

    public Object getMacro(String str) {
        return ((org.csg.group.Lobby)lobby).macros.macros.getOrDefault(str,"null");
    }

    public ScriptContext getContent() {
        return engine.getContext();
    }

    public Map<String, Map<String, Object>> getGroupValues() {
        return compiler.getGroupValues();
    }

    public JSTask getJS(String name) {
        for (Map.Entry<String, JSTask> entry : compiler.getTasks().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name) || entry.getKey().equalsIgnoreCase(name+".js"))
                return entry.getValue();
        }
        return this;
    }

    public Object getMember(String name){
        return engine.getContext().getAttribute(name);
    }

    public void runTaskLater(String func, int delay, Object... paras) {
        if (!stop) {
            //Data.Debug(String.format("JSTask添加定时任务[%s][%stick][%s]",func,delay,stop));
            new BukkitRunnable() {
                @Override
                public void run() {
                    callFunction(func, paras);
                }
            }.runTaskLater(Data.fmain, delay);
        }
    }

    public void runTaskTimer(String func, int delay, int period, Object... paras) {
        if (!stop) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    callFunction(func, paras);
                }
            }.runTaskTimer(Data.fmain, delay, period);
        }
    }

    public boolean isInstance(String classpath, Object obj) {
        try{
            Class clazz = Class.forName(classpath);
            return clazz.isInstance(obj);
        }catch (Exception err) {
            return false;
        }
    }

    public void createListener(String classpath, String callback){
        if(listeners.containsKey(callback)) {
            Data.Debug(String.format("创建监听器失败,已存在监听器[%s]",callback));
            return;
        }
        try {
            Class<? extends Event> clazz = (Class<? extends Event>)Class.forName(classpath);
            ListenerFactory.createListenerClass(clazz);
            Listener listener = ListenerFactory.getListener(clazz, (event) -> {
                this.callFunction(callback,event);
            });
            Bukkit.getPluginManager().registerEvents(listener,Data.fmain);
            listeners.put(callback,listener);
        } catch (Exception e) {
            Data.Debug(String.format("创建监听器[%s]失败",classpath));
            e.printStackTrace();
        }
    }

    public void importClass(String key, String classPath){
        try {
            engine.eval(String.format("var %s = Java.type(\"%s\");",key,classPath));
        }catch (Exception ignored){}
    }

    public void addAttribute(String key, Object obj){
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put(key, obj);
    }

    //工具方法

    public String[] split(String origin, String regex) {
        return origin.split(regex);
    }

    public String[] split(String origin, String regex, int limit) {
        return origin.split(regex,limit);
    }

    public <T> ArrayList<T> newList(Class<T> clazz) {
        return new ArrayList<>();
    }

    public <T> HashSet<T> newSet(Class<T> clazz) {
        return new HashSet<>();
    }

    public <K, V> HashMap<K, V> newMap(Class<K> kClass, Class<V> vClass) {
        return new HashMap<>();
    }

    public Object notNull(Object obj) {
        return obj==null ? "null" : obj;
    }

    public Map<String, Listener> getListeners() {
        return this.listeners;
    }
}

package org.csg.group.task.toolkit;

import customgo.Group;
import customgo.Lobby;
import jdk.internal.dynalink.beans.StaticClass;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.csg.Data;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;

public class JSTask {
    ScriptEngine engine;
    Invocable invocable;
    String name;
    String path;

    String script;
    boolean loaded = false;

    private JsTaskCompiler compiler;
    private Plugin plugin;
    private Group group;
    private Lobby lobby;
    private Player striker;
    private Player player;
    @Getter
    private Map<String, Listener> listeners;


    public JSTask(String name, String script, JsTaskCompiler compiler, Lobby lobby, String path) {
        this.compiler = compiler;
        this.path = path;
        this.name = name;
        this.lobby = lobby;
        this.listeners = new HashMap<>();
        engine = new ScriptEngineManager().getEngineByName("Nashorn");
        invocable = (Invocable) engine;
        this.script = script;
    }

    public boolean load(){
        if (loaded) {
            return true;
        }
        try {
            engine.put("Task",this);
            engine.put("plugin",plugin);
            engine.put("lobby",lobby);
            engine.put("group",group);
            engine.put("striker",striker);
            engine.put("player",player);
            engine.eval(this.script);
            loaded = true;
            this.script = "";
            return true;
        } catch (Exception e) {
            Data.Debug(String.format("加载游戏[%s]的JSTask失败",lobby.getName()));
            Data.Debug(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Object callFunction(String name, Object... paras) {
        try {
            Invocable invocable = (Invocable)engine;
            return invocable.invokeFunction(name,paras);
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
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
        engine.put("group", group);
        engine.put("lobby", lobby);
        engine.put("striker", striker);
        engine.put("player", player);
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

    public Map<String, Object> getLobbyValues() {
        return compiler.getLobbyValues();
    }

    public void createListener(String classpath, String callback){
        if(listeners.containsKey(callback)) {
            Data.Debug(String.format("创建监听器失败,已存在监听器[$s]",callback));
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

    public void importClass(String key, String className)throws ClassNotFoundException{
        StaticClass staticClass = StaticClass.forClass(Class.forName(className));
        engine.put(key,staticClass);
    }
}

package org.csg.group.task.toolkit;

import customgo.Group;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.csg.Data;
import org.csg.group.Lobby;

import javax.script.ScriptContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsTaskCompiler {
    private static Method forClass = null;
    static {
        Class clazz = null;
        try {
            clazz = Class.forName("jdk.internal.dynalink.beans.StaticClass");
        }catch (Exception ignored) {
        }
        if(clazz == null) {
            try {
                clazz = Class.forName("jdk.dynalink.beans.StaticClass");
            }catch (Exception ignored) {
            }
        }
        if(clazz == null) {
            Data.ConsoleError("找不到Nashorn引擎, JS功能失效");
        }else{
            try{
                forClass = clazz.getMethod("forClass",Class.class);
            }catch (Exception ignored){}
        }
    }

    public static Object forClass(Class clazz){
        if(forClass != null) {
            try {
                return forClass.invoke(null, clazz);
            }catch (Exception ignored){}
        }
        return null;
    }



    private final Lobby lobby;
    private final String path;
    private Map<String, String> imports;
    private Map<String, Object> vars;
    @Getter
    private boolean hasContent = false;
    private Map<String, JSTask> tasks = new HashMap<>();
    private Map<String, String> functions = new HashMap<>();
    //<Group <Key, Value>>
    private final Map<String, Map<String, Object>> groupValues;

    public JsTaskCompiler(Lobby lobby) {
        this.lobby = lobby;
        this.path = lobby.getTempFolder().getAbsolutePath();
        imports = new HashMap<>();
        vars = new HashMap<>();
        groupValues = new HashMap<>();
        lobby.getGroupList().forEach(e -> groupValues.put(e.getName(),new HashMap<>()));

        imports.put("Bukkit",Bukkit.class.getName());
        imports.put("Location",Location.class.getName());
        imports.put("Random",Random.class.getName());
        imports.put("Arrays",Arrays.class.getName());
        imports.put("Class",Class.class.getName());
        imports.put("System",System.class.getName());
        imports.put("ListenerFactory",ListenerFactory.class.getName());
        imports.put("Player",Player.class.getName());
        imports.put("Data",Data.class.getName());

        imports.put("String",String.class.getName());
        imports.put("Double",Double.class.getName());
        imports.put("Short",Short.class.getName());
        imports.put("Byte",Byte.class.getName());
        imports.put("Integer",Integer.class.getName());
        imports.put("Float",Float.class.getName());
        imports.put("Long",Long.class.getName());
        imports.put("Object",Object.class.getName());
    }

    public void read(File f) throws IOException {
        hasContent = true;
        Data.ConsoleInfo("正在加载JS脚本 " + f.getName());
        String path = f.getPath();
        FileInputStream input = new FileInputStream(path);
        byte[] buffer = new byte[input.available()];
        input.read(buffer);
        input.close();
        String script = new String(buffer,StandardCharsets.UTF_8);
        buffer = null;

        Map<String, String> funcs = new HashMap<>();
        Pattern pattern = Pattern.compile("(?=(\\b|\\W)+)function\\W+\\w*(?=\\s*\\(.*\\))");
        Matcher matcher = pattern.matcher(script);
        while (matcher.find()) {
            String name = matcher.group().substring(9);
            if(functions.containsKey(name)) {
                Data.LoadError(lobby.getName(),String.format("JS脚本[%s]中方法[%s]与脚本[%s]中方法冲突",f.getName(),name,functions.get(name)));
                return;
            }
            funcs.put(name,f.getName());
        }
        JSTask task = new JSTask(f.getName(),script, this, lobby, f.getPath());
        imports.forEach(task::importClass);
        vars.forEach((k, v)-> {
            if(k!=null && !k.isEmpty()) {
                task.engine.getBindings(ScriptContext.ENGINE_SCOPE).put(k,v);
            }
        });
        if (task.load()) {
            tasks.put(f.getName(), task);
            functions.putAll(funcs);
        } else {
            Data.LoadError(lobby.getName(),String.format("加载脚本JSTask[%s]出错",f.getName()));
        }
    }

    public void unload(){
        tasks.values().forEach(e -> {e.loaded = false;e.stop = true;e.getListeners().clear();});
        getTasks().values().forEach(e -> e.getListeners().values().forEach(HandlerList::unregisterAll));
    }

    public void _setPlugin(Plugin plugin) {
        tasks.values().forEach(e -> e._setPlugin(plugin));
    }

    public void _setMember(customgo.Lobby lobby, Group group, Player striker, Player player) {
        tasks.values().forEach(e -> e._setMember(lobby,group,striker,player));
    }


    public Object callFunction(String name, Object... paras) {
        if (!functions.containsKey(name)) {
            Data.ConsoleError(String.format("JSTask方法[%s]不存在",name));
            return "null";
        } else {
            String jsTask = functions.get(name);
            Data.Debug(String.format("调用JSTask[%s]中方法[%s] 参数%s",jsTask,name,Arrays.deepToString(paras)));
            return tasks.get(jsTask).callFunction(name,paras);
        }
    }

    public Map<String, JSTask> getTasks() {
        return this.tasks;
    }

    public Map<String, Map<String, Object>> getGroupValues() {
        return this.groupValues;
    }
}

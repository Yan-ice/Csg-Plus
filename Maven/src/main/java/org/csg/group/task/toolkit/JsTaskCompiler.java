package org.csg.group.task.toolkit;

import customgo.Group;
import jdk.internal.dynalink.beans.StaticClass;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.csg.Data;
import org.csg.group.Lobby;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsTaskCompiler {
    private final Lobby lobby;
    private final String path;
    private Map<String, StaticClass> imports;
    private Map<String, Object> varss;
    @Getter
    private boolean hasContent = false;
    @Getter
    private Map<String, JSTask> tasks = new HashMap<>();
    private Map<String, String> functions = new HashMap<>();
    @Getter
    private Map<String, Object> lobbyValues = new HashMap<>();

    public JsTaskCompiler(Lobby lobby) {
        this.lobby = lobby;
        this.path = lobby.getTempFolder().getAbsolutePath();
        imports = new HashMap<>();
        varss = new HashMap<>();
        imports.put("Bukkit",StaticClass.forClass(Bukkit.class));
        imports.put("Location",StaticClass.forClass(Location.class));
        imports.put("Random",StaticClass.forClass(Random.class));
        imports.put("Data",StaticClass.forClass(Data.class));
        imports.put("Arrays",StaticClass.forClass(Arrays.class));
        imports.put("JSUtils",StaticClass.forClass(JSUtils.class));
        imports.put("Class",StaticClass.forClass(Class.class));
        imports.put("ListenerFactory",StaticClass.forClass(ListenerFactory.class));

        imports.put("String",StaticClass.forClass(String.class));
        imports.put("Double",StaticClass.forClass(Double.class));
        imports.put("Short",StaticClass.forClass(Short.class));
        imports.put("Byte",StaticClass.forClass(Byte.class));
        imports.put("Integer",StaticClass.forClass(Integer.class));
        imports.put("Long",StaticClass.forClass(Long.class));
        imports.put("Object",StaticClass.forClass(Object.class));

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
                Data.ConsoleInfo(String.format("JS脚本[%s]中方法[%s]与脚本[%s]中方法冲突",f.getName(),functions.get(name)));
                return;
            }
            funcs.put(name,f.getName());
        }
        JSTask task = new JSTask(f.getName(),script, this, lobby, f.getPath());
        if (task.load()) {
            imports.forEach(task.engine::put);
            varss.forEach(task.engine::put);
            tasks.put(f.getName(), task);
            functions.putAll(funcs);
        } else {
            Data.ConsoleError(String.format("加载脚本JSTask[%s]出错",f.getName()));
        }
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
            Data.Debug(String.format("调用JSTask[%s]中方法[%s] 参数%s",jsTask,name,Arrays.toString(paras)));
            return tasks.get(jsTask).callFunction(name,paras);
        }
    }
}

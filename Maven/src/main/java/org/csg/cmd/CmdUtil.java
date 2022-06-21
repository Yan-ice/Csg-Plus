package org.csg.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.csg.Fwmain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class CmdUtil {
    static Class<SimplePluginManager> spmClass = SimplePluginManager.class;
    static Class<PluginCommand> pcClass = PluginCommand.class;
    static Field scmF;
    static Constructor<PluginCommand> pcConstor;
    static CommandMap commandMap;
    static {
        try {
            scmF = spmClass.getDeclaredField("commandMap");
            pcConstor = pcClass.getDeclaredConstructor(String.class, Plugin.class);
            pcConstor.setAccessible(true);
            scmF.setAccessible(true);
            commandMap = (CommandMap) scmF.get(Bukkit.getServer().getPluginManager());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static PluginCommand createPluginCommand(String pluginName, String commandName, Plugin plugin) {
        try {
            PluginCommand pluginCommand = pcConstor.newInstance(commandName, plugin);
            commandMap.register(commandName, pluginName, pluginCommand);
            return pluginCommand;
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }
}

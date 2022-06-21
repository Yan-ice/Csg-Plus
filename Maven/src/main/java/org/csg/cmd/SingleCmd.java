package org.csg.cmd;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class SingleCmd implements CommandExecutor, TabCompleter {

    protected PluginCommand pluginCommand;
    protected String cmd;
    protected String description;

    protected SingleCmd(String cmd, Plugin plugin){
        this(Objects.requireNonNull(CmdUtil.createPluginCommand(plugin.getName(), cmd, plugin)));
    }

    protected SingleCmd(String cmd, String pluginName, Plugin plugin){
        this(Objects.requireNonNull(CmdUtil.createPluginCommand(pluginName, cmd, plugin)));
    }

    protected SingleCmd(PluginCommand pluginCommand) {
        this.cmd = pluginCommand.getName();
        this.pluginCommand = pluginCommand;
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }
    
    protected boolean canPlayer;
    protected String playerPermission;
    protected List<String> playerParas;
    public String playerParas(){
        StringBuilder sb = new StringBuilder()
                .append("/")
                .append(cmd);
        playerParas.forEach(e -> sb.append(" ").append(e));
        return sb.toString();
    }
    abstract protected void player(Player player,String... args);
    abstract protected List<String> playerTab(int para,String arg);

    protected boolean canOp;
    protected String opPermission;
    protected List<String> opParas;
    public String opParas(){
        StringBuilder sb = new StringBuilder()
                .append("/")
                .append(cmd);
        opParas.forEach(e -> sb.append(" ").append(e));
        return sb.toString();
    }
    abstract protected void op(Player player,String... args);
    abstract protected List<String> opTab(int para,String arg);

    protected boolean canConsole;
    protected List<String> consoleParas;
    public String consoleParas(){
        StringBuilder sb = new StringBuilder()
                .append("/")
                .append(cmd);
        consoleParas.forEach(e -> sb.append(" ").append(e));
        return sb.toString();
    }
    abstract protected void console(CommandSender sender,String... args);
    abstract protected List<String> consoleTab(int para,String arg);
    
    public void handle(CommandSender sender,String... args){
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(canOp&&(player.hasPermission(opPermission)||player.isOp())){
                op(player,args);
            }else if(canPlayer&&player.hasPermission(playerPermission)){
                player(player, args);
            }else {
                sender.sendMessage(ChatColor.RED+"命令错误");
            }
        }else if(canConsole){
            console(sender, args);
        }else {
            sender.sendMessage(ChatColor.RED+"命令错误");
        }
    }

    @Override
    public  List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings){
        boolean[] isCmd = new boolean[]{command.getName().equalsIgnoreCase(cmd)};
        command.getAliases().forEach(e -> isCmd[0] = (isCmd[0] || e.equalsIgnoreCase(cmd)));
        List<String> tabs = new ArrayList<>();
        if (!isCmd[0]) {
            return tabs;
        }
        int arg = strings.length-1;
        if (arg > 0) {
            if (commandSender instanceof Player) {
                if (!(arg >= opParas.size()) && canOp && (commandSender.isOp() || commandSender.hasPermission(opPermission))) {
                    tabs.addAll(opTab(arg, strings[strings.length - 1]));
                } else if (!(arg >= playerParas.size()) && (canPlayer && commandSender.hasPermission(playerPermission))) {
                    tabs.addAll(playerTab(arg, strings[strings.length - 1]));
                }
            } else if (!(arg >= consoleParas.size()) && canConsole) {
                tabs.addAll(consoleTab(arg, strings[strings.length - 1]));
            }
        }
        return tabs;
    }

    @Override
    public boolean onCommand( CommandSender commandSender,  Command command,  String s,  String[] strings){
        boolean[] isCmd = new boolean[]{command.getName().equalsIgnoreCase(cmd)};
        command.getAliases().forEach(e -> isCmd[0] = (isCmd[0]|| e.equalsIgnoreCase(cmd)));
        if(isCmd[0]){
            handle(commandSender,strings);
        }
        return false;
    }
}

package org.csg.cmd.label;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.csg.Fwmain;
import org.csg.cmd.Cmd;
import org.csg.cmd.RootCmd;
import org.csg.group.Lobby;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Macro extends Cmd {
    Macro(RootCmd rootCmd) {
        super(rootCmd);
        branch = "SetMacro";
        description = "设置队列的宏";
        playerPermission = "csg.SetMacro";
        opPermission = "";
        canPlayer = true;
        canOp = true;
        canConsole = true;
        playerParas = Arrays.asList("游戏名", "宏", "值");
        opParas = playerParas;
        consoleParas = playerParas;
    }
    @Override
    public void player(Player player, String... args) {
        console(player, args);
    }
    @Override
    public void op(Player player, String... args) {
        console(player, args);
    }
    @Override
    public void console(CommandSender sender, String... args) {
        if(args.length>2) {
            Lobby lobby = Lobby.getLobby(args[0]);
            if(lobby != null) {
                for(File f : lobby.getFolder().listFiles()) {
                    if(f.isDirectory()){
                        continue;
                    }
                    if(f.getName().equals("macro.yml")){
                        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(f);
                        configuration.set(args[1],args[2]);
                        try {
                            configuration.save(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sender.sendMessage(String.format("%s成功将队列%s%s%s中宏%s%s%s的值设为%s%s", ChatColor.GREEN,ChatColor.AQUA,args[0],ChatColor.GREEN,ChatColor.AQUA,args[1],ChatColor.GREEN,ChatColor.AQUA,args[2]));
                        break;
                    }
                }
            }else {
                sender.sendMessage(String.format("%s队列%s%s%s不存在",ChatColor.RED,ChatColor.AQUA,args[0],ChatColor.RED));
            }
        }else {
            sender.sendMessage(consoleParas());
        }
    }
    @Override
    public List<String> playerTab(int para, String[] paras, Player player){
        return consoleTab(para,paras);
    }
    @Override
    public List<String> opTab(int para, String[] paras, Player player){
        return consoleTab(para,paras);
    }
    @Override
    public List<String> consoleTab(int para,String[] paras) {
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Fwmain.lobbyList.forEach(e -> args.add(e.getName()));
        }
        if(para == 1) {
            Lobby lobby = Lobby.getLobby(paras[0]);
            if(lobby != null) {
                args.addAll(lobby.macros.macros.keySet());
            }
        }
        return args;
    }
}

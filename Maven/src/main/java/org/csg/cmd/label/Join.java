package org.csg.cmd.label;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.csg.Fwmain;
import org.csg.cmd.Cmd;
import org.csg.cmd.RootCmd;
import org.csg.group.Lobby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Join extends Cmd {
    public Join(RootCmd rootCmd) {
        super(rootCmd);
        branch = "Join";
        description = "加入游戏";
        playerPermission = "csg.Join";
        opPermission = "";
        canPlayer = true;
        canOp = true;
        canConsole = true;
        playerParas = Arrays.asList("游戏名");
        opParas = Arrays.asList("游戏名","[玩家]");
        consoleParas = Arrays.asList("游戏名","玩家");
    }
    @Override
    public void player(Player player, String... args) {
        if (args.length < 1) {
            player.sendMessage(playerParas());
            return;
        }

        Lobby lobby = Lobby.getLobby(args[0]);
        if (lobby != null) {
            lobby.Join(player);
        } else {
            player.sendMessage("游戏["+args[0]+"]不存在");
        }

    }
    @Override
    public void op(Player player, String... args) {
        if (args.length < 1) {
            player.sendMessage(opParas());
            return;
        }
        Player target = player;
        if (args.length > 1) {
            if (Bukkit.getPlayer(args[1]) != null) {
                target = Bukkit.getPlayer(args[1]);
            } else {
                player.sendMessage("玩家["+args[1]+"]未在线或不存在");
            }
        }
        Lobby lobby = Lobby.getLobby(args[0]);
        if(lobby!=null ){
            lobby.Join(target);
        }else{
            player.sendMessage("游戏["+args[0]+"]不存在");
        }
    }
    @Override
    public void console(CommandSender sender, String... args) {
        if (args.length < 2) {
            sender.sendMessage(consoleParas());
            return;
        }
        if (Bukkit.getPlayer(args[1]) != null) {
            Player target = Bukkit.getPlayer(args[1]);

            Lobby lobby = Lobby.getLobby(args[0]);
            if(lobby!=null){
                lobby.Join(target);
            }
            else {
                sender.sendMessage("游戏["+args[0]+"]不存在");
            }
        } else {
            sender.sendMessage("玩家["+args[1]+"]未在线或不存在");
        }
    }
    @Override
    public List<String> playerTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Fwmain.getInstance().getLobbyList().forEach(e -> args.add(e.getName()));
        }
        return args;
    }
    @Override
    public List<String> opTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Fwmain.getInstance().getLobbyList().forEach(e -> args.add(e.getName()));
        }
        if (para == 1) {
            Bukkit.getOnlinePlayers().forEach(e -> args.add(e.getName()));
        }
        return args;
    }
    @Override
    public List<String> consoleTab(int para,String[] paras) {
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Fwmain.getInstance().getLobbyList().forEach(e -> args.add(e.getName()));
        }
        if (para == 1) {
            Bukkit.getOnlinePlayers().forEach(e -> args.add(e.getName()));
        }
        return args;
    }

    @Override
    public String getPlayerPermission(String... args) {
        if(args.length >0 ) {
            return playerPermission + "." +args[0];
        }
        return playerPermission;
    }
}

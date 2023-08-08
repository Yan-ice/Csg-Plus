package org.csg.cmd.label;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.csg.cmd.Cmd;
import org.csg.cmd.RootCmd;
import org.csg.group.Lobby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Leave extends Cmd {
    public Leave(RootCmd rootCmd) {
        super(rootCmd);
        branch = "Leave";
        description = "离开当前游戏";
        playerPermission = "csg.Leave";
        opPermission = "";
        canPlayer = true;
        canOp = true;
        canConsole = true;
        playerParas = new ArrayList<>();
        opParas = Arrays.asList("[玩家]");
        consoleParas = Arrays.asList("玩家");
    }
    @Override
    public void player(Player player, String... args) {
        Lobby.AutoLeave(player,false);
    }
    @Override
    public void op(Player player, String... args) {
        if (args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                Lobby.AutoLeave(target,false);
            } else {
                player.sendMessage("玩家["+args[1]+"]不在线或不存在");
            }
        } else {
            Lobby.AutoLeave(player,false);
        }
    }
    @Override
    public void console(CommandSender sender, String... args) {
        if (args.length > 0) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                Lobby.AutoLeave(target,false);
            } else {
                sender.sendMessage("玩家["+args[1]+"]不在线或不存在");
            }
        } else {
            sender.sendMessage(consoleParas());
        }
    }
    @Override
    public List<String> playerTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        return args;
    }
    @Override
    public List<String> opTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        if (para == 0) {
            Bukkit.getOnlinePlayers().forEach(e -> args.add(e.getName()));
        }
        return args;
    }
    @Override
    public List<String> consoleTab(int para,String[] paras) {
        List<String> args = new ArrayList<>();
        if (para == 0) {
            Bukkit.getOnlinePlayers().forEach(e -> args.add(e.getName()));
        }
        return args;
    }
}

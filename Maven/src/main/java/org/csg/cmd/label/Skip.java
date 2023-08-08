package org.csg.cmd.label;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.csg.cmd.Cmd;
import org.csg.cmd.RootCmd;
import org.csg.group.Group;
import org.csg.group.Lobby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Skip extends Cmd {
    public Skip(RootCmd rootCmd) {
        super(rootCmd);
        branch = "Skip";
        description = "跳转队列";
        playerPermission = "csg.skip";
        opPermission = "";
        canPlayer = true;
        canOp = true;
        canConsole = true;
        playerParas = Arrays.asList("队列名");
        opParas = Arrays.asList("队列名", "[玩家]");
        consoleParas = Arrays.asList("队列名", "玩家");
    }
    @Override
    public void player(Player player, String... args) {
        if (args.length < 1) {
            player.sendMessage(playerParas());
        }
        Group g = Group.SearchPlayerInGroup(player);
        if(g!=null){
            Lobby l = g.getLobby();
            l.ChangeGroup(player, args[0]);
        }
    }
    @Override
    public void op(Player player, String... args) {
        if (args.length < 1) {
            player.sendMessage(opParas());
        } else if (args.length == 1) {
            Group g = Group.SearchPlayerInGroup(player);
            if(g!=null){
                Lobby l = g.getLobby();
                l.ChangeGroup(player, args[0]);
            }
        } else {
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                Group g = Group.SearchPlayerInGroup(target);
                if (g != null) {
                    Lobby l = g.getLobby();
                    l.ChangeGroup(target, args[0]);
                }
            } else {
                player.sendMessage("玩家["+args[1]+"]不在线或不存在");
            }
        }
    }
    @Override
    public void console(CommandSender sender, String... args) {
        if (args.length < 2) {
            sender.sendMessage(consoleParas());
        } else {
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                Group g = Group.SearchPlayerInGroup(target);
                if (g != null) {
                    Lobby l = g.getLobby();
                    l.ChangeGroup(target, args[0]);
                }
            } else {
                sender.sendMessage("玩家["+args[1]+"]不在线或不存在");
            }
        }
    }
    @Override
    public List<String> playerTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Group g = Group.SearchPlayerInGroup(player);
            if(g!=null) {
                Lobby l = g.getLobby();
                l.getGroupList().forEach(e -> args.add(e.getName()));
            }
        }
        return args;
    }
    @Override
    public List<String> opTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Group g = Group.SearchPlayerInGroup(player);
            if(g!=null) {
                Lobby l = g.getLobby();
                l.getGroupList().forEach(e -> args.add(e.getName()));
            }
        }else if(para == 1) {
            Bukkit.getOnlinePlayers().forEach(e -> args.add(e.getName()));
        }
        return args;
    }
    @Override
    public List<String> consoleTab(int para,String[] paras) {
        List<String> args = new ArrayList<>();
        if(para == 1) {
            Bukkit.getOnlinePlayers().forEach(e -> args.add(e.getName()));
        }
        return args;
    }
}

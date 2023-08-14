package org.csg.cmd.label;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.csg.Fwmain;
import org.csg.cmd.Cmd;
import org.csg.cmd.RootCmd;
import org.csg.group.Lobby;
import org.csg.sproom.TemporaryWorldManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Seril extends Cmd {
    public Seril(RootCmd rootCmd) {
        super(rootCmd);
        branch = "Seril";
        description = "制作独立副本";
        playerPermission = "csg.Seril";
        opPermission = "";
        canPlayer = true;
        canOp = true;
        canConsole = true;
        playerParas = Arrays.asList("游戏名 世界名");
        opParas = playerParas;
        consoleParas = playerParas;
    }
    @Override
    public void player(Player player, String... args) {
        player.sendMessage("您没有权限！");
    }
    @Override
    public void op(Player player, String... args){
        console(player,args);
    }
    @Override
    public void console(CommandSender sender, String... args) {
        if(args.length != 2){
            sender.sendMessage("参数异常！");
            return;
        }

        // 创建世界
        new TemporaryWorldManager(Fwmain.getInstance())
                .createTemporaryWorldFromSource(args[1], Lobby.getLobby(args[0]), Arrays.asList((Player) sender));
    }
    @Override
    public List<String> playerTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Fwmain.lobbyList.forEach(e -> args.add(e.getName()));
        }
        return args;
    }
    @Override
    public List<String> opTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Fwmain.lobbyList.forEach(e -> args.add(e.getName()));
        }
        return args;
    }
    @Override
    public List<String> consoleTab(int para,String[] paras) {
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Fwmain.lobbyList.forEach(e -> args.add(e.getName()));
        }
        return args;
    }
}

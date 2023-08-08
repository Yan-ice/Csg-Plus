package org.csg.cmd.label;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.csg.Fwmain;
import org.csg.cmd.Cmd;
import org.csg.cmd.RootCmd;
import org.csg.group.Lobby;

import java.util.ArrayList;
import java.util.List;

public class Stop extends Cmd{
    public Stop(RootCmd rootCmd) {
        super(rootCmd);
        branch = "Stop";
        description = "关闭所有游戏";
        playerPermission = "csg.Stop";
        opPermission = "";
        canPlayer = true;
        canOp = true;
        canConsole = true;
        playerParas = new ArrayList<>();
        opParas = new ArrayList<>();
        consoleParas = new ArrayList<>();
    }
    @Override
    public void player(Player player, String... args) {
        console(player,args);
    }
    @Override
    public void op(Player player, String... args) {
        console(player,args);
    }
    @Override
    public void console(CommandSender sender, String... args) {
        sender.sendMessage(ChatColor.BLUE+"安全关闭所有游戏！");
        for(Lobby l : Fwmain.getInstance().getLobbyList()){
            l.Clear();
        }
        Fwmain.getInstance().getLobbyList().clear();
    }
    @Override
    public List<String> playerTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        return args;
    }
    @Override
    public List<String> opTab(int para, String[] paras, Player player){
        List<String> args = new ArrayList<>();
        return args;
    }
    @Override
    public List<String> consoleTab(int para,String[] paras) {
        List<String> args = new ArrayList<>();
        return args;
    }

}

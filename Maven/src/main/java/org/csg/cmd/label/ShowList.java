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

public class ShowList extends Cmd {
    public ShowList(RootCmd rootCmd) {
        super(rootCmd);
        branch = "ShowList";
        description = "显示游戏列表";
        playerPermission = "csg.List";
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
        console(player, args);
    }
    @Override
    public void op(Player player, String... args) {
        console(player, args);
    }
    @Override
    public void console(CommandSender sender, String... args) {
        StringBuilder Glist = new StringBuilder();
        List<Lobby> Lobbylist = Fwmain.getInstance().getLobbyList();
        for (Lobby l : Lobbylist) {
            String Name;
            if (l.isComplete()) {
                Name = ChatColor.GREEN + l.getName() + ChatColor.AQUA;
            } else {
                Name = ChatColor.RED + l.getName() + ChatColor.AQUA;
            }
            if (Glist.length()>0) {
                Glist.append(", ").append(Name);
            } else {
                Glist.append(Name);
            }
        }
        sender.sendMessage(ChatColor.AQUA + "当前游戏列表:");
        sender.sendMessage(Glist.toString());
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

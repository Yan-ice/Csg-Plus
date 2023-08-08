package org.csg.cmd.label;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.csg.cmd.Cmd;
import org.csg.cmd.RootCmd;

import java.util.ArrayList;
import java.util.List;

public class Reload extends Cmd {

    public Reload(RootCmd rootCmd) {
        super(rootCmd);
        branch = "Reload";
        description = "重载插件";
        playerPermission = "csg.Reload";
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
        Data.fmain.Reload(player);
    }
    @Override
    public void op(Player player, String... args) {
        Data.fmain.Reload(player);
    }
    @Override
    public void console(CommandSender sender, String... args) {
        Data.fmain.Reload(sender);
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

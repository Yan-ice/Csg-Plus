package org.csg.cmd;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class Cmd {
    protected RootCmd rootCmd;
    protected String branch;
    protected String description;

    protected Cmd(RootCmd rootCmd){
        this.rootCmd = rootCmd;
    }

    public void handle(CommandSender sender,String... args){
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(canOp&&(player.hasPermission(opPermission)||player.isOp())){
                op(player,args);
            }else if(canPlayer&&player.hasPermission(getPlayerPermission(args))){
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


    protected boolean canPlayer;
    protected String playerPermission;
    public String getPlayerPermission(String... args) {
        return playerPermission;
    }
    protected List<String> playerParas;
    public String playerParas(){
        StringBuilder sb = new StringBuilder()
                .append("/")
                .append(rootCmd.root)
                .append(" ")
                .append(branch);
        playerParas.forEach(e -> sb.append(" ").append(e));
        return sb.toString();
    }
    abstract protected void player(Player player,String... args);
    abstract protected List<String> playerTab(int para,String[] args,Player player);

    protected boolean canOp;
    protected String opPermission;

    public String getOpPermission(String... args) {
        return opPermission;
    }
    protected List<String> opParas;
    public String opParas(){
        StringBuilder sb = new StringBuilder()
                .append("/")
                .append(rootCmd.root)
                .append(" ")
                .append(branch);
        opParas.forEach(e -> sb.append(" ").append(e));
        return sb.toString();
    }
    abstract protected void op(Player player,String... args);
    abstract protected List<String> opTab(int para,String[] args,Player player);

    protected boolean canConsole;
    protected List<String> consoleParas;
    public String consoleParas(){
        StringBuilder sb = new StringBuilder()
                .append("/")
                .append(rootCmd.root)
                .append(" ")
                .append(branch);
        consoleParas.forEach(e -> sb.append(" ").append(e));
        return sb.toString();
    }
    abstract protected void console(CommandSender sender,String... args);
    abstract protected List<String> consoleTab(int para,String[] args);
}

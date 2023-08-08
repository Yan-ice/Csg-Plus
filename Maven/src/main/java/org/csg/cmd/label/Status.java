package org.csg.cmd.label;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.csg.Fwmain;
import org.csg.cmd.Cmd;
import org.csg.cmd.RootCmd;
import org.csg.group.Group;
import org.csg.group.Lobby;
import org.csg.sproom.Reflect;
import org.csg.sproom.Room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Status extends Cmd {
    public Status(RootCmd rootCmd) {
        super(rootCmd);
        branch = "Status";
        description = "查询游戏状态";
        playerPermission = "csg.Status";
        opPermission = "";
        canPlayer = true;
        canOp = true;
        canConsole = true;
        playerParas = Arrays.asList("游戏名");
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
        if (args.length < 1) {
            sender.sendMessage(consoleParas());
            return;
        }
        Lobby lobby = Lobby.getLobby(args[0]);
        if(lobby==null){
            sender.sendMessage("游戏["+args[0]+"]不存在");
            return;
        }
        if(lobby.isSpRoom()){
            Room r = Room.searchRoom(args[0]);
            sender.sendMessage(ChatColor.YELLOW+"房间名： "+ChatColor.AQUA+r.getName()+ChatColor.YELLOW+"  游戏进行个数： "+r.allreflects.size()+"/"+r.getMaxReflect());
            for(Reflect rf : r.allreflects){
                if(rf!=null){
                    sender.sendMessage(ChatColor.YELLOW+"  副本镜像"+rf.getId()+"： ");
                    String pli = "";
                    switch(rf.getStatu()){
                        case WAITING:
                            sender.sendMessage("    游戏状态： "+ChatColor.GREEN+"等待中！");
                            for(java.util.UUID p : rf.getLobby().getPlayerList()){
                                pli = pli+ Bukkit.getPlayer(p).getName()+" ";
                            }
                            sender.sendMessage("    游玩玩家："+pli);
                            break;
                        case STARTED:
                            sender.sendMessage("    游戏状态： "+ChatColor.RED+"游戏中");
                            for(java.util.UUID p : rf.getLobby().getPlayerList()){
                                pli = pli+Bukkit.getPlayer(p).getName()+" ";
                            }
                            sender.sendMessage("    游玩玩家："+pli);
                            break;
                        case PREPARING:
                            sender.sendMessage("    游戏状态： "+ChatColor.RED+"正在加载");
                            break;
                        case ENDED:
                            sender.sendMessage("    处于已卸载状态，随时等待重新启用。");
                            break;
                        case UNLOADING:
                            sender.sendMessage("    游戏已结束，等待所有人离开世界将卸载。");
                            break;
                    }
                }
            }
        }else{
            sender.sendMessage(ChatColor.BLUE+lobby.getName()+" :");
            sender.sendMessage(ChatColor.GREEN+"默认队列："+lobby.getDefaultGroup().getName());
            for(Group gro : lobby.getGroupListI()){
                gro.state(sender);
            }
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
        return args;
    }
        @Override
        public List<String> consoleTab(int para,String[] paras) {
        List<String> args = new ArrayList<>();
        if(para == 0) {
            Fwmain.getInstance().getLobbyList().forEach(e -> args.add(e.getName()));
        }
        return args;
    }
}

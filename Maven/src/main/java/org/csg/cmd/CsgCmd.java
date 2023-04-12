package org.csg.cmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.csg.Data;
import org.csg.Fwmain;
import org.csg.Upgrader;
import org.csg.group.Group;
import org.csg.group.Lobby;
import org.csg.sproom.Reflect;
import org.csg.sproom.Room;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CsgCmd extends RootCmd{
    public CsgCmd(PluginCommand pluginCmd) {
        super(pluginCmd);
        root = pluginCmd.getName();
        branchs = new ArrayList<>();
        branchs.addAll(Arrays.asList(
                new Skip(this),
                new Item(this),
                new KillAll(this),
                new ShowList(this),
                new UUID(this),
                new Debug(this),
                new Reload(this),
                new Leave(this),
                new Stop(this),
                new Load(this),
                new Unload(this),
                new Join(this),
                new Status(this),
                new Trigger(this),
                new Seril(this),
                new UpgradeConfig(this)
        ));
    }

    static class Skip extends Cmd{
        Skip(RootCmd rootCmd) {
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
    static class Item extends Cmd{
        Item(RootCmd rootCmd) {
            super(rootCmd);
            branch = "Item";
            description = "问千千这有啥用";
            playerPermission = "csg.item";
            opPermission = "";
            canPlayer = false;
            canOp = false;
            canConsole = false;
            playerParas = new ArrayList<>();
            opParas = new ArrayList<>();
            consoleParas = new ArrayList<>();
        }
        @Override
        public void player(Player player, String... args) {
        }
        @Override
        public void op(Player player, String... args) {
        }
        @Override
        public void console(CommandSender sender, String... args) {
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

    static class KillAll extends Cmd{
        KillAll(RootCmd rootCmd) {
            super(rootCmd);
            branch = "KillAll";
            description = "做掉半径300格内的活物";
            playerPermission = "csg.KillAll";
            opPermission = "";
            canPlayer = true;
            canOp = true;
            canConsole = false;
            playerParas = new ArrayList<>();
            opParas = new ArrayList<>();
            consoleParas = new ArrayList<>();
        }
        @Override
        public void player(Player player, String... args) {
            for(Entity e : player.getNearbyEntities(300, 150, 300)){
                if(!(e instanceof LivingEntity)){
                    continue;
                }
                LivingEntity en = (LivingEntity) e;
                if(!en.isDead()){
                    en.remove();
                }
            }
        }
        @Override
        public void op(Player player, String... args) {
            for(Entity e : player.getNearbyEntities(300, 150, 300)){
                if(!(e instanceof LivingEntity)){
                    continue;
                }
                LivingEntity en = (LivingEntity) e;
                if(!en.isDead()){
                    en.remove();
                }
            }
        }
        @Override
        public void console(CommandSender sender, String... args) {
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
    static class ShowList extends Cmd{
        ShowList(RootCmd rootCmd) {
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
            List<Lobby> Lobbylist = Lobby.getLobbyList();
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

    static class UpgradeConfig extends Cmd{
        UpgradeConfig(RootCmd rootCmd) {
            super(rootCmd);
            branch = "Upgrade";
            description = "升级脚本";
            playerPermission = "csg.Upgrade";
            opPermission = "";
            canPlayer = false;
            canOp = true;
            canConsole = true;
            playerParas = new ArrayList<>();
            opParas = new ArrayList<>();
            consoleParas = new ArrayList<>();
        }
        @Override
        public void player(Player player, String... args) {
            player.sendMessage("请在控制台运行该指令！");
        }
        @Override
        public void op(Player player, String... args) {
            player.sendMessage("请在控制台运行该指令！");
        }
        @Override
        public void console(CommandSender sender, String... args) {
            Upgrader.upgrade(sender);
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

    static class UUID extends Cmd{

        UUID(RootCmd rootCmd) {
            super(rootCmd);
            branch = "UUID";
            description = "切换UUID查看模式";
            playerPermission = "csg.UUID";
            opPermission = "";
            canPlayer = true;
            canOp = true;
            canConsole = false;
            playerParas = new ArrayList<>();
            opParas = new ArrayList<>();
            consoleParas = new ArrayList<>();
        }
        @Override
        public void player(Player player, String... args) {
            if(Data.fmain.getPlist().contains(player)) {
                Data.fmain.getPlist().remove(player);
                player.sendMessage("您退出了UUID查看模式。");
            }else {
                player.sendMessage("您正在UUID查看模式：攻击一个生物来查看它的UUID。");
                Data.fmain.getPlist().add(player);
            }
        }
        @Override
        public void op(Player player, String... args) {
            player(player, args);
        }
        @Override
        public void console(CommandSender sender, String... args) {
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
    static class Debug extends Cmd{

        Debug(RootCmd rootCmd) {
            super(rootCmd);
            branch = "Debug";
            description = "切换调试模式";
            playerPermission = "csg.admin";
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
            if(Data.debug){
                Data.debug = false;
                sender.sendMessage("测试模式已关闭。");
            }else{
                Data.debug = true;
                sender.sendMessage("测试模式已开启。");
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
            return args;
        }
        @Override
        public List<String> consoleTab(int para,String[] paras) {
            List<String> args = new ArrayList<>();
            return args;
        }
    }
    static class Reload extends Cmd{

        Reload(RootCmd rootCmd) {
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
    static class Leave extends Cmd{

        Leave(RootCmd rootCmd) {
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
    static class Stop extends Cmd{
        Stop(RootCmd rootCmd) {
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
            for(Lobby l : Lobby.getLobbyList()){
                l.Clear();
            }
            Lobby.getLobbyList().clear();
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
    //
    //  游戏单独
    //
    static class Load extends Cmd{
        Load(RootCmd rootCmd) {
            super(rootCmd);
            branch = "Load";
            description = "重载游戏";
            playerPermission = "csg.Load";
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
            console(player,args);
        }
        @Override
        public void op(Player player, String... args) {
            console(player,args);
        }
        @Override
        public void console(CommandSender sender, String... args) {
            if (args.length > 0) {
                Lobby lobby = Lobby.getLobby(args[0]);
                if (lobby != null) {
                    sender.sendMessage("重载游戏 " + args[0] + " ！");
                    lobby.load();
                } else {
                    sender.sendMessage("游戏["+args[0]+"]不存在");
                }
            } else {
                sender.sendMessage(consoleParas());
            }
        }
        @Override
        public List<String> playerTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> opTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> consoleTab(int para,String[] paras) {
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
    }
    static class Unload extends Cmd{
        Unload(RootCmd rootCmd) {
            super(rootCmd);
            branch = "Unload";
            description = "卸载游戏";
            playerPermission = "csg.Unload";
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
            console(player,args);
        }
        @Override
        public void op(Player player, String... args) {
            console(player,args);
        }
        @Override
        public void console(CommandSender sender, String... args) {
            if (args.length > 0) {
                Lobby lobby = Lobby.getLobby(args[0]);
                if (lobby != null) {
                    sender.sendMessage("卸载游戏 " + args[0] + " ！");
                    lobby.unLoad();
                } else {
                    sender.sendMessage("游戏["+args[0]+"]不存在");
                }
            } else {
                sender.sendMessage(consoleParas());
            }
        }
        @Override
        public List<String> playerTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> opTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> consoleTab(int para,String[] paras) {
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
    }
    static class Join extends Cmd{
        Join(RootCmd rootCmd) {
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
            if(lobby.isSpRoom()){
                Room.searchRoom(args[0]).JoinRoom(player);
            }else{
                lobby.Join(player);
            }
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
                if(lobby.isSpRoom()){
                    Room.searchRoom(args[0]).JoinRoom(target);
                }else{
                    lobby.Join(target);
                }
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
                    if(lobby.isSpRoom()){
                        Room.searchRoom(args[0]).JoinRoom(target);
                    }else{
                        lobby.Join(target);
                    }
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
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> opTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
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
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
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
    static class Status extends Cmd{
        Status(RootCmd rootCmd) {
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
                                    pli = pli+Bukkit.getPlayer(p).getName()+" ";
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
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> opTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> consoleTab(int para,String[] paras) {
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
    }
    static class Trigger extends Cmd{
        Trigger(RootCmd rootCmd) {
            super(rootCmd);
            branch = "Trigger";
            description = "查询游戏状态";
            playerPermission = "csg.Trigger";
            opPermission = "";
            canPlayer = true;
            canOp = true;
            canConsole = true;
            playerParas = Arrays.asList("游戏名", "函数名", "[触发者]");
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
                Player striker = null;
                if(args.length>3){
                    striker = Bukkit.getPlayer(args[3]);
                }
                if(striker==null && sender instanceof Player){
                    striker = (Player)sender;
                }
                Lobby lobby = Lobby.getLobby(args[0]);
                if (lobby != null) {
                    lobby.callListener(args[2],striker);

                } else {
                    sender.sendMessage("游戏["+args[0]+"]不存在");
                }
            }else {
                sender.sendMessage(consoleParas());
            }
        }
        @Override
        public List<String> playerTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            if (para == 2) {
                Bukkit.getOnlinePlayers().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> opTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            if (para == 2) {
                Bukkit.getOnlinePlayers().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> consoleTab(int para,String[] paras) {
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            if (para == 2) {
                Bukkit.getOnlinePlayers().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
    }
    static class Seril extends Cmd{
        Seril(RootCmd rootCmd) {
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
                if(args.length<2){
                    sender.sendMessage("参数不足！");
                    return;
                }
                Room.serilizeLobby(sender,args[0],args[1]);
        }
        @Override
        public List<String> playerTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> opTab(int para, String[] paras, Player player){
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
        @Override
        public List<String> consoleTab(int para,String[] paras) {
            List<String> args = new ArrayList<>();
            if(para == 0) {
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
            }
            return args;
        }
    }

    static class SetMacro extends Cmd{
        SetMacro(RootCmd rootCmd) {
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
                            sender.sendMessage(String.format("%s成功将队列%s%s%s中宏%s%s%s的值设为%s%s",ChatColor.GREEN,ChatColor.AQUA,args[0],ChatColor.GREEN,ChatColor.AQUA,args[1],ChatColor.GREEN,ChatColor.AQUA,args[2]));
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
                Lobby.getLobbyList().forEach(e -> args.add(e.getName()));
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

}


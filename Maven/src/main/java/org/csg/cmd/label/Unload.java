package org.csg.cmd.label;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.csg.Fwmain;
import org.csg.cmd.Cmd;
import org.csg.cmd.RootCmd;
import org.csg.group.Lobby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Unload extends Cmd {
        public Unload(RootCmd rootCmd) {
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

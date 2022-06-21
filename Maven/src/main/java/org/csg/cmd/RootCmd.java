package org.csg.cmd;

import lombok.Getter;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 根指令
 * @Author Takamina
 */
public abstract class RootCmd implements CommandExecutor, TabCompleter {
    static final ExecutorService executor = Executors.newFixedThreadPool(2);
    String root;

    public RootCmd(PluginCommand pluginCmd){
        pluginCmd.setTabCompleter(this);
        pluginCmd.setExecutor(this);
    }

    @Override
    public  List<String> onTabComplete(CommandSender commandSender,  Command command,  String s,  String[] strings){
        Future<List<String>> result = executor.submit(() -> {
            boolean[] isCmd = new boolean[]{command.getName().equalsIgnoreCase(root)};
            command.getAliases().forEach(e -> isCmd[0] = (isCmd[0] || e.equalsIgnoreCase(root)));
            List<String> tabs = new ArrayList<>();
            if (!isCmd[0]) {
                return tabs;
            }
            if (strings.length == 1) {
                branchs.forEach(e -> {
                    if (commandSender instanceof Player) {
                        if ((e.canOp && (commandSender.isOp() || commandSender.hasPermission(e.getOpPermission(strings)))) || (e.canPlayer) && commandSender.hasPermission(e.getPlayerPermission(strings))) {
                            if(e.branch.toLowerCase().startsWith(strings[0].toLowerCase())){
                                tabs.add(e.branch);
                            }
                        }
                    } else if (e.canConsole) {
                        if(e.branch.toLowerCase().startsWith(strings[0].toLowerCase())){
                            tabs.add(e.branch);
                        }
                    }
                });
            } else {
                Cmd branch = null;
                for (Cmd b : branchs) {
                    if (b.branch.equalsIgnoreCase(strings[0])) {
                        branch = b;
                        break;
                    }
                }
                if (branch == null) {
                    return tabs;
                }
                //
                int arg = strings.length - 2;
                String[] paras = new String[strings.length-1];
                System.arraycopy(strings,1,paras,0,paras.length);
                if (commandSender instanceof Player) {
                    if (!(arg >= branch.opParas.size()) && branch.canOp && (commandSender.isOp() || commandSender.hasPermission(branch.getOpPermission(paras)))) {
                        branch.opTab(arg,strings, (Player) commandSender).stream().filter(e -> e.toLowerCase().startsWith(paras[arg].toLowerCase())).forEach(tabs::add);
                    } else if (!(arg >= branch.playerParas.size()) && (branch.canPlayer && commandSender.hasPermission(branch.getPlayerPermission(paras)))) {
                        branch.playerTab(arg,strings, (Player) commandSender).stream().filter(e -> e.toLowerCase().startsWith(paras[arg].toLowerCase())).forEach(tabs::add);
                    }
                } else if (!(arg >= branch.consoleParas.size()) && branch.canConsole) {
                    branch.consoleTab(arg, strings).stream().filter(e -> e.toLowerCase().startsWith(paras[arg].toLowerCase())).forEach(tabs::add);
                }
            }
            return tabs;
        });
        try{
            return result.get();
        }catch (InterruptedException | ExecutionException err){
            err.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean onCommand( CommandSender commandSender,  Command command,  String s,  String[] strings){
        boolean[] isCmd = new boolean[]{command.getName().equalsIgnoreCase(root)};
        command.getAliases().forEach(e -> isCmd[0] = (isCmd[0]||e.equalsIgnoreCase(root)));
        if(isCmd[0]){
            run(commandSender,strings);
        }
        return false;
    }

    @Getter
    List<Cmd> branchs;
    public void run(CommandSender sender,String... args){
        if (args.length > 0) {
            for (Cmd cmd : branchs) {
                if (cmd.branch.equalsIgnoreCase(args[0])) {
                    String[] argss = new String[args.length - 1];
                    for (int i = 1; i < args.length; i++) {
                        argss[i - 1] = args[i];
                    }
                    cmd.handle(sender, argss);
                    return;
                }
            }
        }
        sender.sendMessage(help(sender));
    }

    public String help(CommandSender sender){
        StringBuilder sb = new StringBuilder()
                .append("命令帮助: ")
                .append(root)
                .append("------------------\n");
        branchs.forEach(e ->{
            if(sender instanceof Player){
                if(e.canOp&&(sender.isOp()||sender.hasPermission(e.getOpPermission()))){
                    sb
                            .append(" ")
                            .append(e.opParas())
                            .append("\n  ")
                            .append(e.description)
                            .append("\n");
                }else if(e.canPlayer&&sender.hasPermission(e.getPlayerPermission())){
                    sb
                            .append(" ")
                            .append(e.playerParas())
                            .append("\n  ")
                            .append(e.description)
                            .append("\n");
                }
            }else if(e.canConsole) {
                    sb
                            .append(" ")
                            .append(e.consoleParas())
                            .append("\n  ")
                            .append(e.description)
                            .append("\n");
            }
        });
        sb.append("-------------------------------------");
        return sb.toString();
    }
}

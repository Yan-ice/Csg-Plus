package org.csg.group.task.csgtask;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.csg.Data;
import org.csg.group.Group;
import org.csg.group.Lobby;
import org.csg.group.task.ItemCheck;
import org.csg.group.task.toolkit.Calculator;
import org.csg.group.task.toolkit.TaskExecuter;

import org.csg.location.FArena;
import org.csg.location.Teleporter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

import java.util.*;

public class CommandTask extends Task {
    String return_value = null;

    String command;
    String arg;

    TargetType target_type;
    String target_filter;
    public CommandTask(String command,String arg,String key) throws TaskSyntaxError{

        try{
            if(command.contains("<-")){
                return_value = command.split("<-")[0].trim();
                this.command = command.split("<-")[1].trim();
            }else{
                this.command = command.trim();
            }
            //读取参数
            this.arg = arg;

            String tg;
            if(key.contains("[")) {
                tg = key.split("\\[")[0].trim();
                target_filter = key.split("\\[")[1].split("]")[0];
            }else{
                tg = key.trim();
            }
            switch (tg) {
                case "@a":
                    target_type = TargetType.Group;
                    break;
                case "@p":
                    target_type = TargetType.Striker;
                    break;
                case "@r":
                    target_type = TargetType.Random;
                    break;
                case "@e":
                    target_type = TargetType.Lobby;
                    break;
                case "@t":
                    target_type = TargetType.Striker_force;
                    break;
                case "@l":
                    target_type = TargetType.Lobby;
                    break;
                case "@g":
                    target_type = TargetType.Group;
                    break;
                case "@server":
                    target_type = TargetType.Server;
                    break;
                default:
                    target_type = TargetType.None;
                    break;
            }
        }catch(Exception e){
            throw new TaskSyntaxError();
        }

    }

    @Override
    public Task execute(TaskExecuter executer, UUID striker) {

        List<UUID> players = new ArrayList<>();
        switch(target_type){
            case Group:
                players.addAll(executer.getField());
                break;
            case Striker:
                if(striker!=null && executer.getField().contains(striker)){
                    players.add(striker);
                }

                break;
            case Random:
                players.addAll(executer.getField());
                if(players.size()>0){
                    int size = players.size();
                    UUID p = players.get(Data.Random(0,size));
                    players.clear();
                    players.add(p);
                }

                break;
            case Lobby:
                players.addAll(executer.lobby.getPlayerList());
                break;
            case Server:
                for(Player p : Bukkit.getOnlinePlayers()){
                    players.add(p.getUniqueId());
                }
                break;
            default:
                break;
        }
        try{
            if(players.size()>0){
                boolean runned = false;
                for(UUID p : players){
                    if(Bukkit.getPlayer(p)!=null){
                        RunTask(executer,Bukkit.getPlayer(p));
                        runned = true;
                    }
                }
                if(!runned){
                    RunTask(executer, null);
                }
            }else{
                RunTask(executer, null);
            }

        }catch(Exception e){
            Data.ConsoleInfo("执行任务"+command+"时出现错误：");
            e.printStackTrace();
        }

        return next;
    }


    private boolean RunTask(TaskExecuter executer, Player Target) throws NullPointerException,NumberFormatException,IndexOutOfBoundsException{

        Object return_obj = null;

        String label = command;
        List<UUID> playerList = executer.getField();

        executer.addVariable("player",Target);
        String cloned_arg = executer.variableReplace(this.variables,arg,Target);
        String cloned_return = return_value!=null ? executer.variableReplace(this.variables,return_value,Target):null;

        String[] args;
        if(cloned_arg.contains(",")){
            args = cloned_arg.split(",");
        }else{
            if(arg.length()==0){
                args = new String[0];
            }else{
                args = new String[1];
                args[0] = cloned_arg;
            }
        }

        //对参数整理，不让其带空格。
        for(int a = 0;a<args.length;a++){
            args[a] = args[a].trim();
        }

        World autoworld = null;
        if(playerList.size()>0){
            if(Bukkit.getPlayer(playerList.get(0))!=null){
                autoworld = Bukkit.getPlayer(playerList.get(0)).getWorld();
            }else{
                Data.ConsoleInfo("游戏内出现了异常的玩家数据！");
            }

        }
        switch (label) {

            case "leave":
                if (Target == null) {
                    return false;
                }
                Lobby.AutoLeave(Target,false);
                break;
            case "command":
                if (args.length==0) {
                    return false;
                }
                CommandRunner(cloned_arg, Target);
                break;
            case "consolecommand":
                if (args.length==0) {
                    return false;
                }
                Data.ConsoleCommand(cloned_arg);
                break;
            case "random":
                if(args.length<2){
                    return false;
                }
                return_obj = Data.Random(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
                break;
            case "spawnpoint":
                if(Target==null){
                    return false;
                }
                Location l = null;
                if(args.length!=0){
                    l = Teleporter.stringToLoc(args[0]);
                }
                if(l==null){
                    l = Target.getLocation();
                }
                executer.lobby.setSpawn(Target,l);
                break;
            case "say":
                if (args.length==0 || Target==null) {
                    return false;
                }
                Target.chat(cloned_arg);
                break;
            case "tell":
                if (args.length==0 || Target == null) {
                    return false;
                }
                Target.sendMessage(cloned_arg);
                break;
            case "title":
                if (args.length==0 || Target == null) {
                    return false;
                }
                if(args.length>=5) {
                    Target.sendTitle(args[0],args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]));
                }else if(args.length==1){
                    Target.sendTitle(args[0],"");
                }else{
                    Target.sendTitle(args[0],args[1]);
                }
                break;
            case "delay":
                if (args.length==0) {
                    return false;
                }
                try {
                    executer.setCooldown(Integer.parseInt(args[0]));
                } catch (NumberFormatException a) {
                    return false;
                }
                break;
            case "divide":
                if(Target==null){
                    return false;
                }
                return_obj = Target;
            case "setproperty":
                if (Target == null) {
                    return false;
                }
                switch(args[0]){
                    case "maxhealth":
                        Target.setMaxHealth(Double.parseDouble(args[1]));
                        break;
                    case "health":
                        Target.setHealth(Double.parseDouble(args[1]));
                        break;
                    case "speed":
                        Target.setWalkSpeed(Float.parseFloat(args[1]));
                        Target.setFlySpeed(Float.parseFloat(args[1]));
                        break;
                    case "food":
                        Target.setFoodLevel(Integer.parseInt(args[1]));
                        break;
                }
                break;
            case "damage":
                try {
                    Damage(Target, Double.parseDouble(args[0]));
                } catch (NumberFormatException a) {
                    return false;
                }
                break;
            case "teleport":
                Teleport(Target, args[0],autoworld);
                break;
            case "setblock":
                Location loc = Teleporter.stringToLoc(args[1],autoworld);
                loc.getWorld().getBlockAt(loc).setType(Material.getMaterial(args[0]));
                break;

            case "notice":
                for(Group g : executer.lobby.getGroupListI()){
                    g.sendNotice(args[0]);
                }
                break;
            case "var":
                if(args.length>1){
                    return_obj = args[1];
                    executer.addVariable(args[0], args[1]);
                }else{
                    return_obj = args[0];
                }
                break;
            case "varnum":
                if(args.length>1){
                    return_obj = Calculator.Calculate(args[1]);
                    executer.addVariable(args[0], return_obj);
                }else{
                    return_obj = Calculator.Calculate(args[0]);
                }

                break;
            case "macro":
                executer.lobby.macros.AddMacro(args[0], args[1]);
                break;
            case "setscore":
                //为了兼容性
                if(args[1].startsWith("+")){
                    args[1] = "~"+args[1];
                }
                if(Target!=null){
                    Object score_obj = executer.lobby.macros.getValue(Target,args[0]);
                    double score = 0;
                    if(score_obj instanceof Double){
                        score = (Double) score_obj;
                    }
                    if(score_obj instanceof Integer){
                        score = (int) score_obj;
                    }

                    args[1] = args[1].replace("~",String.format("%.5f",score));
                    score = Calculator.Calculate(args[1]);
                    executer.lobby.macros.AddScore(Target, args[0], score);
                }else{
                    Object score_obj = executer.lobby.macros.getValue(null,args[0]);
                    double score = 0;
                    if(score_obj instanceof Double){
                        score = (Double) score_obj;
                    }
                    if(score_obj instanceof Integer){
                        score = (int) score_obj;
                    }
                    args[1] = args[1].replace("~",String.format("%.5f",score));
                    score = Calculator.Calculate(args[1]);
                    executer.lobby.macros.AddMacro(args[0], score);
                }

                break;
            case "getscore":
                return_obj = executer.lobby.macros.getValue(Target,args[0]);
                break;
            case "log":
                Data.ConsoleInfo("[log "+executer.lobby.getName()+"] "+args[0]);
                break;
            case "end":
                next = null;
                break;
            case "join":
                executer.lobby.ChangeGroup(Target, args[0]);
                return true;
            case "endwhenclear":
                if(args.length==0){
                    executer.endWhenClear=true;
                    break;
                }
                executer.endWhenClear=(args[0].equals("true"));
                break;
            case "endwhennotarget":
                if(args.length==0){
                    executer.endWhenNoTarget = true;
                }else{
                    executer.endWhenNoTarget = args[0].equals("true");
                }
                break;
            case "addhologram":
                Location lo = Teleporter.stringToLoc(args[0]);
                executer.lobby.hd.AddHologram(lo, args[1], args[2]);
                break;
            case "delhologram":
                executer.lobby.hd.DelHologram(args[0]);
                break;
            case "edithologram":
                executer.lobby.hd.EditHologram(args[0], args[1]);
                break;
            case "clearhologram":
                executer.lobby.hd.ClearHologram();
                break;
            case "spawnmob":
                String[] v = args[2].split(" ");
                if(v.length==3 && Target!=null){
                    Data.ConsoleCommand("mm m spawn "+args[0]+" "+args[1]+" "+autoworld.getName()+","+v[0]+","+v[1]+","+v[2]);
                }else if(v.length>=4){
                    Data.ConsoleCommand("mm m spawn "+args[0]+" "+args[1]+" "+v[3]+","+v[0]+","+v[1]+","+v[2]);
                }else{
                    return false;
                }

                break;
            case "item":
                switch(args[0]){
                    case "check":
                        return_obj = ItemCheck.itemCheck(Target,args[1]);
                        break;
                    case "cost":
                        return_obj = ItemCheck.itemConsume(Target,args[1]);
                }
                break;
            case "close":
                executer.lobby.setCanJoin(false);
                break;
            case "open":
                executer.lobby.setCanJoin(true);
                break;
            case "give":
                if(args.length==0 || Target==null){
                    return false;
                }
                Material m = Material.getMaterial(args[0].toUpperCase());
                if(m==null){
                    Data.ConsoleInfo("未知的材质 "+args[0]+" ?");
                    return false;
                }
                int count = 1;
                if(args.length>=2){
                    count = Integer.parseInt(args[1]);
                }
                ItemStack i = new ItemStack(m,count);
                ItemMeta meta = i.getItemMeta();
                if(args.length>2){
                    meta.setDisplayName(args[2]);
                }
                if(args.length>3){
                    List<String> lore = new ArrayList<String>();
                    if(args[3].contains("|")){
                        Collections.addAll(lore, args[3].split("\\|"));
                    }else{
                        lore.add(args[3]);
                    }
                    meta.setLore(lore);
                }
                i.setItemMeta(meta);
                Target.getInventory().addItem(i);
                break;
            case "effect":
                if(args.length<3 || Target==null){
                    return false;
                }
                PotionEffectType effType = PotionEffectType.getByName(args[0].toUpperCase());
                if(effType==null){
                    Data.ConsoleInfo("未知的药水效果 "+args[0]+" ?");
                    return false;
                }
                PotionEffect effect = new PotionEffect(effType,Integer.parseInt(args[1]),Integer.parseInt(args[2]));
                Target.addPotionEffect(effect);
                break;
            case "removemobs":

                FArena arena = new FArena(args[0],autoworld);
                if(arena.isComplete()){
                    List<LivingEntity> e;
                    if(arena.getWorld()!=null){
                        e = arena.getWorld().getLivingEntities();
                    }else{
                        e = Bukkit.getPlayer(playerList.get(0)).getWorld().getLivingEntities();
                    }
                    for(LivingEntity en : e){
                        if(!(en instanceof HumanEntity)
                                && !(en instanceof Villager)){
                            if(arena.inArea(en.getLocation())){
                                en.remove();
                            }
                        }
                    }
                }
                break;
            default:
                if(label.startsWith("on")){
                    executer.lobby.callListener(label,Target,args);
                }
                String[] replaced = new String[args.length];
                for(int a=0; a<args.length; a++) {
                    replaced[a] = executer.variableReplace(variables,args[a],Target);
                }
                return_obj = executer.lobby.callFunction(label,executer,Target,replaced);
        }
        if(cloned_return!=null){
            if(return_obj!=null){
                executer.addVariable(cloned_return,return_obj);
            }else{
                Data.ConsoleInfo(String.format("注意：企图将无返回值的指令%s赋值给变量！",command));
            }
        }
        return true;
    }


    private void CommandRunner(String command, Player Target) {

        if (Target != null) {
            if (Target.isOp()) {
                executeCommand(Target,command);
            } else {
                Target.setOp(true);
                executeCommand(Target,command);
                Target.setOp(false);
            }
        } else {
            Data.ConsoleCommand(command);
        }
    }

    private void executeCommand(CommandSender sender,String command){
        try{
            Bukkit.dispatchCommand(sender,command);
        }catch(Exception e){
            Data.ConsoleInfo("执行指令 "+command+" 时出现错误：");
            e.printStackTrace();
        }
    }

    private void Damage(Player player, double d) {
        player.setNoDamageTicks(0);
        player.damage(d,null);
    }

    private void Teleport(Player player, String location,World autoworld) {
        Teleporter tel = new Teleporter(player);
        Location Loc = Teleporter.stringToLoc(location);
        if(Loc!=null){
            tel.Teleport(Loc, true);
        }

    }

}
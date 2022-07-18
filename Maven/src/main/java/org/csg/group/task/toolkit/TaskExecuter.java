package org.csg.group.task.toolkit;

import org.csg.Data;
import org.csg.group.Group;
import org.csg.group.task.csgtask.Task;
import org.csg.location.Teleporter;
import org.csg.update.CycleUpdate;
import org.csg.update.MainCycle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;


public class TaskExecuter implements CycleUpdate {
    public Group group;
    public UUID striker;

    public boolean endWhenClear = false;

    public VariableBox variables = new VariableBox();

    int cooldown = 0;
    public Task begin;
    Task next;
    public TaskExecuter(Task begin, Group group){
        this.begin = begin;
        this.group = group;
    }

    int identity = 0;
    private String temp_id(){
        identity++;
        return "temp_"+identity;
    }
    public String variableReplace(String origin,Player target){
        int identity = 0;
        Map<String,Object> temp_object = new HashMap<String,Object>();
        try{
            temp_object.putAll(variables.values);
            temp_object.putAll(group.getLobby().macros.macros);
            temp_object.putAll(group.getLobby().ValueBoard().getValueList());
            if(target!=null){
                Map<String,Double> p = group.getLobby().PlayerValueBoard().getValueList(target);
                if(p!=null){
                    temp_object.putAll(p);
                }
            }
        }catch(Exception e){
            Data.ConsoleInfo("变量出现名字冲突！ ");
            e.printStackTrace();
        }
        origin = Data.ColorChange(origin);
        String origin_old = "";
        while(!origin.equals(origin_old)){
            origin_old = origin;
            Map<String,Object> to = new HashMap<String,Object>();
            for(Map.Entry<String,Object> s : temp_object.entrySet()){
                if(!origin.contains(String.format("$%s$", s.getKey()))){
                    continue;
                }
                if(origin.contains(String.format("$%s$.$", s.getKey()))){
                    to.put(s.getKey(),s.getValue());
                    continue;
                }
                if(s.getValue() instanceof LivingEntity){
                    LivingEntity en = (LivingEntity)s.getValue();

                    origin = origin.replace(String.format("$%s$.health", s.getKey()), "" + en.getHealth());
                    origin = origin.replace(String.format("$%s$.max_health", s.getKey()), "" + en.getMaxHealth());
                    origin = origin.replace(String.format("$%s$.name", s.getKey()), en.getName());

                    if(origin.contains(String.format("$%s$.location", s.getKey()))){
                        String temp = temp_id();
                        origin = origin.replace(String.format("$%s$.location", s.getKey()),String.format("$%s$",temp));
                        to.put(temp,en.getLocation());
                    }

                    if(s.getValue() instanceof Player){
                        Player pl = (Player)s.getValue();
                        origin = origin.replace(String.format("$%s$.level", s.getKey()), "" + pl.getLevel());
                        origin = origin.replace(String.format("$%s$.food", s.getKey()), "" + pl.getFoodLevel());
                        origin = origin.replace(String.format("$%s$.speed", s.getKey()), "" + pl.getWalkSpeed());
                        origin = origin.replace(String.format("$%s$.display", s.getKey()), pl.getDisplayName());
                    }
                    origin = origin.replace(String.format("$%s$", s.getKey()), en.getName());
                    continue;
                }
                if(s.getValue() instanceof Location){
                    Location loc = (Location)s.getValue();
                    origin = origin.replace(String.format("$%s$.x", s.getKey()), "" + loc.getX());
                    origin = origin.replace(String.format("$%s$.y", s.getKey()), "" + loc.getY());
                    origin = origin.replace(String.format("$%s$.z", s.getKey()), "" + loc.getZ());
                    origin = origin.replace(String.format("$%s$.world", s.getKey()), "" + loc.getWorld().getName());
                    origin = origin.replace(String.format("$%s$", s.getKey()), Teleporter.locToString(loc));
                    continue;
                }
                if(s.getValue() instanceof String[]){
                    String[] acc = (String[])s.getValue();
                    origin = origin.replace(String.format("$%s$.length", s.getKey()),""+acc.length);
                    try{
                        for(int a = 0;a<acc.length;a++){
                            String k = String.format("$%s$.%d", s.getKey(),a);
                            if(origin.contains(k)){
                                String temp = temp_id();
                                to.put(temp,acc[a]);
                                origin = origin.replace(k,String.format("$%s$",temp));
                            }

                        }
                    }catch (ArrayIndexOutOfBoundsException e){
                        Data.ConsoleInfo(String.format("解析变量 %s 时出现了数组越界！",String.format("$%s$", s.getKey())));
                    }
                    String k = String.format("$%s$.random", s.getKey());
                    if(origin.contains(k)){
                        String rd = acc[Data.Random(0,acc.length)];
                        origin = origin.replace(k,rd);
                    }
                    origin = origin.replace(String.format("$%s$", s.getKey()),Arrays.toString(acc));
                    continue;
                }
                if(s.getValue() instanceof Double){
                    double d = (Double)s.getValue();
                    String v;
                    if(d % 1 ==0){
                        v = String.valueOf((int)d);
                    }else{
                        v = String.format("%.1f",d);
                    }
                    origin = origin.replace(String.format("$%s$", s.getKey()),v);
                    continue;
                }
                if(true){
                    String str = s.getValue().toString();

                    if(origin.contains(String.format("$%s$.", s.getKey()))){
                        Player p = Bukkit.getPlayer(str);
                        if(p!=null){
                            to.put(s.getKey(),p);
                            continue;
                        }

                        Location l = Teleporter.stringToLoc(str);
                        if(l!=null){
                            to.put(s.getKey(),l);
                            continue;
                        }

                        if(str.startsWith("[") && str.endsWith("]")){
                            String c = str.substring(1,str.length()-1);
                            String[] ls;
                            if(c.contains(",")){
                                ls = c.split(",");
                            }else{
                                ls = new String[]{c};
                            }
                            to.put(s.getKey(),ls);
                            continue;
                        }
                    }
                    origin = origin.replace(String.format("$%s$", s.getKey()),str);

                }
            }
            temp_object.putAll(to);
            to.clear();
        }


        return origin;
    }
    public boolean If(Player target, String If) throws NullPointerException,NumberFormatException,IndexOutOfBoundsException{

        if(If.contains("AND")){
            return (If(target,If.split("AND")[0].trim()) & If(target,If.split("AND")[1].trim()));
        }
        if(If.contains("OR")){
            return (If(target,If.split("OR")[0].trim()) | If(target,If.split("OR")[1].trim()));
        }
        if(If.contains(">=")){
            String[] s = If.split(">=");
            try{
                return Double.parseDouble(s[0].trim()) >= Double.parseDouble(s[1].trim());
            }catch(NumberFormatException e){
                return s[0].contains(s[1].trim());
            }

        }else
        if(If.contains(">")){
            String[] s = If.split(">");
            try{
                return Double.parseDouble(s[0].trim()) > Double.parseDouble(s[1].trim());
            }catch(NumberFormatException e){
                return s[0].contains(s[1].trim()) && !s[0].equals(s[1]);
            }
        }

        if(If.contains("<=")){
            String[] s = If.split("<=");
            try{
                return Double.parseDouble(s[0].trim()) <= Double.parseDouble(s[1].trim());
            }catch(NumberFormatException e){
                return s[1].contains(s[0].trim());
            }
        }else if(If.contains("<")){
            String[] s = If.split("<");
            try{
                return Double.parseDouble(s[0].trim()) < Double.parseDouble(s[1].trim());
            }catch(NumberFormatException e){
                return s[1].contains(s[0].trim()) && !s[0].equals(s[1]);
            }
        }
        if(If.contains("!=")){
            String value = If.split("!=")[1].trim();
            switch(If.split("!=")[0].trim()){
                case "Permission":
                    if(target.hasPermission(value)){
                        return false;
                    }else{
                        return true;
                    }
                case "InGroup":
                    if(group.hasPlayer(target)){
                        return false;
                    }else{
                        return true;
                    }
                default:
                    if(If.split("!=")[0].trim().equals(If.split("!=")[1].trim())){
                        return false;
                    }else{
                        return true;
                    }
            }

        }else
        if(If.contains("==")){
            String value = If.split("==")[1].trim();
            switch(If.split("==")[0].trim()){
                case "Permission":
                    return target.hasPermission(value);
                case "InGroup":
                    return group.hasPlayer(target);
                default:
                    return If.split("==")[0].trim().equals(If.split("==")[1].trim());
            }
        }
        if(If.equals("true")){
            return true;
        }
        return false;
    }

    public void addVariable(String var,Object value){
        variables.declare(var,value);
    }

    public void execute(UUID striker){
        variables.declare("group_name",group.getName());
        variables.declare("group_display",group.GetDisplay());
        if(Bukkit.getPlayer(striker)!=null){
            variables.declare("striker",Bukkit.getPlayer(striker));
        }

        this.striker = striker;
        next = begin;
        MainCycle.registerCall(this);
        onUpdate();

    }

    private void updateVar(){
        variables.declare("group_player_amount",group.getPlayerList().size());
        variables.declare("lobby_player_amount",group.getLobby().getPlayerList().size());
    }

    public void setCooldown(int tick){
        cooldown = tick;
    }

    @Override
    public void onUpdate() {
        if(endWhenClear && group.isClear()){
            next = null;
        }
        if(cooldown>0){
            cooldown--;
            return;
        }

        if(next!=null){
            try{
                updateVar();
                while(cooldown==0 && next!=null){
                    next = next.execute(this,striker);
                }
            }catch(Exception e){
                next = null;
                Data.ConsoleError("在运行任务时出现错误！");
                e.printStackTrace();
            }
        }else{
            MainCycle.unRegisterCall(this);
        }
    }
}

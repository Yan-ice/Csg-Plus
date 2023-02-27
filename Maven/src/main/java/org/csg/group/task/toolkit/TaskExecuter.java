package org.csg.group.task.toolkit;

import org.csg.Data;
import org.csg.group.Group;
import org.csg.group.Lobby;
import org.csg.group.task.VarTable;
import org.csg.group.task.csgtask.Task;
import org.csg.location.Teleporter;
import org.csg.update.CycleUpdate;
import org.csg.update.MainCycle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TaskExecuter implements CycleUpdate {
    public UUID striker;
    public Lobby lobby;
    public boolean endWhenClear = false;
    public boolean endWhenNoTarget = false;

    int cooldown = 0;
    public Task begin;
    Task next;
    public TaskExecuter(Task begin, Lobby lobby){
        this.begin = begin;
        this.lobby = lobby;
    }

    public List<UUID> getField(){
        if(!begin.field.contains(",")){
            return lobby.getPlayerList();
        }
        List<UUID> l = new ArrayList<>();
        for(String s : begin.field.split(",,")){
            Group g = lobby.getGroup(s);
            if(g!=null){
                l.addAll(g.getPlayerList());
            }
        }
        return l;
    }
    int identity = 0;
    private String temp_id(){
        identity++;
        return "temp_"+identity;
    }
    public String variableReplace(List<String> keys, String origin, Player target){
        List<String> copy = new ArrayList<>(keys);
        int stop_cnt = 100;
        //System.out.println("-- replace begin: "+origin);
        while(copy.size()>0 && (stop_cnt-->0)){
            String current = copy.get(0);
            copy.remove(0);
            if(current.contains("{")){
                copy.add(current);
                continue;
            }
            //System.out.println("replacing: "+current);
            String ori = String.format("{%s}", current);
            String tar = VarTable.objToString(lobby.macros.getValue(target,current,this));
            origin = origin.replace(ori,tar);
            copy.replaceAll(s -> s.replace(ori, tar));
        }
        //System.out.println("-- replace end: "+origin);

        origin = Data.ColorChange(origin);
        String origin_old = "";
        while(!origin.equals(origin_old)){
            origin_old = origin;
            Pattern pt = Pattern.compile("\\$[A-Za-z0-9]+\\$");
            Matcher mch = pt.matcher(origin);

            Map<String,Object> to = new HashMap<String,Object>();
            while(mch.find()){
                String s = mch.group();
                Object value = lobby.macros.getValue(target,s);

                if(value instanceof LivingEntity){
                    LivingEntity en = (LivingEntity)value;

                    origin = origin.replace(String.format("$%s$.health", s), "" + en.getHealth());
                    origin = origin.replace(String.format("$%s$.max_health", s), "" + en.getMaxHealth());
                    origin = origin.replace(String.format("$%s$.name", s), en.getName());

                    if(origin.contains(String.format("$%s$.location", s))){
                        String temp = temp_id();
                        origin = origin.replace(String.format("$%s$.location", s),String.format("$%s$",temp));
                        to.put(temp,en.getLocation());
                    }

                    if(value instanceof Player){
                        Player pl = (Player)value;
                        origin = origin.replace(String.format("$%s$.level", s), "" + pl.getLevel());
                        origin = origin.replace(String.format("$%s$.food", s), "" + pl.getFoodLevel());
                        origin = origin.replace(String.format("$%s$.speed", s), "" + pl.getWalkSpeed());
                        origin = origin.replace(String.format("$%s$.display", s), pl.getDisplayName());
                    }
                    origin = origin.replace(String.format("$%s$", s), en.getName());
                    continue;
                }
                if(value instanceof Location){
                    Location loc = (Location)value;
                    origin = origin.replace(String.format("$%s$.x", s), "" + loc.getX());
                    origin = origin.replace(String.format("$%s$.y", s), "" + loc.getY());
                    origin = origin.replace(String.format("$%s$.z", s), "" + loc.getZ());
                    origin = origin.replace(String.format("$%s$.world", s), "" + loc.getWorld().getName());
                    origin = origin.replace(String.format("$%s$", s), Teleporter.locToString(loc));
                    continue;
                }
                if(value instanceof String[]){
                    String[] acc = (String[])value;
                    origin = origin.replace(String.format("$%s$.length", s),""+acc.length);
                    try{
                        for(int a = acc.length-1;a>=0;a--){
                            String k = String.format("$%s$.%d", s,a);
                            if(origin.contains(k)){
                                String temp = temp_id();
                                to.put(temp,acc[a]);
                                origin = origin.replace(k,String.format("$%s$",temp));
                            }

                        }
                    }catch (ArrayIndexOutOfBoundsException e){
                        Data.ConsoleInfo(String.format("解析变量 %s 时出现了数组越界！",String.format("$%s$", s)));
                    }
                    String k = String.format("$%s$.random", s);
                    if(origin.contains(k)){
                        String rd = acc[Data.Random(0,acc.length)];
                        origin = origin.replace(k,rd);
                    }
                    origin = origin.replace(String.format("$%s$", s),Arrays.toString(acc));
                    continue;
                }
                if(value instanceof Double){
                    double d = (Double)value;
                    String v;
                    if(d % 1 ==0){
                        v = String.valueOf((int)d);
                    }else{
                        v = String.format("%.1f",d);
                    }
                    origin = origin.replace(String.format("$%s$", s),v);
                    continue;
                }
                if(true){
                    String str = value.toString();
                    //TODO 字符串自动识别为坐标和数组
                    origin = origin.replace(String.format("$%s$", s),str);

                }
            }
            to.clear();
        }


        return origin;
    }
    public boolean If(Player target, String If) throws NullPointerException,NumberFormatException,IndexOutOfBoundsException{

        if(If.contains("AND")){
            String[] st = If.split("AND",2);
            return (If(target,st[0].trim()) && If(target,st[1].trim()));
        }
        if(If.contains("OR")){
            String[] st = If.split("OR",2);
            return (If(target,st[0].trim()) || If(target,st[1].trim()));
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
                    return !target.hasPermission(value);
                default:
                    return !If.split("!=")[0].trim().equals(If.split("!=")[1].trim());
            }

        }else
        if(If.contains("==")){
            String value = If.split("==")[1].trim();
            switch(If.split("==")[0].trim()){
                case "Permission":
                    return target.hasPermission(value);
                default:
                    return If.split("==")[0].trim().equals(If.split("==")[1].trim());
            }
        }
        return If.equals("true");
    }

    public void addVariable(String var,Object value){
        lobby.macros.AddVariable(this,var,value);
    }

    public void execute(UUID striker){
        if(Bukkit.getPlayer(striker)!=null){
            lobby.macros.AddVariable(this,"striker",Bukkit.getPlayer(striker));
        }

        this.striker = striker;
        next = begin;
        MainCycle.registerCall(this);
        onUpdate();

    }

    private void updateVar(){
        //variables.declare("group_player_amount",group.getPlayerList().size());
        addVariable("lobby_player_amount",lobby.getPlayerAmount());
    }

    public void setCooldown(int tick){
        cooldown = tick;
    }

    @Override
    public void onUpdate() {
        if(endWhenClear && getField().size()==0){
            next = null;
        }
        if(endWhenNoTarget && (striker==null || !lobby.hasPlayer(Bukkit.getPlayer(striker)) )){
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
            lobby.macros.CleanExecuter(this);
            MainCycle.unRegisterCall(this);
        }
    }
}

package org.csg.group.task.toolkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.csg.Data;

import java.util.Arrays;
import java.util.List;

public class TypeManage {

    private static Object cast(String arg, Class expect){

        if(expect==null || expect== Location.class) {
            if (arg.matches("(-?[0-9]+ ){3}\\S+")) {
                String[] sl = arg.split(" ");
                World w = Bukkit.getWorld(sl[3]);
                return new Location(w, Integer.valueOf(sl[0]), Integer.valueOf(sl[1]), Integer.valueOf(sl[2]));
            }
        }
        if(expect==null || expect== Player.class) {
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.getName().equals(arg)){
                    return p;
                }
            }
        }

        if(expect==null || expect==String.class) {
            return arg;
        }

        return null;
    }

    public static Object[] analyze_args(String[] args, List<Class> expected_type){
        if(args.length < expected_type.size()){
            Data.ConsoleError("参数 "+ Arrays.toString(args)+" 不足"+expected_type.size()+"个！");
            return new Object[0];
        }
        Object[] seril_args = new Object[expected_type.size()];
        for(int a = 0;a<expected_type.size();a++){
            seril_args[a] = cast(args[a].trim(),expected_type.get(a));
            if(seril_args[a]==null){
                Data.ConsoleError("参数 "+ args[a].trim() +" 无法被解析为"+expected_type.get(a).getName()+"！");
            }
        }
        return seril_args;
    }

    public static Object[] analyze_args(String arg, List<Class> expected_type){
        String[] args;
        if(arg.contains(",")){
            args = arg.split(",");
        }else{
            if(arg.length()==0){
                args = new String[0];
            }else{
                args = new String[1];
                args[0] = arg;
            }
        }
        return analyze_args(args,expected_type);
    }
}

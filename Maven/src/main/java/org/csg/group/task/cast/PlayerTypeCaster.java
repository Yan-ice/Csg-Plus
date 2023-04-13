package org.csg.group.task.cast;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;

public class PlayerTypeCaster extends TypeCaster{
    public Class<?> targetType(){
        return Player.class;
    }

    /**
     * 将targetType类型的object序列化为字符串。
     * @param s 我们可以保证该参数的类型与targetType相同。
     * @return
     */
    protected String serializeRule(Object s){
        Player p = (Player)s;
        return p.getName();
    }

    /**
     * 将字符串反序列化为targetType类型的object。
     * @param s 源字符串。
     * @return 如果反序列化失败，请返回null。
     */
    protected Object deserializeRule(String s,  Type... typeArguments){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.getName().equals(s))
                return p;
        }
        return null;
    }
}



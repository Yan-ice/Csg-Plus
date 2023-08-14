package org.csg.group.task.cast;

import org.bukkit.entity.Player;
import org.csg.Fwmain;
import org.csg.group.Lobby;

import java.lang.reflect.Type;

public class LobbyTypeCaster extends TypeCaster{
    public Class<?> targetType(){
        return Player.class;
    }

    /**
     * 将targetType类型的object序列化为字符串。
     * @param s 我们可以保证该参数的类型与targetType相同。
     * @return
     */
    protected String serializeRule(Object s){
        Lobby p = (Lobby)s;
        return p.getName();
    }

    /**
     * 将字符串反序列化为targetType类型的object。
     * @param s 源字符串。
     * @return 如果反序列化失败，请返回null。
     */
    protected Object deserializeRule(String s,  Type... typeArguments){
        for(Lobby l : Fwmain.lobbyList){
            if(l.getName().equals(s)){
                return l;
            }
        }

        return null;
    }


}



package org.csg.group.task.cast;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

/**
 * location与字符的转化格式为：
 * x y z world 或 x y z world yaw pitch
 *
 * 此外，所有数字必须是整数。
 */
public class LocationTypeCaster extends TypeCaster{
    public Class<?> targetType(){
        return Location.class;
    }

    /**
     * 将targetType类型的object序列化为字符串。
     * @param s 我们可以保证该参数的类型与targetType相同。
     * @return
     */
    protected String serializeRule(Object s){
        Location obj = (Location)s;
        return String.format("%d %d %d %s %d %d",obj.getBlockX(),obj.getBlockY(),obj.getBlockZ(),
                obj.getWorld().getName(),(int)obj.getYaw(),(int)obj.getPitch());
    }

    /**
     * 将字符串反序列化为targetType类型的object。
     * @param arg 源字符串。
     * @return 如果反序列化失败，请返回null。
     */
    protected Object deserializeRule(String arg, Type... typeArguments) throws UnknownWorldException {
        if (arg.matches("^(-?[0-9]+ ){3}\\S+$")) {
            String[] sl = arg.split(" ");
            World w = Bukkit.getWorld(sl[3]);
            if(w==null) throw new UnknownWorldException(sl[3]);
            return new Location(w, Integer.valueOf(sl[0]), Integer.valueOf(sl[1]), Integer.valueOf(sl[2]));
        }else if (arg.matches("^(-?[0-9]+ ){3}\\S+ (-?[0-9]+) (-?[0-9]+)$")) {
            String[] sl = arg.split(" ");
            World w = Bukkit.getWorld(sl[3]);
            if(w==null) throw new UnknownWorldException(sl[3]);
            return new Location(w, Integer.valueOf(sl[0]), Integer.valueOf(sl[1]),
                    Integer.valueOf(sl[2]),Integer.valueOf(sl[4]),Integer.valueOf(sl[5]));
        }

        return null;
    }
}



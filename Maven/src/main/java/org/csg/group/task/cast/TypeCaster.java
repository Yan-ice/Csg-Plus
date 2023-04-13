package org.csg.group.task.cast;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import org.csg.Data;

import javax.xml.stream.Location;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * 此类为转化规则的基类（当接口用）
 * 负责进行类型转化。
 */
public abstract class TypeCaster {



    public abstract Class<?> targetType();

    /**
     * 将targetType类型的object序列化为字符串。
     * @param s 其类型必须和targetType一样。
     * @return
     */
    public String serialize(Object s){
        try{
            if(targetType().isAssignableFrom(s.getClass())){
                return serializeRule(s);
            }else{
                String ori = s.getClass().getName();
                String tar = targetType().getName();
                throw new ClassCastException("Cannot cast from "+ori+" to "+tar+".");
            }
        }catch(Exception e){
            Data.Debug("Error when casting "+targetType().getName()+"["+e.getClass().getName()+"]: "+e.getMessage());
            return null;
        }

    }

    /**
     * 将targetType类型的object序列化为字符串。
     * @param s 我们可以保证该参数的类型与targetType相同。
     * @return
     */
    protected abstract String serializeRule(Object s) throws Exception;

    /**
     * 将字符串反序列化为targetType类型的object。
     * @param s 源字符串。
     * @return 如果反序列化失败，请返回null。
     */
    public Object deserialize(String s){
        try{
            Object dese = deserializeRule(s);
            if(targetType().isAssignableFrom(dese.getClass())){
                return dese;
            }else{
                String ori = dese.getClass().getName();
                String tar = targetType().getName();
                throw new ClassCastException("Cannot cast from "+ori+" to "+tar+".");
            }
        }catch(Exception e){
            Data.Debug("Error when casting "+targetType().getName()+"["+e.getClass().getName()+"]: "+e.getMessage());
            return null;
        }

    }

    /**
     * 将字符串反序列化为targetType类型的object。
     * @param s 源字符串。
     * @return 如果反序列化失败，请返回null。
     */
    protected abstract Object deserializeRule(String s) throws Exception;
}



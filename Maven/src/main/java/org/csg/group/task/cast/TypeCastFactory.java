package org.csg.group.task.cast;

import java.util.HashSet;
import java.util.Set;

public class TypeCastFactory {

    //保存已注册的规则。
    private static final Set<TypeCaster> caster_list = new HashSet<>();
    {
        caster_list.add(new LocationTypeCaster());
        caster_list.add(new StringTypeCaster());
    }

    /**
     * 注册一个新的序列化规则。
     * @param caster
     */
    public static void registerCaster(TypeCaster caster){
        caster_list.add(caster);
    }

    /**
     * 检查Factory目前是否支持某一个类的转化。
     * 如果没有，请实现新的TypeCaster并注册到这里吧~
     * @param c
     * @return
     */
    public static boolean castAvailable(Class<?> c){
        for(TypeCaster cst : caster_list){
            if(cst.targetType().isAssignableFrom(c)){
                return true;
            }
        }
        return false;
    }

    /**
     * 根据已有的规则执行类型转化。
     * 本质上是以string为媒介的序列化+反序列化。
     * @param object
     * @param target
     */
    public static Object castObject(Object object, Class<?> target){
        String seril = serializeObject(object);
        return deserializeObject(seril, target);
    }

    /**
     * 根据已有的序列化规则执行序列化。
     * @param object
     */
    public static String serializeObject(Object object){
        for(TypeCaster cst : caster_list){
            if(cst.targetType().isAssignableFrom(object.getClass())){
                return cst.serialize(object);
            }
        }
        return null;
    }

    /**
     * 根据已有的序列化规则执行反序列化。
     * 注意， "null"和"none"比较特殊，会直接反序列化为null。
     * @param arg
     * @param target 目标类型
     */
    public static Object deserializeObject(String arg, Class<?> target){
        if("null".equals(arg) || "none".equals(arg)) return null;

        for(TypeCaster cst : caster_list){
            if(cst.targetType().isAssignableFrom(target)){
                return cst.deserialize(arg);
            }
        }
        return null;
    }


}

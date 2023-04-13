package org.csg.group.task.cast;

import org.bukkit.Location;

import javax.lang.model.type.ArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeCastFactory {

    //保存已注册的规则。
    private static final Set<TypeCaster> caster_list = new HashSet<>();
    {
        caster_list.add(new LocationTypeCaster());
        caster_list.add(new StringTypeCaster());
        caster_list.add(new ArrayListTypeCaster());
        caster_list.add(new PlayerTypeCaster());
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
    public static Object castObject(Object object, Type target){
        //如果类型已经相同就无需再转化。
        if(target instanceof Class<?>){
            if(((Class<?>)target).isAssignableFrom(object.getClass())){
                return object;
            }
        }
        if(target instanceof ParameterizedType){
            ParameterizedType p = (ParameterizedType) target;
            if(((Class<?>)p.getRawType()).isAssignableFrom(object.getClass())){
                return object;
            }
        }

        //进行转化。
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
    public static Object deserializeObject(String arg, Type target){
        if("null".equals(arg) || "none".equals(arg)) return null;
        if(target instanceof Class){
            for(TypeCaster cst : caster_list){
                if(cst.targetType().isAssignableFrom((Class<?>)target)){
                    return cst.deserialize(arg);
                }
            }
        }
        if(target instanceof ParameterizedType){
            ParameterizedType pr = (ParameterizedType) target;
            for(TypeCaster cst : caster_list){
                Class<?> t = (Class<?>)pr.getRawType();
                Class<?> s = cst.targetType();
                if(s.isAssignableFrom(t)){
                    return cst.deserialize(arg,pr.getActualTypeArguments());
                }
            }
        }
        return null;
    }

//    以下是对类型转换功能的测试代码。
//    public void Invo(List<String> arg,List<List<String>> h_arg){
//        System.out.println("Success！");
//        for(String s : arg){
//            System.out.println("arg: "+s);
//        }
//        for(List<String> sarg : h_arg){
//            for(String s : sarg) {
//                System.out.println("h_arg: " + s);
//            }
//        }
//
//    }
//    public static void main(String[] ar){
//        Object[] params = new Object[]{"[str]","[[aaaaaw w], [yanice, ww], [aa]]"};
//        TypeCastFactory fac = new TypeCastFactory();
//        for(Method m :fac.getClass().getMethods()){
//            if(m.getName().equals("Invo")){
//                fac.safeCallJavaFunction(m,params);
//            }
//        }
//    }
//    private Object safeCallJavaFunction(Method meth, Object... para){
//        Type[] require_list = meth.getGenericParameterTypes();
//        if(require_list.length > para.length){
//            throw new ClassCastException("param not enough");
//        }
//        Object[] cast_list = new Object[require_list.length];
//        for(int a = 0;a<require_list.length;a++){
//            cast_list[a] = TypeCastFactory.castObject(para[a],require_list[a]);
//
//        }
//        try {
//            return meth.invoke(this,cast_list);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
//    }

}

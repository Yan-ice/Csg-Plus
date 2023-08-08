package org.csg.group.task.toolkit;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 动态创建Bukkit事件的监听类，实现控制反转
 * @author Takamina
 */
public class ListenerFactory {
    static Map<String,CtClass> listenerClasses;
    static ClassPool pool;

    /**
     * 初始化
     */
    public static void enable(){
        pool = ClassPool.getDefault();
        listenerClasses = new HashMap<>();
    }

    /**
     * 解除引用,防止内存溢出
     */
    public static void disable(){
        pool = null;
        listenerClasses = null;
    }

    /**
     * 创建监听器类
     * @param eventClass 监听的事件类
     */
    public static void createListenerClass(Class<? extends Event> eventClass){
        try {
            CtClass listener;
            try {
                listener = pool.getCtClass(ListenerFactory.class.getName()+".dynamic_listener." + eventClass.getSimpleName());

            } catch (NotFoundException err){
                listener = pool.makeClass(ListenerFactory.class.getName()+".dynamic_listener." + eventClass.getSimpleName());
                //实现bukkit的Listener接口
                CtClass listenerI = pool.getCtClass("org.bukkit.event.Listener");
                listener.addInterface(listenerI);
                //泛型Consumer<event>
                SignatureAttribute.ClassSignature cs = new SignatureAttribute.ClassSignature(
                        new SignatureAttribute.TypeParameter[] {
                                new SignatureAttribute.TypeParameter(eventClass.getName())
                        }
                );
                CtClass eventFunction = pool.getCtClass("java.util.function.Consumer");
                eventFunction.setGenericSignature(cs.encode());
                //添加Consumer<event> method字段
                CtField methodF = new CtField(eventFunction, "method", listener);
                listener.addField(methodF);
                //添加构造函数
                CtConstructor constructor = new CtConstructor(new CtClass[]{eventFunction}, listener);
                constructor.setBody("{\n"
                        +"$0.method = $1;\n"
                        +"}");
                listener.addConstructor(constructor);
                //添加@EventHandler注解
                ConstPool constpool = listener.getClassFile().getConstPool();
                AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
                Annotation eventHandlerA = new Annotation("org.bukkit.event.EventHandler",constpool);
                methodAttr.addAnnotation(eventHandlerA);
                //添加方法体
                CtMethod onEventM = new CtMethod(CtClass.voidType, "on"+eventClass.getSimpleName(), new CtClass[]{pool.getCtClass(eventClass.getName())}, listener);
                onEventM.setBody("{$0.method.accept($1);}");
                onEventM.getMethodInfo().addAttribute(methodAttr);
                listener.addMethod(onEventM);
            }
            listener.detach();
            try{
                listener.toClass();
            }catch (CannotCompileException ignored){}
            listenerClasses.put(eventClass.getSimpleName(),listener);
        } catch (NotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取事件的监听类
     * @param eventClass 监听的事件
     * @return 事件的监听类
     */
    public static Class getListenerClass(Class<? extends Event> eventClass){
        if(eventClass.isInterface()){
            Data.Debug(String.format("事件%s为接口，不能注册监听器!",eventClass.getName()));
            return null;
        }
        if (!listenerClasses.containsKey(eventClass.getSimpleName())){
            createListenerClass(eventClass);
        }
        try {
            return Class.forName(listenerClasses.get(eventClass.getSimpleName()).getName());
        }catch (ClassNotFoundException err){
            err.printStackTrace();
            return null;
        }
    }

    /**
     * 获取事件的监听器(未注册)
     * @param event 坚挺的事件类
     * @param method 监听器主体
     * @return 监听器(未注册)
     */
    public static Listener getListener(Class<? extends Event> event, Consumer<? extends Event> method){
        try {
            return (Listener) getListenerClass(event).getDeclaredConstructors()[0].newInstance(method);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

}

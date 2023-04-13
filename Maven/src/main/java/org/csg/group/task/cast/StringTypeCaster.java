package org.csg.group.task.cast;

public class StringTypeCaster extends TypeCaster{
    public Class<?> targetType(){
        return String.class;
    }

    /**
     * 将targetType类型的object序列化为字符串。
     * @param s 我们可以保证该参数的类型与targetType相同。
     * @return
     */
    protected String serializeRule(Object s){
        return (String)s;
    }

    /**
     * 将字符串反序列化为targetType类型的object。
     * @param s 源字符串。
     * @return 如果反序列化失败，请返回null。
     */
    protected Object deserializeRule(String s){
        return s;
    }
}



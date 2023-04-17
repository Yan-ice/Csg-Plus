package org.csg.group.task.cast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ArrayListTypeCaster extends TypeCaster{
    public Class<?> targetType(){
        return List.class;
    }

    /**
     * 将targetType类型的object序列化为字符串。
     * @param s 我们可以保证该参数的类型与targetType相同。
     * @return
     */
    protected String serializeRule(Object s){
        List<?> list = (List<?>)s;
        StringBuilder b = new StringBuilder("[");
        for(Object o : list){
            b.append(TypeCastFactory.serializeObject(o)).append(", ");
        }
        b.delete(b.lastIndexOf(", "),b.length());
        b.append("]");
        return b.toString();
    }

    /**
     * 将字符串反序列化为targetType类型的object。
     * @param s 源字符串。
     * @return 如果反序列化失败，请返回null。
     */
    protected Object deserializeRule(String s,  Type... typeArguments){
        ArrayList lst = new ArrayList<>();
        if(s.matches("^\\[.+\\]$")){
            //TODO: 对于二维数组，[[a,b],[c]]会被分割成[a/b]/[c]三部分，从而导致错误。
            //TODO: 这里我们应该识别出括号，并将它分割为[a,b]/[c]。
            String[] content = s.substring(1,s.length()-1).split(",");
            for(String item : content){
                lst.add(TypeCastFactory.deserializeObject(item.trim(), typeArguments[0]));
            }
        }
        return lst;
    }
}



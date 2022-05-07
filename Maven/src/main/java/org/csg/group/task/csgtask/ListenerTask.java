package org.csg.group.task.csgtask;

import customgo.Group;

import java.util.HashSet;
import java.util.Set;

public class ListenerTask extends FunctionTask {

    String field;
    Set<String> target = new HashSet<>();
    public ListenerTask(String lb, String arg,String field,Set<String> target) throws TaskSyntaxError {
        super(lb, arg);
        this.target.addAll(target);
        this.field = field;
    }
    public boolean checkTarget(Group gro){
        if(target.size()==0){
            return true;
        }
        for(String t : target){
            if(gro.getName().contains(t)){
                return true;
            }
        }
        return false;
    }
    public String getField(){
        return field;
    }

}

package org.csg.group.task.csgtask;

import org.csg.group.task.toolkit.TaskExecuter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


enum TargetType{
    Striker, Striker_force, Group,Random,None, Lobby, Server;
}

public abstract class Task {
    public Task next = null;
    public String field = "";
    /**
     * 执行此命令并返回下一条命令。
     * @return
     */
    public abstract Task execute(TaskExecuter executer, UUID striker) throws Exception;

    public void set_next(Task task){
        next = task;
        if(next!=null){
            next.field = field;
        }

    }

    public void destroy(){
        if(next != null){
            next.destroy();
        }
        next = null;
    }

    List<String> variables = new ArrayList<>();
    public void addVarKey(String key){
        variables.add(key);
    }
}

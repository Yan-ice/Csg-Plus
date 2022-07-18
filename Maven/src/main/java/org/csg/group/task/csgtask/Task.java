package org.csg.group.task.csgtask;

import org.csg.group.task.toolkit.TaskExecuter;
import java.util.UUID;


enum TargetType{
    Striker, Group,Random,None, Lobby, Server;
}

public abstract class Task {
    public Task next = null;
    /**
     * 执行此命令并返回下一条命令。
     * @return
     */
    public abstract Task execute(TaskExecuter executer, UUID striker) throws Exception;

    public void set_next(Task task){
        next = task;
    }

    public void destroy(){
        if(next != null){
            next.destroy();
        }
        next = null;
    }
}

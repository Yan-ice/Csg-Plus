package org.csg.group.task.csgtask;

import org.bukkit.Bukkit;
import org.csg.group.task.toolkit.TaskExecuter;

import java.util.UUID;

public class RepeatTask extends ChooseTask {
    String checker;

    int time_set = 3;

    int time = -1;

    public RepeatTask(String param) throws TaskSyntaxError{
        this.checker = param;
    }
    public void reset(){
        time = -1;
    }

    @Override
    public Task execute(TaskExecuter executer, UUID striker) {
        if(time==-1){
            String str = executer.variableReplace(checker, striker!=null? Bukkit.getPlayer(striker):null);
            time_set = Integer.parseInt(str);
            time = time_set;
        }
        if(time>0){
            time--;
            return next_yes;
        }else{
            time = -1;
            return next;
        }

    }
}

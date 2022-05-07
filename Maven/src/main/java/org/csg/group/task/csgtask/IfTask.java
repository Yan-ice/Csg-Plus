package org.csg.group.task.csgtask;

import org.csg.group.task.toolkit.TaskExecuter;
import org.bukkit.Bukkit;

import java.util.UUID;

public class IfTask extends ChooseTask {
    String checker;
    public IfTask(String arg) throws TaskSyntaxError{
        try{
            this.checker = arg;
        }catch(Exception e){
            throw new TaskSyntaxError();
        }

    }

    @Override
    public Task execute(TaskExecuter executer, UUID striker) {

        //if(If(executer.group, Bukkit.getPlayer(striker), checker, Bukkit.getPlayer(striker))){
        if(executer.If(Bukkit.getPlayer(striker), executer.variableReplace(checker, Bukkit.getPlayer(striker)))){
            return next_yes;
        }else{
            return next;
        }

    }
}

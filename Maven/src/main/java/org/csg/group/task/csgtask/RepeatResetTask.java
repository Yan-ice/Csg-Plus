package org.csg.group.task.csgtask;

import org.csg.group.task.toolkit.TaskExecuter;

import java.util.UUID;

public class RepeatResetTask extends Task {

    public RepeatResetTask() throws TaskSyntaxError{
    }
    @Override
    public Task execute(TaskExecuter executer, UUID striker) {
        if(next instanceof RepeatTask){
            ((RepeatTask)next).reset();
        }
        return next;
    }
}

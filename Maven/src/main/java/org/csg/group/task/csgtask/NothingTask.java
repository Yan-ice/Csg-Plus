package org.csg.group.task.csgtask;

import org.csg.group.task.toolkit.TaskExecuter;

import java.util.UUID;


public class NothingTask extends Task {

    public NothingTask(){

    }

    @Override
    public Task execute(TaskExecuter executer, UUID striker) {
        return next;
    }

}
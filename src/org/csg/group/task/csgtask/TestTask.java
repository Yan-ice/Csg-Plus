package org.csg.group.task.csgtask;

import org.csg.group.task.toolkit.TaskExecuter;

import java.util.UUID;

public class TestTask extends Task {
    String command;
    String[] args;

    public TestTask(String ts){
        command = ts;
    }

    @Override
    public Task execute(TaskExecuter executer, UUID striker) {
        System.out.println(command);
        return next;
    }

}
package org.csg.group.task.csgtask;


public abstract class ChooseTask extends Task{
    public Task next_yes = null;

    public void set_next_Yes(Task task){
        next_yes = task;
    }
    public void destroy(){
        super.destroy();
        if(next_yes != null){
            next_yes.destroy();
        }
        next_yes = null;
    }
}

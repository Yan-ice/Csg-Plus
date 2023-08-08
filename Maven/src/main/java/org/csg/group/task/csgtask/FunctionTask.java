package org.csg.group.task.csgtask;

import org.csg.Utils.CommonUtils;
import org.csg.group.task.toolkit.TaskExecuter;

import java.util.UUID;

public class FunctionTask extends Task {
    String name;
    String[] args;

    private String field = null;

    public String getField(){
        return field;
    }

    public FunctionTask(String lb, String arg, String field) throws TaskSyntaxError {
        this.field = field;
        try{
            name = lb.split(" ")[1].trim();
            //读取参数

            if(arg==null){
                args = new String[0];
            }else{
                if(arg.contains(",")){
                    args = arg.split(",");
                }else{
                    if(arg.trim().length()==0){
                        args = new String[0];
                    }else{
                        args = new String[1];
                        args[0] = arg;
                    }
                }
            }
            CommonUtils.ConsoleDebugMsg(String.format("已成功读取CustomGo函数%s",name));

        }catch(Exception e){
            e.printStackTrace();
            throw new TaskSyntaxError();
        }
    }
    public String getName(){
        return name;
    }
    public void loadArgs(TaskExecuter executer,Object[] give_args){
        if(args.length>give_args.length){
            CommonUtils.ConsoleErrorMsg("未给函数 "+name+" 提供足够的参数！");
            return;
        }
        for(int a = 0;a<args.length;a++){
            executer.addVariable(args[a],give_args[a]);
        }
    }
    @Override
    public Task execute(TaskExecuter executer, UUID striker) {
        CommonUtils.ConsoleDebugMsg("调用函数 " +name);
        return next;
    }

}

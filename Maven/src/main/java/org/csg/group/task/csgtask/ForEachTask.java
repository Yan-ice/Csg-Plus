package org.csg.group.task.csgtask;

import org.csg.Data;
import org.csg.group.task.toolkit.TaskExecuter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class ForEachTask extends ChooseTask {
    TargetType target_type;
    String checker;

    List<UUID> players = new ArrayList<>();

    public ForEachTask(String arg,String rest) throws TaskSyntaxError{
        try{
            this.checker = arg;
            switch (rest) {
                case "@a":
                    target_type = TargetType.Group;
                    break;
                case "@p":
                    target_type = TargetType.Striker;
                    break;
                case "@r":
                    target_type = TargetType.Random;
                    break;
                case "@e":
                    target_type = TargetType.Lobby;
                    break;
                default:
                    target_type = TargetType.None;
                    break;
            }

        }catch(Exception e){
            throw new TaskSyntaxError();
        }

    }
    boolean init = false;

    @Override
    public Task execute(TaskExecuter executer, UUID striker) {

        if(!init){

            init = true;
            switch(target_type){
                case Group:
                    for(UUID u : executer.getField()){
                        if(check(u,executer)){
                            players.add(u);
                        }
                    }
                    break;
                case Striker:
                    if(check(striker,executer)){
                        players.add(striker);
                    }
                    break;
                case Random:
                    for(UUID u : executer.getField()){
                        if(check(u,executer)){
                            players.add(u);
                        }
                    }
                    if(players.size()>0){
                        int size = players.size();
                        UUID p = players.get(Data.Random(0,size));
                        players.clear();
                        players.add(p);
                    }

                    break;
                case Lobby:
                    for(UUID u : executer.lobby.getPlayerList()){
                        if(check(u,executer)){
                            players.add(u);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        if(players.size()>0){
            UUID u = players.get(0);
            players.remove(0);
            Player p = Bukkit.getPlayer(u);
            executer.variables.declare("player",p);
            return next_yes;
        }else{
            init = false;
            return next;
        }
    }

    private boolean check(UUID uid,TaskExecuter e){
        Player p = Bukkit.getPlayer(uid);
        if(p==null){
            return false;
        }
        e.variables.declare("player",p);
        String s = e.variableReplace(this.variables,this.checker,p);
        return e.If(p,s);
    }

}

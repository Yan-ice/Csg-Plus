package org.csg.group.task.csgtask;

import org.csg.Fwmain;
import org.csg.Utils.CommonUtils;
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

    UUID original_striker = null;
    @Override
    public Task execute(TaskExecuter executer, UUID striker) {

        if(!init){
            init = true;
            original_striker = executer.striker;
            switch(target_type){
                case Group:
                    for(UUID u : executer.getField()){
                        if(check(u,executer)){
                            players.add(u);
                        }
                    }
                    break;
                case Striker:
                    if(executer.getField().contains(striker) && check(striker,executer)){
                        players.add(striker);
                    }
                    break;
                case Striker_force:
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
                        UUID p = players.get(CommonUtils.Random(0,size));
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

        while(players.size()>0){
            UUID u = players.get(0);
            players.remove(0);

            executer.striker = u;
            Player p = Bukkit.getPlayer(u);
            executer.addVariable("striker",p);

            executer.addVariable("player",p);
            String s = executer.variableReplace(this.variables,this.checker,p);
            if(!executer.If(p,s)) continue;

            return next_yes;
        }
            executer.striker = original_striker;
            executer.addVariable("striker",original_striker);
            init = false;
            return next;
    }

    private boolean check(UUID uid,TaskExecuter e){
        Player p = Bukkit.getPlayer(uid);
        if(p==null){
            return false;
        }

        String s = e.variableReplace(this.variables,this.checker,p);
        return e.If(p,s);
    }

}

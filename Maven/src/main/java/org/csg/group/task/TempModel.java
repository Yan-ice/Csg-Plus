package org.csg.group.task;

import customgo.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.csg.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempModel {

    JavaPlugin plugin;
    Lobby lobby = null;
    Player striker = null;
    Player player = null;


    Scoreboard _score_board = Bukkit.getScoreboardManager().getNewScoreboard();
    Map<Group, Team> _team_m = new HashMap<>();

    @CsgTaskListener(name="OnTeamLoaded")
    public void initScTeam(Group g){
        Team t = _score_board.registerNewTeam(g.getName());

        List<String> conf = (List<String>) lobby.getMacro("name_invisible");
        if(conf.contains(g.getName())){
            t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        }else{
            t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        Object o = lobby.getMacro("pvp_allow");
        if(o instanceof List){
            if(((List<String>)o).contains(g.getName())){
                t.setAllowFriendlyFire(false);
            }
        }
        String prefix = (String) lobby.getMacro("prefix_"+g.getName());
        if(prefix!=null){
            t.setPrefix(Data.ColorChange(prefix));
        }

        _team_m.put(g, t);
    }

    @CsgTaskListener(name="OnLobbyUnloaded")
    public void destroyScTeam(Group g){
        Team t = _team_m.get(g);
        t.unregister();
        _team_m.remove(g);
    }

    @CsgTaskListener(name="OnPlayerJoinGroup")
    public void joinScTeam(Group g){
        _team_m.get(g).addPlayer(striker);
    }

    @CsgTaskListener(name="OnPlayerLeaveGroup")
    public void leaveScTeam(Group g){
        _team_m.get(g).removePlayer(striker);
    }


}

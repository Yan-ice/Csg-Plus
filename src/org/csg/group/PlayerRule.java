package org.csg.group;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import org.csg.Data;

public class PlayerRule {
	private boolean PvP = false;
	private String PvPMessage = "none";
	
	private boolean Priority = false;
	
	private boolean Potionhit = false;
	private String PotionMessage = "none";

	private boolean Projectile = false;
	private String ProjectileMessage = "none";
	
	private int NameInv = 0;
	private String prefix = "";
	private List<String> WhiteListCommand = new ArrayList<>();
	private boolean chatInGroup = true;
	private String ChatInGroupFormat = "&e[%type%]< &7%group% &e%player% > &r%message%";
	
	protected String debugChange(String Message,String Name,String config,String defaultConfig){
		Message = Message.replaceAll("<name>", Name);
		if(config!=null){
			Message = Message.replaceAll("<config>", config);
		}
		if(defaultConfig!=null){
			Message = Message.replaceAll("<default>", defaultConfig);
		}
		return Message;
	}
	
	boolean useTeam = false;
	public boolean Load(Group group) throws NullPointerException,NumberFormatException{
		if(group.getFileConf().contains("PlayerRule.Prefix")){
			prefix = group.getFileConf().getString("PlayerRule.Prefix");
			useTeam=true;
		}

		if(group.getFileConf().contains("PlayerRule.NameInvisible")){
			String s = group.getFileConf().getString("PlayerRule.NameInvisible");
			this.NameInv = Integer.valueOf(s);
			useTeam=true;
		}
		
		if(group.getFileConf().contains("PlayerRule.PvP")){
			this.PvP = group.getFileConf().getBoolean("PlayerRule.PvP");
		}
		if(group.getFileConf().contains("PlayerRule.HighPriority")){
			this.Priority = group.getFileConf().getBoolean("PlayerRule.HighPriority");
		}
		if(group.getFileConf().contains("PlayerRule.Potionhit")){
			this.Potionhit = group.getFileConf().getBoolean("PlayerRule.Potionhit");
		}
		if(group.getFileConf().contains("PlayerRule.Projectile")){
			this.Projectile = group.getFileConf().getBoolean("PlayerRule.Projectile");
		}
		if(group.getFileConf().contains("PlayerRule.ChatInGroup")){
			this.chatInGroup = group.getFileConf().getBoolean("PlayerRule.ChatInGroup");
		}
		if(group.getFileConf().contains("PlayerRule.PotionHitMessage")){
			this.PotionMessage = group.getFileConf().getString("PlayerRule.PotionHitMessage");
		}
		if(group.getFileConf().contains("PlayerRule.PvPMessage")){
			this.PvPMessage = group.getFileConf().getString("PlayerRule.PvPMessage");
		}
		if(group.getFileConf().contains("PlayerRule.ProjectileMessage")){
			this.ProjectileMessage = group.getFileConf().getString("PlayerRule.ProjectileMessage");
		}
		if(group.getFileConf().contains("PlayerRule.ChatInGroupFormat")){
			this.ChatInGroupFormat = group.getFileConf().getString("PlayerRule.ChatInGroupFormat");
		}
		
		if(group.getFileConf().contains("PlayerRule.WhiteListCommand")){
			this.WhiteListCommand = group.getFileConf().getStringList("PlayerRule.WhiteListCommand");
			if(!WhiteListCommand.contains("csg")){
				WhiteListCommand.add("csg");
			}
		}else{
			WhiteListCommand.add("csg");
		}
		return true;
	}

	public boolean PvP() {
		return PvP;
	}

	public boolean Potionhit() {
		return Potionhit;
	}
	public String PvPMessage() {
		return PvPMessage;
	}

	public String PotionhitMessage() {
		return PotionMessage;
	}
	
	public String Prefix() {
		return prefix;
	}
	public List<String> WhiteListCommand(){
		return WhiteListCommand;
	}
	
	public int NameInv(){
		return NameInv;
	}
	
	public boolean chatInGroup() {
		return chatInGroup;
	}
	public boolean HighPriority() {
		return Priority;
	}
	
	public boolean Projectile(){
		return Projectile;
	}
	public String ProjectileMessage(){
		return ProjectileMessage;
	}
	public String ChatFormat(){
		return ChatInGroupFormat;
	}
	
	
}

class TagAPI
{
  public static void setTag(Player p, String prefix)
    throws Exception
  {
    prefix = ChatColor.translateAlternateColorCodes('&', prefix);
    if (prefix.length() > 16) {
      prefix = prefix.substring(0, 16);
    }
    Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
    Team t = board.getTeam(p.getName());
    if (t == null) {
      t = board.registerNewTeam(p.getName());
    } else {
      t = board.getTeam(p.getName());
    }
    
    
    t.setPrefix(prefix);
    if(Data.HighMCVersion){
        t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }
    t.addPlayer(p);
    
    for (Player o : Bukkit.getOnlinePlayers()) {
      o.setScoreboard(board);
    }
  }

  
  public static void unregisterTag(Player p)
    throws Exception
  {
    Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p).unregister();
  }
  
  
  
  public static void unregisterAll()
    throws Exception
  {
    for (Player o : Bukkit.getOnlinePlayers()) {
      unregisterTag(o);
    }
  }
  
  public static void refresh()
  {
    Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
    for (Player o : Bukkit.getOnlinePlayers()) {
      o.setScoreboard(board);
    }
  }
}


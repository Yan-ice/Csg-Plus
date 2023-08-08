package org.csg.group.model;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamManager {

    private static Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    public static TeamManager TeamManager = new TeamManager();

    /**
     * 添加队伍
     * @param teamName 队伍名
     */
    public static void addTeam(String teamName) {
        // 判断是否已经存在这个队伍
        if (scoreboard.getTeam(teamName) != null) {
            Data.ConsoleWarn("该队伍已经存在！请检查是否重复添加！");
        }
        scoreboard.registerNewTeam(teamName);
    }

    /**
     * 删除队伍
     * @param teamName 队伍名
     */
    public static void delTeam(String teamName) {
        // 判断是否已经存在这个队伍
        if (scoreboard.getTeam(teamName) == null) {
            Data.ConsoleWarn("该队伍不存在！请检查是否重复删除！");
        }
        scoreboard.getTeam(teamName).unregister();
    }

    /**
     * 清除所有队伍
     */
    public static void unregister() {
        scoreboard.getTeams().forEach(Team::unregister);
    }

    /**
     * 添加队伍成员
     * @param teamName 队伍名
     * @param playerName 玩家名
     */
    public static void joinTeam(String teamName, String playerName) {
        // 判断是否已经存在这个队伍
        if (scoreboard.getTeam(teamName) == null) {
            // 如果不存在这个队伍，就新建
            addTeam(teamName);
        }
        // 判断是否已经存在这个玩家
        if (scoreboard.getEntryTeam(playerName) != null) {
            Data.ConsoleWarn("该玩家已经存在！请检查是否重复添加！");
        }
        scoreboard.getTeam(teamName).addEntry(playerName);
    }

    /**
     * 删除队伍成员
     * @param teamName 队伍名
     * @param playerName 玩家名
     */
    public static void leaveTeam(String teamName, String playerName) {
        // 判断是否已经存在这个队伍
        if (scoreboard.getTeam(teamName) == null) {
            Data.ConsoleWarn("该队伍不存在！请先添加队伍！");
        }
        // 判断是否已经存在这个玩家
        if (scoreboard.getEntryTeam(playerName) == null) {
            Data.ConsoleWarn("该玩家不存在！请检查是否重复删除！");
        }
        scoreboard.getTeam(teamName).removeEntry(playerName);
    }

    /**
     * 清空队伍
     * @param teamName 队伍名
     */
    public static void clear(String teamName) {
        // 判断是否已经存在这个队伍
        if (scoreboard.getTeam(teamName) == null) {
            Data.ConsoleWarn("该队伍不存在！请先添加队伍！");
        }
        scoreboard.getTeam(teamName).unregister();
        scoreboard.registerNewTeam(teamName);
    }

    /**
     * 获取队伍人数
     * @param teamName
     * @return
     */
    public static int size(String teamName) {
        // 判断是否已经存在这个队伍
        if (scoreboard.getTeam(teamName) == null) {
            Data.ConsoleWarn("该队伍不存在！请先添加队伍！");
        }
        return scoreboard.getTeam(teamName).getSize();
    }

    /**
     * 设置队伍属性
     * @param teamName
     * @param option
     * @param value
     * @return
     */
    public static void setTeamOption(String teamName, String option, String value) {
        // 判断是否已经存在这个队伍
        if (scoreboard.getTeam(teamName) == null) {
            Data.ConsoleWarn("该队伍不存在！请先添加队伍！");
        }
        switch (option) {
            case "prefix":
                scoreboard.getTeam(teamName).setPrefix(ChatColor.translateAlternateColorCodes('&', value));
                break;
            case "suffix":
                scoreboard.getTeam(teamName).setSuffix(ChatColor.translateAlternateColorCodes('&', value));
                break;
            case "pvp":
                scoreboard.getTeam(teamName).setAllowFriendlyFire(Boolean.parseBoolean(value));
                break;
            case "nametag":
                switch (value) {
                    case "0":
                        scoreboard.getTeam(teamName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                        break;
                    case "1":
                        scoreboard.getTeam(teamName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                        break;
                    case "2":
                        scoreboard.getTeam(teamName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
                        break;
                    case "3":
                        scoreboard.getTeam(teamName).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
                        break;
                    default:
                        Data.ConsoleWarn("无效的选项！");
                }
                break;
            default:
                Data.ConsoleWarn("无效的选项！");
        }
    }

    public static void infoTeam(String teamName, Player targer) {
        // 判断是否已经存在这个队伍
        if (scoreboard.getTeam(teamName) == null) {
            Data.ConsoleWarn("该队伍不存在！请先添加队伍！");
        }
        if (targer != null) {
            targer.sendMessage("§7队伍名称：§a" + teamName);
            targer.sendMessage("§7队伍人数：§a" + scoreboard.getTeam(teamName).getSize());
            targer.sendMessage("§7队伍前缀：§a" + scoreboard.getTeam(teamName).getPrefix());
            targer.sendMessage("§7队伍后缀：§a" + scoreboard.getTeam(teamName).getSuffix());
            targer.sendMessage("§7队伍PVP：§a" + scoreboard.getTeam(teamName).allowFriendlyFire());
            targer.sendMessage("§7名称显示：§a" + scoreboard.getTeam(teamName).getOption(Team.Option.NAME_TAG_VISIBILITY));
        } else {
            CommonUtils.ConsoleInfoMsg("§7队伍名称：§a" + teamName);
            CommonUtils.ConsoleInfoMsg("§7队伍人数：§a" + scoreboard.getTeam(teamName).getSize());
            CommonUtils.ConsoleInfoMsg("§7队伍前缀：§a" + scoreboard.getTeam(teamName).getPrefix());
            CommonUtils.ConsoleInfoMsg("§7队伍后缀：§a" + scoreboard.getTeam(teamName).getSuffix());
            CommonUtils.ConsoleInfoMsg("§7队伍PVP：§a" + scoreboard.getTeam(teamName).allowFriendlyFire());
            CommonUtils.ConsoleInfoMsg("§7名称显示：§a" + scoreboard.getTeam(teamName).getOption(Team.Option.NAME_TAG_VISIBILITY));
        }
    }

}

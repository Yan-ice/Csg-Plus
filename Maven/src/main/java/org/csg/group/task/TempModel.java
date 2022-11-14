//package org.csg.group.task;
//
//import customgo.Group;
//import customgo.Lobby;
//import org.bukkit.ChatColor;
//import org.bukkit.entity.*;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.HandlerList;
//import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.event.player.PlayerCommandPreprocessEvent;
//import org.bukkit.plugin.java.JavaPlugin;
//import org.csg.Data;
//public class TempModel {
//
//            JavaPlugin plugin;
//            Lobby lobby = null;
//           Group group = null;
//           Player striker = null;
//           Player player = null;
//
//
//    GListener ls;
//    public void initGListener(){
//        ls = new GListener();
//        Data.fmain.getServer().getPluginManager().registerEvents(ls, Data.fmain);
//    }
//
//    public void destroyGListener(){
//        HandlerList.unregisterAll(ls);
//    }
//
//    class GListener implements Listener {
//
//        @EventHandler(priority= EventPriority.HIGHEST)
//        private void PVPListen(EntityDamageByEntityEvent evt) {
//            if(!$allow_pvp$){
//                if (evt.getEntity() instanceof Player) {
//                    Player damaged = (Player) evt.getEntity();
//                    Player damager = null;
//                    if (evt.getDamager() instanceof Player) {
//                        damager = (Player) evt.getDamager();
//                    }else if(evt.getDamager() instanceof Projectile){
//                        if(((Projectile)evt.getDamager()).getShooter() != null){
//                            if((((Projectile)evt.getDamager()).getShooter()) instanceof Player){
//                                damager = (Player)(((Projectile)evt.getDamager()).getShooter());
//                            }
//                        }
//                    }
//                    if(damager==null){
//                        return;
//                    }
//                    for(Group g : lobby.getGroupList()){
//                        if(g.hasPlayer(damaged) && g.hasPlayer(damager)){
//                            evt.setCancelled(true);
//                            damager.sendMessage("$pvp_forbidden_message$");
//                        }
//                    }
//
//                }
//            }
//
//        }
//
////        @EventHandler(priority=EventPriority.HIGH)
////        private void PotionListen(PotionSplashEvent evt2) {
////            ThrownPotion pot = evt2.getPotion();
////            if (pot.getShooter() instanceof Player) {
////                Player shooter = (Player) pot.getShooter();
////                if(lobby.hasPlayer(shooter)){
////                    if(!$allow_potionhit$){
////                        evt2.setCancelled(true);
////                        List<Entity> damageds = pot.getNearbyEntities(3.0, 3.0, 3.0);
////                        for (Entity d : damageds) {
////                            if (d instanceof Player) {
////                                Player damaged = (Player)d;
////                                if ((shooter != damaged) && g.hasPlayer(damaged) ) {
////                                    if(rule.PotionhitMessage()!="none"){
////                                        shooter.sendMessage(rule.PotionhitMessage());
////                                    }
////                                } else {
////                                    damaged.addPotionEffects(pot.getEffects());
////                                }
////                            } else if (d instanceof Creature) {
////                                ((Creature)d).addPotionEffects(pot.getEffects());
////                            }
////                        }
////                    }else{
////                        if(rule.HighPriority()){
////                            evt2.setCancelled(false);
////                        }
////                    }
////                }
////            }
////        }
//
//        @EventHandler(priority=EventPriority.LOW)
//        private void CommandListen(PlayerCommandPreprocessEvent evt) {
//            if(evt.isCancelled() || evt.getPlayer().isOp()){
//                return;
//            }
//            if (lobby.hasPlayer(evt.getPlayer())) {
//                String Command = evt.getMessage();
//                if (Command.equalsIgnoreCase("/csg")) {
//                    return;
//                }
//                String whiteList = "$whitelist_command$";
//                whiteList = whiteList.replace("[","").replace("]","");
//
//                for (String s : whiteList.split(", ")) {
//                    if (Command.equalsIgnoreCase("/" + s)) {
//                        return;
//                    }
//                }
//                evt.setCancelled(true);
//                evt.getPlayer().sendMessage(ChatColor.RED + "队列禁止使用本指令。");
//            }
//        }
//
////        @EventHandler(priority=EventPriority.MONITOR)
////        private void ChatListen(PlayerChatEvent evt) {
////            if(evt.isCancelled() || evt.getMessage().startsWith("/")){
////                return;
////            }
////            if(rule.chatInGroup() && g.hasPlayer(evt.getPlayer())){
////                evt.setCancelled(true);
////                String message = rule.ChatFormat();
////                message = Data.ColorChange(message);
////                message = message.replace("%player%", evt.getPlayer().getName());
////
////                message = message.replace("%group%", g.GetDisplay());
////                if(evt.getMessage().startsWith("!")){
////                    message = message.replace("%type%", "[所有人]");
////                    message = message.replace("%message%", evt.getMessage().substring(1));
////                    for(org.csg.group.Group g : g.byLobby.getGroupListI()){
////                        g.sendNotice(message);
////                    }
////                }else{
////                    message = message.replace("%message%", evt.getMessage());
////                    message = message.replace("%type%", "[队伍内]");
////                    g.sendNotice(message);
////                }
////
////            }
////        }
//
//    }
//
//}

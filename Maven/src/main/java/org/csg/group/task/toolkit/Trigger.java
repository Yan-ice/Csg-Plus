package org.csg.group.task.toolkit;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.csg.Data;
import org.csg.group.Group;
import org.csg.group.Lobby;
import org.csg.update.CycleUpdate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Trigger implements Listener, CycleUpdate {
	Lobby lobby;
	public Trigger(Lobby lobby){
		this.lobby = lobby;
	}

	@EventHandler(priority= EventPriority.HIGHEST)
	protected void Listen(PlayerChatEvent evt2) {
		if(evt2.getMessage().startsWith("/")){
			return;
		}
		lobby.callListener("onPlayerChat",evt2.getPlayer(),evt2.getMessage());
	}
	@EventHandler(priority= EventPriority.HIGHEST)
	protected void Listen(PlayerRespawnEvent evt2) {
		Location l = lobby.getSpawn(evt2.getPlayer());
		if(l!=null){
			evt2.setRespawnLocation(l);
		}
		lobby.callListener("onPlayerRespawn",evt2.getPlayer());

	}

	@EventHandler(priority= EventPriority.HIGHEST)
	protected void Listen(BlockBreakEvent evt2) {
		if(evt2.getPlayer()!=null){
			lobby.callListener("onPlayerBreakBlock",evt2.getPlayer(),new Object[]{evt2.getBlock().getType().name(),evt2.getBlock().getLocation()});
		}
	}

	@EventHandler(priority= EventPriority.HIGHEST)
	protected void Listen(EntityDamageEvent evt) {
		if (evt.getEntity() instanceof Player) {
			Player damaged = (Player) evt.getEntity();
			Player damager = null;
			if(evt instanceof EntityDamageByEntityEvent){
				damager = getDamager(((EntityDamageByEntityEvent)evt).getDamager());
			}
			lobby.callListener("onPlayerDamaged",damaged,new Object[]{evt.getDamage(),damager});
		}
	}

	@EventHandler(priority= EventPriority.HIGHEST)
	protected void pdeath(PlayerDeathEvent evt){
		lobby.callListener("onPlayerDeath",evt.getEntity());

		Player killer = evt.getEntity().getKiller();
		if(killer!=null){
			lobby.callListener("onKillPlayer",killer,new Object[]{evt.getEntity()});
		}
	}
	@EventHandler(priority=EventPriority.HIGHEST)
	void KillEntityListen(EntityDeathEvent evt) {
		if (!(evt.getEntity() instanceof ArmorStand)) {
			Player killer = evt.getEntity().getKiller();
			if(killer!=null && lobby.hasPlayer(killer)){
				if(!(evt.getEntity() instanceof Player)){
					lobby.callListener("onKillEntity",killer,new Object[]{evt.getEntity()});
				}
			}
		}
	}

	Set<String> cooldown = new HashSet<>();
	@EventHandler(priority=EventPriority.HIGHEST)
	void KillAllEntityListen(EntityDeathEvent evt) {
		if (evt.getEntity() != null && !(evt.getEntity() instanceof ArmorStand)) {
			final String en_name = evt.getEntity().getName();

			Player killer = evt.getEntity().getKiller();
			boolean clear = true;
			boolean total_clear = true;
			for(Entity e : evt.getEntity().getNearbyEntities(160, 160, 160)){
				if(!(e instanceof LivingEntity)){
					continue;
				}
				if(e instanceof Player){
					if(lobby.hasPlayer((Player)e)){
						killer = (Player)e;
					}
					continue;
				}
				if(!e.isDead()){
					total_clear = false;
					if(e.getName().equals(en_name)){
						clear = false;
					}
				}

			}
			if(!cooldown.contains(en_name)){
				if(clear){
					cooldown.add(en_name);
					lobby.callListener("onKillAllEntity", killer,en_name);
				}
				if(total_clear){
					cooldown.add(en_name);
					lobby.callListener("onKillAllEntity",killer,"all");
				}
			}

		}
	}


	@EventHandler
	void InteractListen(PlayerInteractEntityEvent evt) {
		if(evt.getRightClicked() instanceof LivingEntity){
			Player striker = evt.getPlayer();
			if(cooldown.contains(striker.getName())){
				return;
			}
			cooldown.add(striker.getName());
			LivingEntity striked = (LivingEntity) evt.getRightClicked();
			lobby.callListener("onInteract",striker,striked);
		}
	}

	@EventHandler
	void KillPlayerListen(PlayerInteractEvent evt) {

		if(evt.getClickedBlock()!=null){

			Player pl = evt.getPlayer();
			if(cooldown.contains(pl.getName())){
				return;
			}
			cooldown.add(pl.getName());
			lobby.callListener("onInteractBlock",pl,new Object[]{evt.getClickedBlock().getType().name(),evt.getClickedBlock().getLocation()});
		}

	}

	@EventHandler(priority=EventPriority.HIGH)
	public void Listen(PlayerCommandPreprocessEvent evt2) {
		if(!evt2.getMessage().startsWith("/")){
			return;
		}
		Player pl = evt2.getPlayer();
		String message = evt2.getMessage().substring(1);
		String[] args;
		if(message.contains(" ")){
			args = message.split(" ");
		}else{
			args = new String[]{message};
		}
		lobby.callListener("onSendCommand",pl,new Object[]{args});
	}
	@EventHandler
	void Listen(PlayerToggleSneakEvent evt2) {
		if(!evt2.getPlayer().isSneaking()){
			lobby.callListener("onPlayerSneak",evt2.getPlayer());
		}
	}

	private Player getDamager(Entity e){

		Player damager = null;
		if (e instanceof Player) {
			damager = (Player) e;
		} else if(e instanceof Projectile){
			if(((Projectile)e).getShooter() instanceof Player){
				damager = (Player)((Projectile)e).getShooter();
			}
		}
		return damager;
	}

	int count = 0;
	@Override
	public void onUpdate() {
		count++;
		if(count>4){
			List<UUID> ud = lobby.getPlayerList();
			for(Player pl : Bukkit.getOnlinePlayers()){
				if(ud.contains(pl.getUniqueId())){
					Location BlockWalk = new Location(pl.getLocation().getWorld(), pl.getLocation().getBlockX(),
							(pl.getLocation().getBlockY() - 1), pl.getLocation().getBlockZ());
					Block Blo = BlockWalk.getBlock();
					lobby.callListener("onPlayerWalk",pl,new Object[]{Blo.getType().name()});
				}

			}

			count = 0;
			cooldown.clear();
		}

	}
}

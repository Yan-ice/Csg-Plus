package org.csg.group.hologram;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class FwHologram {
	
	protected Map<String,ArmorStand> hdlist = new HashMap<>();
	

	/**
	 * 添加一个浮空字。
	 * 如果浮空字重名，会先删除原浮空字再用此替代它。
	 * 
	 */
	public void AddHologram(Location loc,String Name,String message) {
		if(hdlist.containsKey(Name)){
			hdlist.get(Name).remove();
			hdlist.replace(Name, SummonArmorStand(loc,message));
		}else{
			hdlist.put(Name, SummonArmorStand(loc,message));
		}
	}
	/**
	 * 添加一个浮空字。
	 * 如果浮空字重名，会先删除原浮空字再用此替代它。
	 * 
	 */
	public void EditHologram(String Name,String message) {
		if(hdlist.containsKey(Name)){
			hdlist.get(Name).setCustomName(message);
		}
	}
	/**
	 * 删除一个浮空字。
	 */
	public void DelHologram(String Name) {
		if(hdlist.containsKey(Name)){
			hdlist.get(Name).remove();
			hdlist.remove(Name);
		}
	}
	
	/**
	 * 清除所有浮空字。
	 */
	public Map<String,ArmorStand> Holograms() {
		return hdlist;
	}
	/**
	 * 清除所有浮空字。
	 */
	public void ClearHologram() {
		for(ArmorStand s : hdlist.values()){
			s.remove();
		}
		hdlist.clear();
	}
	
	ArmorStand SummonArmorStand(Location loc,String display){
		loc.setY(loc.getY()-1.5);
		ArmorStand a = loc.getWorld().spawn(loc, ArmorStand.class);
		a.setVisible(false);
		a.setCustomNameVisible(true);
		a.setCustomName(display);
		a.setGravity(false);
		return a;
	}

}

package org.csg.group.hologram;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class FwHologram {
	
	protected Map<String,ArmorStand> hdlist = new HashMap<>();
	

	/**
	 * ���һ�������֡�
	 * �������������������ɾ��ԭ���������ô��������
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
	 * ���һ�������֡�
	 * �������������������ɾ��ԭ���������ô��������
	 * 
	 */
	public void EditHologram(String Name,String message) {
		if(hdlist.containsKey(Name)){
			hdlist.get(Name).setCustomName(message);
		}
	}
	/**
	 * ɾ��һ�������֡�
	 */
	public void DelHologram(String Name) {
		if(hdlist.containsKey(Name)){
			hdlist.get(Name).remove();
			hdlist.remove(Name);
		}
	}
	
	/**
	 * ������и����֡�
	 */
	public Map<String,ArmorStand> Holograms() {
		return hdlist;
	}
	/**
	 * ������и����֡�
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

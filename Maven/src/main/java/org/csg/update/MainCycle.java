package org.csg.update;

import org.csg.Data;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class MainCycle extends BukkitRunnable {
	private static MainCycle instance;

	static{
		instance = new MainCycle();
		instance.runTaskTimer(Data.fmain, 1, 1);
	}
	public static Set<CycleUpdate> meth = new HashSet<CycleUpdate>();

	public static void registerCall(CycleUpdate call){
		meth.add(call);
	}
	public static void unRegisterCall(CycleUpdate call){
		meth.remove(call);
	}

	@Override
	public void run() {
		if(meth.size()>0){
			Set<CycleUpdate> call = new HashSet<CycleUpdate>(meth);
			for(CycleUpdate c : call){
				c.onUpdate();
			}
		}
	}

}

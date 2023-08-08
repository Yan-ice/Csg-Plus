package org.csg.update;

import org.bukkit.scheduler.BukkitRunnable;
import org.csg.Fwmain;

import java.util.HashSet;
import java.util.Set;

public class SecondCycle extends BukkitRunnable {
	private static SecondCycle instance;

	static{
		instance = new SecondCycle();
		instance.runTaskTimer(Fwmain.getInstance(), 20, 20);
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

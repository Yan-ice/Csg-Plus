package org.csg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import org.csg.group.task.ValueData;

public class Data {
	public static String worldpath = "";
	public static Location defaultLocation = new Location(Bukkit.getWorlds().get(0),0,80,0);

	public static String Version = "Beta1.6";

	public static Random random = new Random();
	public static boolean onDisable = false;
	public static boolean HighMCVersion = true;
	

	
	public static boolean debug = false;
	public static boolean isPaper = false;

	public static Fwmain fmain = null;

	public static List<File> bukkit_core = new ArrayList<>();

	public static File lobbyDir;
	public static File optionFile;
	public static FileConfiguration optionFileConf;
	public static FileConfiguration LanguageFileConf;

	public static boolean LoadWhenJoin = false;
	
	public static ValueData data;

	public static void ConsoleInfo(String info) {
		fmain.getLogger().info(info);
	}
	public static void ConsoleError(String info) {
		fmain.getLogger().info(ChatColor.RED+"ERROR: "+info);
	}

	
	public static void LoadOption() {
		try {
			debug = optionFileConf.getBoolean("Debug");

			if(optionFileConf.contains("DefaultLoc")){
				defaultLocation = (Location)optionFileConf.get("DefaultLoc");
			}

			if(optionFileConf.contains("IsPaperSpigot")){
				isPaper = optionFileConf.getBoolean("IsPaperSpigot");
			}

			if(optionFileConf.contains("HighMCVersion")){
     	    	Data.HighMCVersion = optionFileConf.getBoolean("HighMCVersion");
     	    }
			LoadWhenJoin = false;
			//LoadWhenJoin = optionFileConf.getBoolean("LoadWhenJoin");

			//获取配置文件中的Depends
			Fwmain.setOptionDepends(optionFileConf.getStringList("Depends"));

		} catch (NullPointerException x) {
			return;
		}
	}

	public static int Random(int a, int b) {
		int s;
		int length;
		if (a > b) {
			length = a - b;
			s = random.nextInt(length) + b;
		} else if (a < b) {
			length = b - a;
			s = random.nextInt(length) + a;
		} else {
			return a;
		}
		return s;
	}

	public static String ColorChange(String str) {
		return str.replace("&", "§");
	}

	public static List<String> ColorChange(List<String> str) {
		for (int a = 0; a < str.size(); a++) {
			str.set(a, ColorChange(str.get(a)));
		}
		return str;
	}

	public static void save() {
		try {
			optionFileConf.save(optionFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void ConsoleCommand(String command) {
		Bukkit.dispatchCommand(Data.fmain.getServer().getConsoleSender(), command);
	}

	public static void Debug(String str){
		if(debug){
			ConsoleInfo("[Debug] "+str);
		}
	}

}

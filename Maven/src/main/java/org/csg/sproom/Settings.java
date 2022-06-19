package org.csg.sproom;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.csg.Data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    Map<String,String> gamerule = new HashMap<>();
    boolean allowBuilding = false;
    boolean PvP = false;
    String difficulty = "easy";
    int maxReflect = 5;
    Location leaveLoc = Data.defaultLocation;
    public Settings(){
        gamerule.put("keepInventory","true");
        gamerule.put("CommandBlockOutput","false");
    }
    public Settings(File f){
        loadConfig(f);
        saveConfig(f);
    }
    public void loadConfig(File f){
        FileConfiguration config = Data.fmain.load(f);
        if(config.contains("maxReflect")){
            maxReflect = config.getInt("maxReflect");
        }

        if(config.contains("allowBuilding")){
            allowBuilding = config.getBoolean("allowBuilding");
        }
        if(config.contains("leaveLoc")){
            leaveLoc = (Location)config.get("leaveLoc");
        }
        if(config.contains("PvP")){
            PvP = config.getBoolean("PvP");
            //properties.setBoolean(SlimeProperties.PVP,config.getBoolean("PvP"));
        }
        if(config.contains("Difficulty")){
            difficulty = config.getString("Difficulty");
        }
        if(config.contains("gameRule")){
            ConfigurationSection sc = config.getConfigurationSection("gameRule");

            for(String s : sc.getKeys(false)){
                gamerule.put(s,sc.getString(s));
            }
        }

    }

    public void saveConfig(File f){
        FileConfiguration config = Data.fmain.load(f);
        config.set("maxReflect",maxReflect);
        config.set("allowBuilding",allowBuilding);
        //config.set("leaveLoc",leaveLoc);
        config.set("PvP",PvP);
        config.set("Difficulty",difficulty);
        for(Map.Entry<String,String> e : gamerule.entrySet()){
            config.set("gameRule."+e.getKey(),e.getValue());
        }
        try {
            config.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package org.csg.group.task;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Macro {
    public Map<String,Object> macros = new HashMap<String,Object>();

    public void AddMacro(String key,Object obj){
        if(macros.containsKey(key)){
            macros.replace(key,obj);
        }else{
            macros.put(key,obj);
        }
    }

    /**
     * 0:无配置 1:有配置为null 2:有配置
     * @param key
     * @return
     */
    public int HasMacro(String key){
        if(!macros.containsKey(key)){
            return 0;
        }
        Object obj = macros.get(key);
        if(obj==null){
            return 1;
        }
        if(obj instanceof String){
            if("null".equals(obj)){
                return 1;
            }
        }
        return 2;
    }
    public void LoadMacro(ConfigurationSection config){
        for(String key : config.getKeys(false)){

            if(config.isList(key)) {
                List<String> try1 = config.getStringList(key);
                macros.put(key, try1.toArray(new String[0]));
            }else
            if(config.isDouble(key)){
                macros.put(key,config.getDouble(key));
            }else
            if(config.isInt(key)){
                macros.put(key,config.getInt(key));
            }else
            if(config.isString(key)){
                macros.put(key,config.getString(key));
            }else
            if(config.isConfigurationSection(key)){
                LoadMacro(config.getConfigurationSection(key));
            }
        }
    }
}

package org.csg.group.task;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.csg.Utils.CommonUtils;
import org.csg.group.Group;
import org.csg.group.task.toolkit.TaskExecuter;
import org.csg.location.Teleporter;

import java.util.*;

public class VarTable {
    // 全局宏
    public Map<String,Object> macros = new HashMap<String,Object>();
    // 玩家/全局分数
    public Map<String,Map<UUID, Double>> scores = new HashMap<>();
    // 玩家键值对
    public Map<String,Map<UUID, String>> values = new HashMap<>();
    public Map<TaskExecuter, Map<String,Object>> variables = new HashMap<>();

    public void AddMacro(String key,Object obj){
        if(macros.containsKey(key)){
            macros.replace(key,obj);
        }else{
            macros.put(key,obj);
        }
    }

    /**
     * 添加玩家键值对
     * @param key
     * @param uuid
     * @param value
     */
    public void addValues(String key, UUID uuid, String value){
        // 如果已经存在这个键值对，就替换
        if(values.containsKey(key)){
            values.get(key).put(uuid,value);
        }else{
            // 如果不存在这个键值对，就新建
            Map<UUID, String> map = new HashMap<>();
            map.put(uuid,value);
            values.put(key,map);
        }
    }

    /**
     * 移除玩家键值对
     * @param key 键
     * @param uuid 玩家
     * @return 是否成功移除
     */
    public boolean removeValues(String key, UUID uuid) {
        // 如果存在这个键值对，就移除
        if (values.containsKey(key)) {
            values.get(key).remove(uuid);
            return true;
        }
        return false;
    }

    /**
     * 获得玩家键值对
     * @param key 键
     * @param uuid 玩家
     * @return 值
     */
    public String getValues(String key, UUID uuid){
        if(values.containsKey(key)){
            return values.get(key).get(uuid);
        }
        return null;
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


    public void LoadMacro(ConfigurationSection config, String parentKey){
        for(String key : config.getKeys(false)){
            Object value;
            if(config.isList(key)) {
                value = config.getStringList(key);
            }else
            if(config.isDouble(key)){
                value = config.getDouble(key);
            }else
            if(config.isInt(key)){
                value = config.getInt(key);
            }else
            if(config.isString(key)){
                value = config.getString(key);
            }else
            if(config.isConfigurationSection(key)){
                String childPAth = key;
                if(parentKey != null && !parentKey.isEmpty()) {
                    childPAth = parentKey + "." + childPAth;
                }
                LoadMacro(config.getConfigurationSection(key), childPAth);
                continue;
            }else{
                value = config.get(key);
            }

            if(parentKey != null && !parentKey.isEmpty()) {
                key = parentKey + "." + key;
            }
            macros.put(key, value);
        }

    }

    public void AddVariable(TaskExecuter ex, String key, Object obj){
        if(!variables.containsKey(ex)){
            variables.put(ex, new HashMap<>());
        }
        Map<String,Object> vari = variables.get(ex);

        if(vari.containsKey(key)){
            vari.replace(key,obj);
        }else{
            vari.put(key,obj);
        }
    }

    public void CleanExecuter(TaskExecuter ex){
        Map<String,Object> vari = variables.get(ex);
        if(vari!=null){
            vari.clear();
            variables.remove(ex);
        }

    }

    public void AddScore(Player p, String key, double score){
        if(scores.containsKey(key)){
            Map<UUID, Double> m = scores.get(key);
            if(m.containsKey(p.getUniqueId())){
                m.replace(p.getUniqueId(),score);
            }else{
                m.put(p.getUniqueId(),score);
            }
        }else{
            scores.put(key,new HashMap<UUID, Double>());
            AddScore(p,key,score);
        }
    }

    /**
     * 递增分数
     * @param incr
     */
    public void incrScore(Player p, String key, double incr) {
        // 判断是否存在这个分数变量
        if(scores.containsKey(key)) {
            // 获取对应的分数变量
            Map<UUID, Double> m = scores.get(key);
            if(m.containsKey(p.getUniqueId())) {
                // 替换值
                m.replace(p.getUniqueId(), m.get(p.getUniqueId()) + incr);
            }else{
                // 新建值
                m.put(p.getUniqueId(), incr);
            }
        }else{
            // 如果不存在这个分数变量，就新建
            scores.put(key,new HashMap<UUID, Double>());
            incrScore(p,key,incr);
        }
    }

    public double getScore(Player p, String key) {
        if(scores.containsKey(key)) {
            Map<UUID, Double> m = scores.get(key);
            if(m.containsKey(p.getUniqueId())) {
                return m.get(p.getUniqueId());
            }
        }
        return 0;
    }

    public static String objToString(Object origin){
        if(origin==null){
            return "[null]";
        }
        if(origin instanceof LivingEntity){
            LivingEntity en = (LivingEntity)origin;
            return en.getName();
        }

        if(origin instanceof Location){
            Location loc = (Location)origin;
            return Teleporter.locToString(loc);
        }

        if(origin instanceof String[]){
            String[] acc = (String[])origin;
            return Arrays.toString(acc);
        }
        if(origin instanceof Double){
            double d = (Double)origin;
            if(d % 1 ==0){
                return ""+(int)d;
            }else{
                return String.format("%.2f",d);
            }
        }
        if(origin instanceof Integer){
            return ""+origin;
        }

        if(origin instanceof Group){
            return ((Group)origin).getName();
        }
        if(origin instanceof String){
            return (String)origin;
        }
        return "[不可序列化]";
    }
    public Object getValue(Player p, String key){
        return getValue(p, key, null);
    }
    public Object getValue(Player p, String key, TaskExecuter executer){
        if(key.contains(".")){
            String[] pr = key.split("\\.",2);
            Object o = getValue(p,pr[0],executer);
            return getMember(o,pr[1]);
        }

        Map<String,Object> vr = variables.get(executer);
        if(vr!=null) {
            if(vr.get(key)!=null){
                return vr.get(key);
            }
        }

        Object value = null;
        if(p!=null){
            Map<UUID, Double> m = scores.get(key);
            if(m!=null){
                value = m.get(p.getUniqueId());
            }
            if(value!=null) return value;
        }

        value = macros.get(key);
        if(value!=null) return value;

        return "";
    }

    private Object getMember(Object origin, String memberKey){
        if(memberKey.contains(".")){
            String[] pr = memberKey.split("\\.",2);
            Object mb = getMember(origin, pr[0]);
            return getMember(mb,pr[1]);
        }

        if(origin instanceof LivingEntity){
            LivingEntity en = (LivingEntity)origin;
            switch(memberKey){
                case "health": return en.getHealth();
                case "max_health": return en.getMaxHealth();
                case "name": return en.getName();
                case "location": return en.getLocation();
            }

            if(en instanceof Player){
                Player pl = (Player)en;
                switch(memberKey){
                    case "level": return pl.getLevel();
                    case "food": return pl.getFoodLevel();
                    case "display": return pl.getDisplayName();
                }
            }
        }

        if(origin instanceof Location){
            Location loc = (Location)origin;
            switch(memberKey){
                case "x": return loc.getX();
                case "y": return loc.getY();
                case "z": return loc.getZ();
                case "world": return loc.getWorld().getName();
            }
        }

        if(origin instanceof String[]){
            String[] acc = (String[])origin;
            if(memberKey.equals("length")){
                return acc.length;
            }
            if(memberKey.equals("random")){
                return acc[CommonUtils.Random(0,acc.length)];
            }
            for(int a = acc.length-1;a>=0;a--){
                if(memberKey.equals(a+"")){
                    return acc[a];
                }
            }
        }

        if (origin instanceof List) {
            List list = (List) origin;
            if (memberKey.equals("length")) {
                return list.size();
            }
            if (memberKey.equals("random")) {
                return list.get(CommonUtils.Random(0, list.size()));
            }
            for (int a = list.size() - 1; a >= 0; a--) {
                if (memberKey.equals(a + "")) {
                    return list.get(a);
                }
            }
        }

        if(origin instanceof Double){
            double d = (Double)origin;
            String v;
            if(d % 1 ==0){
                return (int)d;
            }else{
                return d;
            }
        }
        return "[未知变量]";
    }
}

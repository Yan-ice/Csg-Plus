package org.csg.group.task.toolkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VariableBox {
	Map<String,Object> values = new HashMap<>();

	public void declare(String key,Object value){
		if(value==null){
			values.remove(key);
			return;
		}

		if(values.containsKey(key)){
			values.replace(key,value);
		}else{
			values.put(key, value);
		}
	}

	public Object read(String key){
		if(values.containsKey(key)) {
			return values.get(key);
		}
		return null;
	}

	public Set<String> ValueList(){
		return values.keySet();
	}
	public Set<Map.Entry<String,Object>> EntryList(){
		return values.entrySet();
	}
}

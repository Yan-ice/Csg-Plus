package org.csg.group.task.value;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValueBoard implements customgo.ValueBoard  {
	public Map<String,Double> value = new HashMap<>();

	public Map<String,Double> getValueList(){
		return value;
	}
	public void Value(String Name,double Value){
		if(value.containsKey(Name)){
			value.remove(Name);
		}
		value.put(Name, Value);
	}

	public void ValueAdd(String Name,double Value){
		if(value.containsKey(Name)){

			value.replace(Name, value.get(Name)+Value);
		}else{
			value.put(Name, Value);
		}
	}


	public double getValue(String Name){
		if(value.containsKey(Name)){
			return value.get(Name);
		}
		return 0;
	}


	public void removeValue(String Name){
		if(value.containsKey(Name)){
			value.remove(Name);
		}
	}

	/**
	 * ��üƷְ��ϵ����б�����
	 * @return �����б�
	 */
	public Set<String> ValueList(){
		return value.keySet();
	}
}

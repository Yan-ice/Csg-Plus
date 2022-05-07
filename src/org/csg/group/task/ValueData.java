package org.csg.group.task;

import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;

import org.csg.Data;

public class ValueData {
	FileConfiguration data;
	public ValueData(FileConfiguration Data){
		data = Data;
	}
	public void Value(String Name,String Value){
		data.set(Name, Value);
	}
	public void Save(){
		try {
			data.save(Data.fmain.data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getValue(String Name){
		if(data.contains(Name) && data.get(Name) instanceof String){
			return data.getString(Name);
		}
		return "null";
	}
	public void removeValue(String Name){
		if(data.contains(Name)){
			data.set(Name, null);
		}
	}
}

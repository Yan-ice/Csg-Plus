package org.csg.sproom;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import org.bukkit.ChatColor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsgSlimeLoader implements SlimeLoader {
    byte[] dataSource = null;
    File source;

    public CsgSlimeLoader(File source){
        this.source = source;
    }

    @Override
    public byte[] loadWorld(String s, boolean readOnly) throws UnknownWorldException, WorldInUseException, IOException {
        if(dataSource==null && source.exists()){
            FileInputStream os = new FileInputStream(source);
            dataSource = new byte[os.available()];
            os.read(dataSource);
            os.close();
        }
        CommonUtils.ConsoleInfoMsg(ChatColor.BLUE+"Csg-Plus SpRoom world loaded.");
        return dataSource;
    }

    @Override
    public boolean worldExists(String s) throws IOException {
        return false;
    }

    @Override
    public List<String> listWorlds() throws IOException {
        return new ArrayList<>();
    }

    @Override
    public void saveWorld(String s, byte[] bytes, boolean b) throws IOException {

        if(source.exists()){
            source.delete();
        }
        source.createNewFile();
        FileOutputStream os = new FileOutputStream(source);
        os.write(bytes);
        os.flush();
        os.close();
        dataSource = bytes.clone();
        CommonUtils.ConsoleInfoMsg(ChatColor.BLUE+"Csg-Plus SpRoom world saved.");
    }


    @Override
    public void unlockWorld(String s) throws UnknownWorldException, IOException {
        return;
    }

    @Override
    public boolean isWorldLocked(String s) throws UnknownWorldException, IOException {
        return false;
    }

    @Override
    public void deleteWorld(String s) throws UnknownWorldException, IOException {
        if(source.exists()){
            source.delete();
        }
        return;
    }
}

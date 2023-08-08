package org.csg.cmd;

import org.bukkit.command.PluginCommand;
import org.csg.cmd.label.*;
import java.util.*;

public class CsgCmd extends RootCmd {
    public CsgCmd(PluginCommand pluginCmd) {
        super(pluginCmd);
        root = pluginCmd.getName();
        branchs = new ArrayList<>();
        branchs.addAll(Arrays.asList(
                new Skip(this),
                new ShowList(this),
                new Reload(this),
                new Leave(this),
                new Stop(this),
                new Load(this),
                new Unload(this),
                new Join(this),
                new Status(this),
                new Seril(this)
        ));
    }
}


package org.csg.sproom;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.csg.group.Lobby;

import java.util.List;


public class TemporaryWorldEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final World world;

    @Getter
    private final Lobby sourceLobby;

    @Getter
    private final List<Player> waitPlayer;

    public TemporaryWorldEvent(World world, Lobby sourceLobby, List<Player> waitPlayer) {
        this.world = world;
        this.sourceLobby = sourceLobby;
        this.waitPlayer = waitPlayer;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}

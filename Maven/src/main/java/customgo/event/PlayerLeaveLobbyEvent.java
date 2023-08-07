package customgo.event;

import customgo.LobbyAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerLeaveLobbyEvent extends PlayerEvent{

    private static final HandlerList handlers = new HandlerList();

    private LobbyAPI lobby;
    public PlayerLeaveLobbyEvent(Player who, LobbyAPI lb) {
        super(who);
        lobby = lb;
    }

    public LobbyAPI getLobby(){
        return lobby;
    }
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}

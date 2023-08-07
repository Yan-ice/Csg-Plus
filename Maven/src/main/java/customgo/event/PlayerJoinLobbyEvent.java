package customgo.event;

import customgo.LobbyAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerJoinLobbyEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private LobbyAPI lobby;
    public PlayerJoinLobbyEvent(Player who, LobbyAPI lb) {
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

    boolean cancel = false;
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        cancel = true;
    }
}

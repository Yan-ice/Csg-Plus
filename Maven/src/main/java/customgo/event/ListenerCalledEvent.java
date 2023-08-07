package customgo.event;

import customgo.LobbyAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ListenerCalledEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private LobbyAPI lobby;
    private Player player;
    private Object[] args;
    private String listener;
    public ListenerCalledEvent(String li, LobbyAPI lb, Player p, Object[] a) {
        super();
        listener = li;
        lobby = lb;
        player = p;
        args = a;
    }

    public LobbyAPI getLobby(){
        return lobby;
    }
    public Player getStriker(){
        return player;
    }
    public Object[] getArgs(){
        return args;
    }
    public Object[] getListenerName(){
        return args;
    }


    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}

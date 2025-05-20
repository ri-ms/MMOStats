package com.klrir.mmoStats.API.PlayerEvent;

import com.klrir.mmoStats.game.GamePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GamePlayerEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final GamePlayer player;
    public GamePlayerEvent(@NotNull GamePlayer player){
        this.player = player;
    }
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList(){
        return HANDLERS;
    }
    public @NotNull GamePlayer getPlayer(){
        return player;
    }
}

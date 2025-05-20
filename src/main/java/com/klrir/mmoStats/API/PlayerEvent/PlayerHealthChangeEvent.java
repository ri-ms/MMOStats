package com.klrir.mmoStats.API.PlayerEvent;

import com.klrir.mmoStats.API.HealthChangeReason;
import com.klrir.mmoStats.game.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerHealthChangeEvent extends Event implements Cancellable {
    private boolean isCancelled = false;
    private static final HandlerList HANDLERS = new HandlerList();
    private final GamePlayer player;
    @Setter
    @Getter
    private int healthChangeAmount;
    private final HealthChangeReason reason;

    public PlayerHealthChangeEvent(GamePlayer player, int health, HealthChangeReason reason){
        this.player = player;
        healthChangeAmount = health;
        this.reason = reason;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

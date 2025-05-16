package com.klrir.mmoStats.API.PlayerEvent;

import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.game.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GetTotalStatEvent extends GamePlayerEvent implements Cancellable {
    @Getter
    private final Stats stat;
    @Setter
    @Getter
    private double value;
    @Getter
    private double multiplier = 1;
    private boolean isCancelled = false;

    public GetTotalStatEvent(GamePlayer player, Stats stats, double value) {
        super(player);
        stat = stats;
        this.value = value;

    }

    public void addMultiplier(double d) {
        multiplier *= d;
    }
    public void addValue(double d) {
        value += d;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return null;
    }
}
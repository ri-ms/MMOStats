package com.klrir.mmoStats.API.PlayerEvent;

import com.klrir.mmoStats.entities.EntityHandler;
import com.klrir.mmoStats.game.Calculator;
import com.klrir.mmoStats.game.GamePlayer;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

@Getter
public class GameDamagePlayerToEntityExecuteEvent extends PlayerEvent{
    private static final HandlerList HANDLERS = new HandlerList();
    private final Calculator calculator;
    @Getter
    private final LivingEntity entity;
    @Getter
    private final int hits;
    public GameDamagePlayerToEntityExecuteEvent(GamePlayer player, LivingEntity e, Calculator c) {
        super(player);
        calculator = c;
        entity = e;
        hits = EntityHandler.getOrDefault("hit", e, PersistentDataType.INTEGER, 0);
    }
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
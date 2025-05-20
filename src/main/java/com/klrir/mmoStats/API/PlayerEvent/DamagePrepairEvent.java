package com.klrir.mmoStats.API.PlayerEvent;

import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.entities.EntityHandler;
import com.klrir.mmoStats.game.Calculator;
import com.klrir.mmoStats.game.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Getter
public class DamagePrepairEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final LivingEntity entity;
    private double preMultiplier;
    private double postMultiplier;
    private final Calculator calculator;
    private final HashMap<Stats, Double> stats;
    @Setter
    private double weaponDamage;
    private final int hits;
    public DamagePrepairEvent(GamePlayer player, LivingEntity entity, Calculator calculator, double pre, double post, HashMap<Stats, Double> stats, double weaponDamage) {
        super(player);
        this.entity = entity;
        this.calculator = calculator;
        preMultiplier = pre;
        postMultiplier = post;
        this.stats = stats;
        this.weaponDamage = weaponDamage;
        hits = EntityHandler.getOrDefault("hit", entity, PersistentDataType.INTEGER, 0);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList(){
        return HANDLERS;
    }
    public void addPreMultiplier(double d){
        preMultiplier+=d;
    }

    public void addPostMultiplier(double d){
        postMultiplier*=d;
    }

}
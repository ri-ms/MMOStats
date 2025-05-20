package com.klrir.mmoStats.API.ItemEvent;

import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.game.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public class GetStatFromItemEvent extends ItemStackEvent{
    @Getter
    private final Stats stat;
    @Getter
    private double value;
    private final GamePlayer player;
    @Getter
    @Setter
    private double multiplier = 1;
    public GetStatFromItemEvent(ItemStack item, Stats stats, double value, GamePlayer player) {
        super(item);
        stat = stats;
        this.value = value;
        this.player = player;

    }
    public void addValue(double i){
        value += i;
    }
}
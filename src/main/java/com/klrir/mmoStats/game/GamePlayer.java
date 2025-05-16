package com.klrir.mmoStats.game;

import com.klrir.mmoStats.API.PlayerEvent.GetTotalStatEvent;
import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.swing.plaf.synth.Region;
import java.util.HashMap;

public abstract class GamePlayer {
    private static HashMap<Player, GamePlayer> players = new HashMap<>();
    private final Player player;

    public double healingMulti = 1;
    public int currhealth;
    public double currmana;

    private HashMap<Stats, Double> baseStat = new HashMap<>();

    public int absorbtion;

    @Getter
    private double manaRegenMult = 1;
    private boolean hasDeployableEffect = false;
    public int speedCap = 500;
    private double rawDamageMult = 1;

    @Getter
    @Setter
    private Region region;

    @Getter
    private int magicalpower = 0;

    public GamePlayer(Server server, Player player){
        this(server, player, false);
    }
    public GamePlayer(Server server, Player player, boolean isChecked) {
        this.player = player;
        players.put(player, this);
        for (Stats stat : Stats.values()){
            if (stat == Stats.WeaponDamage) continue;
            double value = stat.getMaxAmount();
            setBaseStat(stat, value);
        }
        currmana = (int) MMOStats.getPlayerStat(this, Stats.Inteligence);
        currhealth = (int) MMOStats.getPlayerStat(this, Stats.Health);
    }

    public double getBaseStat(Stats stat) {
        return baseStat.getOrDefault(stat, 0d);
    }

    public void setBaseStat(Stats stat, double value) {
        baseStat.put(stat, value);
    }

    public ItemStack getItemInHand(){
        return this.player.getInventory().getItemInMainHand();
    }

    public synchronized double getStat(Stats stat) {
        double value = this.getBaseStat(stat);
        GetTotalStatEvent event = new GetTotalStatEvent(this, stat, value);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return stat.getBaseAmount();
        value = event.getValue() * event.getMultiplier();
        return value;
    }

    public static GamePlayer getGamePlayer(Player player){
        return players.get(player);
    }

    public void setMana(double value) {
        currmana = value;
        double max = MMOStats.getPlayerStat(this, Stats.Inteligence);
        if (currmana > max) currmana = max;
    }
}

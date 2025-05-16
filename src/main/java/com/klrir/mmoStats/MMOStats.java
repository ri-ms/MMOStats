package com.klrir.mmoStats;

import com.klrir.mmoStats.API.PlayerEvent.PlayerManaRegenEvent;
import com.klrir.mmoStats.game.GamePlayer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class MMOStats extends JavaPlugin {

    @Getter
    private static MMOStats instance;

    private BukkitRunnable statrunnable;
    public FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        // Plugin startup logic
        config.addDefault("StatSystem", true);
        config.options().copyDefaults(true);
        saveConfig();
        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void Stats(){
        statrunnable = new BukkitRunnable() {
            @Override
            public void run(){
                Bukkit.getOnlinePlayers().forEach(p -> {
                   GamePlayer player = GamePlayer.getGamePlayer(p);
                   double mana = getPlayerStat(player, Stats.Inteligence);
                   if (player.currmana < mana) {
                       double manaadd = ((mana * 0.02) * player.getManaRegenMult());
                       double finalmana = manaadd + player.currmana;
                       PlayerManaRegenEvent event = new PlayerManaRegenEvent(player, mana, player.currmana, finalmana);
                       Bukkit.getPluginManager().callEvent(event);
                       if (!event.isCancelled()){
                           player.setMana((int) mana);
                       }
                   }
                });
            }
        };
        statrunnable.runTaskTimer(this, 0, 20);
    }

    public synchronized static double getPlayerStat(GamePlayer player, Stats stat) {
        return player.getStat(stat);
    }
}

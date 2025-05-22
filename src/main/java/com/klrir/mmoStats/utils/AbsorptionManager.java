package com.klrir.mmoStats.utils;

import com.klrir.mmoStats.API.HealthChangeReason;
import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AbsorptionManager {

    /**
     * Aplica efeito de absorção ao jogador por um determinado tempo
     */
    public static void applyAbsorptionEffect(Player player, int times) {
        GamePlayer p = GamePlayer.getGamePlayer(player);
        PlayerStatsManager.updateHealthBar(p);

        MMOStats.getAbsorbtionrunntime().replace(player, MMOStats.getAbsorbtionrunntime().get(player) + times);

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().contains(player)) {
                    MMOStats.getAbsorbtionrunntime().replace(player, MMOStats.getAbsorbtionrunntime().get(player) - 1);

                    if (MMOStats.getAbsorbtionrunntime().get(player) <= 0) {
                        double health = MMOStats.getPlayerStat(p, Stats.Health);
                        if (MMOStats.getAbsorbtion().get(player) + p.currhealth > health) {
                            p.setHealth(health, HealthChangeReason.Ability);
                        } else {
                            p.setHealth((MMOStats.getAbsorbtion().get(player) + p.currhealth) * p.healingMulti, HealthChangeReason.Ability);
                        }
                        MMOStats.getAbsorbtion().replace(player, 0);
                        PlayerStatsManager.updateHealthBar(p);
                        MMOStats.getAbsorbtionrunntime().replace(player, 0);
                        return;
                    } else {
                        applyAbsorptionEffect(player, 0);
                    }
                }
            }
        };

        runnable.runTaskLater(MMOStats.getInstance(), 20);
    }
}
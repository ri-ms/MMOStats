package com.klrir.mmoStats.utils;

import com.klrir.mmoStats.API.HealthChangeReason;
import com.klrir.mmoStats.API.PlayerEvent.PlayerManaRegenEvent;
import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerStatsManager {

    /**
     * Atualiza a barra de vida do jogador com base em seus atributos
     */
    public static void updateHealthBar(GamePlayer player) {
        if (MMOStats.getDeathPersons().contains(player)) return;

        if (player.currhealth <= 0) {
            MMOStats.getDeathPersons().add(player);
            assert player.getPlayer() != null;
            player.getPlayer().damage(Float.MAX_VALUE);
            return;
        }

        double maxhealth = MMOStats.getPlayerStat(player, Stats.Health);
        updateMaxHealth(player, maxhealth);
        updateAbsorptionDisplay(player);
    }

    /**
     * Atualiza o valor máximo de saúde do jogador com base no valor de Health
     */
    private static void updateMaxHealth(GamePlayer player, double maxhealth) {
        if (maxhealth < 125) {
            player.setMaxHealth(20);
        } else if (maxhealth < 165) {
            player.setMaxHealth(22);
        } else if (maxhealth < 230) {
            player.setMaxHealth(24);
        } else if (maxhealth < 300) {
            player.setMaxHealth(26);
        } else if (maxhealth < 400) {
            player.setMaxHealth(28);
        } else if (maxhealth < 500) {
            player.setMaxHealth(30);
        } else if (maxhealth < 650) {
            player.setMaxHealth(32);
        } else if (maxhealth < 800) {
            player.setMaxHealth(34);
        } else if (maxhealth < 1000) {
            player.setMaxHealth(36);
        } else if (maxhealth < 1250) {
            player.setMaxHealth(38);
        } else if (maxhealth >= 1250) {
            player.setMaxHealth(40);
        }
    }

    /**
     * Atualiza a exibição de absorção do jogador
     */
    private static void updateAbsorptionDisplay(GamePlayer player) {
        Player p = player.getPlayer();
        if (p == null || !MMOStats.getAbsorbtion().containsKey(p)) return;

        int abs = MMOStats.getAbsorbtion().get(p);
        if (abs == 0) {
            player.setAbsorptionAmount(0);
        } else if (abs < 0) {
            player.setAbsorptionAmount(2);
        } else if (abs < 165) {
            player.setAbsorptionAmount(4);
        } else if (abs < 230) {
            player.setAbsorptionAmount(6);
        } else if (abs < 300) {
            player.setAbsorptionAmount(8);
        } else if (abs < 400) {
            player.setAbsorptionAmount(10);
        } else if (abs < 500) {
            player.setAbsorptionAmount(12);
        } else if (abs < 650) {
            player.setAbsorptionAmount(14);
        } else if (abs < 800) {
            player.setAbsorptionAmount(16);
        } else if (abs < 1000) {
            player.setAbsorptionAmount(18);
        } else if (abs < 1250) {
            player.setAbsorptionAmount(20);
        } else {
            player.setAbsorptionAmount(22);
        }
    }

    /**
     * Processa a regeneração de mana do jogador
     */
    public static void processManaRegeneration(GamePlayer player) {
        double mana = MMOStats.getPlayerStat(player, Stats.Inteligence);
        if (player.currmana < mana) {
            double manaadd = ((mana * 0.02) * player.getManaRegenMult());
            double finalmana = manaadd + player.currmana;
            PlayerManaRegenEvent event = new PlayerManaRegenEvent(player, mana, player.currmana, finalmana);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                player.setMana((int) mana);
            }
        } else if (player.currmana > mana) {
            player.setMana((int) mana);
        }
    }

    /**
     * Processa a regeneração de vida do jogador
     */
    public static void processHealthRegeneration(GamePlayer player) {
        double health = MMOStats.getPlayerStat(player, Stats.Health);
        if (player.currhealth < health) {
            int healthadd = (int) (health * 0.015);
            int finalhealth = (int) (player.currhealth + (healthadd * player.healingMulti));
            player.setHealth(finalhealth, HealthChangeReason.Regenerate);
        } else if (player.currhealth > health) {
            player.setHealth(health, HealthChangeReason.Regenerate);
        }
    }

    /**
     * Atualiza a velocidade do jogador com base no atributo Speed
     */
    public static void updatePlayerSpeed(GamePlayer player) {
        float speedpersentage = (float) MMOStats.getPlayerStat(player, Stats.Speed) / 100;
        if (speedpersentage > 5) speedpersentage = 5;
        player.setWalkSpeed((float) 0.2 * speedpersentage);
    }
}

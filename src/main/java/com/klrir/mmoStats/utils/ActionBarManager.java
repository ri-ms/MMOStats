package com.klrir.mmoStats.utils;

import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.game.GamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class ActionBarManager {

    /**
     * Atualiza a barra de ação do jogador com informações de status
     */
    public static void updateActionBar(GamePlayer player) {
        if (MMOStats.getDeathPersons().contains(player)) return;

        double health = player.currhealth;
        float estimated = (float) ((health / player.getMaxHealth()) * 100);

        if (estimated < 0) estimated = 0;
        if (estimated > player.getMaxHealth()) estimated = (float) player.getMaxHealth();

        assert player.getPlayer() != null;

        String stackMsg = "";
        String afterManaString = "";
        String extraafterdef = "";

        // Obter estatísticas do jogador
        health = MMOStats.getPlayerStat(player, Stats.Health);
        double defense = MMOStats.getPlayerStat(player, Stats.Defense);
        double mana = MMOStats.getPlayerStat(player, Stats.Inteligence);

        // Criar string de defesa
        String defenseString = String.format("%.0f", Tools.round(defense, 0)) + Stats.Defense.symbol + " Defense";
        if (player.showDefenceString) defenseString = player.defenseString;

        // Verificar absorção
        Player p = player.getPlayer();
        if (p != null && MMOStats.getAbsorbtion().containsKey(p) && MMOStats.getAbsorbtion().get(p) != 0) {
            // Vida: dourado
            TextComponent actionBar = Component.text(player.currhealth + MMOStats.absorbtion.get(player) + "/" + String.format("%.0f", Tools.round(health, 0)) + Stats.Health.getSymbol(), NamedTextColor.GOLD)
                    .append(Component.text(stackMsg + " ", NamedTextColor.WHITE))
                    .append(Component.text(defenseString + " ", NamedTextColor.GRAY))
                    .append(Component.text(extraafterdef + "   ", NamedTextColor.GRAY))
                    .append(Component.text(player.currmana + "/" + String.format("%.0f", Tools.round(mana, 0)) + "✎ Mana", NamedTextColor.BLUE))
                    .append(Component.text(afterManaString + " ", NamedTextColor.BLUE));

            player.sendActionBar(actionBar);
        } else {
            // Vida: vermelho
            TextComponent actionBar = Component.text(player.currhealth + "/" + String.format("%.0f", Tools.round(health, 0)) + Stats.Health.getSymbol(), NamedTextColor.RED)
                    .append(Component.text(stackMsg + "    ", NamedTextColor.WHITE))
                    .append(Component.text(defenseString + " ", NamedTextColor.GRAY))
                    .append(Component.text(extraafterdef + "   ", NamedTextColor.GRAY))
                    .append(Component.text(player.currmana + "/" + String.format("%.0f", Tools.round(mana, 0)) + "✎ Mana", NamedTextColor.BLUE))
                    .append(Component.text(afterManaString + " ", NamedTextColor.BLUE));

            player.sendActionBar(actionBar);
        }
    }
}

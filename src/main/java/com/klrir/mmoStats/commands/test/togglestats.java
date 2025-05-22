package com.klrir.mmoStats.commands.test;

import com.klrir.mmoStats.MMOStats;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;


public class togglestats implements CommandExecutor{

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String @NotNull [] args) {
        if (label.equalsIgnoreCase("statsystem")) {

            if (MMOStats.getInstance().getConfig().getBoolean("StatSystem")) {
                MMOStats.getInstance().getConfig().set("StatSystem", false);
                MMOStats.getInstance().saveConfig();
                MMOStats.getInstance().reloadConfig();
                BukkitRunnable statRunnable = MMOStats.getInstance().getStatRunnable();
                if (statRunnable != null) {
                    statRunnable.cancel();
                }
                for(Player player : Bukkit.getOnlinePlayers()) {
                    AttributeInstance playerMaxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                    if (playerMaxHealth != null) playerMaxHealth.setBaseValue(20.0);
                }

            }else {
                MMOStats.getInstance().getConfig().set("StatSystem", true);
                MMOStats.getInstance().saveConfig();
                MMOStats.getInstance().reloadConfig();
                MMOStats.getInstance().Stats();
            }
        }


        return false;
    }
}

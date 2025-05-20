package com.klrir.mmoStats.items.enchantments;

import com.klrir.mmoStats.MMOStats;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class GameEnchants {
    public static Map<String, CustomEnchantment> registeredEnchants = new HashMap<>();

    private static void registerEvent(Listener listener) {
        MMOStats.getInstance().getServer().getPluginManager().registerEvents(listener, MMOStats.getInstance());
    }
}

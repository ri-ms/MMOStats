package com.klrir.mmoStats.hook;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;

import java.util.*;

public class Hook {

    public enum HookType {
        ProtocolLib
    }

    private static final EnumMap<HookType, Boolean> hookList = new EnumMap<>(HookType.class);

    static {
        // Inicializa todos com false
        for (HookType hook : HookType.values()) {
            hookList.put(hook, false);
        }
    }

    public static void register() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            hookList.put(HookType.ProtocolLib, true);
        }

        for (HookType hook : HookType.values()) {
            if (isHooked(hook)) {
                System.out.println("[MMOStats Hook] Hooking " + hook.name());
                continue;
            }
            System.out.println("[MMOStats Hook] " + hook.name() + " not found! Continuating...");
        }
    }

    public static ProtocolManager getProtocolManager() {
        if (isHooked(HookType.ProtocolLib)) {
            return ProtocolLibrary.getProtocolManager();
        }
        return null;
    }

    public static boolean isHooked(HookType hook) {
        return hookList.getOrDefault(hook, false);
    }
}

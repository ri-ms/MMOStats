package com.klrir.mmoStats.hook;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.klrir.mmoStats.MMOStats;
import lombok.Getter;

public class ProtocolLibHook {
    @Getter
    public static ProtocolManager protocolManager;
    public static void register(){
        protocolManager = ProtocolLibrary.getProtocolManager();
    }
}

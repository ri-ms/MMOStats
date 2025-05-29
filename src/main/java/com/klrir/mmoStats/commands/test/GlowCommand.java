package com.klrir.mmoStats.commands.test;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.hook.Hook;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class GlowCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        if (args.length != 1) {
            viewer.sendMessage("Uso correto: /glow <jogador>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            viewer.sendMessage("Jogador não encontrado.");
            return true;
        }

        try {
            MMOStats.getGlowingEntities().setGlowing(target, viewer);
        } catch (ReflectiveOperationException e) {
            MMOStats.LOGGER.info("Erro ao enviar o GlowingEffect de " + target.getName() + " para " + viewer.getName());
        }

        viewer.sendMessage("Brilho ativado para " + target.getName());
        return true;
    }
}

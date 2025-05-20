package com.klrir.mmoStats.commands;

import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.configs.ConfigFile;
import com.klrir.mmoStats.game.GamePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (label.equalsIgnoreCase("stats")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("Du kannst das net");
                return true;
            }
            Player player = (Player) sender;
            if (args.length < 2) {
                player.sendMessage("Wrong args");
                return false;
            }
            try {
                Double.parseDouble(args[1]);

            }catch(Exception e) {
                player.sendMessage("Wrong Arg ussage");
                return false;
            }
            GamePlayer p = GamePlayer.getGamePlayer(player);
            double value = Double.parseDouble(args[1]);
            ConfigFile statsConfig = new ConfigFile(p, "stats");
            Stats s = Stats.getFromDataName(args[0]);
            if(s == null) return false;
            player.sendMessage("Set Base "+s.getName()+" to: " + args[1]);
            p.setBaseStat(s, value);
            statsConfig.get().set(s.getDataName(), Double.parseDouble(args[1]));
            statsConfig.save();
            MMOStats.updatebar(GamePlayer.getGamePlayer(player));
            return true;
        }
        return false;
    }
}

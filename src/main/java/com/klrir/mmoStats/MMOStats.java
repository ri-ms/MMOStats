package com.klrir.mmoStats;

import com.klrir.mmoStats.API.HealthChangeReason;
import com.klrir.mmoStats.API.ItemEvent.GetStatFromItemEvent;
import com.klrir.mmoStats.API.PlayerEvent.PlayerManaRegenEvent;
import com.klrir.mmoStats.commands.OpenMenu;
import com.klrir.mmoStats.commands.StatsCommand;
import com.klrir.mmoStats.commands.statsTAB;
import com.klrir.mmoStats.commands.test.togglestats;
import com.klrir.mmoStats.commands.test.toggletab;
import com.klrir.mmoStats.configs.DataManager;
import com.klrir.mmoStats.entities.BasicEntity;
import com.klrir.mmoStats.events.EventManager;
import com.klrir.mmoStats.game.EntityMap;
import com.klrir.mmoStats.game.GameEntity;
import com.klrir.mmoStats.game.GamePlayer;
import com.klrir.mmoStats.hook.ProtocolLibHook;
import com.klrir.mmoStats.items.ItemHandler;
import com.klrir.mmoStats.items.ItemManager;
import com.klrir.mmoStats.items.ItemRarity;
import com.klrir.mmoStats.utils.Tools;
import com.klrir.mmoStats.utils.inventories.items.StarHandler;
import com.klrir.mmoStats.utils.log.DebugLogger;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class MMOStats extends JavaPlugin {
    @Getter
    private static MMOStats instance;

    @Getter
    private BukkitRunnable runnable;
    private BukkitRunnable statrunnable;
    public FileConfiguration config = getConfig();
    public static final ArrayList<Player> deathPersons = new ArrayList<>();

    public static HashMap<Player, Integer> absorbtion = new HashMap<>();
    public static HashMap<Player, Integer> absorbtionrunntime = new HashMap<>();
    public static HashMap<Entity, Boolean> entitydead = new HashMap<>();
    public static boolean isLocalHost = true;

    @Getter
    public static DebugLogger debug;
    public static DataManager data;

    @Override
    public void onEnable() {
        // Plugin startup logic
        config.addDefault("StatSystem", true);
        config.options().copyDefaults(true);
        saveConfig();
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null)
            ProtocolLibHook.register();

        //debug.debug("Registering Commands", false);

        getCommand("statsystem").setExecutor(new togglestats());
        getCommand("statsystem").setTabCompleter(new toggletab());
        getCommand("stats").setExecutor(new StatsCommand());
        getCommand("stats").setTabCompleter(new statsTAB());
        getCommand("e").setExecutor(new OpenMenu());

        this.getServer().getPluginManager().registerEvents(new EventManager(), this);
        this.getServer().getPluginManager().registerEvents(new OpenMenu(), this);

        if (getConfig().getBoolean("StatSystem")) {
            Stats();
        }

        if (!Bukkit.getOnlinePlayers().isEmpty()) for (Player p : Bukkit.getOnlinePlayers()) {

            GamePlayer player = new GamePlayer((CraftServer) this.getServer(), ((CraftPlayer) p).getHandle());
            absorbtion.put(p, 0);
            absorbtionrunntime.put(p, 0);
        }
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
                    if (!deathPersons.contains(player)) {
                        player.setSaturation(100);

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
                        if (player.currmana > mana) {
                            player.setMana((int) mana);
                        }

                        double health = getPlayerStat(player, Stats.Health);
                        if (player.currhealth < health) {
                            int healthadd = (int) (health * 0.015);
                            int finalhealth = (int) (player.currhealth + (healthadd * player.healingMulti));
                            player.setHealth(finalhealth, HealthChangeReason.Regenerate);
                        }
                        if (player.currhealth > health) {
                            player.setHealth(health, HealthChangeReason.Regenerate);
                        }

                        float speedpersentage = (float) getPlayerStat(player, (Stats.Speed)) / 100;
                        if (speedpersentage > 5) speedpersentage = 5;
                        player.setWalkSpeed((float) 0.2 * (float) speedpersentage);
                        updatebar(player);

                    }
                });
            }
        };
        statrunnable.runTaskTimer(this, 0, 20);
    }

    public BukkitRunnable getStatRunnable() {
        return statrunnable;
    }

    public void absorbtioneffect(Player player, int times) {
        GamePlayer p = GamePlayer.getGamePlayer(player);
        updatebar(GamePlayer.getGamePlayer(player));
        absorbtionrunntime.replace(player, absorbtionrunntime.get(player) + times);

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().contains(player)) {

                    absorbtionrunntime.replace(player, absorbtionrunntime.get(player) - 1);

                    if (absorbtionrunntime.get(player) <= 0) {
                        double health = getPlayerStat(GamePlayer.getGamePlayer(player), Stats.Health);
                        if (absorbtion.get(player) + p.currhealth > health)
                            p.setHealth(health, HealthChangeReason.Ability);
                        else {
                            p.setHealth((absorbtion.get(player) + p.currhealth) * p.healingMulti, HealthChangeReason.Ability);
                        }
                        absorbtion.replace(player, 0);

                        updatebar(GamePlayer.getGamePlayer(player));
                        absorbtionrunntime.replace(player, 0);
                        return;
                    } else {
                        absorbtioneffect(player, 0);
                    }
                }
            }
        };
        runnable.runTaskLater(instance, 20);
    }

    public static void updatebar(GamePlayer player) {
        if (deathPersons.contains(player)) return;

        if (player.currhealth <= 0) {
            deathPersons.add(player);
            assert player.getPlayer() != null;
            player.getPlayer().damage(Float.MAX_VALUE);
        }
        double maxhealth = getPlayerStat(player, Stats.Health);
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


        if (absorbtion.containsKey(player) && absorbtion.get(player) != 0) {
            int abs = absorbtion.get(player);

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
        } else {
            player.setAbsorptionAmount(0);
        }

        double health = player.currhealth;
        float estimated = (float) ((health / player.getMaxHealth()) * maxhealth);

        String extraafterdef = "";

        if (estimated < 0) estimated = 0;
        if (estimated > player.getMaxHealth()) estimated = (float) player.getMaxHealth();
        assert player.getPlayer() != null;
        //player.getPlayer().setHealth(estimated);

        String stackMsg = "";

        String afterManaString = "";

        health = getPlayerStat(player, Stats.Health);
        double defense = getPlayerStat(player, Stats.Defense);
        double mana = getPlayerStat(player, Stats.Inteligence);

        String defenseString = Component.text(String.format("%.0f", Tools.round(defense, 0)) + "❈ Defense", NamedTextColor.GREEN).content();
        if (player.showDefenceString) defenseString = player.defenseString;

        if (!(absorbtion.containsKey(player.getPlayer()) && absorbtion.get(player) != 0)) {
            // Vida: vermelho
            TextComponent actionBar = Component.text(player.currhealth + "/" + String.format("%.0f", Tools.round(health, 0)) + "❤ ", NamedTextColor.RED)
                    .append(Component.text(stackMsg + "    ", NamedTextColor.WHITE))
                    .append(Component.text(defenseString + "  ", NamedTextColor.GRAY))
                    .append(Component.text(extraafterdef + "   ", NamedTextColor.GRAY))
                    .append(Component.text(String.format("%.0f", Tools.round(player.currmana, 0)) + "/" + String.format("%.0f", Tools.round(mana, 0)) + "✎ Mana", NamedTextColor.AQUA))
                    .append(Component.text(afterManaString, NamedTextColor.WHITE));

            player.getPlayer().sendActionBar(actionBar);
        } else {
            TextComponent actionBar = Component.text(player.currhealth + absorbtion.get(player) + "/" + String.format("%.0f", Tools.round(health, 0)) + "❤ ", NamedTextColor.GOLD)
                    .append(Component.text(stackMsg + "    ", NamedTextColor.WHITE))
                    .append(Component.text(defenseString + "  ", NamedTextColor.GRAY))
                    .append(Component.text(extraafterdef + "   ", NamedTextColor.GRAY))
                    .append(Component.text(String.format("%.0f", Tools.round(player.currmana, 0)) + "/" + String.format("%.0f", Tools.round(mana, 0)) + "✎ Mana", NamedTextColor.AQUA))
                    .append(Component.text(afterManaString, NamedTextColor.WHITE));

            player.getPlayer().sendActionBar(actionBar);
        }
    }

    public static void EntityDeath(LivingEntity entity) {
        entitydead.put(entity, true);
    }

    private static void updateMaxHealth(GamePlayer player, double maxHealth) {

        // Define os thresholds e os valores de maxHealth correspondentes
        double[] thresholds  = { 125, 165, 230, 300, 400, 500, 650, 800, 1000, 1250 };
        double[] maxHearts   = { 20,  22,  24,  26,  28,  30,  32,  34,   36,   38,  40 };

        double newMaxHealth = 20;
        for (int i = 1; i < thresholds.length; i++) {
            if (maxHealth < thresholds[i]) {
                newMaxHealth = maxHearts[i];
                break;
            }
            // se for o último threshold e ainda maior, pega o último valor
            if (i == thresholds.length - 1 && maxHealth >= thresholds[i]) {
                newMaxHealth = maxHearts[i + 1];
            }
        }

        // Ajusta o atributo de vida
        player.setMaxHealth(newMaxHealth);
    }

    public static void updateAbsorption(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (absorbtion.containsKey(player) && absorbtion.get(player) != 0) {
            int abs = absorbtion.get(player);

            int[] thresholds = { Integer.MIN_VALUE, 0, 165, 230, 300, 400, 500, 650, 800, 1000, 1250 };
            double[] absorptionValues = { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22 };

            double absorptionAmount = 0;
            for (int i = thresholds.length - 1; i >= 0; i--) {
                if (abs >= thresholds[i]) {
                    absorptionAmount = absorptionValues[i];
                    break;
                }
            }
            player.setAbsorptionAmount(absorptionAmount);
        } else {
            player.setAbsorptionAmount(0);
        }
    }

    public void killarmorstand(ArmorStand stand) {
        stand.addScoreboardTag("remove");
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                stand.remove();
            }
        };
        runnable.runTaskLater(instance, 20);
    }

    public static void updateentitystats(LivingEntity entity) {
        if (EntityMap.exists(entity)) {
            GameEntity.updateEntity(EntityMap.getGmEntity(entity));
            return;
        }
        new BasicEntity(entity, (int) (entity.getAttribute(Attribute.MAX_HEALTH).getBaseValue() * 5));
    }

    public synchronized static double getPlayerStat(GamePlayer player, Stats stat) {
        return player.getStat(stat);
    }

    public static FileConfiguration getData() {
        return data.getConfig();
    }

    public static void saveData() {
        data.saveConfig();
    }

    public synchronized static double getItemStat(GamePlayer player, Stats stat, ItemStack item) {
        if (item == null) {
            return 0;
        }
        ItemManager manager = ItemHandler.getItemManager(item);
        if (manager == null) return 0;
        double value = manager.getStat(stat);
        ItemRarity rarity = manager.getRarity(item, player);
        value *= StarHandler.getStarBuff(item);
        double val = value;
        double kekw;
        GetStatFromItemEvent event = new GetStatFromItemEvent(item, stat, val, player);
        Bukkit.getPluginManager().callEvent(event);
        kekw = event.getValue() * event.getMultiplier();
        kekw = Tools.round(kekw, 1);
        return kekw;
    }

}

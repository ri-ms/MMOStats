package com.klrir.mmoStats;

import com.klrir.mmoStats.API.HealthChangeReason;
import com.klrir.mmoStats.API.ItemEvent.GetStatFromItemEvent;
import com.klrir.mmoStats.API.PlayerEvent.PlayerManaRegenEvent;
import com.klrir.mmoStats.commands.OpenMenu;
import com.klrir.mmoStats.commands.StatsCommand;
import com.klrir.mmoStats.commands.statsTAB;
import com.klrir.mmoStats.commands.test.GlowCommand;
import com.klrir.mmoStats.commands.test.togglestats;
import com.klrir.mmoStats.commands.test.toggletab;
import com.klrir.mmoStats.configs.DataManager;
import com.klrir.mmoStats.entities.BasicEntity;
import com.klrir.mmoStats.events.EventManager;
import com.klrir.mmoStats.game.EntityMap;
import com.klrir.mmoStats.game.GameEntity;
import com.klrir.mmoStats.game.GamePlayer;
import com.klrir.mmoStats.hook.Hook;
import com.klrir.mmoStats.items.ItemHandler;
import com.klrir.mmoStats.items.ItemManager;
import com.klrir.mmoStats.items.ItemRarity;
import com.klrir.mmoStats.utils.AbsorptionManager;
import com.klrir.mmoStats.utils.ActionBarManager;
import com.klrir.mmoStats.utils.PlayerStatsManager;
import com.klrir.mmoStats.utils.Tools;
import com.klrir.mmoStats.utils.inventories.items.StarHandler;
import com.klrir.mmoStats.utils.log.DebugLogger;
import fr.skytasul.glowingentities.GlowingEntities;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public final class MMOStats extends JavaPlugin {
    @Getter
    private static MMOStats instance;

    @Getter
    private BukkitRunnable runnable;
    private BukkitRunnable statrunnable;

    public FileConfiguration config = getConfig();

    @Getter
    public static final ArrayList<Player> deathPersons = new ArrayList<>();

    @Getter
    public static HashMap<Player, Integer> absorbtion = new HashMap<>();

    @Getter
    public static HashMap<Player, Integer> absorbtionrunntime = new HashMap<>();

    @Getter
    public static HashMap<Entity, Boolean> entitydead = new HashMap<>();

    public static boolean isLocalHost = true;

    @Getter
    public static DebugLogger debug;

    public static DataManager data;

    public static Logger LOGGER;

    @Getter
    private static GlowingEntities glowingEntities;

    @Override
    public void onEnable() {
        // Inicialização básica
        LOGGER = getLogger();

        initializeConfig();
        instance = this;
        glowingEntities = new GlowingEntities(this);

        // Registrar hooks
        Hook.register();

        // Registrar comandos
        registerCommands();

        // Registrar eventos
        registerEvents();

        // Iniciar sistema de estatísticas se habilitado
        if (getConfig().getBoolean("StatSystem")) {
            initializeStatSystem();
        }

        // Inicializar jogadores online
        initializeOnlinePlayers();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        glowingEntities.disable();
    }

    private void initializeConfig() {
        config.addDefault("StatSystem", true);
        config.options().copyDefaults(true);
        saveConfig();
    }

    private void registerCommands() {
        registerCommand("statsystem", new togglestats(), new toggletab());
        registerCommand("stats", new StatsCommand(), new statsTAB());
        registerCommand("e", new OpenMenu(), null);
        registerCommand("glow", new GlowCommand(), null);
    }

    public void registerCommand(String name, CommandExecutor executor, @Nullable TabCompleter tabCompleter) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            if (tabCompleter != null) {
                command.setTabCompleter(tabCompleter);
            }
        } else {
            LOGGER.warning("Command '" + name + "' not registered!");
        }
    }

    /**
     * Registra eventos do plugin
     */
    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new EventManager(), this);
        this.getServer().getPluginManager().registerEvents(new OpenMenu(), this);
    }

    /**
     * Inicializa o sistema de estatísticas
     */
    private void initializeStatSystem() {
        statrunnable = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    GamePlayer player = GamePlayer.getGamePlayer(p);
                    if (!deathPersons.contains(player)) {
                        player.setSaturation(100);

                        // Processar regeneração de mana
                        PlayerStatsManager.processManaRegeneration(player);

                        // Processar regeneração de vida
                        PlayerStatsManager.processHealthRegeneration(player);

                        // Atualizar velocidade do jogador
                        PlayerStatsManager.updatePlayerSpeed(player);

                        // Atualizar barra de ação
                        ActionBarManager.updateActionBar(player);
                    }
                });
            }
        };
        statrunnable.runTaskTimer(this, 0, 20);
    }

    /**
     * Obtém o BukkitRunnable para estatísticas
     */
    public BukkitRunnable getStatRunnable() {
        return statrunnable;
    }

    /**
     * Aplica efeito de absorção ao jogador
     */
    public void absorbtioneffect(Player player, int times) {
        AbsorptionManager.applyAbsorptionEffect(player, times);
    }

    /**
     * Atualiza a barra de status do jogador
     */
    public static void updatebar(GamePlayer player) {
        PlayerStatsManager.updateHealthBar(player);
    }

    /**
     * Inicializa jogadores online
     */
    private void initializeOnlinePlayers() {
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                GamePlayer player = new GamePlayer((CraftServer) this.getServer(), ((CraftPlayer) p).getHandle());
                absorbtion.put(p, 0);
                absorbtionrunntime.put(p, 0);
            }
        }
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
        assert player != null;
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
        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        double baseHealth = (maxHealth != null) ? maxHealth.getBaseValue() : 20.0;

        new BasicEntity(entity, (int) (baseHealth * 5));
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

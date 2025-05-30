package com.klrir.mmoStats.database;

import com.google.gson.Gson;
import com.klrir.mmoStats.MMOStats;
import com.klrir.mmoStats.Stats;
import com.klrir.mmoStats.game.GamePlayer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public final class Database {
    @Getter
    private static final Database instance = new Database();

    private final Gson gson = new Gson();
    private final Settings settings;
    private Connection connection;

    private Database() {
        this.settings = Settings.getInstance();
    }

    public void load() {


        this.connect();
        this.createTables();
    }

    private void connect() {
        try {
            if (this.connection != null && !this.connection.isClosed())
                return;

            if (this.settings.getDatabaseType() == DatabaseType.MYSQL) {
                Class.forName("com.mysql.cj.jdbc.Driver");

                final String jdbcUrl =
                        "jdbc:mysql://" + this.settings.getMysqlHost() + ":" + this.settings.getMysqlPort() + "/" + this.settings.getMysqlDatabase() + "?useSSL=false&autoReconnect=true";

                MMOStats.LOGGER.info("[MMOStats] Connecting to MySQL database: " + jdbcUrl + " with user " + this.settings.getMysqlUsername());

                this.connection = DriverManager.getConnection(jdbcUrl, this.settings.getMysqlUsername(), this.settings.getMysqlPassword());

                MMOStats.LOGGER.info("[MMOStats] Connected to MySQL database");
            } else {
                Class.forName("org.sqlite.JDBC");

                final File file = new File(MMOStats.getInstance().getDataFolder(), this.settings.getSqliteFile());

                if (!file.exists())
                    file.createNewFile();

                final String jdbcUrl = "jdbc:sqlite:" + file.getAbsolutePath();
                this.connection = DriverManager.getConnection(jdbcUrl);

                MMOStats.LOGGER.info("[MMOStats] Connected to SQLite database");
            }
        } catch (final Exception ex) {
            MMOStats.LOGGER.severe("[MMOStats] Failed to connect to database: " + ex.getMessage());
        }
    }

    private void createTables() {
        String statsString = Stats.values().length == 0 ? "" :
                Arrays.stream(Stats.values())
                        .map(stat -> "'" + stat.getDataName() + "'" + " INT NOT NULL")
                        .collect(Collectors.joining(", ")) + ", ";

        try {
            final Statement statement = this.connection.createStatement();

            statement.execute(
                    "CREATE TABLE IF NOT EXISTS player_stats (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY NOT NULL, " +
                    "player_name VARCHAR(16) NOT NULL, " +
                    "player_xp DOUBLE NOT NULL, " +
                    statsString +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );

            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_player_uuid ON player_stats(player_uuid)");

            statement.close();

        } catch (final SQLException ex) {
            MMOStats.LOGGER.severe("[MMOStats] Failed to create tables: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void close() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();

                MMOStats.LOGGER.info("[MMOStats] Database connection closed.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public enum DatabaseType {
        MYSQL,
        SQLITE
    }

    private HashMap<Stats, Integer> getPlayerStats(Player player) {
        HashMap<Stats, Integer> stats = new HashMap<>();
        try {
            StringBuilder queryBuilder = new StringBuilder("SELECT ");
            Stats[] values = Stats.values();
            for (int i = 0; i < values.length; i++) {
                queryBuilder.append("\"" + values[i].getDataName() + "\"");
                if (i < values.length - 1) queryBuilder.append(", ");
            }
            queryBuilder.append(" FROM player_stats WHERE player_uuid = ?");

            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());

            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                for (Stats stat : values) {
                    int value = rs.getInt(stat.getDataName());
                    stats.put(stat, value);
                }
            } else {
                System.out.println("Nenhum dado encontrado para o jogador: " + player.getName());
            }

            System.out.println(stats);

            return stats;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void setupInitialPlayerData(Player player) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO player_stats (" +
                        "player_uuid, " +
                        "player_name, " +
                        "player_xp, " +
                        "'health', " +
                        "'def', " +
                        "'mana', " +
                        "'speed', " +
                        "'strength', " +
                        "'cd', " +
                        "'cc', " +
                        "'abilitydamage', " +
                        "'as', " +
                        "'truedefense', " +
                        "'healthregen', " +
                        "'manaregen', " +
                        "'hearts', " +
                        "'dmg', " +
                        "'vitality') " +
                        "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"
            );
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            statement.setDouble(3, 0);
            statement.setDouble(4, Stats.Health.getBaseAmount());
            statement.setDouble(5, Stats.Defense.getBaseAmount());
            statement.setDouble(6, Stats.Inteligence.getBaseAmount());
            statement.setDouble(7, Stats.Speed.getBaseAmount());
            statement.setDouble(8, Stats.Strength.getBaseAmount());
            statement.setDouble(9, Stats.CritDamage.getBaseAmount());
            statement.setDouble(10, Stats.CritChance.getBaseAmount());
            statement.setDouble(11, Stats.AbilityDamage.getBaseAmount());
            statement.setDouble(12, Stats.AttackSpeed.getBaseAmount());
            statement.setDouble(13, Stats.TrueDefense.getBaseAmount());
            statement.setDouble(14, Stats.HealthRegen.getBaseAmount());
            statement.setDouble(15, Stats.ManaRegen.getBaseAmount());
            statement.setDouble(16, Stats.Hearts.getBaseAmount());
            statement.setDouble(17, Stats.WeaponDamage.getBaseAmount());
            statement.setDouble(18, Stats.Vitality.getBaseAmount());

            final int rows = statement.executeUpdate();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public HashMap<Stats, Integer> initializePlayerData(GamePlayer player) {
        synchronized (this.connection) {
            HashMap<Stats, Integer> stats = getPlayerStats(player);
            if (stats != null) {
                return stats;
            }
            setupInitialPlayerData(player);
            return getPlayerStats(player);
        }
    }

    public boolean savePlayerStats(GamePlayer player) {
        synchronized (this.connection) {

            try {
                PreparedStatement check = connection.prepareStatement(
                        "SELECT 1 FROM player_stats WHERE player_uuid = ?"
                );
                check.setString(1, player.getUniqueId().toString());
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO player_stats (" +
                                    "player_uuid, " +
                                    "player_name, " +
                                    "player_xp, " +
                                    "health, " +
                                    "def, " +
                                    "mana, " +
                                    "speed, " +
                                    "strength, " +
                                    "cd, " +
                                    "cc, " +
                                    "abilitydamage, " +
                                    "as, " +
                                    "truedefense, " +
                                    "healthregen, " +
                                    "manaregen, " +
                                    "hearts, " +
                                    "dmg, " +
                                    "vitality, " +
                                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"
                    );
                    insert.setString(1, player.getUniqueId().toString());
                    insert.setString(2, player.getName());
                    insert.setDouble(3, 0);
                    insert.setDouble(4, Stats.Health.getBaseAmount());
                    insert.setDouble(5, Stats.Defense.getBaseAmount());
                    insert.setDouble(6, Stats.Inteligence.getBaseAmount());
                    insert.setDouble(7, Stats.Speed.getBaseAmount());
                    insert.setDouble(8, Stats.Strength.getBaseAmount());
                    insert.setDouble(9, Stats.CritDamage.getBaseAmount());
                    insert.setDouble(10, Stats.CritChance.getBaseAmount());
                    insert.setDouble(11, Stats.AbilityDamage.getBaseAmount());
                    insert.setDouble(12, Stats.AttackSpeed.getBaseAmount());
                    insert.setDouble(13, Stats.TrueDefense.getBaseAmount());
                    insert.setDouble(14, Stats.HealthRegen.getBaseAmount());
                    insert.setDouble(15, Stats.ManaRegen.getBaseAmount());
                    insert.setDouble(16, Stats.Hearts.getBaseAmount());
                    insert.setDouble(17, Stats.WeaponDamage.getBaseAmount());
                    insert.setDouble(18, Stats.Vitality.getBaseAmount());
                    return true;
                }


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
    }
}

package com.klrir.mmoStats.database;

import com.klrir.mmoStats.MMOStats;
import lombok.Getter;

import com.klrir.mmoStats.database.Database.*;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class Settings {
    @Getter
    public static Settings instance = new Settings();
    @Getter
    private static DatabaseType databaseType;
    @Getter
    private static String mysqlHost;
    @Getter
    private static int mysqlPort;
    @Getter
    private static String mysqlDatabase;
    @Getter
    private static String mysqlUsername;
    @Getter
    private static String mysqlPassword;
    @Getter
    private static String sqliteFile;

    private static YamlConfiguration config;
    private static File file;

    private Settings() {
    }

    public void load() {
        file = new File(MMOStats.getInstance().getDataFolder(), "settings.yml");

        if (!file.exists())
            MMOStats.getInstance().saveResource("settings.yml", false);

        config = new YamlConfiguration();

        try {
            config.options().parseComments(true);
        } catch (final Throwable e) {
            //
        }

        try {
            config.load(file);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        databaseType = DatabaseType.valueOf(config.getString("Database.Type").toUpperCase());
        mysqlHost = config.getString("Database.MySQL.Host");
        mysqlPort = config.getInt("Database.MySQL.Port", 0);
        mysqlDatabase = config.getString("Database.MySQL.Database");
        mysqlUsername = config.getString("Database.MySQL.User");
        mysqlPassword = config.getString("Database.MySQL.Password");
        sqliteFile = config.getString("Database.SQLite.File");

        if (databaseType == DatabaseType.MYSQL && (mysqlHost == null || mysqlPort == 0 || mysqlDatabase == null || mysqlUsername == null || mysqlPassword == null))
            throw new RuntimeException("Missing MySQL database keys from settings.yml");
    }

    public void save() {
        try {
            config.save(file);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public void set(String path, Object value) {
        config.set(path, value);

        save();
    }
}

package byfr0n.pvptoggle.manager;

import byfr0n.pvptoggle.config.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class ConfigManager {
    private static final Path CONFIG_PATH = Paths.get("config", "pvptoggle.json");
    private static Config config;

    public static void loadConfig() {
        Gson gson = new Gson();
        File configFile = CONFIG_PATH.toFile();

        if (!configFile.exists()) {
            config = new Config();
            saveConfig();
        } else {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Config newConfig = gson.fromJson(reader, Config.class);
                if (newConfig != null) {
                    config = newConfig;
                } else {
                    System.err.println("Parsed PvP Toggle config was null, keeping previous config.");
                }
            } catch (Exception e) {
                System.err.println("Failed to load PvP Toggle config due to invalid JSON: " + e.getMessage());
                if (config == null) {
                    config = new Config();
                    saveConfig();
                }
            }
        }
    }

    private static void saveConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                gson.toJson(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save PvP Toggle config: " + e.getMessage());
        }
    }

    public static Config getConfig() {
        return config;
    }

    public static void startConfigWatcher() {
        new Thread(() -> {
            try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                Path configDir = CONFIG_PATH.getParent();
                configDir.register(watcher, ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW) continue;

                        Path changedFile = (Path) event.context();
                        if (changedFile.equals(CONFIG_PATH.getFileName())) {
                            loadConfig();
                            System.out.println("PvP Toggle config reloaded due to file change.");
                        }
                    }
                    boolean valid = key.reset();
                    if (!valid) break;
                }
            } catch (IOException e) {
                System.err.println("Error in config watcher: " + e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "PvPConfigWatcher").start();
    }
}
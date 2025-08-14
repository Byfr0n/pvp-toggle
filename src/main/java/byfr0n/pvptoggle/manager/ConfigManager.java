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
            System.out.println("Config file not found at: " + CONFIG_PATH.toAbsolutePath() + ", creating default config.");
            config = new Config();
            saveConfig();
        } else {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                config = gson.fromJson(reader, Config.class);
                if (config == null) {
                    System.err.println("Parsed config was null, using default config.");
                    config = new Config();
                    saveConfig();
                }
            } catch (Exception e) {
                System.err.println("Failed to load PvP Toggle config due to: " + e.getMessage());
                if (config == null) {
                    System.out.println("Creating default config due to load failure.");
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
                System.out.println("Saved config to: " + CONFIG_PATH.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to save PvP Toggle config: " + e.getMessage());
        }
    }

    public static Config getConfig() {
        if (config == null) {
            System.out.println("Config is null, loading default config.");
            loadConfig();
        }
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
                            System.out.println("Config file changed, reloading...");
                            loadConfig();
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

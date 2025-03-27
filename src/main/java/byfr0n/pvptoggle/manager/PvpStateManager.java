package byfr0n.pvptoggle.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PvpStateManager {
    private static final Map<UUID, Boolean> pvpStates = new HashMap<>();
    private static final Path STATES_PATH = Paths.get("config", "pvptoggle_states.json");

    public static void loadPvpStates() {
        Gson gson = new Gson();
        if (Files.exists(STATES_PATH)) {
            try (Reader reader = Files.newBufferedReader(STATES_PATH)) {
                Map<String, Boolean> loadedStates = gson.fromJson(reader, Map.class);
                if (loadedStates != null) {
                    loadedStates.forEach((uuid, state) -> pvpStates.put(UUID.fromString(uuid), state));
                }
            } catch (IOException e) {
                System.err.println("Failed to load PvP states: " + e.getMessage());
            }
        }
    }

    public static void savePvpStates() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.createDirectories(STATES_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(STATES_PATH)) {
                Map<String, Boolean> stringStates = new HashMap<>();
                pvpStates.forEach((uuid, state) -> stringStates.put(uuid.toString(), state));
                gson.toJson(stringStates, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save PvP states: " + e.getMessage());
        }
    }

    public static boolean isPvpEnabled(ServerPlayerEntity player) {
        return pvpStates.getOrDefault(player.getUuid(), ConfigManager.getConfig().getDefaultPvpState());
    }

    public static void togglePvpState(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        boolean currentState = pvpStates.getOrDefault(playerId, ConfigManager.getConfig().getDefaultPvpState());
        pvpStates.put(playerId, !currentState);
    }
}
package byfr0n.pvptoggle.utils;

import byfr0n.pvptoggle.manager.ConfigManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandCooldown {
    private static final Map<UUID, Long> lastCommandUse = new HashMap<>();

    public static boolean canExecute(ServerPlayerEntity player) {
        long currentTime = System.currentTimeMillis() / 1000;
        UUID playerId = player.getUuid();
        long lastUse = lastCommandUse.getOrDefault(playerId, 0L);
        int cooldown = ConfigManager.getConfig().getCommandCooldownSeconds();

        return (currentTime - lastUse) >= cooldown;
    }

    public static void setLastUsed(ServerPlayerEntity player) {
        lastCommandUse.put(player.getUuid(), System.currentTimeMillis() / 1000);
    }
}
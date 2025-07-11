package byfr0n.pvptoggle.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private final int minimumPermissionLevel = 2;
    private final List<String> blacklistedPlayers = new ArrayList<>();
    private final boolean defaultPvpState = true;
    private final int commandCooldownSeconds = 5;

    public int getMinimumPermissionLevel() {
        return minimumPermissionLevel;
    }

    public List<String> getBlacklistedPlayers() {
        return blacklistedPlayers;
    }

    public boolean getDefaultPvpState() {
        return defaultPvpState;
    }

    public int getCommandCooldownSeconds() {
        return commandCooldownSeconds;
    }
}
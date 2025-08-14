package byfr0n.pvptoggle.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private int minimumPermissionLevel = 2;
    private List<String> blacklistedPlayers = new ArrayList<>();
    private boolean defaultPvpState = true;
    private int commandCooldownSeconds = 5;

    public Config() {
    }

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
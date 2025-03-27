package byfr0n.pvptoggle;

import byfr0n.pvptoggle.manager.ConfigManager;
import byfr0n.pvptoggle.manager.PvpStateManager;
import byfr0n.pvptoggle.utils.CommandCooldown;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

public class PvpToggle implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigManager.loadConfig();
        ConfigManager.startConfigWatcher();
        PvpStateManager.loadPvpStates();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    net.minecraft.server.command.CommandManager.literal("pvp")
                            .requires(source -> {
                                ServerPlayerEntity player = source.getPlayer();
                                if (player == null) return false;
                                if (source.getServer().getPermissionLevel(player.getGameProfile()) < ConfigManager.getConfig().getMinimumPermissionLevel()) {
                                    return false;
                                }
                                if (ConfigManager.getConfig().getBlacklistedPlayers().contains(player.getUuid().toString())) {
                                    return false;
                                }
                                if (!CommandCooldown.canExecute(player)) {
                                    player.sendMessage(Text.literal("Command on cooldown!").formatted(Formatting.YELLOW), false);
                                    return false;
                                }
                                return true;
                            })
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayer();
                                if (player == null) {
                                    context.getSource().sendError(Text.of("This command can only be executed by a player."));
                                    return 0;
                                }

                                CommandCooldown.setLastUsed(player);
                                PvpStateManager.togglePvpState(player);

                                boolean pvpEnabled = PvpStateManager.isPvpEnabled(player);
                                if (pvpEnabled) {
                                    player.sendMessage(Text.literal("PvP has been enabled.").formatted(Formatting.GREEN), false);
                                } else {
                                    player.sendMessage(Text.literal("PvP has been disabled.").formatted(Formatting.RED), false);
                                }

                                PvpStateManager.savePvpStates();
                                return 1;
                            })
            );
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof PlayerEntity) {
                ServerPlayerEntity attacker = (ServerPlayerEntity) player;
                ServerPlayerEntity target = (ServerPlayerEntity) entity;

                if (!PvpStateManager.isPvpEnabled(attacker) || !PvpStateManager.isPvpEnabled(target)) {
                    attacker.sendMessage(Text.literal("PvP is disabled for one or both players.").formatted(Formatting.YELLOW), true);
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });
    }
}
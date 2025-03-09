package com.byfr0n.pvptoggle;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PvpToggle implements ModInitializer {

    private static final Map<UUID, Boolean> pvpStates = new HashMap<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    net.minecraft.server.command.CommandManager.literal("pvp")
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayer();
                                if (player == null) {
                                    context.getSource().sendError(Text.of("This command can only be executed by a player."));
                                    return 0;
                                }

                                UUID playerId = player.getUuid();
                                boolean pvpEnabled = pvpStates.getOrDefault(playerId, true);
                                pvpStates.put(playerId, !pvpEnabled);

                                // send msg
                                if (pvpEnabled) {
                                    player.sendMessage(Text.literal("PvP has been disabled.").formatted(Formatting.RED), false);
                                } else {
                                    player.sendMessage(Text.literal("PvP has been enabled.").formatted(Formatting.GREEN), false);
                                }

                                return 1;
                            })
            );
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof PlayerEntity) {
                ServerPlayerEntity attacker = (ServerPlayerEntity) player;
                ServerPlayerEntity target = (ServerPlayerEntity) entity;

                if (!isPvpEnabled(attacker) || !isPvpEnabled(target)) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });
    }

    public static boolean isPvpEnabled(ServerPlayerEntity player) {
        return pvpStates.getOrDefault(player.getUuid(), true);
    }
}
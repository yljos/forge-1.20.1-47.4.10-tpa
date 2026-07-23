package com.yljos.tpamod.commands;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class TeleportUtils {
    public static void teleportWithEffects(ServerPlayer player, ServerLevel level, double x, double y, double z, float yRot, float xRot) {
        ServerLevel originalLevel = player.serverLevel();

        // Effects at departure
        originalLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        originalLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 32, 0.5, 1.0, 0.5, 0.1);

        player.teleportTo(level, x, y, z, yRot, xRot);

        // Effects at arrival
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 32, 0.5, 1.0, 0.5, 0.1);
    }
}
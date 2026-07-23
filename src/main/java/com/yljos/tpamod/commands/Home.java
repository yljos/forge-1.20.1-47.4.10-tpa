package com.yljos.tpamod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Home {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("sethome")
                .executes(Home::executeSetHome));

        dispatcher.register(Commands.literal("home")
                .executes(Home::executeHome));
    }

    // Persist home data on player death or return from the End
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        
        if (oldData.contains("ModHome")) {
            CompoundTag homeData = oldData.getCompound("ModHome").copy();
            event.getEntity().getPersistentData().put("ModHome", homeData);
        }
    }

    private static int executeSetHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        CompoundTag persistentData = player.getPersistentData();

        CompoundTag homeData = new CompoundTag();
        homeData.putDouble("x", player.getX());
        homeData.putDouble("y", player.getY());
        homeData.putDouble("z", player.getZ());
        homeData.putString("dimension", player.serverLevel().dimension().location().toString());
        homeData.putFloat("yRot", player.getYRot());
        homeData.putFloat("xRot", player.getXRot());

        persistentData.put("ModHome", homeData);
        player.sendSystemMessage(Component.literal("Home set."));

        return 1;
    }

    private static int executeHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        CompoundTag persistentData = player.getPersistentData();

        if (!persistentData.contains("ModHome")) {
            player.sendSystemMessage(Component.literal("Home not set."));
            return 0;
        }

        CompoundTag homeData = persistentData.getCompound("ModHome");
        String[] dimParts = homeData.getString("dimension").split(":");

        ServerLevel targetLevel = player.server.getLevel(
                ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimParts[0], dimParts[1]))
        );

        if (targetLevel == null) {
            targetLevel = player.serverLevel();
        }

        TeleportUtils.teleportWithEffects(player, targetLevel, homeData.getDouble("x"), homeData.getDouble("y"), homeData.getDouble("z"), homeData.getFloat("yRot"), homeData.getFloat("xRot"));
        player.sendSystemMessage(Component.literal("Teleported to home."));

        return 1;
    }
}
package com.yljos.tpamod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Tpa {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("tpa")
                // Accept all input as a single string to handle both cases
                .then(Commands.argument("input", StringArgumentType.greedyString())
                        // Filter out the sender from suggestions
                        .suggests((context, builder) -> {
                            String sourceName = context.getSource().getTextName();
                            return SharedSuggestionProvider.suggest(
                                    context.getSource().getOnlinePlayerNames().stream()
                                            .filter(name -> !name.equals(sourceName)),
                                    builder
                            );
                        })
                        .executes(Tpa::executeTpa)));
    }

    private static int executeTpa(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer source = context.getSource().getPlayerOrException();
        String input = StringArgumentType.getString(context, "input");

        // Check if the input is a valid online player
        ServerPlayer target = context.getSource().getServer().getPlayerList().getPlayerByName(input);

        if (target != null) {
            // Teleport to player
            TeleportUtils.teleportWithEffects(source, target.serverLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
            source.sendSystemMessage(Component.literal("Teleported to " + target.getName().getString()));
        } else {
            // Parse as coordinates if not a player
            String[] coords = input.split(" ");
            if (coords.length == 3) {
                try {
                    double x = Double.parseDouble(coords[0]);
                    double y = Double.parseDouble(coords[1]);
                    double z = Double.parseDouble(coords[2]);
                    
                    TeleportUtils.teleportWithEffects(source, source.serverLevel(), x, y, z, source.getYRot(), source.getXRot());
                    source.sendSystemMessage(Component.literal(String.format("Teleported to coordinates: %.1f, %.1f, %.1f", x, y, z)));
                } catch (NumberFormatException e) {
                    source.sendSystemMessage(Component.literal("Invalid player name or coordinates."));
                }
            } else {
                source.sendSystemMessage(Component.literal("Invalid player name or coordinates."));
            }
        }

        return 1;
    }
}
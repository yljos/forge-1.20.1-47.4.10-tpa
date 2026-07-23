package com.yljos.tpamod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
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
                .then(Commands.argument("target", EntityArgument.player())
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
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        TeleportUtils.teleportWithEffects(source, target.serverLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
        source.sendSystemMessage(Component.literal("Teleported to " + target.getName().getString()));

        return 1;
    }
}
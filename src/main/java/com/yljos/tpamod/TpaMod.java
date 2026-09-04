package com.yljos.tpamod;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TpaMod.MODID)
public class TpaMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tpamod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public TpaMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    // Remove attack cooldown by setting high attack speed
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            var attackSpeed = event.player.getAttribute(Attributes.ATTACK_SPEED);
            if (attackSpeed != null && attackSpeed.getBaseValue() != 100.0D) {
                attackSpeed.setBaseValue(100.0D);
            }
        }
    }
    
    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        // Check if the entity being attacked is a player
        if (event.getEntity() instanceof Player target) {
            // Check if the player has the "dad" tag
            if (target.getTags().contains("dad")) {
                Entity attacker = event.getSource().getEntity();
                
                if (attacker != null) {
                    // Ensure we are on the server side
                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        BlockPos pos = attacker.blockPosition();
                        
                        // Play thunder and impact sounds explicitly
                        serverLevel.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
                        serverLevel.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 2.0F, 1.0F);
                        
                        // Create visual lightning bolt
                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
                        if (lightning != null) {
                            lightning.moveTo(pos.getX(), pos.getY(), pos.getZ());
                            lightning.setVisualOnly(true);
                            serverLevel.addFreshEntity(lightning);
                        }
                        
                        // Apply damage attributed to the attacked player
                        attacker.hurt(serverLevel.damageSources().playerAttack(target), 1024.0F);
                    }
                    
                    // Force kill only if the attacker is a player in creative mode
                    if (attacker instanceof Player attackerPlayer && attackerPlayer.isCreative()) {
                        attacker.kill();
                    }
                }
            }
        }
    }
}
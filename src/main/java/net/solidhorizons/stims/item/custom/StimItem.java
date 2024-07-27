package net.solidhorizons.stims.item.custom;
import net.solidhorizons.stims.item.ModItems;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;

public class StimItem extends Item {

    private static final int TICKS_PER_SECOND = 20; // Standard Minecraft tick rate
    private static final String LAST_USED_STIM_KEY = "last_used_stim"; // Key to store last used stim type in NBT
    private static final String LAST_USED_TICK_KEY = "last_used_tick"; // Key to store tick when the stim was last used

    // Class to hold the configuration for each stimulant
    private static class StimConfig {
        boolean hasAfterDelayEffect;
        int delaySeconds;
        int effectDuration;
        int afterDelayEffectDuration;

        StimConfig(boolean hasAfterDelayEffect, int delaySeconds, int effectDuration, int afterDelayEffectDuration) {
            this.hasAfterDelayEffect = hasAfterDelayEffect;
            this.delaySeconds = delaySeconds;
            this.effectDuration = effectDuration;
            this.afterDelayEffectDuration = afterDelayEffectDuration;
        }
    }

    // Map to hold different configurations for each stimulant
    private static final Map<String, StimConfig> STIM_CONFIG_MAP = new HashMap<>();

    static {
        // Initialize the STIM_CONFIG_MAP with stimulant types and their configurations

        STIM_CONFIG_MAP.put("item.stims.propital_injector", new StimConfig(true, 90, 60, 20)); // 2 minutes delay, 12 seconds effect, 5 seconds after-delay
        //STIM_CONFIG_MAP.put("item.yourmodid.other_stimulant", new StimConfig(false, 0, 10, 0)); // No delay, 10 seconds effect

    }

    public StimItem(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this); // Register the event bus to listen for ticks
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Store the type of item using its string identifier
        String stimType = itemStack.getDescriptionId(); // Using the item's translation key

        // Record the last used stimulant type and the current game tick
        player.getPersistentData().putString(LAST_USED_STIM_KEY, stimType);
        player.getPersistentData().putInt(LAST_USED_TICK_KEY, (int) player.getCommandSenderWorld().getGameTime()); // Get current game time

        // Apply the initial effect immediately
        applyInitialEffect(player, stimType);

        // Optionally consume the item
        itemStack.shrink(1); // Remove one from the stack

        return InteractionResultHolder.success(itemStack);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Check if the player is the one with a stim effect active
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            // Get the stored stimulant type and the tick it was last used
            String lastUsedStim = player.getPersistentData().getString(LAST_USED_STIM_KEY);
            int lastUsedTick = player.getPersistentData().getInt(LAST_USED_TICK_KEY);
            int currentTick = (int) player.getCommandSenderWorld().getGameTime(); // Get current game time

            // Retrieve the configuration for this stimulant
            StimConfig config = STIM_CONFIG_MAP.get(lastUsedStim);
            if (config == null) return; // If stimulant configuration is not found, exit

            // If the stimulant has a delay configured
            int delayTicks = secondsToTicks(config.delaySeconds);
            if (config.hasAfterDelayEffect && (currentTick - lastUsedTick >= delayTicks)) {
                // Apply the after-delay effect
                applyAfterDelayEffect(player, lastUsedStim);
                // Clear the stored data after applying the effect
                player.getPersistentData().remove(LAST_USED_STIM_KEY);
                player.getPersistentData().remove(LAST_USED_TICK_KEY);
            }
        }
    }

    private void applyInitialEffect(Player player, String stimType) {
        // Example: Apply an initial effect based on item type
        StimConfig config = STIM_CONFIG_MAP.get(stimType);
        if (config != null) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, secondsToTicks(config.effectDuration), 0, true, true));
        }
    }

    private void applyAfterDelayEffect(Player player, String stimType) {
        // Apply the after delay effect based on stimulant type
        StimConfig config = STIM_CONFIG_MAP.get(stimType);
        if (config != null) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, secondsToTicks(config.afterDelayEffectDuration), 0, true, true));
        }
    }

    // Utility method to convert seconds to ticks
    private int secondsToTicks(int seconds) {
        return seconds * TICKS_PER_SECOND;
    }
}
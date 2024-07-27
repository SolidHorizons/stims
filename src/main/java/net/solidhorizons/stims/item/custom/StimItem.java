package net.solidhorizons.stims.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.solidhorizons.stims.item.ModItems;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    public StimItem(Properties properties) {
        super(properties.stacksTo(2)); // Set maximum stack size to 2
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        String stimType = pStack.getDescriptionId(); // Using the item's translation key
        String tooltip = TOOLTIP_MAP.get(stimType);

        if (tooltip != null) {
            pTooltipComponents.add(Component.translatable(tooltip));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.stims.default.tooltip")); // Fallback tooltip
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    private static final Map<String, StimConfig> STIM_CONFIG_MAP = new HashMap<>();

    private static final Map<String, String> TOOLTIP_MAP = new HashMap<>();

    private static final List<StimUse> STIM_USE_LIST = new LinkedList<>();

    // Inner class to track individual stim usage
    private static class StimUse {
        String stimType;
        int tick;

        StimUse(String stimType, int tick) {
            this.stimType = stimType;
            this.tick = tick;
        }
    }

    static {
        // Initialize the STIM_CONFIG_MAP with stimulant types and their configurations
        STIM_CONFIG_MAP.put("item.stims.propital_injector", new StimConfig(true, 90, 60, 20));
        STIM_CONFIG_MAP.put("item.stims.etg_c_injector", new StimConfig(true, 60, 30, 30));
        STIM_CONFIG_MAP.put("item.stims.morphine_injector", new StimConfig(true, 90, 60, 20));
        STIM_CONFIG_MAP.put("item.stims.obdolbos_injector", new StimConfig(true, 90, 60, 20));
        STIM_CONFIG_MAP.put("item.stims.obdolbos_two_injector", new StimConfig(true, 90, 60, 20));
        STIM_CONFIG_MAP.put("item.stims.sj_six_injector", new StimConfig(true, 90, 60, 20));
        STIM_CONFIG_MAP.put("item.stims.xtg_twelve_injector", new StimConfig(true, 90, 60, 20));

        // Initialize the TOOLTIP_MAP with stimulant types and their tooltips
        TOOLTIP_MAP.put("item.stims.propital_injector", "A powerful regenerative stimulant.");
        TOOLTIP_MAP.put("item.stims.etg_c_injector", "Boosts your physical capabilities temporarily.");
        TOOLTIP_MAP.put("item.stims.morphine_injector", "A pain relief stimulant.");
        TOOLTIP_MAP.put("item.stims.obdolbos_injector", "A unique stimulant for special abilities.");
        TOOLTIP_MAP.put("item.stims.obdolbos_two_injector", "An enhanced version for better effects.");
        TOOLTIP_MAP.put("item.stims.sj_six_injector", "A stimulant with extraordinary effects.");
        TOOLTIP_MAP.put("item.stims.xtg_twelve_injector", "A rarely found powerful stimulant.");
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Store the type of item using its string identifier
        String stimType = itemStack.getDescriptionId(); // Using the item's translation key

        int currentTick = (int) player.getCommandSenderWorld().getGameTime(); // Get the current game time
        // Record the last used stimulant type and the current game tick
        player.getPersistentData().putString(LAST_USED_STIM_KEY, stimType);
        player.getPersistentData().putInt(LAST_USED_TICK_KEY, currentTick); // Get current game time

        // Add to the list of stim uses
        STIM_USE_LIST.add(new StimUse(stimType, currentTick));

        // Apply the initial effect immediately
        applyInitialEffect(player, stimType);

        // Optionally consume the item
        itemStack.shrink(1); // Remove one from the stack

        return InteractionResultHolder.success(itemStack);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            int currentTick = (int) player.getCommandSenderWorld().getGameTime(); // Get current game time

            // Create a list to hold stims to remove after processing
            List<StimUse> toRemove = new LinkedList<>();

            for (StimUse stimUse : STIM_USE_LIST) {
                // Retrieve the configuration for this stimulant
                StimConfig config = STIM_CONFIG_MAP.get(stimUse.stimType);
                if (config == null) continue; // Skip if config is not found

                // If the stimulant has a delay configured
                int delayTicks = secondsToTicks(config.delaySeconds);
                if (config.hasAfterDelayEffect && (currentTick - stimUse.tick >= delayTicks)) {
                    // Apply the after-delay effect
                    applyAfterDelayEffect(player, stimUse.stimType);
                    // Mark this stim use for removal
                    toRemove.add(stimUse);
                }
            }

            // Remove processed stims from the list
            STIM_USE_LIST.removeAll(toRemove);
        }
    }

    private void applyInitialEffect(Player player, String stimType) {
        // Example: Apply an initial effect based on item type
        StimConfig config = STIM_CONFIG_MAP.get(stimType);
        if (config != null) {
            switch (stimType) {
                case "item.stims.propital_injector" ->  //still to add different status effects as tested
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.etg_c_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.morphine_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.obdolbos_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.obdolbos_two_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.xtg_twelve_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.sj_six_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, secondsToTicks(config.effectDuration), 0, false, false));
            }
        }
    }


    private void applyAfterDelayEffect(Player player, String stimType) {
        // Apply the after delay effect based on stimulant type
        StimConfig config = STIM_CONFIG_MAP.get(stimType);
        if (config != null) {
            switch (stimType) {
                case "item.stims.propital_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, secondsToTicks(config.afterDelayEffectDuration), 0, false, false));
                case "item.stims.etg_c_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.morphine_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.POISON, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.obdolbos_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.obdolbos_two_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.xtg_twelve_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, secondsToTicks(config.effectDuration), 0, false, false));
                case "item.stims.sj_six_injector" ->
                        player.addEffect(new MobEffectInstance(MobEffects.WITHER, secondsToTicks(config.effectDuration), 0, false, false));
            }
        }
    }

    // Utility method to convert seconds to ticks
    private int secondsToTicks(int seconds) {
        return seconds * TICKS_PER_SECOND;
    }
}
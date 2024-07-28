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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StimItem extends Item {

    private static final int TICKS_PER_SECOND = 20; // Standard Minecraft tick rate
    private static final String LAST_USED_STIM_KEY = "last_used_stim"; // Key to store last used stim type in NBT
    private static final String LAST_USED_TICK_KEY = "last_used_tick"; // Key to store tick when the stim was last used
    private static final Logger log = LoggerFactory.getLogger(StimItem.class);

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
    private static final List<StimUse> STIM_USE_LIST = Collections.synchronizedList(new LinkedList<>()); // Use synchronized list

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
        STIM_CONFIG_MAP.put("item.stims.propital_injector", new StimConfig(true, 90, 120, 30));
        STIM_CONFIG_MAP.put("item.stims.etg_c_injector", new StimConfig(true, 90, 60, 30));
        STIM_CONFIG_MAP.put("item.stims.morphine_injector", new StimConfig(true, 180, 150, 20));
        STIM_CONFIG_MAP.put("item.stims.obdolbos_injector", new StimConfig(true, 90, 60, 20));
        STIM_CONFIG_MAP.put("item.stims.obdolbos_two_injector", new StimConfig(true, 90, 60, 20));
        STIM_CONFIG_MAP.put("item.stims.sj_six_injector", new StimConfig(true, 150, 120, 20));
        STIM_CONFIG_MAP.put("item.stims.xtg_twelve_injector", new StimConfig(true, 2, 1, 10));

        // Initialize the TOOLTIP_MAP with stimulant types and their tooltips
        TOOLTIP_MAP.put("item.stims.propital_injector", " + \n Regeneration 1 " +
                "\n\n - " +
                "\n Nausea 1 \n Darkness 1");
        TOOLTIP_MAP.put("item.stims.etg_c_injector", " + \n Regeneration 3 \n Saturation 1 \n Healthboost 3 " +
                "\n\n - " +
                "\n Slowness 1 \n Hunger 2");
        TOOLTIP_MAP.put("item.stims.morphine_injector", " + \n Fire Resistance 2 \n Absorption 2 " +
                "\n\n - " +
                "\n Hunger 1 ");
        TOOLTIP_MAP.put("item.stims.obdolbos_injector", " + \n 25%: Speed 2, Strength 2 " +
                "\n 25%: Night Vision 2, Invisibility " +
                "\n 25%: Hero of the village, Dolphins Grace, Luck " +
                "\n 25%: Instant death" +
                "\n\n - " +
                "\n 25%: Nausea 2 " +
                "\n 25%: Weakness 3 " +
                "\n 25%: Hunger 3 " +
                "\n 25%: Wither 2");
        TOOLTIP_MAP.put("item.stims.obdolbos_two_injector", " + \n Speed 2 \n Night Vision 2 \n Strength 2" +
                "\n\n - " +
                "\n Slowness 3 \n Hunger 1");
        TOOLTIP_MAP.put("item.stims.sj_six_injector", " + \n Speed 3 " +
                "\n\n - " +
                "\n Darkness 1 \n Nausea 1");
        TOOLTIP_MAP.put("item.stims.xtg_twelve_injector", " + \n Removes all effects " +
                "\n\n - " +
                "\n Wither 1");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Check if this is running on the client side
        if (level.isClientSide) {
            log.info("Client-side use ignored");
            return InteractionResultHolder.fail(player.getItemInHand(hand)); // Prevent client-side usage logic
        }

        log.info("activated use");
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

        return InteractionResultHolder.consume(itemStack);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            int currentTick = (int) player.getCommandSenderWorld().getGameTime(); // Get current game time

            // Create a list to hold stims to remove after processing
            List<StimUse> toRemove = new ArrayList<>();

            synchronized (STIM_USE_LIST) { // Synchronize access to the list
                for (StimUse stimUse : STIM_USE_LIST) {
                    // Retrieve the configuration for this stimulant
                    StimConfig config = STIM_CONFIG_MAP.get(stimUse.stimType);
                    if (config == null) continue; // Skip if config is not found

                    // If the stimulant has a delay configured
                    int delayTicks = secondsToTicks(config.delaySeconds);
                    if (config.hasAfterDelayEffect && (currentTick - stimUse.tick >= delayTicks)) {

                        applyAfterDelayEffect(player, stimUse.stimType);

                        toRemove.add(stimUse);
                    }
                }
                // Now remove processed stims from the list after iteration is complete
                STIM_USE_LIST.removeAll(toRemove);
            }
        }
    }

    private void applyInitialEffect(Player player, String stimType) {
        log.info("Applying initial effect for stim type: " + stimType);
        StimConfig config = STIM_CONFIG_MAP.get(stimType);
        if (config != null) {
            switch (stimType) {
                case "item.stims.propital_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, secondsToTicks(config.effectDuration), 0, false, false));
                    break;

                case "item.stims.etg_c_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.SATURATION, secondsToTicks(config.effectDuration), 0, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, secondsToTicks(config.effectDuration), 2, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, secondsToTicks(config.effectDuration), 2, false, false));
                    break;

                case "item.stims.morphine_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, secondsToTicks(config.effectDuration), 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, secondsToTicks(config.effectDuration), 1, false, false));
                    break;

                case "item.stims.obdolbos_injector":
                    Random newrand = new Random();
                    int randint = newrand.nextInt(1, 5);
                    log.info("chosen randint: " + randint);

                    switch (randint) {
                        case 1:
                            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, secondsToTicks(config.effectDuration), 1, false, false));
                            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, secondsToTicks(config.effectDuration), 1, false, false));
                            break;

                        case 2:
                            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, secondsToTicks(config.effectDuration), 1, false, false));
                            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, secondsToTicks(config.effectDuration), 1, false, false));
                            break;

                        case 3:
                            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, secondsToTicks(config.effectDuration), 0, false, false));
                            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, secondsToTicks(config.effectDuration), 0, false, false));
                            player.addEffect(new MobEffectInstance(MobEffects.LUCK, secondsToTicks(config.effectDuration), 0, false, false));
                            break;

                        case 4:
                            player.addEffect(new MobEffectInstance(MobEffects.HARM, secondsToTicks(config.effectDuration), 100, false, false));
                            break;
                    }
                    break;

                case "item.stims.obdolbos_two_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, secondsToTicks(config.effectDuration), 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, secondsToTicks(config.effectDuration), 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, secondsToTicks(config.effectDuration), 1, false, false));
                    break;

                case "item.stims.xtg_twelve_injector":
                    player.removeAllEffects();
                    break;

                case "item.stims.sj_six_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, secondsToTicks(config.effectDuration), 2, false, false));
                    break;

                default:
                    log.info("stim use failed");
                    break;
            }
        } else {
            log.info("Config not found for stim type: " + stimType);
        }
    }

    private void applyAfterDelayEffect(Player player, String stimType) {
        log.info("Applying delay effect for stim type: " + stimType);
        StimConfig config = STIM_CONFIG_MAP.get(stimType);
        if (config != null) {
            switch (stimType) {
                case "item.stims.propital_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, secondsToTicks(config.afterDelayEffectDuration), 0, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, secondsToTicks(config.afterDelayEffectDuration), 0, false, false));
                    break;

                case "item.stims.etg_c_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, secondsToTicks(config.afterDelayEffectDuration), 0, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, secondsToTicks(config.afterDelayEffectDuration), 1, false, false));
                    break;

                case "item.stims.morphine_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, secondsToTicks(config.afterDelayEffectDuration), 0, false, false));
                    break;

                case "item.stims.obdolbos_injector":
                    Random newrand = new Random();
                    int randint = newrand.nextInt(1, 5);

                    switch (randint) {
                        case 1:
                            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, secondsToTicks(config.afterDelayEffectDuration), 1, false, false));
                            break;

                        case 2:
                            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, secondsToTicks(config.afterDelayEffectDuration), 2, false, false));
                            break;

                        case 3:
                            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, secondsToTicks(config.afterDelayEffectDuration), 2, false, false));
                            break;

                        case 4:
                            player.addEffect(new MobEffectInstance(MobEffects.WITHER, secondsToTicks(config.afterDelayEffectDuration), 1, false, false));
                            break;
                    }
                    break;

                case "item.stims.obdolbos_two_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, secondsToTicks(config.afterDelayEffectDuration), 2, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, secondsToTicks(config.afterDelayEffectDuration), 0, false, false));
                    break;

                case "item.stims.xtg_twelve_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, secondsToTicks(config.afterDelayEffectDuration), 0, false, false));
                    break;

                case "item.stims.sj_six_injector":
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, secondsToTicks(config.afterDelayEffectDuration), 0, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, secondsToTicks(config.afterDelayEffectDuration), 0, false, false));
                    break;
            }
        }
    }

    // Utility method to convert seconds to ticks
    private int secondsToTicks(int seconds) {
        return seconds * TICKS_PER_SECOND;
    }
}
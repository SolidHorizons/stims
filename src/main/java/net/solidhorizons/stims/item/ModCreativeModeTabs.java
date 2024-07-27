package net.solidhorizons.stims.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.solidhorizons.stims.Stims;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Stims.MOD_ID);

    //items that go in the stims tab
    public static final RegistryObject<CreativeModeTab> STIMS_TAB = CREATIVE_MODE_TABS.register("stims_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PROPITAL.get()))
                    .title(Component.translatable("creativetab.stims_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        pOutput.accept(ModItems.PROPITAL.get());

                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

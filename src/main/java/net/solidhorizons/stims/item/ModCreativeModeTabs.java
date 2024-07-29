package net.solidhorizons.stims.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.solidhorizons.stims.Stims;
import net.solidhorizons.stims.block.ModBlocks;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Stims.MOD_ID);

    //items that go in the stims tab
    public static final RegistryObject<CreativeModeTab> STIMS_TAB = CREATIVE_MODE_TABS.register("stims_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PROPITAL.get()))
                    .title(Component.translatable("creativetab.stims_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        pOutput.accept(ModItems.INJECTOR.get());
                        pOutput.accept(ModItems.NEEDLE.get());
                        pOutput.accept(ModItems.PROPITAL.get());
                        pOutput.accept(ModItems.XTG12.get());
                        pOutput.accept(ModItems.SJ6.get());
                        pOutput.accept(ModItems.ETG_C.get());
                        pOutput.accept(ModItems.MORPHINE.get());
                        pOutput.accept(ModItems.OBDOLBOS.get());
                        pOutput.accept(ModItems.OBDOLBOS_2.get());

                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

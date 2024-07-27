package net.solidhorizons.stims.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.solidhorizons.stims.Stims;
import net.solidhorizons.stims.item.custom.StimItem;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Stims.MOD_ID);


    //injectors
    public static final RegistryObject<Item> PROPITAL = ITEMS.register("propital_injector",
            () -> new StimItem(new Item.Properties()));

    public static final RegistryObject<Item> XTG12 = ITEMS.register("xtg_twelve_injector",
            () -> new StimItem(new Item.Properties()));

    public static final RegistryObject<Item> SJ6 = ITEMS.register("sj_six_injector",
            () -> new StimItem(new Item.Properties()));

    public static final RegistryObject<Item> ETG_C = ITEMS.register("etg_c_injector",
            () -> new StimItem(new Item.Properties()));

    public static final RegistryObject<Item> MORPHINE = ITEMS.register("morphine_injector",
            () -> new StimItem(new Item.Properties()));

    public static final RegistryObject<Item> OBDOLBOS = ITEMS.register("obdolbos_injector",
            () -> new StimItem(new Item.Properties()));

    public static final RegistryObject<Item> OBDOLBOS_2 = ITEMS.register("obdolbos_two_injector",
            () -> new StimItem(new Item.Properties()));

    public static final RegistryObject<Item> INJECTOR = ITEMS.register("injector",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> NEEDLE = ITEMS.register("needle",
            () -> new Item(new Item.Properties()));


    //ore
    public static final RegistryObject<Item> RUST = ITEMS.register("rust",
            () -> new Item(new Item.Properties()));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

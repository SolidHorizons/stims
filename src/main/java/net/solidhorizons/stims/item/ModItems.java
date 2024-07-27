package net.solidhorizons.stims.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.solidhorizons.stims.Stims;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Stims.MOD_ID);


    //injectors
    public static final RegistryObject<Item> PROPITAL = ITEMS.register("propital_injector",
            () -> new Item(new Item.Properties()));

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

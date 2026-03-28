package com.replenishinggourd.mod.init;

import com.replenishinggourd.mod.ReplenishingGourdMod;
import com.replenishinggourd.mod.item.GourdBaseItem;
import com.replenishinggourd.mod.item.ReplenishingGourdItem;
import com.replenishinggourd.mod.item.MysterySeedItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ReplenishingGourdMod.MOD_ID);

    // Base gourd (empty, used in crafting)
    public static final RegistryObject<Item> GOURD_BASE = ITEMS.register("gourd_base",
            () -> new GourdBaseItem(new Item.Properties()));

    // Mystery seed (catalyst in crafting)
    public static final RegistryObject<Item> MYSTERY_SEED = ITEMS.register("mystery_seed",
            () -> new MysterySeedItem(new Item.Properties()));

    // The replenishing gourd (result of crafting)
    public static final RegistryObject<Item> REPLENISHING_GOURD = ITEMS.register("replenishing_gourd",
            () -> new ReplenishingGourdItem(new Item.Properties().stacksTo(1).durability(3)));
}

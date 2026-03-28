package com.replenishinggourd.mod;

import com.replenishinggourd.mod.init.ModItems;
import com.replenishinggourd.mod.init.ModCreativeTabs;
import com.replenishinggourd.mod.item.ModRecipeSerializers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ReplenishingGourdMod.MOD_ID)
public class ReplenishingGourdMod {

    public static final String MOD_ID = "replenishinggourd";

    public ReplenishingGourdMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
}

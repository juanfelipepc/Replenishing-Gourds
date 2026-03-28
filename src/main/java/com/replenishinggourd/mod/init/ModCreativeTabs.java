package com.replenishinggourd.mod.init;

import com.replenishinggourd.mod.ReplenishingGourdMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ReplenishingGourdMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> GOURD_TAB = CREATIVE_MODE_TABS.register("gourd_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.replenishinggourd"))
                    .icon(() -> new ItemStack(ModItems.GOURD_BASE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.GOURD_BASE.get());
                        output.accept(ModItems.MYSTERY_SEED.get());
                        output.accept(ModItems.REPLENISHING_GOURD.get());
                    })
                    .build());
}

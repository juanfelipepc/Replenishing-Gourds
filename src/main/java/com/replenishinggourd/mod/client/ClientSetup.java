package com.replenishinggourd.mod.client;

import com.replenishinggourd.mod.init.ModItems;
import com.replenishinggourd.mod.item.ReplenishingGourdItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.replenishinggourd.mod.ReplenishingGourdMod.MOD_ID;

/**
 * Registers a custom item property "effect_id" so the model JSON can
 * select a different texture per stored potion effect.
 *
 * The property returns a float 0.0–1.0 that indexes into the overrides list
 * in replenishing_gourd.json.
 */
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    // Keep this list in the SAME ORDER as the overrides in replenishing_gourd.json
    private static final String[] EFFECT_ORDER = {
        "speed", "slowness", "strength", "weakness",
        "instant_health", "instant_damage", "regeneration", "poison",
        "fire_resistance", "water_breathing", "night_vision", "invisibility",
        "jump", "slow_falling", "movement_slowdown"
    };

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
            ItemProperties.register(
                ModItems.REPLENISHING_GOURD.get(),
                new ResourceLocation(MOD_ID, "effect_index"),
                (stack, level, entity, seed) -> {
                    var tag = stack.getTag();
                    if (tag == null || !tag.contains("EffectId")) return 0f;
                    var loc = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("EffectId"));
                    if (loc == null) return 0f;
                    String path = loc.getPath();
                    for (int i = 0; i < EFFECT_ORDER.length; i++) {
                        if (EFFECT_ORDER[i].equals(path)) return (i + 1) / 16.0f;
                    }
                    return 0f;
                }
            )
        );
    }
}

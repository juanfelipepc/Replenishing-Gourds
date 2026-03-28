package com.replenishinggourd.mod.item;

import com.google.gson.JsonObject;
import com.replenishinggourd.mod.ReplenishingGourdMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ReplenishingGourdMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<GourdCraftingRecipe>> GOURD_CRAFTING =
            RECIPE_SERIALIZERS.register("gourd_crafting", GourdRecipeSerializer::new);

    // Inline serializer — reads/writes the recipe from JSON and network packets
    public static class GourdRecipeSerializer implements RecipeSerializer<GourdCraftingRecipe> {

        @Override
        public GourdCraftingRecipe fromJson(net.minecraft.resources.ResourceLocation id, JsonObject json) {
            CraftingBookCategory category = CraftingBookCategory.CODEC
                    .byName(GsonHelper.getAsString(json, "category", null),
                            CraftingBookCategory.MISC);
            return new GourdCraftingRecipe(category);
        }

        @Override
        public GourdCraftingRecipe fromNetwork(net.minecraft.resources.ResourceLocation id,
                                               FriendlyByteBuf buf) {
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            return new GourdCraftingRecipe(category);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, GourdCraftingRecipe recipe) {
            buf.writeEnum(recipe.category());
        }
    }
}

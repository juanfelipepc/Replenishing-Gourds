package com.replenishinggourd.mod.item;

import com.replenishinggourd.mod.init.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Special shapeless recipe:
 *   Gourd Base + Mystery Seed + Any Potion -> Replenishing Gourd of [Potion Effect]
 *
 * Implements CraftingRecipe directly to avoid the ResourceLocation constructor
 * ambiguity in CustomRecipe across Forge 47.x patch versions.
 */
public class GourdCraftingRecipe implements CraftingRecipe {

    private final CraftingBookCategory category;

    public GourdCraftingRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean hasGourd  = false;
        boolean hasSeed   = false;
        boolean hasPotion = false;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.GOURD_BASE.get()))   { hasGourd  = true; continue; }
            if (stack.is(ModItems.MYSTERY_SEED.get()))  { hasSeed   = true; continue; }
            if (stack.is(Items.POTION))                 { hasPotion = true; continue; }

            // Any other item in the grid -> recipe doesn't match
            return false;
        }

        return hasGourd && hasSeed && hasPotion;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack potionStack = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(Items.POTION)) {
                potionStack = stack;
                break;
            }
        }

        if (potionStack.isEmpty()) return ItemStack.EMPTY;

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(potionStack);
        if (effects.isEmpty()) return ItemStack.EMPTY;

        MobEffectInstance primaryEffect = effects.get(0);
        ItemStack gourd = new ItemStack(ModItems.REPLENISHING_GOURD.get());
        return ReplenishingGourdItem.createFilledGourd(gourd, primaryEffect);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(ModItems.REPLENISHING_GOURD.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.GOURD_CRAFTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation("replenishinggourd", "gourd_crafting");
    }
}

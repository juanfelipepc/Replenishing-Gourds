package com.replenishinggourd.mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Replenishing Gourd.
 *
 * NBT tags used:
 *   - "EffectId"       : ResourceLocation string of the stored MobEffect
 *   - "EffectDuration" : int, duration in ticks copied from the crafted potion
 *   - "EffectAmplifier": int, amplifier copied from the crafted potion
 *   - "Charges"        : int, remaining uses (0-3)
 *   - "LastUsedTime"   : long, world game-time when last charge was consumed
 *
 * Charge replenishment:
 *   One charge every 60 seconds (1200 ticks).
 *   Checked lazily when the item is used or inspected.
 */
public class ReplenishingGourdItem extends Item {

    public static final int MAX_CHARGES = 3;
    /** Ticks between each charge replenishment: 60 s × 20 t/s = 1200 */
    public static final long RECHARGE_TICKS = 1200L;

    // NBT keys
    private static final String TAG_EFFECT_ID        = "EffectId";
    private static final String TAG_EFFECT_DURATION  = "EffectDuration";
    private static final String TAG_EFFECT_AMPLIFIER = "EffectAmplifier";
    private static final String TAG_CHARGES          = "Charges";
    private static final String TAG_LAST_USED_TIME   = "LastUsedTime";

    public ReplenishingGourdItem(Properties properties) {
        super(properties);
    }

    // -----------------------------------------------------------------------
    // Static helpers — used by the crafting recipe handler
    // -----------------------------------------------------------------------

    /**
     * Imprint a potion effect onto a gourd stack.
     * Called from the custom crafting recipe after validating ingredients.
     */
    public static ItemStack createFilledGourd(ItemStack gourdStack, MobEffectInstance effect) {
        CompoundTag tag = gourdStack.getOrCreateTag();
        tag.putString(TAG_EFFECT_ID, ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect()).toString());
        tag.putInt(TAG_EFFECT_DURATION, effect.getDuration());
        tag.putInt(TAG_EFFECT_AMPLIFIER, effect.getAmplifier());
        tag.putInt(TAG_CHARGES, MAX_CHARGES);
        tag.putLong(TAG_LAST_USED_TIME, 0L);
        return gourdStack;
    }

    // -----------------------------------------------------------------------
    // Charge replenishment (lazy evaluation)
    // -----------------------------------------------------------------------

    /**
     * Recalculate how many charges should have replenished since last use,
     * and update the stack NBT. Call this before reading/modifying charges.
     */
    private void refreshCharges(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_CHARGES)) return;

        int charges = tag.getInt(TAG_CHARGES);
        if (charges >= MAX_CHARGES) return; // Already full

        long lastUsed  = tag.getLong(TAG_LAST_USED_TIME);
        long now       = level.getGameTime();
        long elapsed   = now - lastUsed;

        // How many full recharge intervals have passed?
        int gained = (int) (elapsed / RECHARGE_TICKS);
        if (gained <= 0) return;

        int newCharges = Math.min(MAX_CHARGES, charges + gained);
        tag.putInt(TAG_CHARGES, newCharges);

        if (newCharges < MAX_CHARGES) {
            // Advance lastUsedTime by the ticks we consumed
            tag.putLong(TAG_LAST_USED_TIME, lastUsed + (long) gained * RECHARGE_TICKS);
        }
        // If now full, no need to keep tracking time
    }

    // -----------------------------------------------------------------------
    // Item use
    // -----------------------------------------------------------------------

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        // Must have an effect imprinted
        if (!tag.contains(TAG_EFFECT_ID)) {
            player.displayClientMessage(
                    Component.literal("This gourd is empty. Craft it with a potion first!").withStyle(ChatFormatting.RED),
                    true);
            return InteractionResultHolder.fail(stack);
        }

        // Lazy-refresh charges
        if (!level.isClientSide) {
            refreshCharges(stack, level);
        }

        int charges = tag.getInt(TAG_CHARGES);
        if (charges <= 0) {
            player.displayClientMessage(
                    Component.literal("No charges left — wait for it to replenish!").withStyle(ChatFormatting.GOLD),
                    true);
            return InteractionResultHolder.fail(stack);
        }

        // Apply the stored effect
        if (!level.isClientSide) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(
                    ResourceLocation.tryParse(tag.getString(TAG_EFFECT_ID)));

            if (effect != null) {
                int duration  = tag.getInt(TAG_EFFECT_DURATION);
                int amplifier = tag.getInt(TAG_EFFECT_AMPLIFIER);
                player.addEffect(new MobEffectInstance(effect, duration, amplifier));

                // Deduct a charge and record the time
                int newCharges = charges - 1;
                tag.putInt(TAG_CHARGES, newCharges);
                if (newCharges < MAX_CHARGES) {
                    // Start the recharge clock from now (only if we weren't already counting)
                    long lastUsed = tag.getLong(TAG_LAST_USED_TIME);
                    if (newCharges == MAX_CHARGES - 1) {
                        // First charge consumed — begin timer
                        tag.putLong(TAG_LAST_USED_TIME, level.getGameTime());
                    }
                    // If already counting (lastUsed != 0 and charges were partially filled),
                    // keep the existing timer so partial progress is preserved.
                }

                player.displayClientMessage(
                        Component.literal("Charges remaining: " + newCharges + "/" + MAX_CHARGES)
                                .withStyle(ChatFormatting.GREEN),
                        true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    // -----------------------------------------------------------------------
    // Durability bar (visual charge indicator)
    // -----------------------------------------------------------------------

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getOrCreateTag().contains(TAG_CHARGES);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int charges = stack.getOrCreateTag().getInt(TAG_CHARGES);
        return Math.round(13.0f * charges / MAX_CHARGES);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int charges = stack.getOrCreateTag().getInt(TAG_CHARGES);
        if (charges == MAX_CHARGES) return 0x00FF00; // Green — full
        if (charges > 0)            return 0xFFAA00; // Orange — partial
        return 0xFF2200;                              // Red — empty
    }

    // -----------------------------------------------------------------------
    // Tooltip
    // -----------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(TAG_EFFECT_ID)) {
            tooltip.add(Component.literal("Empty — craft with a potion to fill it.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(
                ResourceLocation.tryParse(tag.getString(TAG_EFFECT_ID)));

        if (effect != null) {
            tooltip.add(Component.literal("Effect: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(effect.getDescriptionId())
                            .withStyle(ChatFormatting.AQUA)));
        }

        int charges = tag.getInt(TAG_CHARGES);
        tooltip.add(Component.literal("Charges: " + charges + "/" + MAX_CHARGES)
                .withStyle(charges > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.add(Component.literal("Replenishes 1 charge every 60 seconds.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    // -----------------------------------------------------------------------
    // Prevent stacking gourds with different effects / charges
    // -----------------------------------------------------------------------

    @Override
    public boolean isFoil(ItemStack stack) {
        // Slight enchantment glint when fully charged
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains(TAG_CHARGES) && tag.getInt(TAG_CHARGES) == MAX_CHARGES;
    }
}

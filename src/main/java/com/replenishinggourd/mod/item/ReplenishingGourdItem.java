package com.replenishinggourd.mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * The Replenishing Gourd.
 *
 * NBT tags:
 *   "EffectId"        - ResourceLocation string of the stored MobEffect
 *   "EffectDuration"  - int, ticks
 *   "EffectAmplifier" - int
 *   "Charges"         - int 0-3
 *   "LastUsedTime"    - long, game-time when a charge was last consumed
 *
 * Charge replenishment: 1 charge per 60 s (1200 ticks), checked lazily.
 */
public class ReplenishingGourdItem extends Item {

    public static final int  MAX_CHARGES    = 3;
    public static final long RECHARGE_TICKS = 1200L; // 60 s

    private static final String TAG_EFFECT_ID        = "EffectId";
    private static final String TAG_EFFECT_DURATION  = "EffectDuration";
    private static final String TAG_EFFECT_AMPLIFIER = "EffectAmplifier";
    private static final String TAG_CHARGES          = "Charges";
    private static final String TAG_LAST_USED_TIME   = "LastUsedTime";

    /**
     * Maps MobEffect registry path → texture suffix used in the model filename.
     * e.g.  "speed" → "replenishing_gourd_speed"
     */
    private static final Map<String, String> EFFECT_TO_MODEL = Map.ofEntries(
        Map.entry("speed",              "replenishing_gourd_speed"),
        Map.entry("slowness",           "replenishing_gourd_slowness"),
        Map.entry("strength",           "replenishing_gourd_strength"),
        Map.entry("weakness",           "replenishing_gourd_weakness"),
        Map.entry("instant_health",     "replenishing_gourd_instant_health"),
        Map.entry("instant_damage",     "replenishing_gourd_instant_damage"),
        Map.entry("regeneration",       "replenishing_gourd_regeneration"),
        Map.entry("poison",             "replenishing_gourd_poison"),
        Map.entry("fire_resistance",    "replenishing_gourd_fire_resistance"),
        Map.entry("water_breathing",    "replenishing_gourd_water_breathing"),
        Map.entry("night_vision",       "replenishing_gourd_night_vision"),
        Map.entry("invisibility",       "replenishing_gourd_invisibility"),
        Map.entry("jump",               "replenishing_gourd_jump_boost"),
        Map.entry("slow_falling",       "replenishing_gourd_slow_falling"),
        Map.entry("movement_slowdown",  "replenishing_gourd_movement_slowdown")
    );

    public ReplenishingGourdItem(Properties properties) {
        super(properties);
    }

    // ── Static factory called from GourdCraftingRecipe ───────────────────────

    public static ItemStack createFilledGourd(ItemStack gourdStack, MobEffectInstance effect) {
        CompoundTag tag = gourdStack.getOrCreateTag();
        tag.putString(TAG_EFFECT_ID,        ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect()).toString());
        tag.putInt(TAG_EFFECT_DURATION,     effect.getDuration());
        tag.putInt(TAG_EFFECT_AMPLIFIER,    effect.getAmplifier());
        tag.putInt(TAG_CHARGES,             MAX_CHARGES);
        tag.putLong(TAG_LAST_USED_TIME,     0L);
        return gourdStack;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns the model name suffix for the stored effect, e.g. "replenishing_gourd_speed".
     * Falls back to "replenishing_gourd" (base texture) if unknown.
     */
    public static String getModelName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_EFFECT_ID)) return "replenishing_gourd";
        ResourceLocation loc = ResourceLocation.tryParse(tag.getString(TAG_EFFECT_ID));
        if (loc == null) return "replenishing_gourd";
        String path = loc.getPath(); // e.g. "speed", "jump", "instant_health"
        return EFFECT_TO_MODEL.getOrDefault(path, "replenishing_gourd");
    }

    private void refreshCharges(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_CHARGES)) return;
        int charges = tag.getInt(TAG_CHARGES);
        if (charges >= MAX_CHARGES) return;

        long lastUsed = tag.getLong(TAG_LAST_USED_TIME);
        long elapsed  = level.getGameTime() - lastUsed;
        int gained    = (int) (elapsed / RECHARGE_TICKS);
        if (gained <= 0) return;

        int newCharges = Math.min(MAX_CHARGES, charges + gained);
        tag.putInt(TAG_CHARGES, newCharges);
        if (newCharges < MAX_CHARGES) {
            tag.putLong(TAG_LAST_USED_TIME, lastUsed + (long) gained * RECHARGE_TICKS);
        }
    }

    // ── Item use ─────────────────────────────────────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(TAG_EFFECT_ID)) {
            player.displayClientMessage(
                Component.literal("This gourd is empty — craft it with a potion first!").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) refreshCharges(stack, level);

        int charges = tag.getInt(TAG_CHARGES);
        if (charges <= 0) {
            player.displayClientMessage(
                Component.literal("No charges left — wait for it to replenish!").withStyle(ChatFormatting.GOLD), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(
                ResourceLocation.tryParse(tag.getString(TAG_EFFECT_ID)));
            if (effect != null) {
                player.addEffect(new MobEffectInstance(effect, tag.getInt(TAG_EFFECT_DURATION), tag.getInt(TAG_EFFECT_AMPLIFIER)));
                int newCharges = charges - 1;
                tag.putInt(TAG_CHARGES, newCharges);
                if (newCharges == MAX_CHARGES - 1) {
                    tag.putLong(TAG_LAST_USED_TIME, level.getGameTime());
                }
                player.displayClientMessage(
                    Component.literal("Charges: " + newCharges + "/" + MAX_CHARGES).withStyle(ChatFormatting.GREEN), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    // ── Durability bar ───────────────────────────────────────────────────────

    @Override public boolean isBarVisible(ItemStack stack) {
        return stack.getOrCreateTag().contains(TAG_CHARGES);
    }
    @Override public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * stack.getOrCreateTag().getInt(TAG_CHARGES) / MAX_CHARGES);
    }
    @Override public int getBarColor(ItemStack stack) {
        int c = stack.getOrCreateTag().getInt(TAG_CHARGES);
        return c == MAX_CHARGES ? 0x00FF00 : c > 0 ? 0xFFAA00 : 0xFF2200;
    }

    // ── Tooltip ──────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_EFFECT_ID)) {
            tooltip.add(Component.literal("Empty — craft with a potion to fill it.").withStyle(ChatFormatting.GRAY));
            return;
        }
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(tag.getString(TAG_EFFECT_ID)));
        if (effect != null) {
            tooltip.add(Component.literal("Effect: ").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(effect.getDescriptionId()).withStyle(ChatFormatting.AQUA)));
        }
        int charges = tag.getInt(TAG_CHARGES);
        tooltip.add(Component.literal("Charges: " + charges + "/" + MAX_CHARGES)
            .withStyle(charges > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltip.add(Component.literal("Replenishes 1 charge every 60 seconds.").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains(TAG_CHARGES) && tag.getInt(TAG_CHARGES) == MAX_CHARGES;
    }
}

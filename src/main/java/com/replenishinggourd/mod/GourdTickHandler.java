package com.replenishinggourd.mod;

import com.replenishinggourd.mod.init.ModItems;
import com.replenishinggourd.mod.item.ReplenishingGourdItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Ticks every second (20 ticks) on the server side.
 * For each player, checks all gourd stacks in the hotbar and main hand.
 * If a charge has replenished since last tick, it updates the NBT in-place
 * and calls setChanged() so the client receives the updated durability bar.
 */
@Mod.EventBusSubscriber(modid = ReplenishingGourdMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GourdTickHandler {

    private static final int CHECK_INTERVAL = 20; // ticks (1 second)

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only run on server side, only at end of tick, once per CHECK_INTERVAL
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        long gameTime = player.level().getGameTime();

        // Only check once per second to avoid unnecessary work
        if (gameTime % CHECK_INTERVAL != 0) return;

        // Check every slot in the hotbar (slots 0-8) plus off-hand
        var inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            refreshStack(inventory.getItem(i), gameTime);
        }
        refreshStack(player.getOffhandItem(), gameTime);
    }

    private static void refreshStack(ItemStack stack, long gameTime) {
        if (stack.isEmpty()) return;
        if (!stack.is(ModItems.REPLENISHING_GOURD.get())) return;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Charges")) return;

        int charges = tag.getInt("Charges");
        if (charges >= ReplenishingGourdItem.MAX_CHARGES) return; // already full, nothing to do

        long lastUsed = tag.getLong("LastUsedTime");
        if (lastUsed == 0L) return; // timer not started yet

        long elapsed = gameTime - lastUsed;
        int gained = (int) (elapsed / ReplenishingGourdItem.RECHARGE_TICKS);
        if (gained <= 0) return; // no new charges yet

        // A charge has replenished — update NBT and notify the client
        int newCharges = Math.min(ReplenishingGourdItem.MAX_CHARGES, charges + gained);
        tag.putInt("Charges", newCharges);

        if (newCharges < ReplenishingGourdItem.MAX_CHARGES) {
            // Advance the timestamp so we don't double-count ticks
            tag.putLong("LastUsedTime", lastUsed + (long) gained * ReplenishingGourdItem.RECHARGE_TICKS);
        }

        // Mark the stack dirty — this tells Forge/Vanilla to sync the item
        // NBT to the client so the durability bar redraws
        stack.setTag(tag);
    }
}

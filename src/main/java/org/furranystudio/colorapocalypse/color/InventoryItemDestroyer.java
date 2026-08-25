/**
 * File: InventoryItemDestroyer.java
 * Author: Maxime66410
 * Created: 2026-08-24
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.color;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

// Clears matching-colored items from every online player's inventory and equipment. Opt-in.
public final class InventoryItemDestroyer {

    private InventoryItemDestroyer() {
    }

    public static int destroy(DyeColor color, MinecraftServer server) {
        int destroyed = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Inventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (!stack.isEmpty() && ItemColorResolver.resolve(stack.getItem()) == color) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                    destroyed++;
                }
            }

            for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty() && ItemColorResolver.resolve(stack.getItem()) == color) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    destroyed++;
                }
            }
        }

        return destroyed;
    }
}

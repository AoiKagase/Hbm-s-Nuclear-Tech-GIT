package com.hbm.inventory;

import com.hbm.items.machine.ItemMachineUpgrade;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Common container behavior used by HBM machines.
 */
public abstract class ContainerNTM extends Container {

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
		if(clickType == ClickType.QUICK_MOVE && slotId >= 0 && slotId < inventorySlots.size()) {
			Slot source = inventorySlots.get(slotId);
			if(source.inventory == player.inventory && source.getHasStack()) {
				ItemStack stack = source.getStack();
				if(stack.getItem() instanceof ItemMachineUpgrade) {
					ItemStack original = stack.copy();
					if(mergeIntoUpgradeSlots(stack)) {
						if(stack.isEmpty()) {
							source.putStack(ItemStack.EMPTY);
						} else {
							source.onSlotChanged();
						}
						return original;
					}
					return ItemStack.EMPTY;
				}
			}
		}
		return super.slotClick(slotId, dragType, clickType, player);
	}

	private boolean mergeIntoUpgradeSlots(ItemStack source) {
		boolean changed = false;
		for(Slot target : inventorySlots) {
			if(!(target instanceof SlotUpgrade) || !target.isItemValid(source)) {
				continue;
			}

			ItemStack existing = target.getStack();
			int limit = Math.min(target.getSlotStackLimit(), source.getMaxStackSize());
			if(existing.isEmpty()) {
				int moved = Math.min(limit, source.getCount());
				ItemStack inserted = source.copy();
				inserted.setCount(moved);
				target.putStack(inserted);
				source.shrink(moved);
				changed = true;
			} else if(ItemStack.areItemsEqual(existing, source)
					&& ItemStack.areItemStackTagsEqual(existing, source)
					&& existing.getCount() < limit) {
				int moved = Math.min(limit - existing.getCount(), source.getCount());
				existing.grow(moved);
				source.shrink(moved);
				target.onSlotChanged();
				changed = true;
			}

			if(source.isEmpty()) {
				break;
			}
		}
		return changed;
	}
}

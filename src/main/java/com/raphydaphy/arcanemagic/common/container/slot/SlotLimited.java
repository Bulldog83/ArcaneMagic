package com.raphydaphy.arcanemagic.common.container.slot;

import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotLimited extends SlotItemHandler
{
	private Predicate<ItemStack> canAccept;

	public SlotLimited(IItemHandler itemHandler, int index, int xPosition, int yPosition,
			Predicate<ItemStack> canAccept)
	{
		super(itemHandler, index, xPosition, yPosition);

		this.canAccept = canAccept;
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return canAccept.test(stack);
	}

}
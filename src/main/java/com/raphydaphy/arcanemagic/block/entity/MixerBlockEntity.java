package com.raphydaphy.arcanemagic.block.entity;

import com.raphydaphy.arcanemagic.block.entity.base.DoubleBlockEntity;
import com.raphydaphy.arcanemagic.block.entity.base.DoubleFluidBlockEntity;
import com.raphydaphy.arcanemagic.init.ModRegistry;
import com.raphydaphy.arcanemagic.network.ArcaneMagicPacketHandler;
import com.raphydaphy.arcanemagic.network.ClientBlockEntityUpdatePacket;
import com.raphydaphy.arcanemagic.util.ArcaneMagicUtils;
import io.github.prospector.silk.fluid.DropletValues;
import io.github.prospector.silk.fluid.FluidContainer;
import io.github.prospector.silk.fluid.FluidInstance;
import net.minecraft.client.network.packet.BlockEntityUpdateS2CPacket;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.InventoryUtil;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

public class MixerBlockEntity extends DoubleFluidBlockEntity implements SidedInventory, Tickable, FluidContainer
{
	private static final String WATER_KEY = "Water";
	private static final String LIQUIFIED_SOUL_KEY = "LiquifiedSoul";
	private static final int MAX_FLUID = DropletValues.BUCKET * 4;

	private FluidInstance water = FluidInstance.EMPTY.copy();
	private FluidInstance liquified_soul = FluidInstance.EMPTY.copy();
	public long ticks = 0;

	private final int[] slots = { 0 };

	public MixerBlockEntity()
	{
		super(ModRegistry.MIXER_TE, 1);
	}

	@Override
	public void tick()
	{
		if (world.isClient)
		{
			ticks++;
		}
		if (!setBottom)
		{
			bottom = ArcaneMagicUtils.isBottomBlock(world, pos, ModRegistry.MIXER);
			setBottom = true;
		}
	}

	@Override
	public void fromTag(CompoundTag tag)
	{
		super.fromTag(tag);
		if (tag.containsKey(WATER_KEY))
		{
			water = new FluidInstance((CompoundTag)tag.getTag(WATER_KEY));
		} else
		{
			System.out.println("No tag found... setting water to empty");
			water = FluidInstance.EMPTY.copy();
		}
		if (tag.containsKey(LIQUIFIED_SOUL_KEY))
		{
			liquified_soul = new FluidInstance((CompoundTag)tag.getTag(LIQUIFIED_SOUL_KEY));
		} else
		{
			System.out.println("No tag found... setting liquified soul to empty");
			liquified_soul = FluidInstance.EMPTY.copy();
		}
	}

	@Override
	public void writeContents(CompoundTag tag)
	{
		if (bottom)
		{
			InventoryUtil.serialize(tag, contents);
			if (!water.isEmpty())
			{
				CompoundTag waterTag = new CompoundTag();
				waterTag.putString(FluidInstance.FLUID_KEY, Registry.FLUID.getId(water.getFluid()).toString());
				waterTag.putInt(FluidInstance.AMOUNT_KEY, water.getAmount());
				tag.put(WATER_KEY, waterTag);
			}
			if (!liquified_soul.isEmpty())
			{
				CompoundTag liquifiedSoulTag = new CompoundTag();
				liquifiedSoulTag.putString(FluidInstance.FLUID_KEY, Registry.FLUID.getId(liquified_soul.getFluid()).toString());
				liquifiedSoulTag.putInt(FluidInstance.AMOUNT_KEY, liquified_soul.getAmount());
				tag.put(LIQUIFIED_SOUL_KEY, liquifiedSoulTag);
			}
		}
	}

	@Override
	public int getMaxCapacity()
	{
		return MAX_FLUID;
	}

	@Override
	protected boolean canInsertFluidImpl(boolean bottom, Direction fromSide, Fluid fluid, int amount)
	{
		System.out.println(this.water.getAmount() + " droplets" );
		return !bottom && fluid == Fluids.WATER && this.water.getAmount() + amount <= MAX_FLUID;
	}

	@Override
	protected boolean canExtractFluidImpl(boolean bottom, Direction fromSide, Fluid fluid, int amount)
	{
		return bottom && fluid == ModRegistry.LIQUIFIED_SOUL && this.liquified_soul.getAmount() + amount <= MAX_FLUID;
	}

	@Override
	protected void insertFluidImpl(boolean bottom, Direction fromSide, Fluid fluid, int amount)
	{
		if (!world.isClient && fluid == Fluids.WATER && this.water.getAmount() + amount <= MAX_FLUID)
		{
			this.water.addAmount(amount);

			if (this.water.getFluid() != fluid)
			{
				this.water.setFluid(fluid);
			}
			markDirty();
		}
	}

	@Override
	protected void extractFluidImpl(boolean bottom, Direction fromSide, Fluid fluid, int amount)
	{
		if (!world.isClient && this.liquified_soul.getFluid() == fluid && this.liquified_soul.getAmount() - amount >= 0)
		{
			this.liquified_soul.subtractAmount(amount);

			if (this.liquified_soul.getAmount() == 0)
			{
				this.liquified_soul = FluidInstance.EMPTY.copy();
			}
			markDirty();
		}
	}

	@Override
	protected void setFluidImpl(boolean bottom, Direction fromSide, FluidInstance instance)
	{
		if (!world.isClient)
		{
			if (bottom)
			{
				this.liquified_soul = instance;
			} else
			{
				this.water = instance;
			}
			markDirty();
		}
	}

	@Override
	protected FluidInstance[] getFluidsImpl(boolean bottom, Direction fromSide)
	{
		if (bottom)
		{
			return new FluidInstance[] {liquified_soul};
		} else
		{
			return new FluidInstance[] {water};
		}
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		ArcaneMagicPacketHandler.sendToAllAround(new ClientBlockEntityUpdatePacket(toInitialChunkDataTag()), world, getPos(), 300);
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket()
	{
		CompoundTag tag = super.toInitialChunkDataTag();
		writeContents(tag);
		return new BlockEntityUpdateS2CPacket(getPos(), -1, tag);
	}

	@Override
	public CompoundTag toInitialChunkDataTag()
	{
		CompoundTag tag = super.toInitialChunkDataTag();
		writeContents(tag);
		return tag;
	}

	@Override
	public int getInvMaxStackAmount()
	{
		return 1;
	}

	@Override
	public boolean isValidInvStackBottom(int slot, ItemStack item)
	{
		return getInvStack(slot).isEmpty() && !item.isEmpty() && item.getItem() == ModRegistry.SOUL_PENDANT;
	}

	@Override
	public int[] getInvAvailableSlots(Direction dir)
	{
		return dir == Direction.UP ? new int[0] : slots;
	}

	@Override
	public boolean canInsertInvStack(int slot, ItemStack stack, Direction dir)
	{
		return isValidInvStack(slot, stack);
	}

	@Override
	public boolean canExtractInvStack(int slot, ItemStack stack, Direction dir)
	{
		return true;
	}
}
package com.minecraftabnormals.environmental.common.item;

import com.minecraftabnormals.environmental.common.block.GiantLilyPadBlock;
import com.minecraftabnormals.environmental.core.registry.EnvironmentalBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public class GiantLilyPadItem extends BlockItem {
	public GiantLilyPadItem(Item.Properties builder) {
		super(EnvironmentalBlocks.GIANT_LILY_PAD.get(), builder);
	}

	public ActionResultType useOn(ItemUseContext context) {
		return ActionResultType.PASS;
	}

	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		RayTraceResult raytraceresult = getPlayerPOVHitResult(worldIn, playerIn, RayTraceContext.FluidMode.SOURCE_ONLY);
		if (raytraceresult.getType() == RayTraceResult.Type.MISS) {
			return ActionResult.pass(itemstack);
		} else {
			if (raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
				BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult) raytraceresult;
				BlockPos blockpos = blockraytraceresult.getBlockPos();
				Direction direction = blockraytraceresult.getDirection();
				if (!worldIn.mayInteract(playerIn, blockpos) || !playerIn.mayUseItemAt(blockpos.relative(direction), direction, itemstack)) {
					return ActionResult.fail(itemstack);
				}

				BlockPos blockpos1 = blockpos.above();
				FluidState ifluidstate = worldIn.getFluidState(blockpos);
				if ((ifluidstate.getType() == Fluids.WATER) && GiantLilyPadBlock.checkPositions(worldIn, blockpos1, this.getBlock().defaultBlockState())) {

					// special case for handling block placement with water lilies
					BlockSnapshot blocksnapshot = BlockSnapshot.create(worldIn.dimension(), worldIn, blockpos1);
					if (!worldIn.isClientSide())
						GiantLilyPadBlock.placeAt(worldIn, blockpos1, this.getBlock().defaultBlockState(), 18);
					if (ForgeEventFactory.onBlockPlace(playerIn, blocksnapshot, net.minecraft.util.Direction.UP)) {
						blocksnapshot.restore(true, false);
						return ActionResult.fail(itemstack);
					}

					if (playerIn instanceof ServerPlayerEntity) {
						CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) playerIn, blockpos1, itemstack);
					}

					if (!playerIn.abilities.instabuild) {
						itemstack.shrink(1);
					}

					playerIn.awardStat(Stats.ITEM_USED.get(this));
					worldIn.playSound(playerIn, blockpos, SoundEvents.LILY_PAD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
					return ActionResult.success(itemstack);
				}
			}

			return ActionResult.fail(itemstack);
		}
	}
}

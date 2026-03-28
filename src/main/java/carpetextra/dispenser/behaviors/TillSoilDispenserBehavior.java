package carpetextra.dispenser.behaviors;

import java.util.Set;

import carpetextra.dispenser.DispenserItemUsageContext;
import carpetextra.mixins.HoeItem_TilledBlocksAccessorMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class TillSoilDispenserBehavior extends OptionalDispenseItemBehavior {
    public static final Set<Block> TILLED_BLOCKS = HoeItem_TilledBlocksAccessorMixin.getTilledBlocks().keySet();

    @Override
    protected @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        this.setSuccess(true);
        ServerLevel level = source.level();
        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos frontBlockPos = source.pos().offset(dispenserFacing.getUnitVec3i());

        // check block in front of dispenser and one block down
        for(int i = 0; i < 2; i++) {
            BlockPos hoeBlockPos = frontBlockPos.below(i);
            BlockState hoeBlockState = level.getBlockState(hoeBlockPos);
            Block hoeBlock = hoeBlockState.getBlock();

            // check if hoe can be used on block
            if(TILLED_BLOCKS.contains(hoeBlock)) {
                BlockHitResult hitResult = new BlockHitResult(new Vec3(hoeBlockPos), dispenserFacing.getOpposite(), hoeBlockPos, false);
                UseOnContext useOnContext = new DispenserItemUsageContext(level, stack, hitResult);
                DispenserItemUsageContext context = new DispenserItemUsageContext(level, stack, hitResult);

                // use on block, test if successful
                if(stack.getItem().useOn(context).consumesAction()) {
                    // damage hoe, remove if broken
                    stack.hurtAndBreak(1, level, null, (item) -> stack.setCount(0));
                    return stack;
                }
            }
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }
}

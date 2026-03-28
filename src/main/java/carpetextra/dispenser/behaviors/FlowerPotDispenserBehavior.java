package carpetextra.dispenser.behaviors;

import carpetextra.helpers.FlowerPotHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class FlowerPotDispenserBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected @NotNull ItemStack execute(BlockSource source, ItemStack stack) {
        this.setSuccess(true);
        Item item = stack.getItem();
        ServerLevel world = source.level();
        BlockPos frontBlockPos = source.pos().offset(source.state().getValue(DispenserBlock.FACING).getUnitVec3i());
        BlockState frontBlockState = world.getBlockState(frontBlockPos);
        FlowerPotBlock frontBlock = (FlowerPotBlock) frontBlockState.getBlock();

        // check if flower pot is empty
        if(frontBlock.getPotted() == Blocks.AIR && FlowerPotHelper.isPottable(item)) {
            FlowerPotBlock pottedBlock = FlowerPotHelper.getPottedBlock(item);

            // place filled flower pot
            world.setBlock(frontBlockPos, pottedBlock.defaultBlockState(), Block.UPDATE_ALL);
            world.gameEvent(null, GameEvent.BLOCK_CHANGE, frontBlockPos);

            // check if flower pot should load chunk
            FlowerPotHelper.updateLoadStatus(world, frontBlockPos, pottedBlock.getPotted(), true);

            // remove flower and return
            stack.shrink(1);
            return stack;
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }
}

package carpetextra.dispenser.behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import org.jetbrains.annotations.NotNull;

public class BlazePowderDispenserBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        this.setSuccess(true);
        ServerLevel level = source.level();
        BlockPos frontBlockPos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
        net.minecraft.world.level.block.state.BlockState frontBlockState = level.getBlockState(frontBlockPos);
        net.minecraft.world.level.block.Block frontBlock = frontBlockState.getBlock();

        if(frontBlock == Blocks.NETHER_WART) {
            int age = frontBlockState.getValue(NetherWartBlock.AGE);
            if(age < 3) {
                // grow netherwart one stage
                level.setBlock(frontBlockPos, frontBlockState.setValue(NetherWartBlock.AGE, age + 1), Block.UPDATE_ALL);
                // green sparkles
                level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, frontBlockPos, 0);

                // decrement item and return
                stack.shrink(1);
                return stack;
            }
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }
}

package carpetextra.dispenser.behaviors;

import carpetextra.dispenser.DispenserBehaviorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class CauldronEmptyingDispenserBehavior extends DispenserBehaviorHelper {
    @Override
    protected @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        this.setSuccess(true);
        ServerLevel level = source.level();
        BlockPos frontBlockPos = source.pos().offset(source.state().getValue(DispenserBlock.FACING).getUnitVec3i());
        BlockState frontBlockState = level.getBlockState(frontBlockPos);
        Block frontBlock = frontBlockState.getBlock();

        // check if front block is cauldron and is full
        if(frontBlock instanceof AbstractCauldronBlock && ((AbstractCauldronBlock) frontBlock).isFull(frontBlockState)) {
            // lava
            if(frontBlock == Blocks.LAVA_CAULDRON) {
                setCauldron(level, frontBlockPos, SoundEvents.BUCKET_FILL_LAVA);
                return this.addOrDispense(source, stack, new ItemStack(Items.LAVA_BUCKET));
            }
            // water
            else if(frontBlock == Blocks.WATER_CAULDRON) {
                setCauldron(level, frontBlockPos, SoundEvents.BUCKET_FILL);
                return this.addOrDispense(source, stack, new ItemStack(Items.WATER_BUCKET));
            }
            // powder snow
            else if(frontBlock == Blocks.POWDER_SNOW_CAULDRON) {
                setCauldron(level, frontBlockPos, SoundEvents.BUCKET_FILL_POWDER_SNOW);
                return this.addOrDispense(source, stack, new ItemStack(Items.POWDER_SNOW_BUCKET));
            }
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }

    // set cauldron, play sound, emit game event
    private static void setCauldron(ServerLevel world, BlockPos pos, SoundEvent soundEvent) {
        world.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
        world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
    }
}

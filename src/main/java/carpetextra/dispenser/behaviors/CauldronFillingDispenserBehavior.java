package carpetextra.dispenser.behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class CauldronFillingDispenserBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected @NotNull ItemStack execute(BlockSource source, ItemStack stack) {
        this.setSuccess(true);
        Item item = stack.getItem();
        ServerLevel level = source.level();
        BlockPos frontBlockPos = source.pos().offset(source.state().getValue(DispenserBlock.FACING).getUnitVec3i());
        BlockState frontBlockState = level.getBlockState(frontBlockPos);
        Block frontBlock = frontBlockState.getBlock();

        if(frontBlock instanceof AbstractCauldronBlock) {
            // lava
            if(item == Items.LAVA_BUCKET) {
                setCauldron(level, frontBlockPos, Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA);
                return new ItemStack(Items.BUCKET);
            }
            // water
            else if(item == Items.WATER_BUCKET) {
                BlockState cauldronState = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
                setCauldron(level, frontBlockPos, cauldronState, SoundEvents.BUCKET_EMPTY);
                return new ItemStack(Items.BUCKET);
            }
            // powder snow
            else if(item == Items.POWDER_SNOW_BUCKET) {
                BlockState cauldronState = Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
                setCauldron(level, frontBlockPos, cauldronState, SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
                return new ItemStack(Items.BUCKET);
            }
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }

    // set cauldron, play sound, emit game event
    private static void setCauldron(ServerLevel world, BlockPos pos, BlockState state, SoundEvent soundEvent) {
        world.setBlock(pos, state, Block.UPDATE_ALL);
        world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
    }
}

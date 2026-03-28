package carpetextra.dispenser.behaviors;


import carpet.script.annotation.Locator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DragonBreathDispenserBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        this.setSuccess(true);
        ServerLevel level = source.level();
        BlockPos frontBlockPos = source.pos().offset(source.state().getValue(DispenserBlock.FACING).getUnitVec3i());
        Block frontBlock = level.getBlockState(frontBlockPos).getBlock();

        // check if cobblestone, place end stone
        if(frontBlock == Blocks.COBBLESTONE) {
            level.setBlock(frontBlockPos, Blocks.END_STONE.defaultBlockState(), Block.UPDATE_ALL);

            // play dragon fireball shoot sound
            level.playSound(null, frontBlockPos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.BLOCKS, 1.0F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);

            // spawn some dragon breath particles around end stone
            Vec3 center = Vec3.atCenterOf(frontBlockPos);
            level.sendParticles((ParticleOptions) ParticleTypes.DRAGON_BREATH, center.x, center.y, center.z, 10, 0.5, 0.5, 0.5, 0.01);

            // decrement dragon breath and return
            stack.shrink(1);
            return stack;
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }
}

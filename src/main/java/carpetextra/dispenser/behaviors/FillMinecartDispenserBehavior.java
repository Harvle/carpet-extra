package carpetextra.dispenser.behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;



public class FillMinecartDispenserBehavior extends OptionalDispenseItemBehavior {
    private final EntityType<? extends @NotNull AbstractMinecart> MINECART_TYPE;

    public FillMinecartDispenserBehavior(EntityType<? extends @NotNull AbstractMinecart> minecartType) {
        this.MINECART_TYPE = minecartType;
    }

    @Override
    protected @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        this.setSuccess(true);
        ServerLevel level = source.level();
        BlockPos frontBlockPos = source.pos().offset(source.state().getValue(DispenserBlock.FACING).getUnitVec3i());
        AABB frontBlockBox = new AABB(frontBlockPos);

        // get non-mounted minecarts in front of dispenser
        List<Minecart> minecarts = level.getEntitiesOfClass(Minecart.class, frontBlockBox, minecartEntity ->
                !minecartEntity.isPassenger());

        if(!minecarts.isEmpty()) {
            // choose a random minecart in front of dispenser to fill
            Minecart oldMinecart = minecarts.get(level.getRandom().nextInt(minecarts.size()));
            AbstractMinecart newMinecart = AbstractMinecart.createMinecart(level, oldMinecart.getX(), oldMinecart.getY(), oldMinecart.getZ(), this.MINECART_TYPE, EntitySpawnReason.DISPENSER, ItemStack.EMPTY, null);

            // Copy data from original minecart to new minecart
            // Possibly missing some things to copy here. Add more if needed
            assert newMinecart != null;
            newMinecart.setDeltaMovement(oldMinecart.getDeltaMovement());
            newMinecart.setXRot(oldMinecart.getXRot());
            newMinecart.setYRot(oldMinecart.getYRot());
            newMinecart.setCustomName(oldMinecart.getCustomName());
            newMinecart.setRemainingFireTicks(oldMinecart.getRemainingFireTicks());


            // remove old minecart, spawn new minecart
            oldMinecart.discard();
            level.addFreshEntity(newMinecart);

            // decrement item and return
            stack.shrink(1);
            return stack;
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }
}

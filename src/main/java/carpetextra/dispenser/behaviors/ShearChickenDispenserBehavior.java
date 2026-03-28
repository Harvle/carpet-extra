package carpetextra.dispenser.behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;



public class ShearChickenDispenserBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        this.setSuccess(true);
        ServerLevel level = source.level();
        BlockPos frontBlockPos = source.pos().offset(source.state().getValue(DispenserBlock.FACING).getUnitVec3i());
        AABB frontBlockBox = new AABB(frontBlockPos);

        // get adult chickens in front of dispenser
        List<Chicken> chickens = level.getEntitiesOfClass(Chicken.class, frontBlockBox, (chickenEntity ->
                !chickenEntity.isBaby() && chickenEntity.isAlive()));

        if(!chickens.isEmpty()) {
            // choose a random chicken in front of dispenser to shear
            Chicken chicken = chickens.get(level.getRandom().nextInt(chickens.size()));
            // damage chicken, drop feather if successful
            if(chicken.hurtServer(level, level.damageSources().generic(), 1)) {
                chicken.drop(new ItemStack(Items.FEATHER), false, false);

                // damage shears, remove if broken
                stack.hurtAndBreak(1, level, null, (item) -> stack.setCount(0));

                // return shears
                return stack;
            }
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }
}

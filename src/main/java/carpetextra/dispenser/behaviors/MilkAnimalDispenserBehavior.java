package carpetextra.dispenser.behaviors;

import java.util.List;

import carpetextra.dispenser.DispenserBehaviorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class MilkAnimalDispenserBehavior extends DispenserBehaviorHelper {
    @Override
    protected @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        this.setSuccess(true);
        ServerLevel level = source.level();
        BlockPos frontBlockPos = source.pos().offset(source.state().getValue(DispenserBlock.FACING).getUnitVec3i());

        // check if non-baby cows/mooshrooms/goats are in front of dispenser
        List<Animal> milkableAnimals = level.getEntitiesOfClass(Animal.class, new AABB(frontBlockPos), (animalEntity ->
            !animalEntity.isBaby() && (animalEntity instanceof  Cow || animalEntity instanceof Goat)));

        if(!milkableAnimals.isEmpty()) {
            // play milking sound for a random animal in front of dispenser
            Animal milkAnimal = milkableAnimals.get(level.getRandom().nextInt(milkableAnimals.size()));
            level.playSound(null, frontBlockPos, getMilkSound(milkAnimal), SoundSource.NEUTRAL, 1.0F, 1.0F);

            // add or dispense milk bucket stack
            return this.addOrDispense(source, stack, new ItemStack(Items.MILK_BUCKET));
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }

    private static SoundEvent getMilkSound(Animal animal) {
        if(animal.getType() == EntityType.GOAT) {
            return ((Goat) animal).isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
        }
        return SoundEvents.COW_MILK;
    }
}

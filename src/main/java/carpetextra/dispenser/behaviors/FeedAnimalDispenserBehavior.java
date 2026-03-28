package carpetextra.dispenser.behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;



public class FeedAnimalDispenserBehavior extends OptionalDispenseItemBehavior {
    @Override
    protected @NotNull ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        this.setSuccess(true);
        ServerLevel level = source.level();
        BlockPos frontBlockPos = source.pos().offset(source.state().getValue(DispenserBlock.FACING).getUnitVec3i());
        AABB frontBlockBox = new AABB(frontBlockPos);

        // get all animals in front of dispenser that are able to be fed current item and can breed or grow up
        // ✅ modern Mojang way
        List<Animal> animals = level.getEntitiesOfClass(Animal.class,frontBlockBox, animalEntity ->
                animalEntity.isAlive() && animalEntity.isFood(stack) && animalEntity.getAge() <= 0
        );

        if(!animals.isEmpty()) {
            ItemStack fedStack = tryFeed(level, animals, stack);
            if(fedStack != null) {
                return fedStack;
            }
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }

    private static ItemStack tryFeed(ServerLevel level, List<Animal> animals, ItemStack foodStack) {
        // try to feed all adult animals first
        for(Animal animal : animals) {
            // check if breeding age is 0 (is adult and can breed)
            if(animal.getAge() == 0) {
                // check if animal can enter love mode with item
                if(canLoveWithItem(animal, foodStack)) {
                    animal.setInLove(null);
                }

                // eat item
                return eatItem(animal, foodStack);
            }
        }
        // try to grow up baby animals next
        for(Animal animal : animals) {
            if(animal.isBaby()) {
                // grow up baby animal slightly
                animal.ageUp((-animal.getAge() / 200), true);
                // spawn growth sparkle particle
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + 0.5, animal.getZ(), 1, 0, 0, 0, 0);

                // eat item
                return eatItem(animal, foodStack);
            }
        }

        // no animal can be fed
        return null;
    }

    // handles special cases for animals eating items
    // returns food item stack after being eaten
    private static ItemStack eatItem(Animal animal, ItemStack foodStack) {
        EntityType<?> type = animal.getType();

        // axolotl returns water bucket if fed tropical fish bucket
        if(type == EntityType.AXOLOTL && foodStack.getItem() == Items.TROPICAL_FISH_BUCKET) {
            return new ItemStack(Items.WATER_BUCKET);
        }

        // cats and foxes play a sound when being fed
        if(type == EntityType.CAT) {
            animal.playSound(SoundEvents.GENERIC_EAT.value(), 1.0F, 1.0F);
        }

        else if(type == EntityType.FOX) {
            animal.playSound(SoundEvents.FOX_EAT, 1.0F, 1.0F);
        }

        // remove one item and return
        foodStack.shrink(1);
        return foodStack;
    }

    // checks special cases if animal can enter love mode with item
    private static boolean canLoveWithItem(Animal animal, ItemStack foodStack) {
        EntityType<?> type = animal.getType();
        Item item = foodStack.getItem();

        // llamas only breed with hay bales
        if ((type == EntityType.LLAMA || type == EntityType.TRADER_LLAMA) && item != Items.HAY_BLOCK) {
            return false;
        }
        // horses/donkeys/mules only breed with golden carrot, golden apple, or enchanted golden apple
        else return (type != EntityType.HORSE && type != EntityType.DONKEY && type != EntityType.MULE) || (item == Items.GOLDEN_CARROT || item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE);
    }
}

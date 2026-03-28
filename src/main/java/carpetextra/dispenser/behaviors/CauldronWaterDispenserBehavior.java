package carpetextra.dispenser.behaviors;

import carpetextra.dispenser.DispenserBehaviorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class CauldronWaterDispenserBehavior extends DispenserBehaviorHelper {
    @Override
    protected @NotNull ItemStack execute(BlockSource source, ItemStack stack) {
        this.setSuccess(true);
        Item item = stack.getItem();
        ServerLevel level = source.level();
        BlockPos frontBlockPos = source.pos().offset(source.state().getValue(DispenserBlock.FACING).getUnitVec3i());
        BlockState frontBlockState = level.getBlockState(frontBlockPos);
        Block frontBlock = frontBlockState.getBlock();

        if (frontBlock == Blocks.WATER_CAULDRON) {
            if (isWaterBottle(stack)) {
                // check if cauldron is not full
                if (!((AbstractCauldronBlock) frontBlock).isFull(frontBlockState)) {
                    // increase cauldron level
                    int fillLevel = frontBlockState.getValue(LayeredCauldronBlock.LEVEL);
                    BlockState cauldronState = frontBlockState.setValue(LayeredCauldronBlock.LEVEL, fillLevel + 1);
                    setCauldron(level, frontBlockPos, cauldronState, SoundEvents.BOTTLE_EMPTY, GameEvent.FLUID_PLACE);

                    // return glass bottle
                    return this.addOrDispense(source, stack, new ItemStack(Items.GLASS_BOTTLE));
                }
            }
            else if (item == Items.GLASS_BOTTLE) {
                // decrease cauldron level
                LayeredCauldronBlock.lowerFillLevel(frontBlockState, level, frontBlockPos);
                // return water bottle
                return this.addOrDispense(source, stack, PotionContents.createItemStack(Items.POTION, Potions.WATER));
            }
            else if (Block.byItem(item) instanceof ShulkerBoxBlock) {
                // make sure item isn't plain shulker box
                if (item != Items.SHULKER_BOX) {
                    // decrease cauldron level
                    LayeredCauldronBlock.lowerFillLevel(frontBlockState, level, frontBlockPos);
                    // turn dyed shulker box into undyed shulker box
                    ItemStack undyedShulkerBox = stack.transmuteCopy(Blocks.SHULKER_BOX, 1);

                    // return undyed shulker box
                    return this.addOrDispense(source, stack, undyedShulkerBox);
                }
            }
            if (stack.has(DataComponents.DYED_COLOR)) {
                // check if dyeable item has color
                if (stack.has(DataComponents.DYED_COLOR)) {
                    // decrease cauldron level
                    LayeredCauldronBlock.lowerFillLevel(frontBlockState, level, frontBlockPos);
                    // remove color
                    stack.remove(DataComponents.DYED_COLOR);
                    // return undyed item
                    return stack;
                }
            }
            else if (item instanceof BannerItem) {
                // check if banner has layers (https://minecraft.wiki/w/Banner#Patterns)
                BannerPatternLayers bannerPatterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
                if (!bannerPatterns.layers().isEmpty()) {
                    // decrease cauldron level
                    LayeredCauldronBlock.lowerFillLevel(frontBlockState, level, frontBlockPos);
                    // copy banner stack, set to one item
                    ItemStack cleanedBanner = stack.copy();
                    cleanedBanner.setCount(1);
                    // remove layer from banner
                    cleanedBanner.set(DataComponents.BANNER_PATTERNS, bannerPatterns.removeLast());
                    // return cleaned banner
                    return this.addOrDispense(source, stack, cleanedBanner);
                }
            }
        }
        else if (frontBlock == Blocks.CAULDRON && isWaterBottle(stack)) {
            // increase cauldron level
            BlockState cauldronState = Blocks.WATER_CAULDRON.defaultBlockState();
            setCauldron(level, frontBlockPos, cauldronState, SoundEvents.BOTTLE_EMPTY, GameEvent.FLUID_PLACE);

            // return glass bottle
            return this.addOrDispense(source, stack, new ItemStack(Items.GLASS_BOTTLE));
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }

    // set cauldron, play sound, emit game event
    private static void setCauldron(ServerLevel level, BlockPos pos, BlockState state, SoundEvent soundEvent, Holder<GameEvent> gameEvent) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
        level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, gameEvent, pos);
    }
    
    private static boolean isWaterBottle(ItemStack stack) {
        if (stack.getItem() != Items.POTION) {
            return false;
        }

        PotionContents content = stack.get(DataComponents.POTION_CONTENTS);
        return content != null && content.is(Potions.WATER);
    }

    public static boolean isWaterCauldronItem(ItemStack stack) {
        /* accept empty and water bottles, banners and dyeable items */
        Item item = stack.getItem();
        if (item == Items.GLASS_BOTTLE || item instanceof BannerItem || isWaterBottle(stack))
            return true;
        if (Block.byItem(item) instanceof ShulkerBoxBlock) {
            return item != Items.SHULKER_BOX; // dyed Shulkers only
        }
        if (stack.has(DataComponents.DYED_COLOR)) {
            return stack.getComponents().has(DataComponents.DYED_COLOR);
        }
        return false;
    }
}

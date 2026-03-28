package carpetextra.mixins;

import java.util.Map;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FlowerPotBlock.class)
public interface FlowerPotBlock_ContentAccessorMixin {
    @Accessor("CONTENT_TO_POTTED")
    static Map<Block, Block> getPottedBlocksMap() {
        throw new AssertionError();
    }
}

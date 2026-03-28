package carpetextra.fakes;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface DispenserBlockEntityInterface
{
    public NonNullList<@NotNull ItemStack> getInventory();
}

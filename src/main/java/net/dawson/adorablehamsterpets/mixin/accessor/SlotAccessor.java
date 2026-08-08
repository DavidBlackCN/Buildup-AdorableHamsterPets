package net.dawson.adorablehamsterpets.mixin.accessor;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Provides access to the Mojang-mapped container field within a slot. */
@Mixin(Slot.class)
public interface SlotAccessor {
    /**
     * Gets the inventory associated with this slot.
     * @return The slot's inventory.
     */
    @Accessor("container")
    Container adorablehamsterpets$getInventory();
}

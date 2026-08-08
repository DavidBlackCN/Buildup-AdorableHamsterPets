package net.dawson.adorablehamsterpets.screen;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.dawson.adorablehamsterpets.registry.DeferredRegister;
import net.dawson.adorablehamsterpets.registry.RegistrySupplier;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;

public class ModScreenHandlers {

    // --- 1. DeferredRegister for MenuTypes ---
    public static final DeferredRegister<MenuType<?>> SCREEN_HANDLERS =
            DeferredRegister.create(AdorableHamsterPets.MOD_ID, BuiltInRegistries.MENU);

    // --- 2. Register the Extended Menu Type ---
    public static final RegistrySupplier<MenuType<HamsterInventoryScreenHandler>> HAMSTER_INVENTORY_SCREEN_HANDLER =
            SCREEN_HANDLERS.register("hamster_inventory", () ->
                    new ExtendedMenuType<>((syncId, playerInventory, entityId) -> {
                        final Entity entity = playerInventory.player.level().getEntity(entityId);
                        // We pass the found entity (or null) to the client-side constructor.
                        // The constructor itself will handle the case where the entity is not a hamster.
                        return new HamsterInventoryScreenHandler(syncId, playerInventory, (HamsterEntity) entity);
                    }, ByteBufCodecs.VAR_INT)
            );

    // --- 3. Main Registration Call ---
    public static void register() {
        SCREEN_HANDLERS.register();
    }
}

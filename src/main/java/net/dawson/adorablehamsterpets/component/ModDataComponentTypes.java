package net.dawson.adorablehamsterpets.component;

import net.dawson.adorablehamsterpets.registry.DeferredRegister;
import net.dawson.adorablehamsterpets.registry.RegistrySupplier;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.custom.WoodVariant;
import net.dawson.adorablehamsterpets.config.WanderDistance;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.core.UUIDUtil;
import java.util.UUID;

public class ModDataComponentTypes {
    // Internal Fabric registry wrapper retained to minimize registration churn.
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(AdorableHamsterPets.MOD_ID, BuiltInRegistries.DATA_COMPONENT_TYPE);

    // Each component registered via the register(...) method and stored in a RegistrySupplier
    public static final RegistrySupplier<DataComponentType<UUID>> LINKED_HAMSTER_UUID =
            DATA_COMPONENT_TYPES.register("linked_hamster_uuid",
                    () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).cacheEncoding().build());

    public static final RegistrySupplier<DataComponentType<Component>> LINKED_HAMSTER_NAME =
            DATA_COMPONENT_TYPES.register("linked_hamster_name",
                    () -> DataComponentType.<Component>builder().persistent(ComponentSerialization.CODEC).cacheEncoding().build());

    public static final RegistrySupplier<DataComponentType<WanderDistance>> WANDER_DISTANCE =
            DATA_COMPONENT_TYPES.register("wander_distance",
                    () -> DataComponentType.<WanderDistance>builder().persistent(WanderDistance.CODEC).cacheEncoding().build());

    public static final RegistrySupplier<DataComponentType<WoodVariant>> WOOD_VARIANT =
            DATA_COMPONENT_TYPES.register("wood_variant",
                    () -> DataComponentType.<WoodVariant>builder().persistent(WoodVariant.CODEC).cacheEncoding().build());

    // Called from AdorableHamsterPets.initRegistries() to perform the actual registration
    public static void registerDataComponentTypes() {
        DATA_COMPONENT_TYPES.register();
    }
}

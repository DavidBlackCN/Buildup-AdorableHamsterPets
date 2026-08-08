package net.dawson.adorablehamsterpets.block;

import net.dawson.adorablehamsterpets.registry.DeferredRegister;
import net.dawson.adorablehamsterpets.registry.RegistrySupplier;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.block.entity.HamsterBedBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(AdorableHamsterPets.MOD_ID, BuiltInRegistries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<HamsterBedBlockEntity>> HAMSTER_BED_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("hamster_bed_be", () ->
                    FabricBlockEntityTypeBuilder.create(HamsterBedBlockEntity::new, ModBlocks.HAMSTER_BED.get()).build());

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}

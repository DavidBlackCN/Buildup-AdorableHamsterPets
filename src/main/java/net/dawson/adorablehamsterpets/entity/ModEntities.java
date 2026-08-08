package net.dawson.adorablehamsterpets.entity;

import net.dawson.adorablehamsterpets.registry.DeferredRegister;
import net.dawson.adorablehamsterpets.registry.RegistrySupplier;
import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.dawson.adorablehamsterpets.entity.custom.HamsterBlockHiderEntity;
import net.dawson.adorablehamsterpets.entity.custom.HamsterEntity;
import net.dawson.adorablehamsterpets.entity.custom.HamsterProjectileEntity;
import net.dawson.adorablehamsterpets.entity.custom.HamsterTreeSearcherEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    // --- 1. DeferredRegister for EntityTypes ---
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(AdorableHamsterPets.MOD_ID, BuiltInRegistries.ENTITY_TYPE);

    // --- 2. EntityType Registration as a RegistrySupplier ---
    public static final RegistrySupplier<EntityType<HamsterEntity>> HAMSTER = ENTITY_TYPES.register("hamster", () ->
            EntityType.Builder.of(HamsterEntity::new, MobCategory.CREATURE)
                    .sized(0.425F, 0.425F).build(key("hamster")));

    public static final RegistrySupplier<EntityType<HamsterTreeSearcherEntity>> HAMSTER_TREE_SEARCHER = ENTITY_TYPES.register("hamster_tree_searcher", () ->
            EntityType.Builder.<HamsterTreeSearcherEntity>of(HamsterTreeSearcherEntity::new, MobCategory.MISC)
                    .sized(0.01F, 0.01F) // Tiny, invisible
                    .build(key("hamster_tree_searcher")));

    public static final RegistrySupplier<EntityType<HamsterBlockHiderEntity>> HAMSTER_BLOCK_HIDER = ENTITY_TYPES.register("hamster_block_hider", () ->
            EntityType.Builder.<HamsterBlockHiderEntity>of(HamsterBlockHiderEntity::new, MobCategory.MISC)
                    .sized(0.01F, 0.01F) // Tiny, invisible
                    .build(key("hamster_block_hider")));

    public static final RegistrySupplier<EntityType<HamsterProjectileEntity>> HAMSTER_PROJECTILE = ENTITY_TYPES.register("hamster_projectile", () ->
            EntityType.Builder.<HamsterProjectileEntity>of(HamsterProjectileEntity::new, MobCategory.MISC)
                    .sized(0.425F, 0.425F) // Match hamster size
                    .build(key("hamster_projectile")));

    private static ResourceKey<EntityType<?>> key(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, path));
    }

    // --- 3. Main Registration Call ---
    public static void register() {
        ENTITY_TYPES.register();
    }
}
